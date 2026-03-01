package org.yugiohproject;

import org.yugiohproject.domain.card.Card;

/**
 * Main class of the YugiDex project
 *
 * @author bertrandrenaudin
 */
public class Main {

    static void main() {
        System.out.println("=== YugiDex démarré ===");
        // nouvelles cartes
        Card darkMagicien = new Card(
                89631139, "Dark Magician", "The ultimate wizard in terms of attack and defense.", 2500, 2100, 7,
                "DARK", "SpellCaster", "Effect Monster");
        Card potOfGreed = new Card(
                55144522, "Pot of Greed", "Draw 2 cards", null, null, null,
                null, null, "Normal Spell");

        // Afficher les cartes
        System.out.println(darkMagicien);
        System.out.println(potOfGreed.getName());
        System.out.println("ATK de " + darkMagicien.getName() + " : " + darkMagicien.getAtk());
        System.out.println("ATK de " + potOfGreed.getName() + " : " + potOfGreed.getAtk());
    }
}
