# TD-03 — API Client : appeler YGOProDeck depuis Java
> Remplacer les données mock par la vraie API YGOProDeck
> Package : `org.yugiohproject`

---

## Avant de commencer

À ce stade, `GET /cards` retourne des données en dur depuis `MockCardRepository`.

L'objectif de ce TD : **remplacer le mock par de vraies données** en appelant l'API externe YGOProDeck.

**Question** : pourquoi appeler l'API depuis le backend Java, plutôt que directement depuis Angular ?

Pense-y avant de lire la suite.

> **Réponses possibles** (plusieurs sont valides) :
> - Masquer les détails de l'API externe (clé d'API si nécessaire, URL...)
> - Mettre en cache les résultats côté serveur (éviter de re-appeler pour chaque utilisateur)
> - Transformer la réponse au format qu'on veut (ne garder que les champs utiles)
> - Avoir un seul point de contrôle si l'API externe change

---

## Étape 0 — Lire l'API YGOProDeck

### Concepts : API REST, paramètres de requête, documentation

Avant d'écrire du code, explore l'API manuellement.

**Endpoints utiles** :

```bash
# Toutes les cartes (attention : beaucoup !)
curl "https://db.ygoprodeck.com/api/v7/cardinfo.php"

# Une carte par nom exact
curl "https://db.ygoprodeck.com/api/v7/cardinfo.php?name=Dark+Magician"

# Recherche par nom partiel
curl "https://db.ygoprodeck.com/api/v7/cardinfo.php?fname=Dragon"

# Filtrer par type
curl "https://db.ygoprodeck.com/api/v7/cardinfo.php?type=Normal+Monster"

# Limiter le nombre de résultats
curl "https://db.ygoprodeck.com/api/v7/cardinfo.php?fname=Dragon&num=5&offset=0"
```

**Structure de la réponse** (exemple simplifié) :
```json
{
  "data": [
    {
      "id": 89631139,
      "name": "Dark Magician",
      "type": "Normal Monster",
      "desc": "The ultimate wizard...",
      "atk": 2500,
      "def": 2100,
      "level": 7,
      "race": "Spellcaster",
      "attribute": "DARK",
      "card_images": [
        { "image_url": "https://..." }
      ]
    }
  ]
}
```

**Exercice** : ouvre les URLs ci-dessus dans ton navigateur ou avec `curl`. Note :
1. La structure JSON exacte (notamment le tableau `data` qui contient les cartes)
2. Les champs disponibles
3. Ce qui diffère entre un monstre et une carte magie/piège (atk, def, level absents)

---

## Étape 1 — Modèle de réponse API

### Concepts : DTOs, désérialisation JSON, Jackson

On a vu que la structure JSON renvoyée par l’API **n’est pas** exactement la même que notre modèle interne `Card` (record ou classe).

→ On ne va **pas** réutiliser directement notre `Card` pour désérialiser le JSON.
→ À la place, on crée des classes **spécifiques à l’API externe** : ce sont des **DTO** (Data Transfer Objects).

#### C’est quoi un DTO ? (définition simple)

Un **DTO** (aussi appelé *Data Transfer Object*) est une classe (ou record) **très plate** qui sert **uniquement** à transporter des données entre deux systèmes :
- De l’API externe → vers notre application
- Ou de notre application → vers un client frontend / une autre API

**Caractéristiques typiques d’un DTO** :
- Pas de logique métier (pas de méthodes calculées, pas de validation business)
- Champs qui correspondent **exactement** (ou presque) à la structure JSON/XML reçue/envoyée
- Souvent immutable (record en Java 17+)
- Peut contenir des champs qu’on ignore ou qu’on mappe plus tard

#### Pourquoi c’est important d’utiliser des DTOs ? (et pas notre modèle interne)

| Problème si on réutilise `Card` directement | Pourquoi c’est un souci (Clean Arch / Uncle Bob) | Solution avec DTO |
|---------------------------------------------|--------------------------------------------------|-------------------|
| L’API change un nom de champ (`atk` → `attack`) | Notre domaine interne casse → on doit modifier `Card` pour une raison externe | DTO absorbe le changement, domaine reste stable |
| L’API renvoie 50 champs inutiles (`card_sets`, `card_prices`, `banlist_info`…) | On pollue notre modèle métier avec du bruit externe | DTO prend tout, on extrait seulement ce qu’on veut dans le Service |
| L’API utilise des types différents (String pour atk au lieu d’Integer) | On force des conversions dans le domaine → violation de Single Responsibility | DTO accepte le format brut, Service mappe vers `Card` propre |
| **Code duplication** apparente | On a deux classes presque identiques → semble redondant | C’est **volontaire** : chaque bounded context (externe vs interne) a son modèle adapté |

> **Tips de l’oncle Bob** — Separation of Concerns & Bounded Contexts
> « Ne mélange jamais les modèles de tes bounded contexts. »
> Ton domaine interne (`Card`) représente **tes règles métier** (calcul puissance, effets, etc.).
> Le modèle de l’API externe représente **le contrat de l’API tierce** (ce qu’ils nous envoient).
> Les faire coïncider par magie = couplage fort + futur bug garanti quand l’API évolue.
> Accepte la petite duplication : c’est un **investissement** pour la stabilité et la maintenabilité.

#### Wrapper de réponse : c’est quoi ?

La plupart des APIs REST renvoient un objet **enveloppant** (wrapper) autour des données utiles :

```json
{
  "data": [               ← tableau de cartes
    { "id": 89631139, "name": "Dark Magician", ... },
    ...
  ],
  "meta": { ... }         ← parfois pagination, infos diverses
}

```

-> On va créer un DTO pour le wrapper de réponse (par exemple `YgoApiResponse`) pour représenter exactement cette enveloppe. On appelle ça un Wrapper car il capture toute la structure de la réponse.

##### Organisation des packages (rappel Clean Architecture)

Crée un package dédié pour le client API externe :

```
card/
├── client/                ← tout ce qui concerne l’API externe (DTOs + futur client HTTP)
│   ├── YgoApiResponse.java
│   └── YgoCardDto.java
├── Card.java              ← notre modèle domaine interne
├── CardService.java
└── ...
```

##### Code des DTOs

**YgoApiResponse** : wrapper qui contient la liste

```java
// Hint : Jackson utilise les noms des champs pour mapper le JSON
// Le champ "data" dans le JSON → champ "data" dans la classe
public record YgoApiResponse(List<YgoCardDto> data) {}
```

**YgoCardDto** : représentation brute de l'API

```java
public record YgoCardDto(
    int id,
    String name,
    String type,
    String desc,
    Integer atk,
    Integer def,
    Integer level,
    String race,
    String attribute
    // Pour l'instant on ignore (mais on pourra ajouter plus tard) :
    // List<CardImage> card_images,
    // List<CardSetInfo> card_sets,
    // etc.
) {}
```

> **Jackson et les noms de champs** : par défaut, Jackson mappe `"name"` JSON → champ `name` Java.
> Si le JSON a `"card_images"` et ton champ Java s'appelle `cardImages`, tu dois annoter avec
> `@JsonProperty("card_images")`. Pour l'instant, on nomme les champs pareil que le JSON.

**Exercice** :
1. Crée le package `card.client`.
2. Crée les deux records `YgoApiResponse` et `YgoCardDto` dans ce package.
3. Vérifie que le code compile.

---

## Étape 2 — RestClient

### Concepts : HTTP client, RestClient (Spring 6.1+), builder pattern

#### Qu'est-ce qu'un client HTTP ?

Un **client HTTP** est une bibliothèque qui permet à ton application Java de **faire des requêtes** vers un serveur distant (comme une API web), exactement comme ton navigateur ou curl le fait.

Exemples de ce qu’un client HTTP fait pour toi :
- Construit la requête (GET/POST, URL, headers, query params…)
- Envoie la requête sur le réseau
- Attend la réponse
- Lit le statut HTTP (200, 404, 500…)
- Lit le body (JSON, XML…)
- Gère les erreurs réseau, timeouts, redirections…

Sans client HTTP, tu devrais tout coder à la main avec `java.net.HttpURLConnection` (très verbeux et pénible).

> Pour les curieux : [Doc Oracle HttpURLConnection](https://docs.oracle.com/javase/8/docs/api/java/net/HttpURLConnection.html#getResponseMessage--)

#### C’est quoi RestClient ? Pourquoi Spring l’a introduit ?

Depuis **Spring Framework 6.1** (et Spring Boot 3.2+, fin 2023), Spring a ajouté **RestClient** : une API **moderne, fluide et synchrone** pour appeler des APIs REST.

**Pourquoi RestClient ?**
- Il remplace **RestTemplate** (l’ancien client synchrone, datant de 2003, très verbeux et plus maintenu activement)
- Il offre une syntaxe **flue** (fluent API) comme **WebClient**, mais **sans reactive** (pas de Mono/Flux, pas besoin de WebFlux)
- Il est **plus simple** à lire et à écrire que RestTemplate
- Il gère automatiquement la désérialisation JSON → objets Java (via Jackson)
- Il est **thread-safe** et performant pour la plupart des apps classiques (pas besoin de 100k req/s ultra-réactives)

```java
RestClient client = RestClient.builder()
    .baseUrl("https://db.ygoprodeck.com/api/v7")
    //         ^^^^^^^^^^^
    //         URL de base — les appels suivants ajoutent leur chemin par-dessus
    .build();

// Exemple d'appel
YgoApiResponse response = client.get()
    .uri("/cardinfo.php?name=Dark+Magician")
    .retrieve()
    .body(YgoApiResponse.class);
    //     ^^^^^^^^^^^^^^^^^
    //     Jackson désérialise le JSON en YgoApiResponse automatiquement
```

-> Si la carte existe : `response.data()` contient la liste des cartes.
-> Si la carte n'existe pas : `response.data()` contient une liste vide, ou l'API renvoie une erreur `400 Bad Request`.

> **RestClient vs WebClient** : `RestClient` est synchrone (attend la réponse avant de continuer). `WebClient` est asynchrone (non-bloquant, meilleur pour les applis à très fort trafic). Pour apprendre, `RestClient` est plus simple. On verra l'asynchrone plus tard.

**Configurer le RestClient comme bean Spring** :

On ne créé pas le client à la main dans le Service mais on le déclare comme un bean Spring pour :
- le réutiliser partout
- le configurer une seule fois
- le tester facilement (mock)

**Exercice** :
1. Crée le fichier `YgoApiConfig.java` dans le package `card.client`.
2. Déclare le bean `ygoRestClient` avec `@Bean`.
3. Vérifie que le code compile.

Exemple de fichier :

```java
// Fichier : card/client/YgoApiConfig.java
@Configuration
// ^^^^^^^^^^^
// Classe dédiée à la configuration de beans (pas de logique métier ici)
public class ShopApiConfig {

    @Bean
    // ^^^^
    // Dit à Spring : "Crée cet objet une fois et rends-le injectable partout"
    public RestClient shopRestClient() {
        return RestClient.builder()
            .baseUrl("https://api.shop.com")
            // .defaultHeader("User-Agent", "YugiDex/1.0")  // optionnel
            // .requestInterceptor(...)                     // pour logs, auth, etc.
            .build();
    }
}
```

[Documentation RestClient](https://jakarta.ee/learn/docs/jakartaee-tutorial/current/websvcs/rest-client/rest-client.html)

-> **Pourquoi un `@Bean` dans une `@Configuration` plutôt qu'un `@Component` ?**
Parce que `RestClient` est une classe externe qu'on ne peut pas annoter. Le pattern `@Configuration + @Bean` permet de déclarer des beans pour des classes qu'on ne contrôle pas.

> **Tips de l’oncle Bob** : **Encapsulation & Single Responsibility**
> - Le code qui appelle une API externe doit être isolé et encapsulé.
> - Ne pollue pas ton **CardService** (cœur métier) avec des URLs, des RestClient.builder(), des try-catch réseau…
> - Crée une classe dédiée (ou un bean) pour tout ce qui touche l’API externe : c’est un bounded context à part.
> - Si YGOProDeck change d’URL ou ajoute OAuth demain, seul ce bean change → ton domaine reste propre et stable.
---

## Étape 3 — YgoCardRepository : remplacer le Mock

### Concepts : implémentation alternative d'une interface, `@Primary`

Rappel : on a une interface `CardRepository` avec deux implémentations possibles :

```
CardRepository (interface)
├── MockCardRepository    ← données en dur (TD-01)
└── YgoCardRepository     ← appelle l'API (ce TD)
```

**Pourquoi deux implémentations ?**

Jusqu'ici, `CardService` dépend de l'interface `CardRepository`, pas d'une implémentation concrète. C'est un choix délibéré : ça nous a permis de travailler avec des données en dur pendant qu'on construisait le reste. Maintenant on va brancher la vraie source de données.

> **Uncle Bob** : dépendre d'une abstraction, pas d'une implémentation. `CardService` ne sait pas, et ne doit jamais savoir d'où viennent les cartes.

#### Créer `YgoCardMapper``

Avant d'écrire le **Repository**, poses toi cette question :
> *Qui est responsable de transformer un `YgoCardDto` (format API) en `Card` (ton domaine) ?*

`YgoCardRepository`a déjà une **responsabilité** : accéder à l'API.

Lui confier la conversion des données serait lui donner une nouvelle responsabilité et donc, lui donner deux raisons de changer (c'est à dire que le fichier YgoCardRepository devrait être modifié si l'API change ou si le format de la carte change). C'est une **violation du principe Single Responsibility**.

**Crée `YgoCardMapper`** :

```java
public class YgoCardMapper {

    public Card toCard(YgoCardDto dto) {
        // Hints :
        // - CardType et CardAttribute sont des enums : il faut convertir le String de l'API
        // - "Normal Monster" → CardType.NORMAL_MONSTER (réfléchis à une stratégie de mapping, hint : switch)
        // - Un monstre a un attribut, une magie/piège n'en a pas → attention aux null
        throw new UnsupportedOperationException("TODO");
    }
}
```

> Question à se poser : faut-il que YgoCardMapper soit un bean Spring (@Component) ou une simple classe Java instanciée à la main ? Quels sont les avantages de chaque approche ?

---

#### Créer `YgoCardRepository`

```java
@Repository
public class YgoCardRepository implements CardRepository {

    private final RestClient restClient;
    private final YgoCardMapper mapper;

    public YgoCardRepository(RestClient restClient, YgoCardMapper mapper) {
        this.restClient = restClient;
        this.mapper = mapper;
    }

    @Override
    public List<Card> findAll() {
        // L'API retourne 10 000+ cartes sans paramètres — commence avec num=5
        // Endpoint : /cardinfo.php?num=5&offset=0
        YgoApiResponse response = restClient.get()
            .uri(/* À toi de construire l'URI */)
            .retrieve()
            .body(YgoApiResponse.class);

        return response.data().stream()
            .map(mapper::toCard)
            .toList();
    }

    @Override
    public Optional<Card> findById(int id) {
        // L'API retourne toujours { "data": [...] } même pour un seul résultat
        // Endpoint : /cardinfo.php?id=...
        throw new UnsupportedOperationException("TODO");
    }
}
```

#### Résoudre le conflit Spring

**Problème** : Spring voit deux `@Repository` qui implémentent `CardRepository`. Lequel injecter dans `CardService` ?

C'est une bonne situation : ça veut dire que notre architecture est flexible. Spring a deux solutions :

```java
// Solution 1 : @Primary sur celui qu'on veut utiliser par défaut
@Repository
@Primary  // ← Spring utilisera celui-ci quand plusieurs candidats existent
public class YgoCardRepository implements CardRepository { ... }

// Solution 2 : @Qualifier dans CardService (plus explicite)
public CardService(@Qualifier("ygoCardRepository") CardRepository repository) { ... }
```

> **Réfléchis** : laquelle de ces deux options préserve le mieux le principe leaving options open ? Laquelle oblige à modifier CardService si on change d'implémentation ?
> *leaving options open : permet de garder les options ouvertes pour les futures implémentations*

**Exercice** : implémente `YgoCardMapper.toCard()`, puis `YgoCardRepository.findAll()`. Teste avec `num=5`. Vérifie que `CardService` n'a pas bougé d'une ligne.

---

## Étape 5 — Gestion des erreurs HTTP

### Pourquoi des exceptions métier ?

Quand `findById()`ne trouve pas de carte, que doit-il se passer ? Plusieurs options :
- Retourner `null`-> le code appelant doit penser à vérifier, et s'il oublie : `NullPointerException` en production
- Retourner `Optional.empty()` -> c'est déjà mieux mais ça ne dit pas pourquoi c'est vide
- Lever une exception -> interrompt le flux, remonte jusqu'à quelqu'un capable de gérer l'exception

L'`Optional`et l'exception ne sont pas en opposition : `Optional`dit "je n'ai peut-être rien", l'exception dit "quelque chose s'est mal passé". Une carte introuvable dans une recherche par ID, c'est plutôt le second cas : l'appelant a fourni un ID qui ne correspond à aucune carte, c'est une situation anormale.
> Règle : une absence prévisible -> `Optional`, une absence anormale -> exception.

### Créer une exception métier

Une exception métier, c'est simplement une classe qui hérite de `RuntimeException` et qui porte un nom explicite pour le domaine métier. Par exemple :

```java
public class CardNotFoundException extends RuntimeException {
    public CardNotFoundException(int id) {
        super("Carte non trouvée : " + id);
    }
}
```

### Faut-il une exception pour chaque cas ?

Non — et c'est un piège courant. Le principe est : **une exception par *situation métier* distincte**, pas une par méthode.

Quelques exemples pour se donner une règle :

- `CardNotFoundException` → la carte demandée n'existe pas
- `YgoApiUnavailableException` → l'API externe ne répond pas (situation différente, cause différente, traitement différent)
- Pas besoin de `FindByIdFailedException`, `FetchCardsException`, etc. — c'est du bruit

Si deux situations appellent la même réponse (même log, même code HTTP retourné), c'est probablement la même exception.

---

### Où les placer dans l'architecture ?

C'est une question d'architecture qui mérite qu'on s'y arrête. Ton domaine (`Card`, `CardRepository`) ne devrait pas dépendre de Spring, de HTTP, ou de quoi que ce soit d'infrastructure. Les exceptions métier suivent la même règle :
```
com.toi.yugioh
├── domain
│   ├── Card.java
│   ├── CardRepository.java
│   └── exception
│       └── CardNotFoundException.java   ← ici : c'est une règle métier
│
├── infrastructure
│   └── api
│       ├── YgoCardRepository.java       ← lève CardNotFoundException
│       ├── YgoCardMapper.java
│       └── YgoApiUnavailableException.java  ← ici : spécifique à l'infra
│
└── web
    └── CardController.java              ← attrape et traduit en réponse HTTP
```

`CardNotFoundException` vit dans le domaine parce que "une carte peut ne pas être trouvée" est une règle métier, indépendante de la source de données. `YgoApiUnavailableException` vit dans l'infra parce qu'elle est spécifique à YGOProDeck, si demain tu changes de source, elle disparaît.

### Implémentation dans le code

```java
@Override
public Optional<Card> findById(int id) {
    YgoApiResponse response = restClient.get()
        .uri(/* À toi : /cardinfo.php?id=... */)
        .retrieve()
        .onStatus(status -> status.is4xxClientError(), (req, res) -> {
            throw new CardNotFoundException(id);
        })
        .onStatus(status -> status.is5xxServerError(), (req, res) -> {
            throw new YgoApiUnavailableException("YGOProDeck a retourné : " + res.getStatusCode());
        })
        .body(YgoApiResponse.class);

    // L'API retourne toujours { "data": [...] } — même pour un seul résultat
    return response.data().stream()
        .map(mapper::toCard)
        .findFirst();
}
```

> *Remarque :* on retourne toujours un Optional ici — c'est `findFirst()` qui gère le cas "liste vide". `CardNotFoundException` n'est levée que si l'API elle-même dit que la carte n'existe pas (400). Ce sont deux cas distincts.

#### Remonter l'erreur jusqu'au Controller

Pour l'instant, si `CardNotFoundException` est levée, Spring retourne une 500 générique. Ce n'est pas ce qu'on veut : du point de vue du client HTTP, une carte introuvable c'est un 404.

**Exercice :** ajoute un `@ExceptionHandler` dans le Controller — ou mieux, dans une classe séparée annotée `@ControllerAdvice`. Pourquoi `@ControllerAdvice` est-il préférable à un handler directement dans `CardController` ?

---

## Étape 6 — Vérifier avec curl

Lance l'app et teste :

```bash
# Liste des premières cartes
curl http://localhost:8080/cards | jq .

# Une carte spécifique
curl http://localhost:8080/cards/89631139 | jq .

# Carte inexistante
curl -i http://localhost:8080/cards/99999
# Doit retourner HTTP 404
```

> `jq` est un outil pour formater le JSON dans le terminal. Sur Mac : `brew install jq`. Sur Windows : https://jqlang.github.io/jq/

---
