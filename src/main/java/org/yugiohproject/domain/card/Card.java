package org.yugiohproject.domain.card;

public record Card (

    // Attributs
     int id,
     String name,
     Integer rank,
     Attribute attribut,
     Integer atk,
     Integer def,
     Types type
)

/**
 * Représente une carte Yu-Gi-Oh.
 * Un record immuable contenant toutes les informations d'une carte.
 *
 * @param ID l'id de la carte
 * @param name le nom de la carte, ne peut pas être null ou vide
 * @param rank le rang de la carte (nombre d'étoiles)
 * @param attribut l'attribut de la carte (DARK, LIGHT, FIRE...)
 * @param atk les points d'attaque de la carte
 * @param def les points de défense de la carte
 * @param type le type de la carte
 * @throws IllegalArgumentException si le nom est null ou vide
 */

    {

    public Card {
        if (name == null || name.isBlank()) {
            try {
                throw new IllegalAccessException("La carte à besoin d'un nom nullos va");
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
    }



    // ── MÉTHODES ──────────────────────────
    public void afficher() {
        System.out.println(name + " — ATK: " + atk + " / DEF: " + def);
    }


}
