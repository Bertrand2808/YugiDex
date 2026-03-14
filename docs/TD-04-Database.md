# TD-04 — Base de données : PostgreSQL + JPA
> Persister les decks des joueurs avec Spring Data JPA et PostgreSQL
> Package : `org.yugiohproject`

---

## Avant de commencer

**Question** : on a l'API YGOProDeck pour les cartes. Pourquoi a-t-on besoin d'une base de données ?

Réfléchis avant de lire.

> **Réponse** : les cartes sont des données publiques, statiques (rarement mises à jour). On peut les récupérer à la demande depuis l'API. Mais les **decks** appartiennent à un joueur, sont créés par lui, modifiés, supprimés. Ce sont des **données utilisateur** → elles doivent être persistées quelque part. L'API YGOProDeck ne connaît pas tes decks.

---

## Étape 0 — Lancer PostgreSQL avec Docker

### Concepts : Docker, Docker Compose, conteneurs

Au lieu d'installer PostgreSQL sur ta machine, on utilise **Docker** : un outil qui lance des applications dans des conteneurs isolés.

**Vérifie que Docker est installé** :
```bash
docker --version
# Docker version 27.x.x
```

Crée à la racine du projet un fichier `docker-compose.yml` :

```yaml
version: '3.8'
services:
  postgres:
    image: postgres:16          # image officielle PostgreSQL 16
    container_name: yugidex-db
    environment:
      POSTGRES_DB: yugidex
      POSTGRES_USER: yugidex
      POSTGRES_PASSWORD: yugidex  # suffisant pour le dev local
    ports:
      - "5432:5432"             # port_hôte:port_conteneur
    volumes:
      - yugidex_data:/var/lib/postgresql/data  # données persistées

volumes:
  yugidex_data:
```

**Démarrer la base** :
```bash
docker-compose up -d
# -d = detached : tourne en arrière-plan

docker-compose ps        # vérifier que le conteneur tourne
docker-compose logs -f   # voir les logs en temps réel
docker-compose down      # arrêter (les données sont gardées grâce au volume)
```

**Se connecter à la base pour vérifier** :
```bash
docker exec -it yugidex-db psql -U yugidex -d yugidex
# \l   → lister les bases
# \q   → quitter
```

---

## Étape 1 — Ajouter les dépendances JPA

### Concepts : ORM, JPA, Hibernate, pilote JDBC

**Ajoute dans `pom.xml`** :

```xml
<!-- Spring Data JPA : annotations @Entity, JpaRepository, etc. -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>

<!-- Pilote PostgreSQL : permet à Java de parler à PostgreSQL -->
<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
    <scope>runtime</scope>
    <!-- scope runtime : pas utilisé à la compilation, seulement à l'exécution -->
</dependency>
```

> **JPA vs Hibernate vs Spring Data JPA** :
> - **JPA** = spécification Java (les interfaces, les annotations `@Entity`, `@Id`...)
> - **Hibernate** = implémentation concrète de JPA (il fait vraiment le SQL)
> - **Spring Data JPA** = couche au-dessus, génère les requêtes SQL pour toi avec des interfaces comme `JpaRepository`

---

## Étape 2 — Configurer la connexion

### Concepts : `application.properties`, datasource, DDL auto

```properties
# src/main/resources/application.properties

# Connexion à PostgreSQL
spring.datasource.url=jdbc:postgresql://localhost:5432/yugidex
spring.datasource.username=yugidex
spring.datasource.password=yugidex
spring.datasource.driver-class-name=org.postgresql.Driver

# JPA / Hibernate
spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect

# DDL : que faire avec le schéma au démarrage ?
# "validate"  → vérifier que le schéma correspond aux entités (prod)
# "update"    → modifier le schéma si besoin (dev)
# "create"    → recréer à chaque démarrage (tests)
# "none"      → ne rien faire
spring.jpa.hibernate.ddl-auto=update

# Afficher les requêtes SQL générées (utile pour apprendre)
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
```

> **`ddl-auto=update` en dev, jamais en prod** : en production, on utilise des outils de migration (Liquibase, Flyway) pour contrôler précisément les changements de schéma.

---

## Étape 3 — L'entité Deck

### Concepts : `@Entity`, `@Id`, `@GeneratedValue`, `@ElementCollection`

Un **Deck** appartient à un joueur et contient une liste de card IDs (pas les cartes elles-mêmes — les cartes restent dans l'API).

```java
// Fichier : deck/Deck.java
@Entity
//  ^^^^^^
//  Indique à JPA : cette classe = une table en base
//  Par défaut, nom de table = "deck" (lowercase du nom de classe)
@Table(name = "decks")
//  ^^^^^^^^^^^^^^^^^^^
//  Nom explicite de la table (bonne pratique)
public class Deck {

    @Id
    //  ^^^
    //  Clé primaire
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    //  ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
    //  PostgreSQL génère l'ID automatiquement (SERIAL / auto-increment)
    private Long id;

    @Column(nullable = false)
    //  ^^^^^^^^^^^^^^^^^^^^^^
    //  Colonne NOT NULL en base
    private String name;

    @Column(nullable = false)
    private String ownerName;
    //                ^^^^^^^
    //  On simplifie : pas d'entité User pour l'instant, juste un nom

    @ElementCollection
    //  ^^^^^^^^^^^^^^^^^^
    //  Persiste une List<Integer> dans une table séparée
    //  (deck_card_ids avec colonnes deck_id et card_ids)
    private List<Integer> cardIds;

    // Constructeur vide OBLIGATOIRE pour JPA
    protected Deck() {}

    public Deck(String name, String ownerName) {
        this.name = name;
        this.ownerName = ownerName;
        this.cardIds = new ArrayList<>();
    }

    // Getters
    public Long getId()              { return id; }
    public String getName()          { return name; }
    public String getOwnerName()     { return ownerName; }
    public List<Integer> getCardIds() { return Collections.unmodifiableList(cardIds); }
    //                                           ^^^^^^^^^^^^^^^^^^^^^^^^
    //                                           On expose une copie non-modifiable

    // Méthodes métier (on ne modifie pas la liste directement depuis l'extérieur)
    public void addCard(int cardId) {
        if (cardIds.size() >= 60) {
            throw new IllegalStateException("Un deck ne peut pas dépasser 60 cartes");
        }
        cardIds.add(cardId);
    }

    public void removeCard(int cardId) {
        cardIds.remove(Integer.valueOf(cardId));
        //              ^^^^^^^^^^^^^^^
        //              Important : remove(Object) pas remove(int index)
    }
}
```

> **Pourquoi un constructeur vide `protected` ?** JPA crée des objets via réflexion (sans passer par ton constructeur). Sans constructeur vide, ça plante. `protected` plutôt que `private` = accessible à Hibernate mais pas au reste de ton code.

**Exercice** : crée l'entité `Deck` dans un nouveau package `deck/`. Lance l'app : Spring doit créer la table `decks` automatiquement (vérifie avec `psql`).

---

## Étape 4 — DeckRepository

### Concepts : `JpaRepository`, requêtes générées automatiquement

```java
// Fichier : deck/DeckRepository.java
@Repository
public interface DeckRepository extends JpaRepository<Deck, Long> {
    //                                   ^^^^^^^^^^^^^^^^^^^^^^^^^^^
    //                                   JpaRepository<TypeEntité, TypeId>
    //                                   Génère automatiquement :
    //                                   findAll(), findById(), save(), delete(), count()...

    // Spring Data génère le SQL à partir du NOM de la méthode :
    List<Deck> findByOwnerName(String ownerName);
    //          ^^^^^^^^^^^^^^^^^^^
    //          SELECT * FROM decks WHERE owner_name = ?

    boolean existsByNameAndOwnerName(String name, String ownerName);
    //       ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
    //       SELECT COUNT(*) > 0 FROM decks WHERE name = ? AND owner_name = ?
}
```

> **Convention de nommage Spring Data** :
> - `findBy<Champ>` → WHERE champ = ?
> - `findBy<Champ1>And<Champ2>` → WHERE champ1 = ? AND champ2 = ?
> - `findBy<Champ>Containing` → WHERE champ LIKE '%?%'
> - `findBy<Champ>OrderBy<AutreChamp>Asc` → ... ORDER BY

**Exercice** : crée l'interface `DeckRepository`. Ajoute `findByOwnerName` et `countByOwnerName`. Tu n'as pas à écrire de SQL.

---

## Étape 5 — DeckService

### Concepts : logique métier, `@Transactional`

```java
// Fichier : deck/DeckService.java
@Service
public class DeckService {

    private final DeckRepository deckRepository;

    public DeckService(DeckRepository deckRepository) {
        this.deckRepository = deckRepository;
    }

    public Deck createDeck(String name, String ownerName) {
        if (deckRepository.existsByNameAndOwnerName(name, ownerName)) {
            throw new IllegalArgumentException(
                "Tu as déjà un deck nommé '" + name + "'"
            );
        }
        Deck deck = new Deck(name, ownerName);
        return deckRepository.save(deck);
        //                     ^^^^
        //                     INSERT en base + retourne le Deck avec son ID généré
    }

    public List<Deck> getDecksForPlayer(String ownerName) {
        return deckRepository.findByOwnerName(ownerName);
    }

    public Deck findById(Long id) {
        return deckRepository.findById(id)
            .orElseThrow(() -> new DeckNotFoundException("Deck " + id + " introuvable"));
    }

    @Transactional
    //  ^^^^^^^^^^^^^^
    //  Toutes les opérations dans cette méthode font partie de la même transaction
    //  Si une opération échoue → tout est annulé (rollback)
    public Deck addCardToDeck(Long deckId, int cardId) {
        Deck deck = findById(deckId);
        deck.addCard(cardId);
        return deckRepository.save(deck);
        // Avec @Transactional, save() pourrait être optionnel
        // car Hibernate détecte automatiquement les modifications (dirty checking)
        // On l'écrit quand même pour la clarté
    }

    @Transactional
    public Deck removeCardFromDeck(Long deckId, int cardId) {
        Deck deck = findById(deckId);
        deck.removeCard(cardId);
        return deckRepository.save(deck);
    }

    public void deleteDeck(Long id) {
        if (!deckRepository.existsById(id)) {
            throw new DeckNotFoundException("Deck " + id + " introuvable");
        }
        deckRepository.deleteById(id);
    }
}
```

**Exercice** : crée `DeckService` et `DeckNotFoundException`. Teste chaque méthode dans un test unitaire (sans base de données — utilise Mockito pour mocker le repository).

---

## Étape 6 — Vérifier en base

Lance l'app, et inspecte la base :

```bash
docker exec -it yugidex-db psql -U yugidex -d yugidex

-- Dans psql :
\dt                          -- lister les tables
\d decks                     -- décrire la table decks
SELECT * FROM decks;         -- voir les decks créés
```

---

## Récap des fichiers créés

```
docker-compose.yml               ← à la racine du projet

src/main/
├── resources/
│   └── application.properties   ← datasource + JPA config
└── java/org/yugiohproject/
    └── deck/
        ├── Deck.java              ← @Entity
        ├── DeckRepository.java   ← extends JpaRepository
        ├── DeckService.java      ← @Service + @Transactional
        └── DeckNotFoundException.java
```

---

## Prochaine étape

**TD-05 — Deck CRUD** : exposer le DeckService via un `DeckController` REST et écrire les tests unitaires et d'intégration.
