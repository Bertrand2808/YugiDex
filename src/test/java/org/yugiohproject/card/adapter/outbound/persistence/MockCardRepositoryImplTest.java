package org.yugiohproject.card.adapter.outbound.persistence;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.yugiohproject.card.domain.Card;
import org.yugiohproject.card.domain.CardType;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class MockCardRepositoryImplTest {

    private CardRepository repository;

    // BeforeEach + performant
    @BeforeEach
    void setUp() {
        repository = new MockCardRepositoryImpl();
    }

    @Test
    void findAll_returnsAllCards() {
        List<Card> allCards = repository.findAll();

        assertNotNull(allCards, "La liste retournée ne doit pas être null");
        assertFalse(allCards.isEmpty(), "La liste ne doit pas être vide");
        assertEquals(7, allCards.size(), "Le mock doit contenir exactement 7 cartes");
    }

    @Test
    void findById_returnsCard_whenIdExists() {
        // Dark Magician = 89631139
        Optional<Card> result = repository.findById(89631139);

        assertTrue(result.isPresent(), "La carte avec l'ID 89631139 devrait exister");
        Card card = result.get();
        assertEquals("Dark Magician", card.name());
        assertEquals(2500, card.atk());
        assertEquals(7, card.level());
    }

    @Test
    void findById_returnsEmpty_whenIdUnknown() {
        Optional<Card> result = repository.findById(123456789);

        assertFalse(result.isPresent(), "Un ID inexistant ne doit pas retourner de carte");
    }

    @Test
    void findByName_returnsMatchingCards() {
        List<Card> result = repository.findByName("Dragon");

        assertEquals(2, result.size(), "Devrait trouver exactement 2 cartes contenant 'Dragon'");

        // Vérification des noms (peu importe l'ordre)
        assertTrue(
                result.stream().anyMatch(c -> c.name().contains("Blue-Eyes White Dragon")),
                "Blue-Eyes White Dragon devrait être présent"
        );
        assertTrue(
                result.stream().anyMatch(c -> c.name().contains("Red-Eyes Black Dragon")),
                "Red-Eyes Black Dragon devrait être présent"
        );
    }

    @Test
    void findByName_isCaseInsensitive() {
        List<Card> resultUpper = repository.findByName("Dragon");
        List<Card> resultLower = repository.findByName("dragon");
        List<Card> resultMixed = repository.findByName("dRaGoN");

        assertEquals(resultUpper.size(), resultLower.size(),
                "La recherche doit être insensible à la casse (minuscules)");
        assertEquals(resultUpper.size(), resultMixed.size(),
                "La recherche doit être insensible à la casse (mélange)");

        // Optionnel : on peut aussi vérifier que le contenu est identique
        assertTrue(resultUpper.containsAll(resultLower) && resultLower.containsAll(resultUpper));
    }

    @Test
    void findByType_returnOnlyRequestedType() {
        List<Card> spells = repository.findByType(CardType.SPELL);

        // Vérification du nombre (selon le mock actuel)
        assertEquals(2, spells.size(), "Il devrait y avoir 2 sorts dans le mock");

        // Vérification que TOUTES les cartes retournées sont bien de type SPELL
        assertTrue(
                spells.stream().allMatch(card -> card.type() == CardType.SPELL),
                "Toutes les cartes retournées doivent être de type SPELL"
        );

        // Vérification optionnelle de quelques noms
        assertTrue(
                spells.stream().anyMatch(c -> c.name().equals("Pot of Greed")),
                "Pot of Greed devrait être présent"
        );
        assertTrue(
                spells.stream().anyMatch(c -> c.name().equals("Monster Reborn")),
                "Monster Reborn devrait être présent"
        );
    }
}