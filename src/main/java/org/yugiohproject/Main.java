package org.yugiohproject;

import org.yugiohproject.card.domain.Card;
import org.yugiohproject.card.domain.CardAttribute;
import org.yugiohproject.card.domain.CardType;

/**
 * Main class of the YugiDex project
 *
 * @author bertrandrenaudin
 */
public class Main {

    public static void main(String[] args) {
        System.out.println("=== YugiDex démarré ===\n");

        var darkMagician = new Card(
                89631139,
                "Dark Magician",
                "The ultimate wizard...",
                CardType.EFFECT,
                2500, 2100, 7,
                CardAttribute.DARK,
                "Spellcaster"
        );

        var potOfGreed = new Card(
                55144522,
                "Pot of Greed",
                "Draw 2 cards.",
                CardType.SPELL,
                null, null, null,
                null,
                "Normal"
        );

        var exodia = new Card(
                33396948, // vrai id d'Exodia Head
                "Exodia the Forbidden One",
                "...",
                CardType.EFFECT,
                1000, 1000, 3,
                CardAttribute.DARK,
                "Spellcaster"
        );

        System.out.println(darkMagician);
        System.out.println(potOfGreed);
        System.out.println(exodia.getName() + " — attribut : " + exodia.getAttribute());
    }
}
