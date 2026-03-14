# TD-05 — Deck CRUD + Tests
> Exposer les opérations sur les decks via REST et écrire les premiers tests
> Package : `org.yugiohproject`

---

## Avant de commencer

À ce stade :
- Les cartes sont servies par `GET /cards` (API YGOProDeck)
- Les decks sont persistés en PostgreSQL via JPA

Il manque : **exposer les decks via des endpoints REST** et **tester le code**.

**Question** : quelles opérations doit-on exposer sur les decks ?

Pense à toutes les actions qu'un utilisateur peut faire avec un deck :

> Créer, lire, modifier le nom, ajouter une carte, retirer une carte, supprimer le deck...

---

## Étape 0 — Les verbes HTTP

### Concepts : GET, POST, PUT, PATCH, DELETE, idempotence

| Verbe | Utilisation | Idempotent ? |
|-------|------------|-------------|
| `GET` | Lire des données | Oui |
| `POST` | Créer une ressource | Non |
| `PUT` | Remplacer une ressource entière | Oui |
| `PATCH` | Modifier partiellement | Non |
| `DELETE` | Supprimer | Oui |

> **Idempotent** : appeler la requête plusieurs fois donne le même résultat qu'une seule fois. `GET /decks/1` retourne toujours le même deck. `DELETE /decks/1` supprime le deck une fois — le rappeler (deck déjà supprimé) donne 404, mais l'état de la base ne change plus.

**Notre API Deck** :
```
GET    /decks                    → liste tous les decks
GET    /decks/{id}               → un deck par ID
POST   /decks                    → créer un deck
DELETE /decks/{id}               → supprimer un deck
POST   /decks/{id}/cards         → ajouter une carte au deck
DELETE /decks/{id}/cards/{cardId} → retirer une carte du deck
```

---

## Étape 1 — DTOs de requête/réponse

### Concepts : DTO request/response, séparation entité/API

**Ne jamais exposer l'entité JPA directement** dans le Controller. Pourquoi ?

1. L'entité a des annotations JPA qui n'ont rien à faire dans le JSON
2. On contrôle exactement ce qu'on expose (sécurité)
3. Le format API peut évoluer sans changer le modèle de données

Crée un package `deck/dto/` :

**Requête de création** (ce que le client envoie) :
```java
// CreateDeckRequest.java
public record CreateDeckRequest(
    String name,
    String ownerName
) {
    // Validation dans le compact constructor
    public CreateDeckRequest {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Le nom du deck est requis");
        }
        if (ownerName == null || ownerName.isBlank()) {
            throw new IllegalArgumentException("Le nom du joueur est requis");
        }
    }
}
```

**Réponse** (ce qu'on retourne) :
```java
// DeckResponse.java
public record DeckResponse(
    Long id,
    String name,
    String ownerName,
    int cardCount,
    List<Integer> cardIds
) {
    // Factory method : construit le DTO depuis l'entité
    public static DeckResponse from(Deck deck) {
        return new DeckResponse(
            deck.getId(),
            deck.getName(),
            deck.getOwnerName(),
            deck.getCardIds().size(),
            deck.getCardIds()
        );
    }
}
```

> **Factory method `from()`** : plutôt que d'avoir un constructeur qui prend un `Deck`, on utilise une méthode statique nommée. Ça se lit mieux : `DeckResponse.from(deck)`.

**Exercice** : crée ces deux records dans `deck/dto/`.

---

## Étape 2 — DeckController

### Concepts : `@PostMapping`, `@DeleteMapping`, `@RequestBody`, codes HTTP 201/204

```java
// Fichier : deck/DeckController.java
@RestController
@RequestMapping("/decks")
public class DeckController {

    private final DeckService deckService;

    public DeckController(DeckService deckService) {
        this.deckService = deckService;
    }

    // GET /decks?owner=Bertrand
    @GetMapping
    public List<DeckResponse> getDecks(
            @RequestParam(required = false) String owner) {
        //   ^^^^^^^^^^^
        //   Paramètre de requête : /decks?owner=Bertrand
        //   required = false → si absent, owner = null
        if (owner != null) {
            return deckService.getDecksForPlayer(owner)
                .stream().map(DeckResponse::from).toList();
        }
        return deckService.getAllDecks()
            .stream().map(DeckResponse::from).toList();
    }

    // GET /decks/{id}
    @GetMapping("/{id}")
    public DeckResponse getDeckById(@PathVariable Long id) {
        return DeckResponse.from(deckService.findById(id));
    }

    // POST /decks → 201 Created
    @PostMapping
    public ResponseEntity<DeckResponse> createDeck(
            @RequestBody CreateDeckRequest request) {
        //   ^^^^^^^^^^^
        //   Jackson désérialise le corps JSON de la requête en CreateDeckRequest
        Deck created = deckService.createDeck(request.name(), request.ownerName());
        return ResponseEntity
            .status(HttpStatus.CREATED)
            //      ^^^^^^^^^^^^^^^^^^^
            //      201 au lieu de 200 : convention REST pour "ressource créée"
            .body(DeckResponse.from(created));
    }

    // DELETE /decks/{id} → 204 No Content
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDeck(@PathVariable Long id) {
        deckService.deleteDeck(id);
        return ResponseEntity.noContent().build();
        //                   ^^^^^^^^^
        //                   204 No Content : suppression réussie, pas de corps
    }

    // POST /decks/{id}/cards → ajouter une carte
    @PostMapping("/{id}/cards")
    public DeckResponse addCard(
            @PathVariable Long id,
            @RequestBody AddCardRequest request) {
        // AddCardRequest : record avec { "cardId": 89631139 }
        // TODO : à implémenter
        throw new UnsupportedOperationException("TODO");
    }

    // DELETE /decks/{id}/cards/{cardId}
    @DeleteMapping("/{id}/cards/{cardId}")
    public DeckResponse removeCard(
            @PathVariable Long id,
            @PathVariable int cardId) {
        // TODO
        throw new UnsupportedOperationException("TODO");
    }
}
```

**Exercice** : complète les méthodes `addCard` et `removeCard`. Crée `AddCardRequest`.

---

## Étape 3 — Gestion globale des erreurs

### Concepts : `@RestControllerAdvice`, `@ExceptionHandler`

Plutôt que de gérer les exceptions dans chaque controller, on centralise :

```java
// Fichier : common/GlobalExceptionHandler.java
@RestControllerAdvice
//  ^^^^^^^^^^^^^^^^^^^
//  Intercepte les exceptions de tous les controllers
public class GlobalExceptionHandler {

    // Représentation d'une erreur en JSON
    public record ErrorResponse(String message, int status) {}

    @ExceptionHandler(DeckNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleDeckNotFound(DeckNotFoundException e) {
        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(new ErrorResponse(e.getMessage(), 404));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleBadRequest(IllegalArgumentException e) {
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(new ErrorResponse(e.getMessage(), 400));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleConflict(IllegalStateException e) {
        // Ex : deck plein (60 cartes max)
        return ResponseEntity
            .status(HttpStatus.CONFLICT)
            .body(new ErrorResponse(e.getMessage(), 409));
    }
}
```

**Exercice** : crée `GlobalExceptionHandler`. Teste qu'un `GET /decks/999` retourne `{ "message": "Deck 999 introuvable", "status": 404 }`.

---

## Étape 4 — Tests unitaires avec JUnit 5

### Concepts : JUnit 5, Mockito, `@ExtendWith`, `given/when/then`

Les tests unitaires vérifient la logique **sans base de données, sans Spring**.

```xml
<!-- Ajouter dans pom.xml (Spring Boot l'inclut déjà via starter-test) -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-test</artifactId>
    <scope>test</scope>
</dependency>
```

**Structure d'un test** :

```java
// Fichier : src/test/java/org/yugiohproject/deck/DeckServiceTest.java
@ExtendWith(MockitoExtension.class)
//          ^^^^^^^^^^^^^^^^^^^^^^
//          Active Mockito (pas Spring — on reste unitaire)
class DeckServiceTest {

    @Mock
    //  ^^^^^
    //  Crée un faux DeckRepository — on contrôle ses réponses
    DeckRepository deckRepository;

    @InjectMocks
    //  ^^^^^^^^^^^^
    //  Crée un vrai DeckService en injectant le @Mock ci-dessus
    DeckService deckService;

    @Test
    void createDeck_shouldReturnSavedDeck() {
        // GIVEN (préparation)
        String name = "Mon Deck Dragon";
        String owner = "Bertrand";
        Deck savedDeck = new Deck(name, owner);
        // Configurer le mock : quand save() est appelé, retourner savedDeck
        when(deckRepository.existsByNameAndOwnerName(name, owner)).thenReturn(false);
        when(deckRepository.save(any(Deck.class))).thenReturn(savedDeck);

        // WHEN (action)
        Deck result = deckService.createDeck(name, owner);

        // THEN (vérification)
        assertThat(result.getName()).isEqualTo(name);
        assertThat(result.getOwnerName()).isEqualTo(owner);
        verify(deckRepository).save(any(Deck.class));
        //     ^^^^^^^^^^^^^^^^^^^^^^^^^^
        //     Vérifie que save() a bien été appelé une fois
    }

    @Test
    void createDeck_shouldThrowWhenDeckAlreadyExists() {
        // GIVEN
        when(deckRepository.existsByNameAndOwnerName("Mon Deck", "Bertrand"))
            .thenReturn(true);  // ← le deck existe déjà

        // WHEN + THEN
        assertThatThrownBy(() -> deckService.createDeck("Mon Deck", "Bertrand"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Mon Deck");
    }
}
```

**Exercice** : écris des tests pour `addCardToDeck()`. Cas à couvrir :
1. Ajouter une carte → succès
2. Ajouter à un deck qui n'existe pas → `DeckNotFoundException`
3. Ajouter une 61ème carte → `IllegalStateException`

---

## Étape 5 — Tests d'intégration du Controller

### Concepts : `@WebMvcTest`, `MockMvc`, tester les endpoints HTTP

Les tests d'intégration vérifient que le controller fonctionne correctement **avec Spring MVC**, mais **sans base de données**.

```java
// Fichier : src/test/java/.../deck/DeckControllerTest.java
@WebMvcTest(DeckController.class)
//          ^^^^^^^^^^^^^^^^^^^^
//          Charge uniquement le controller (pas tout Spring)
class DeckControllerTest {

    @Autowired
    MockMvc mockMvc;
    //       ^^^^^^^
    //       Simulateur HTTP — envoie des requêtes fictives

    @MockBean
    //  ^^^^^^^^^
    //  Remplace le vrai DeckService par un mock dans le contexte Spring
    DeckService deckService;

    @Autowired
    ObjectMapper objectMapper;  // pour sérialiser Java → JSON

    @Test
    void POST_decks_shouldReturn201() throws Exception {
        // GIVEN
        CreateDeckRequest request = new CreateDeckRequest("Dragon Deck", "Bertrand");
        Deck mockDeck = new Deck("Dragon Deck", "Bertrand");
        when(deckService.createDeck("Dragon Deck", "Bertrand")).thenReturn(mockDeck);

        // WHEN + THEN
        mockMvc.perform(
                post("/decks")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
            .andExpect(status().isCreated())
            //                 ^^^^^^^^^
            //                 201 Created
            .andExpect(jsonPath("$.name").value("Dragon Deck"))
            //                  ^^^^^^
            //                  JSONPath : naviguer dans le JSON de la réponse
            .andExpect(jsonPath("$.ownerName").value("Bertrand"));
    }

    @Test
    void GET_decks_unknownId_shouldReturn404() throws Exception {
        when(deckService.findById(999L))
            .thenThrow(new DeckNotFoundException("Deck 999 introuvable"));

        mockMvc.perform(get("/decks/999"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.message").value("Deck 999 introuvable"));
    }
}
```

**Exercice** : écris les tests pour `DELETE /decks/{id}` et `POST /decks/{id}/cards`.

---

## Étape 6 — Lancer les tests

```bash
# Lancer tous les tests
mvn test

# Lancer un seul fichier de test
mvn test -Dtest=DeckServiceTest

# Voir le rapport de couverture (si jacoco est configuré)
mvn verify
```

---

## Récap des fichiers créés

```
src/
├── main/java/org/yugiohproject/
│   ├── common/
│   │   └── GlobalExceptionHandler.java
│   └── deck/
│       ├── DeckController.java
│       └── dto/
│           ├── CreateDeckRequest.java
│           ├── AddCardRequest.java
│           └── DeckResponse.java
└── test/java/org/yugiohproject/
    └── deck/
        ├── DeckServiceTest.java      ← tests unitaires
        └── DeckControllerTest.java   ← tests d'intégration
```

---

## Prochaine étape

**Phase 2 — Intégration Frontend/Backend** :
- Configurer CORS dans Spring Boot (pour qu'Angular puisse appeler le backend)
- Angular appelle `GET /cards` et `GET /decks`
- Introduction au TDD : écrire les tests *avant* le code (JUnit côté Java)
- Introduction au DDD : identifier les bounded contexts et les agrégats

**Lectures recommandées** :
- *Clean Code* — Robert C. Martin (Uncle Bob)
- *Domain-Driven Design* — Eric Evans (le livre de référence)
- *Test-Driven Development by Example* — Kent Beck
