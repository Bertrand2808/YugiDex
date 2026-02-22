package org.yugiohproject.domain.card;

public class Card {

    // Attributs
    public String name;
    public Integer atk;
    public Integer def;
    public Types type;

    /**
     * Constructor
     * @param name
     * @param atk
     * @param def
     */
    public Card(String name, Integer atk, Integer def, Types type) {
        this.name = name;
        this.atk = atk;
        this.def = def;
        this.type = type;
    }

    public Card() {

    }

    // ── MÉTHODES ──────────────────────────
    public void afficher() {
        System.out.println(name + " — ATK: " + atk + " / DEF: " + def);
    }
}


