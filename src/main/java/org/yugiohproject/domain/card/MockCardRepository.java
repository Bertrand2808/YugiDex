package org.yugiohproject.domain.card;

import java.util.List;
import java.util.Optional;

/**
 * Implémentation en mémoire du {@link CardRepository}.
 * Utilisée pour les tests et le développement, sans base de données.
 * Contient un jeu de 6 cartes Yu-Gi-Oh prédéfinies.
 */
public class MockCardRepository implements CardRepository {

    private final List<Card> cards = List.of(
            new Card(4, "Dark Magician", 7, Attribute.DARK, 2500, 2100, Types.MONSTRE),
            new Card(5, "Blue-Eyes White Dragon", 8, Attribute.LIGHT, 3000, 2500, Types.MONSTRE),
            new Card(6, "Exodia", 3, Attribute.DARK, 1000, 1000, Types.MONSTRE),
            new Card(7, "Pot of Greed", 0, Attribute.SPELL, null, null, Types.MAGIQUE),
            new Card(8, "Mirror Force", 0, Attribute.TRAP, null, null, Types.PIEGE),
            new Card(9, "Summoned Skull", 6, Attribute.DARK, 2500, 1200, Types.MONSTRE)
    );

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Card> findAll() {
        return cards;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Card> findById(int id) {
        return cards.stream()
                .filter(c -> c.id() == id)
                .findFirst();
    }

    /**
     * {@inheritDoc}
     * La recherche est insensible à la casse.
     */
    @Override
    public List<Card> findByName(String partialName) {
        return cards.stream()
                .filter(c -> c.name().toLowerCase().contains(partialName.toLowerCase()))
                .toList();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Card> findByType(Types type) {
        return cards.stream()
                .filter(c -> c.type() == type)
                .toList();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Card> findByAttribute(Attribute attribute) {
        return cards.stream()
                .filter(c -> c.attribut() == attribute)
                .toList();
    }
}