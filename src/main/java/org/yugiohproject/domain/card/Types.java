package org.yugiohproject.domain.card;

/**
 * Types possibles d'une carte Yu-Gi-Oh.
 */
public enum Types {

    /** Carte Monstre — possède des points d'ATK et DEF */
    MONSTRE,

    /** Carte Magie — effet instantané, pas d'ATK ni DEF */
    MAGIQUE,

    /** Carte Piège — se déclenche en réaction, pas d'ATK ni DEF */
    PIEGE
}