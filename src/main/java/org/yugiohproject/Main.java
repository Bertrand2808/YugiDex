package org.yugiohproject;

import org.yugiohproject.domain.card.*;
import org.yugiohproject.service.CardService;
import org.yugiohproject.service.MonsterCardService;



//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {

    // TODO : 1. Regarder l'API (les champs des cartes que retourne l'API https://ygoprodeck.com/api-guide/)
    // TODO : 2. Créer une classe domain.Card avec les attributs que l'ont veut récupérer de l'API (Name, Type, Description, Atk, Def etc )
    // TODO : 3. Créer une classe api.CardClient qui fait un appel externe à l'API
    // TODO : 4. Créer une classe controller.card.CardController qui fait un appel GET pour récupérer une seule (pour le moment) carte
    // et créé une nouvelle Card et l'affiche en console

    static MonsterCardService monsterCardService;

    static void main() {
        // variables
        Integer atk = 2500;
        Integer def = 1000;

        Card newCard = new Card(1,"Magicien Sombre", 7, Attribute.DARK, 2500, 1500, Types.MONSTRE);
        Card newCard2 = new Card(2,"Pot of Greed",0 ,Attribute.SPELL, null, null, Types.MONSTRE);
        Card newCard3 = new Card (3,"Exodia The Forbidden One",0,Attribute.DARK,null,null,Types.MONSTRE);

        IO.println("Carte : " + newCard.name() + " - Attack : " + newCard.atk());
        newCard.afficher();
        newCard2.afficher();

        MonsterCardService.attaquer(newCard,newCard2);
        IO.println(newCard.name() +" "+ newCard.attribut()+ " " + "ATK : " + newCard.atk() + " DEF : " + newCard.def() + " ★ " + newCard.rank());
        IO.println(newCard2.name());
        IO.println("ATK de " + newCard.name() + " : " +newCard.atk());
        IO.println("ATK de " + newCard2.name() + " : " +newCard2.atk());
        IO.println(newCard3.name() + " - attribut : " + newCard3.attribut().getLabel());

        CardRepository repository = new MockCardRepository();

        System.out.println("\nEn rang deux par deux");
        repository.findAll().forEach(c -> System.out.println(c.name()));

        System.out.println("\nLe numéro 4 est appelé à l'accueil svp");
        repository.findById(4).ifPresentOrElse(
                c -> System.out.println("Trouvé : " + c.name()),
                () -> System.out.println("Ah batar t'es pas la")
        );

        System.out.println("\nLe numéro 99 est appelé à l'accueil svp");
        repository.findById(99).ifPresentOrElse(
                c -> System.out.println("Trouvé : " + c.name()),
                () -> System.out.println("Ah batar t'es pas la")
        );

        System.out.println("\nVoilà les tous les dragons");
        repository.findByName("Dragon").forEach(c -> System.out.println(c.name()));

        System.out.println("\nVoilà les tous les monstres");
        repository.findByType(Types.MONSTRE).forEach(c -> System.out.println(c.name()));

        System.out.println("\nTous les méchants");
        repository.findByAttribute(Attribute.DARK).forEach(c -> System.out.println(c.name()));

        CardService service = new CardService(repository);


// 1 - Recherche par nom
        System.out.println("\n=== Recherche Dragon ===");
        service.rechercherParNom("Dragon").forEach(c -> System.out.println(c.name()));

// 2 - Monstres par force
        System.out.println("\n=== Monstres par force ===");
        service.getMonstresParForce().forEach(c -> System.out.println(c.name() + " — " + c.atk()));

// 3 - Cartes par attribut
        System.out.println("\n=== Cartes DARK ===");
        service.getCartesParAttribut(Attribute.DARK).forEach(c -> System.out.println(c.name()));

// 4 - Statistiques par type
        System.out.println("\n=== Statistiques ===");
        service.getStatistiquesParType().forEach((type, count) -> System.out.println(type + " : " + count + " cartes"));

// 5 - Top 3
        System.out.println("\n=== Top 3 ATK ===");
        service.getTop3Monstres().forEach(c -> System.out.println(c.name() + " — " + c.atk()));
    }



    /**
     * Calculate Power of a monster
     * @param attack the atck of the monster
     * @param defense the defense of the monster
     * @return  the poweeeer
     */
    public static Integer calculPuissance(Integer attack, Integer defense) {
        return attack + defense;
    }

    // surcharge
    public static Integer calculPuissance(Integer attack, Integer defense, int boost) {
        return (attack + defense) * boost;
    }



}
