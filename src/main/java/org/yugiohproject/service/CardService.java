package org.yugiohproject.service;

import org.yugiohproject.domain.card.Attribute;
import org.yugiohproject.domain.card.Card;
import org.yugiohproject.domain.card.CardRepository;
import org.yugiohproject.domain.card.Types;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service métier pour la gestion des cartes Yu-Gi-Oh.
 * Contient la logique applicative et délègue l'accès aux données au {@link CardRepository}.
 */
public class CardService {

    private final CardRepository repository;

    /**
     * Crée un CardService avec le repository fourni.
     *
     * @param repository l'implémentation du repository à utiliser
     */
    public CardService(CardRepository repository) {
        this.repository = repository;
    }

    /**
     * Recherche les cartes dont le nom contient la chaîne fournie.
     * La recherche est insensible à la casse.
     *
     * @param nomPartiel la chaîne à rechercher, doit faire au moins 2 caractères
     * @return liste des cartes correspondantes
     * @throws IllegalArgumentException si nomPartiel est null ou fait moins de 2 caractères
     */
    public List<Card> rechercherParNom(String nomPartiel) {
        if (nomPartiel == null || nomPartiel.length() < 2) {
            throw new IllegalArgumentException("La recherche doit faire au moins 2 caractères !");
        }
        return repository.findByName(nomPartiel);
    }

    /**
     * Retourne uniquement les cartes ayant des points d'attaque,
     * triées par ATK décroissante.
     *
     * @return liste des monstres triés par force décroissante
     */
    public List<Card> getMonstresParForce() {
        return repository.findAll().stream()
                .filter(c -> c.atk() != null)
                .sorted((a, b) -> b.atk() - a.atk())
                .toList();
    }

    /**
     * Retourne toutes les cartes d'un attribut donné, triées par nom alphabétique.
     *
     * @param attribut l'attribut à filtrer (DARK, LIGHT, FIRE...)
     * @return liste des cartes de cet attribut triées par nom
     */
    public List<Card> getCartesParAttribut(Attribute attribut) {
        return repository.findByAttribute(attribut).stream()
                .sorted((a, b) -> a.name().compareTo(b.name()))
                .toList();
    }

    /**
     * Calcule le nombre de cartes par type.
     *
     * @return une Map associant chaque type au nombre de cartes correspondant
     */
    public Map<Types, Long> getStatistiquesParType() {
        return repository.findAll().stream()
                .collect(Collectors.groupingBy(Card::type, Collectors.counting()));
    }

    /**
     * Retourne les 3 monstres ayant le plus de points d'attaque.
     *
     * @return liste des 3 cartes avec la plus haute ATK, triées par ATK décroissante
     */
    public List<Card> getTop3Monstres() {
        return repository.findAll().stream()
                .filter(c -> c.atk() != null)
                .sorted((a, b) -> b.atk() - a.atk())
                .limit(3)
                .toList();
    }
}