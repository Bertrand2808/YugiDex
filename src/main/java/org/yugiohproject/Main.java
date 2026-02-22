package org.yugiohproject;

import org.yugiohproject.domain.card.Card;
import org.yugiohproject.domain.card.Types;
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

        Card newCard = new Card("Magicien Sombre", 2500, 1500, Types.MAGIQUE);
        Card newCard2 = new Card("Magicien pas sombre", 2500, 1500, Types.MONSTRE);

        IO.println("Carte : " + newCard.name + " - Attack : " + newCard.atk);
        newCard.afficher();

        MonsterCardService.attaquer(newCard,newCard2);

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
