package org.yugiohproject.card.domain;

/**
 * Enumeration for Card type
 *
 * @author bertrandrenaudin
 */
public enum CardType {

    // Monstres
    NORMAL,
    EFFECT,
    RITUAL,
    FUSION,
    SYNCHRO,
    XYZ,
    LINK,

    // Magie & Piège
    SPELL,
    TRAP;

    /**
     * @return the french libelle from the real cards
     */
    public String getLabelFrançais() {
        return switch (this) {
            case NORMAL  -> "Monstre Normal";
            case EFFECT  -> "Monstre Effet";
            case RITUAL  -> "Monstre Rituel";
            case FUSION  -> "Monstre Fusion";
            case SYNCHRO -> "Monstre Synchro";
            case XYZ     -> "Monstre Xyz";
            case LINK    -> "Lien";
            case SPELL   -> "Carte Magie";
            case TRAP    -> "Carte Piège";
        };
    }

    public boolean isMonster() {
        return ordinal() <= LINK.ordinal();
    }

    public boolean isSpellOrTrap() {
        return !isMonster();
    }
}
