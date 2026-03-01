package org.yugiohproject.card.adapter.outbound.persistence;

import org.yugiohproject.card.domain.Card;
import org.yugiohproject.card.domain.CardAttribute;
import org.yugiohproject.card.domain.CardType;

import java.util.List;
import java.util.Optional;

/**
 * Outbound port to access to YuGiOh cards.
 * <p>
 * This contract defines the read operations available on the cards.
 * Concrete implementations (mock, JPA, YGOPRODeck API call, etc.)
 * must comply exactly with this contract.
 * </p>
 */
public interface CardRepository {
    /**
     * Return all cards available
     * @return a list of cards
     */
    List<Card> findAll();

    /**
     * Return a card by id
     * @param id the id of the card
     * @return the card or null
     */
    Optional<Card> findById(int id);

    /**
     * Return a list of cards by partial name
     * @param partialName the partial name of the card
     * @return a list of cards
     */
    List<Card> findByName(String partialName);

    /**
     * Return a list of cards by their types
     * @param type the type of the card concerned
     * @return a list of cards
     */
    List<Card> findByType(CardType type);

    /**
     * Return a list of cards by their attribute
     * @param attribute the attribute of the card
     * @return a list of cards
     */
    List<Card> findByAttribute(CardAttribute attribute);
}
