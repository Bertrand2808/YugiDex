package org.yugiohproject.domain.card;

/**
 * Attributs possibles d'une carte Yu-Gi-Oh.
 * Chaque constante possède un label en français pour l'affichage.
 */
public enum Attribute {

    /** Attribut Ténèbre */
    DARK("ténèbre"),
    /** Attribut Lumière */
    LIGHT("lumière"),
    /** Attribut Feu */
    FIRE("feu"),
    /** Attribut Eau */
    WATER("eau"),
    /** Attribut Terre */
    EARTH("terre"),
    /** Attribut Vent */
    WIND("vent"),
    /** Attribut Divin */
    DIVINE("divin"),
    /** Carte Piège */
    TRAP("piège"),
    /** Carte Magie */
    SPELL("magie");

    private final String label;

    /**
     * Crée un attribut avec son label français.
     *
     * @param label la traduction française de l'attribut
     */
    Attribute(String label) {
        this.label = label;
    }

    /**
     * Retourne le label français de l'attribut.
     *
     * @return le label en français (ex: "ténèbre", "lumière")
     */
    public String getLabel() {
        return label;
    }
}