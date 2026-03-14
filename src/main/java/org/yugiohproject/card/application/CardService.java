package org.yugiohproject.card.application;

import org.yugiohproject.card.adapter.outbound.persistence.CardRepository;
import org.yugiohproject.card.domain.Card;
import org.yugiohproject.card.domain.CardAttribute;
import org.yugiohproject.card.domain.CardType;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Card Service for Card Context
 */
public class CardService {

    private final CardRepository cardRepository;

    /**
     * Public constructor
     * @param cardRepository the card repository
     */
    public CardService(CardRepository cardRepository) {
        this.cardRepository = cardRepository;
    }

    /**
     * Search cards by name that contains the given texte.
     * Must be +2 char search
     *
     * @param partialName the partial name
     */
    public List<Card> findByName(String partialName) {
        if (partialName == null || partialName.trim().length() < 2) {
            return Collections.emptyList();
        }
        return cardRepository.findByName(partialName.trim());
    }

    /**
     * Get Monsters only (cards with non null ATK) sorted by desc strength.
     */
    public List<Card> getMonsterByStrength() {
        return cardRepository.findAll().stream()
                .filter(card -> card.atk() != null)
                .sorted(Comparator.comparingInt(Card::atk).reversed())
                .collect(Collectors.toList());
    }

    /**
     * Get all cards that had the given attribute, sorting by name.
     *
     * @param attribut the searched attribute
     */
    public List<Card> getCardsByAttributes(CardAttribute attribut) {
        if (attribut == null) {
            return Collections.emptyList();
        }
        return cardRepository.findByAttribute(attribut).stream()
                .sorted(Comparator.comparing(Card::name))
                .collect(Collectors.toList());
    }

    /**
     * Return a map with card numbers by type.
     */
    public Map<CardType, Long> getStatisticsByTypes() {
        return cardRepository.findAll().stream()
                .collect(Collectors.groupingBy(
                        Card::type,
                        Collectors.counting()
                ));
    }

    /**
     * Return top 3 monsters with highest ATK.
     */
    public List<Card> getTop3Monsters() {
        return cardRepository.findAll().stream()
                .filter(card -> card.atk() != null)
                .sorted(Comparator.comparingInt(Card::atk).reversed())
                .limit(3)
                .collect(Collectors.toList());
    }
}
