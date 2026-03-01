# YugiDex

Application de gestion et consultation de cartes Yu-Gi-Oh!  
(Projet d'apprentissage Clean Architecture / Hexagonal / Package-by-Feature)

## Philosophie & Architecture

Ce projet suit une approche **feature-first** (package par fonctionnalité) inspirée des principes de :

- Clean Architecture (Uncle Bob)
- Hexagonal Architecture / Ports & Adapters
- Package-by-feature plutôt que package-by-layer

### Pourquoi ce choix ?

- **Localisation rapide** : tout ce qui concerne les cartes est dans `src/main/java/org/yugioh/card/…`
- **Suppression facile** : une feature entière = un dossier → on peut la virer sans tout casser
- **Évolutivité** : facile de transformer une feature en module Maven/Gradle ou microservice plus tard
- **Découplage fort** : le domaine ne connaît ni Spring, ni HTTP, ni JPA
- **Testabilité maximale** : les use-cases sont purs → faciles à tester sans mocks lourds

### Structure actuelle des packages (2026-03)

```
src/main/java/org/yugioh
└── card                           ← feature principale pour le moment
├── domain                     ← entités riches + value objects + règles métier pures
│   └── Card.java
│   └── exceptions/
├── application                ← use-cases / services métier (interface + impl)
│   └── CardService.java
│   └── dto/                   ← objets d’entrée/sortie des use-cases
├── adapter
│   ├── inbound                ← ce qui rentre (REST, CLI, etc.)
│   │   └── rest
│   │       └── CardController.java
│   └── outbound               ← ce qui sort (BDD, API externe YGOPRODeck, etc.)
│       └── persistence
│           └── CardRepository.java     (interface)
│           └── JpaCardRepository.java  (impl Spring Data)
└── CardModuleConfiguration.java   ← @Configuration si besoin de wiring spécifique
```


→ **Règle d’or** : les flèches ne pointent jamais vers le haut. Le domaine reste au centre et ne dépend de rien.

## Prérequis

- Java 21 (ou 17 LTS)
- Maven / Gradle (au choix)
- (optionnel) Docker + PostgreSQL pour la persistence

## Lancement rapide

```bash
# 1. Build
./mvnw clean package
# ou
./gradlew build

# 2. Lancement
java -jar target/yugidex-0.0.1-SNAPSHOT.jar
# ou
./mvnw spring-boot:run
```

## Inspirations & lectures recommandées

- Clean Architecture – Robert C. Martin
- Get Your Hands Dirty on Clean Architecture – Tom Hombergs
- https://phauer.com/2020/package-by-feature/
- https://www.youtube.com/watch?v=mbNzUkNjrnA (Steve Pember – Clean Arch in Spring)