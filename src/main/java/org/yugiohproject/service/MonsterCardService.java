package org.yugiohproject.service;

import org.yugiohproject.domain.card.Card;
import org.yugiohproject.domain.card.Types;

public class MonsterCardService {

    public static void attaquer(Card monstreAttaquant, Card monstreDefenseur) {
        if (!(monstreAttaquant.type() == Types.MONSTRE) || !(monstreDefenseur.type() == Types.MONSTRE)) {
            IO.println("C'est pas un monstre debilos");
            return;
        }
        IO.println("ATTAQUE de : " + monstreAttaquant.name() + " sur " + monstreDefenseur.name());
    }

    public static void passerEnDefense() {}
}
