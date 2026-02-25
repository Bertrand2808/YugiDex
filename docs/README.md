# YugiDex

> Ci dessous se trouverons une liste des différentes tâches à effectuer pour développer le projet YugiDex.

## TD-01 — Card Catalog : les fondamentaux Java

- [ ] : Créer `src/main/java/org/yugiohproject/Main.java`
- [ ] : Créer `src/main/java/org/yugiohproject/card/Card.java`
- [ ] : Créer les enums `CardType`, `CardAttribute`
- [ ] : Transformer la classe `Card` en `Record`
- [ ] : Créer `src/main/java/org/yugiohproject/card/CardRepository.java`
- [ ] : Créer le tests unitaire de `CardRepository` dans `src/test/java/org/yugiohproject/card/MockCardRepositoryTest.java`
- [ ] : Créer `src/main/java/org/yugiohproject/card/CardService.java`
- [ ] : Créer le tests unitaire de `CardService` dans `src/test/java/org/yugiohproject/card/CardServiceTest.java`


## TD-02 — Spring Boot : du code Java à une vraie API REST

- [ ] : Modifier le `pom.xml` pour ajouter les dépendances Spring Boot nécessaires
- [ ] : Modifier le `src/main/java/org/yugiohproject/Main.java` pour ajouter `@SpringBootApplication`
- [ ] : Annoter `MockCardRepository` avec `@Repository`
- [ ] : Annoter `CardService` avec `@Service`
- [ ] : Créer `src/main/java/org/yugiohproject/card/CardController.java`
- [ ] : Ajouter le endpoint par id dans `CardController`
- [ ] : Créer le fichier `application.properties` dans `src/main/resources/`
- [ ] : Ajouter les propriétés de configuration dans `application.properties`
