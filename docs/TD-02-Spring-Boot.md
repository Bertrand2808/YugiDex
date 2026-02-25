# TD-02 — Spring Boot : du code Java à une vraie API REST
> Transformer le projet Java standalone en application web exposant des endpoints HTTP
> Package : `org.yugiohproject`

---

## Étape 0 — Comprendre Spring Boot

### Le gros problème quand on grandit un projet Java "normal"

En suivant le fil du TD précédent et des exemples donnés pour un projet e-commerce, on se retrouve avec un projet relativement simple :
- On a créé `ProduitRepository` avec `new MockProduitRepository()`
- On a créé `ProduitService` avec `new ProduitService(monRepository)`
- On l’utilise dans `Main` : `new ProduitService(...).getProduitsEnPromo()`

Ça va bien pour le moment mais petit à petit, on peut vite se retrouver avec :

- 15 repositories
- 20 services
- Des services qui dépendent d’autres services
- Des tests où tu dois créer 5–6 `new` à chaque fois

→ On passe plus de temps à **bricoler les "new..."** et à **garder l’ordre correct** qu’à coder la vraie logique business.
C’est source d’erreurs, et impossible à maintenir quand l’équipe grandit.

**La solution Spring** : **déclarer** les objets avec des **annotations**.

Si on veut vulgariser, Spring Boot est comme un **super assistant intelligent** qui :

1. **Regarde ton code** (grâce aux annotations que tu ajoutes)
2. **Comprend tout seul** quels objets il faut créer
3. **Crée ces objets** au bon moment
4. **Les relie automatiquement** (il met le bon repository dans le bon service)
5. **Te les donne** quand tu en as besoin, sans que tu écrives `new`

→ Tu te concentres sur **quoi faire** (la logique métier), pas sur **comment assembler les pièces**.

```
Ton code            Spring (le "conteneur")
-----------         ----------------------
@Service            →  crée CardService automatiquement
@Repository         →  crée MockCardRepository automatiquement
@Autowired /        →  injecte MockCardRepository dans CardService
constructeur           sans que tu écrives "new"
```

Exemple ultra-simple :

```java
// Avant Spring : tu dois écrire tous les new toi-même
ProduitRepository repo = new MockProduitRepository();
ProduitService service = new ProduitService(repo);

// Avec Spring : tu écris ÇA SEULEMENT
@Service
public class ProduitService {

    private final ProduitRepository repository;

    // Spring va automatiquement mettre le bon repository ici
    public ProduitService(ProduitRepository repository) {  // ← injection par constructeur (la meilleure pratique)
        this.repository = repository;
    }

    public List<Produit> getProduitsPasChers() {
        return repository.findAll()
                         .stream()
                         .filter(p -> p.prix() < 30)
                         .toList();
    }
}
```

**Analogie** : Spring est comme un chef cuisinier qui organise toute la brigade. Tu lui dis "j'ai besoin d'un CardService" et il prépare tout ce qu'il faut, dans le bon ordre, sans que tu aies à y penser.

**Spring Boot** = Spring + configuration automatique. Au lieu de configurer Spring à la main (XML ou Java config), Spring Boot devine ce dont tu as besoin en regardant tes dépendances.

---

## Étape 1 — Ajouter Spring Boot au projet

### Concepts : pom.xml, parent POM, starters

Ouvre `pom.xml`. Il ressemble à :

```xml
<project>
    <modelVersion>4.0.0</modelVersion>
    <groupId>org.yugiohproject</groupId>
    <artifactId>YugiDex</artifactId>
    <version>1.0-SNAPSHOT</version>
    <properties>
        <maven.compiler.source>25</maven.compiler.source>
        <maven.compiler.target>25</maven.compiler.target>
    </properties>
</project>
```

Pour ajouter Spring Boot, tu as besoin de **deux choses** :

### 1.1 — Le parent POM (spring-boot-starter-parent)

Le `parent` Spring Boot gère les versions de toutes les bibliothèques pour toi. Tu n'as plus à écrire `<version>` dans tes dépendances.

```xml
<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>3.5.0</version>  <!-- ou la dernière stable en février 2026, ex: 4.0.3 si tu préfères la branche 4.x -->
    <relativePath/> <!-- lookup parent from repository -->
</parent>
```

Ton pom.xml commence maintenant à ressembler à :

```xml
<project ...>
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.5.0</version>
        <relativePath/>
    </parent>

    <groupId>org.yugiohproject</groupId>
    <artifactId>YugiDex</artifactId>
    <version>1.0-SNAPSHOT</version>

    <!-- ... tes properties ... -->
</project>
```

> **Pourquoi un "parent" ? et pas une simple dépendance ?**

| Option | Comment on l'ajoute | Ce que ça fait | Avantages | Inconvénients | Quand l'utiliser ? |
| --- | --- | --- | --- | --- | --- |
| Dépendance normale (<dependency>) | `<dependency><groupId>org.springframework.boot</groupId><artifactId>spring-boot-starter-web</artifactId></dependency>` | Ajoute seulement ce starter (ex: web) | Simple si tu as déjà un parent | Tu dois gérer toutes les versions toi-même → risque de conflits (ex: Jackson 2.15 vs 2.17) | Projet très petit ou sans Spring Boot complet |
| spring-boot-dependencies (comme BOM via <dependencyManagement>) | `<dependencyManagement><dependencies><dependency><groupId>org.springframework.boot</groupId><artifactId>spring-boot-dependencies</artifactId><version>3.5.0</version><type>pom</type><scope>import</scope></dependency></dependencies></dependencyManagement>` | Importe un catalogue de versions compatibles (BOM = Bill Of Materials) | "Versions gérées, mais tu gardes ton propre parent" | "Pas de configs par défaut pour plugins Maven (compiler, jar, etc.)" | "Quand ton projet a déjà un parent POM (multi-module, entreprise)" |
| spring-boot-starter-parent (ce qu’on choisit ici) | `<parent>...</parent>` comme ci-dessus | "Hérite de tout : versions compatibles + configs Maven par défaut (Java 17+, encoding UTF-8, plugins compiler/surefire/jar, etc.)" | Tout est prêt : versions OK + build propre sans config manuelle | Ton projet n’a plus de parent personnalisé (mais tu peux surcharger) | Cas le plus courant pour les débutants et projets simples |

**En résumé** : pourquoi le parent plutôt qu’une simple dépendance ?

- Si tu ajoutes seulement des starters comme dépendances → Maven ne sait pas quelles versions utiliser → tu dois écrire `<version>2.42.3</version>` pour chaque lib → galère + bugs de compatibilité.
- Le spring-boot-starter-parent est comme un super parent intelligent qui dit :
« Je gère pour toi : Spring Web = 6.2.3, Jackson = 2.18.0, Hibernate = 6.6.x, etc. Et en plus je configure Maven pour que ton build soit propre dès le départ. »

→ Tu n’écris jamais de `<version>` pour les libs Spring Boot → Spring Boot décide des versions compatibles entre elles.
→ C’est la raison principale pour laquelle on utilise le parent : simplicité + zéro risque de versions incompatibles.

### 1.2 — Ajouter les starters (les briques dont tu as besoin)

Les **starters** sont des ensembles de dépendances groupées. `spring-boot-starter-web` t'apporte :
- Le serveur web embarqué (Tomcat)
- Spring MVC (pour les controllers REST)
- Jackson (pour convertir Java ↔ JSON)

```xml
<dependencies>
    <!-- Pour faire une API web REST -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
        <!-- Pas de <version> : géré par le parent -->
    </dependency>

    <!-- Pour des tests faciles (JUnit 5 + assertions + mockito intégré) -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-test</artifactId>
        <scope>test</scope>
    </dependency>

    <!-- Optionnel : si tu veux du JSON joli et lisible dans la console -->
    <dependency>
        <groupId>com.fasterxml.jackson.datatype</groupId>
        <artifactId>jackson-datatype-jsr310</artifactId>  <!-- pour Java Time API -->
    </dependency>
</dependencies>
```

### 1.3 — Le plugin Maven

Pour lancer l'app avec `mvn spring-boot:run` :

```xml
<build>
    <plugins>
        <plugin>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-maven-plugin</artifactId>
        </plugin>
    </plugins>
</build>
```

**Exercice** : modifie ton `pom.xml` pour y ajouter ces trois éléments. Lance ensuite `mvn dependency:tree` dans le terminal et observe toutes les dépendances qui arrivent automatiquement.

---

## Étape 2 — Transformer Main.java

### Concepts : `@SpringBootApplication`, `SpringApplication.run()`

**Avant** (standalone) :

```java
public class Main {
    public static void main(String[] args) {
        CardRepository repo = new MockCardRepository();
        CardService service = new CardService(repo);
        // ...
    }
}
```

**Après** (Spring Boot) :

```java
@SpringBootApplication
//  ^^^^^^^^^^^^^^^^^^^
//  Combinaison de 3 annotations :
//  @Configuration : cette classe peut déclarer des beans
//  @EnableAutoConfiguration : Spring Boot détecte et configure automatiquement
//  @ComponentScan : scanne tous les packages pour trouver @Service, @Repository, etc.
public class Main {
    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
        //                 ^^^^^^^^^^^
        //                 Lance le serveur web Tomcat et le conteneur Spring
    }
}
```

**Ce qui se passe au démarrage** :
1. Spring scanne tous les packages sous `org.yugiohproject`
2. Il trouve les classes annotées (`@Service`, `@Repository`...)
3. Il les crée dans le bon ordre (les dépendances d'abord)
4. Il démarre le serveur HTTP sur le port 8080

**Exercice** : remplace le contenu de `Main.java`. Lance avec `mvn spring-boot:run` ou le bouton ▶ IntelliJ. Tu dois voir dans la console :

```
Started Main in 2.345 seconds (process running for 2.7)
```

> Si tu vois des erreurs sur les classes `CardService` ou `MockCardRepository` : c'est normal, on va les corriger à l'étape suivante.

---

## Étape 3 — Annoter le Service et le Repository

### Rappel Uncle Bob

Avant de passer à Spring en lui même, un petit rappel sur pourquoi on a séparé les rôles :

- **Repository** :

C'est le **gestionnaire de données**. Son seul job est : **lire** ou **écrire** des données (depuis un mock, une base de données, un fichier, une API, ...).
Il ne fait **AUCUN** calcul, aucune transformation, aucune logique métier.
Exemple : `findAll()`, `findById()`, `findByNom()`.

- **Service** :

C'est le **cerveau** de l'application. Il récupère les données via le **Repository** et applique les règles business (calculs, filtres, validations, tris, combinaisons, etc), et renvoi un résultat prêt à l'emploi.
Exemple : `getTop5CartesLesPlusPuissantes()`, `calculerPuissanceAvecBonusTerrain()`.

> **Tips de l'Oncle Bob** : Separation of concerns (autrement appelé Principe de Séparation des Responsabilités)
> Chaque classe doit avoir **une seule raison de changer**.
> Si tu changes la façon dont tu stockes les cartes (mock → base de données → API externe), seul le Repository doit changer.
> Si tu changes une règle métier (ex: bonus terrain +300 au lieu de +200), seul le Service doit changer.
> C’est ça la **vraie** propreté du code.

### Concepts : `@Service`, `@Repository`, injection par constructeur

Spring a besoin de savoir quels objets il doit gérer. On lui dit avec des annotations.

**MockCardRepository** :
```java
@Repository
//  ^^^^^^^^^^
// Dit à Spring : "Cette classe est un composant qui gère les données → crée-la et gère-la pour moi"
//  @Repository = spécialisation de @Component pour les accès données
public class MockCardRepository implements CardRepository {
    // ... pas de changement dans le corps
}
```
Avec cette annotation, Spring scanne automatiquement, crée un bean, et le rend disponible pour injection.

**CardService** :
```java
@Service
//  ^^^^^^^^
//  @Service = spécialisation de @Component pour la logique métier
// Dit à Spring : "Ceci est un service métier → crée-le et gère-le"
public class CardService {

    private final CardRepository repository;

    // "Injection par constructeur" : Spring voit ce constructeur
    // et injecte automatiquement le MockCardRepository qu'il a créé
    public CardService(CardRepository repository) {
        this.repository = repository;
    }

    // Tes méthodes métier restent INCHANGÉES
}
```

Sprint va :

- Voir `@Service` → créer un bean `CardService`
- Voir que le constructeur attend un `CardRepository`
- Voir qu'il y a un bean `@Repository`qui implémente `CardRepository`
- Injecter le bean `MockCardRepository` dans `CardService`
-> Pas de `new` nulle part !

> **Pourquoi l'injection par constructeur plutôt que `@Autowired` sur un champ ?**
> Parce qu'elle rend les dépendances explicites, facilite les tests (tu peux passer un mock manuellement), et c'est la recommandation officielle de Spring.

**À ne PAS faire** (même si ça marche) :
```java
@Service
public class CardService {
    @Autowired  // ← déconseillé
    private CardRepository repository;
}

// De même que
@Service
public class CardService {
    @Autowired
    public void setRepository(CardRepository repository) {
        this.repository = repository;
    }
}
```

**Exercice** : annote `MockCardRepository` et `CardService`. Relance l'app. -> Plus d'erreurs.

---

## Étape 4 — Premier Controller REST

### Concepts : `@RestController`, `@GetMapping`, HTTP 200, JSON automatique

Maintenant que Spring gère nos Services et Repositories, on va exposer notre logique métier au monde extérieur via **HTTP**.

Le **Controller** est la couche la plus externe d'une application :

> **Tips de Bertrand** : Un controlleur dans une architecture propre, c'est un peu comme un standardiste : il ne fait rien lui même, il redirige vers le bon service.

Donc, un controlleur :

- Il reçoit les **requêtes HTTP** (GET, POST, etc.)
- Il appelle le bon Service
- Il renvoie une réponse (souvent du JSON)
- Il ne fait **presque rien d’autre** : pas de logique métier, pas d’accès direct aux données

> **Tips de l'oncle Bob** — Single Responsibility Principle (encore lui !)
> Le Controller doit avoir **une seule responsabilité** : traduire une requête HTTP en appel à un Service, et traduire le résultat en réponse HTTP.
> S’il commence à faire des calculs, des filtres complexes ou à parler directement au Repository → c’est un signal d’alarme.
> Déplace tout ce qui sent le métier dans le Service.

### 4.1 - Créer le Controller

Crée un nouveau fichier `src/main/java/org/yugiohproject/card/CardController.java` (ou adapte le package à ton projet).

Voici un exemple :

```java
package org.monsiteecommerce.card;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
// ^^^^^^^^^^^^^
// Combinaison de @Controller + @ResponseBody
// → Spring sait que cette classe gère des requêtes HTTP
// → et que les retours doivent être convertis automatiquement en JSON (via Jackson)

@RequestMapping("/catalog")
// ^^^^^^^^^^^^^^^^^^^^^
// Toutes les méthodes de ce controller auront un préfixe /catalog dans l’URL

public class CatalogController {

    private final CatalogService catalogService;

    // Injection par constructeur (comme pour le Service !)
    public CatalogController(CatalogService catalogService) {
        this.catalogService = catalogService;
    }

    @GetMapping
    // ^^^^^^^^^^
    // Répond à : GET http://localhost:8080/catalog
    // (pas de chemin supplémentaire → c’est la route racine du controller)
    public List<Card> getAllArticles() {
        return catalogService.getAllArticles();
        // ^^^^^^^^^^^
        // On délègue TOUT au Service → le Controller reste fin et stupide
    }
}
```

**Jackson fait la conversion automatiquement** :
```
Java                          JSON
------                        ----
List<Card>              →     [ { "id": 89631139, "name": "Dark Magician", ... }, ... ]
Card record             →     { "id": ..., "name": ..., "atk": ..., ... }
null                    →     null (ou absent selon config)
```

> **Exercice** : crée `CardController.java`, ajoute `getAllCards()` dans `CardService` si besoin. Lance et teste :

> **Tips de l'oncle Bob** — Nommage et lisibilité
> Les noms de méthodes dans un Controller doivent refléter l’intention métier vue de l’extérieur (pas l’implémentation interne).
> getAllCards() est parfait : clair, idiomatique REST, facile à comprendre même pour un frontend dev qui lit l’API.
> Évite listCards(), fetchAll(), retrieveCards() → getAllCards() dit exactement ce que fait l’endpoint.

```bash
# Dans un terminal
curl http://localhost:8080/cards

# Ou dans ton navigateur : http://localhost:8080/cards
# Ou dans Postman / Bruno : http://localhost:8080/cards
```

Résultat attendu (tableau JSON) :
```json
[
  { "id": 89631139, "name": "Dark Magician", "type": "NORMAL_MONSTER", ... },
  { "id": 5318639,  "name": "Blue-Eyes White Dragon", ... }
]
```

### 4.2 — Ce qui se passe automatiquement grâce à Spring Boot + Jackson

Quand tu appelles http://localhost:8080/cards :

1. Tomcat (serveur intégré) reçoit la requête GET
2. Spring route vers `CardController` grâce à `@RequestMapping("/cards") + @GetMapping`
3. Spring injecte automatiquement `CardService`
4. La méthode `getAllCards()` est appelée → retourne `List<Card>`
5. Jackson (inclus dans `spring-boot-starter-web`) convertit la liste en JSON
6. Réponse HTTP 200 OK avec `Content-Type: application/json`

**Tips Intellij Idea** : Debug Rapide

- Mets un breakpoint sur la ligne `return catalogService.getAllArticles();`
- Lance l'application en mode Debug (icône insecte)
- Ouvre ton navigateur et va sur http://localhost:8080/cards
- Tu peux alors inspecter les variables et suivre l'exécution pas à pas

---

## Étape 5 — Endpoint par ID

### Concepts : `@PathVariable`, `ResponseEntity`, HTTP 404

Maintenant qu’on a la liste complète (`GET /cards`), on veut pouvoir récupérer **une seule carte** par son ID :
`GET /cards/89631139` → retourne la carte Dark Magician (200 OK)
`GET /cards/999999` → retourne rien (404 Not Found)

C’est un pattern REST classique : **resource par identifiant unique**.

#### 5.1 — Ajouter la méthode dans le Service (d’abord le cerveau métier)

Dans `CardService.java`, ajoute une méthode qui délègue au Repository et renvoie un `Optional<Card>` (c’est idiomatique quand quelque chose peut ne pas exister) :

Exemple :

```java
public Optional<Article> findById(int id) {
    return repository.findById(id);
    // ^^^^^^^^^^^
    // Suppose que le repository a déjà une méthode findById(int id);
}

// Dans le repository (interface)
Optional<Article> findById(int id);

// Dans l'implémentation du repository (Mock...Repository)
@Override
public Optional<Article> findById(int id) {
    return CATALOGUE.stream()
                     .filter(article -> article.id() == id)
                     .findFirst();
}
```

#### 5.2 — Créer l’endpoint dans le Controller

Voici un exemple de controller :

```java
@GetMapping("/{id}")
// ^^^^^^^^^^^^^^^^
// {id} est une variable dans l’URL
// Exemples :
//   GET /articles/89631139   → id = 89631139
//   GET /articles/abc        → id ne sera pas converti en int → 400 Bad Request auto

public ResponseEntity<Article> getArticleById(@PathVariable int id) {
    // ^^^^^^^^^^^^^^^^^^^               ^^^^^^^^^^^^^
    // ResponseEntity = contrôle total : statut HTTP + headers + body
    // @PathVariable   = Spring extrait la valeur de l’URL et la met dans la variable id

    return catalogService.findById(id)
                      .map(article -> ResponseEntity.ok(article))
                      // ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
                      // Si présent → 200 OK + la carte en JSON dans le body
                      .orElse(ResponseEntity.notFound().build());
                      // ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
                      // Si absent → 404 Not Found + corps vide (build() = réponse vide)
}
```

> **Pourquoi `ResponseEntity` ?** Si on retourne `Card` directement, Spring ne peut retourner que 200. Avec `ResponseEntity`, on contrôle le code de statut.

| Retour possible | Statut HTTP forcé | Avantages / Inconvénients | Quand l’utiliser ? |
|----------------|-------------------|-----------------------------|--------------------|
| Card directement | Toujours 200 | "Simple, mais impossible de renvoyer 404 proprement" | Seulement si tu es sûr que ça existe toujours |
| Optional<Card> | Toujours 200 | Spring renvoie 200 même si empty (JSON null ou absent) → pas RESTful | Rarement |
| ResponseEntity<Card> | Tu décides (200, 404, 400…) | Contrôle fin du statut + headers + body. C’est la recommandation officielle pour les cas optionnels | Toujours pour les endpoints “get by id” |

> **Tips de l'oncle Bob — Clean Code & REST**
> Un bon endpoint “détail” doit être prévisible :
> Ressource trouvée → `200 OK + représentation JSON`
> Ressource non trouvée → `404 Not Found` (pas 200 + null, pas 204 vide)
> Mauvais ID (ex: string au lieu d’int) → Spring gère auto `400 Bad Request`
> Ne cache pas les erreurs : dis clairement au client ce qui s’est passé.
> C’est plus facile à debugger, à documenter (OpenAPI/Swagger), et à consommer depuis un frontend.

**Exercice** : implémente `findById(int id)` dans `CardService` (utilise le repository). Teste :

```bash
curl http://localhost:8080/cards/89631139   # → 200 + la carte
curl http://localhost:8080/cards/99999      # → 404
```

**Tips IntelliJ pour debugger**
- Mets un breakpoint dans `getCardById(...)` ou dans `findById(...)` du Service
- Lance en Debug (icône bug ou Shift+F9)
- Fais un curl ou refresh navigateur → tu verras le flux s’arrêter au breakpoint
- Inspecte la valeur de `id` et le résultat de `findById`

---

## Étape 6 — application.properties

### Concepts : configuration externalisée

Spring Boot adore être **configurable sans toucher au code**.
C’est là qu’intervient le fichier `src/main/resources/application.properties` (ou son cousin YAML : `application.yml`).

#### 6.1 — Où se trouve ce fichier et comment Spring le lit-il ?

- Crée-le (ou ouvre-le) dans `src/main/resources/`
  (IntelliJ le crée souvent automatiquement quand tu ajoutes Spring Boot)
- Spring Boot le lit **automatiquement** au démarrage
- Il surcharge les valeurs par défaut de Spring Boot
- Il peut être différent par environnement (dev, test, prod)

```properties
# Nom de l'application (visible dans les logs de démarrage)
spring.application.name=YugiDex

# Port du serveur web intégré (Tomcat par défaut)
server.port=8080

# Niveau de logs plus détaillé pour voir ce que fait Spring MVC
logging.level.org.springframework.web=DEBUG
# ou TRACE pour encore plus de détails (attention : très verbeux !)

# Optionnel : désactiver la bannière Spring Boot au démarrage
spring.main.banner-mode=off
```

**Exercice bonus** : Change le port en `9090`. Vérifie que `curl http://localhost:9090/cards` fonctionne.

#### 6.2 - Pourquoi ce fichier est important ?

- **Séparation config / code** : Le code reste propre et identique partout. La config change selon l’environnement. Exemple concret : port serveur 8080 en dev, 8081 en prod. -> moins de bugs de déploiement
- **Multi-environnements** : Un seul code → plusieurs fichiers : `application-dev.properties`, `application-test.properties`, `application-prod.properties`. Exemple : dev : logs DEBUG, prod : logs WARN. -> plus de flexibilité
- **pas besoin de recompiler** : tu peux changer le port, les logs, les accès BDD, les clés API sans recompiler (pas besoin de `mvn clean install`) -> plus de productivité

> **Tips de l’oncle Bob (et des 12 factors)**
> Ne jamais coder en dur des valeurs qui changent selon l’environnement (port, URL de base de données, clés API, niveau de log…).
> Si tu dois recompiler pour changer le port → c’est un code smell majeur.
> Externalise tout ce qui est configurable → ton code devient plus propre, plus testable, et prêt pour la vraie vie (serveurs, cloud, CI/CD).
---

#### Bonus

- Crée `application-dev.properties` et `application-prod.properties`
- Ajoute dans `application.properties` :

```properties
spring.profiles.active=dev
```

- Dans `application-dev.properties` :

```properties
server.port=9090
logging.level.org.springframework.web=DEBUG
```

- Dans `application-prod.properties` :

```properties
server.port=80
logging.level.org.springframework.web=INFO
```

- Lance avec `--spring.profiles.active=prod` (dans IntelliJ : Edit Configurations → Program arguments)
- Tu vois la différence sans recompiler !
