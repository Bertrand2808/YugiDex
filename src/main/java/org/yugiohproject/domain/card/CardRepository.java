package org.yugiohproject.domain.card;

import java.util.List;
import java.util.Optional;

/**
 * Contrat d'accès aux données des cartes Yu-Gi-Oh.
 * Toute implémentation (base de données, mock, API...) doit respecter ce contrat.
 */
public interface CardRepository {

    /**
     * Retourne toutes les cartes disponibles.
     *
     * @return liste de toutes les cartes, vide si aucune carte
     */
    List<Card> findAll();

    /**
     * Recherche une carte par son identifiant unique.
     *
     * @param id l'identifiant de la carte
     * @return un Optional contenant la carte si trouvée, Optional.empty() sinon
     */
    Optional<Card> findById(int id);

    /**
     * Recherche les cartes dont le nom contient la chaîne fournie.
     * La recherche est insensible à la casse.
     *
     * @param partialName la chaîne de caractères à rechercher dans le nom
     * @return liste des cartes correspondantes, vide si aucun résultat
     */
    List<Card> findByName(String partialName);

    /**
     * Retourne toutes les cartes d'un type donné.
     *
     * @param type le type de carte (MONSTRE, MAGIQUE, PIEGE)
     * @return liste des cartes du type demandé, vide si aucun résultat
     */
    List<Card> findByType(Types type);

    /**
     * Retourne toutes les cartes d'un attribut donné.
     *
     * @param attribute l'attribut recherché (DARK, LIGHT, FIRE...)
     * @return liste des cartes de cet attribut, vide si aucun résultat
     */
    List<Card> findByAttribute(Attribute attribute);
}