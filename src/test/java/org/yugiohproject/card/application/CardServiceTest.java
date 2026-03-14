package org.yugiohproject.card.application;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.yugiohproject.card.adapter.outbound.persistence.MockCardRepositoryImpl;
import org.yugiohproject.card.domain.Card;
import org.yugiohproject.card.domain.CardAttribute;
import org.yugiohproject.card.domain.CardType;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class CardServiceTest {

    private CardService service;

    @BeforeEach
    void setUp() {
        service = new CardService(new MockCardRepositoryImpl());
    }

    @Test
    void findByName_rejetteRechercheTropCourte() {
        assertTrue(service.findByName(null).isEmpty());
        assertTrue(service.findByName("").isEmpty());
        assertTrue(service.findByName(" ").isEmpty());
        assertTrue(service.findByName("D").isEmpty()); // 1 caractère
        assertFalse(service.findByName("Dr").isEmpty()); // 2 caractères → OK
    }

    @Test
    void findByName_trouveLesCartesDragon() {
        List<Card> result = service.findByName("dragon");
        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(c -> c.name().contains("Blue-Eyes")));
        assertTrue(result.stream().anyMatch(c -> c.name().contains("Red-Eyes")));
    }

    @Test
    void getMonstresParForce_returnsOnlyMonstersWithAtk() {
        List<Card> monstres = service.getMonsterByStrength();

        assertFalse(monstres.isEmpty());
        assertTrue(monstres.stream().allMatch(c -> c.atk() != null),
                "Tous les éléments doivent avoir une ATK non nulle");
        assertTrue(monstres.stream().noneMatch(c -> c.type() == CardType.SPELL || c.type() == CardType.TRAP),
                "Aucun sort ni piège ne doit apparaître");
    }

    @Test
    void getMonstresParForce_isSortedByAtkDescending() {
        List<Card> monstres = service.getMonsterByStrength();

        for (int i = 0; i < monstres.size() - 1; i++) {
            assertTrue(monstres.get(i).atk() >= monstres.get(i + 1).atk(),
                    "Les monstres doivent être triés par ATK décroissante");
        }
    }

    @Test
    void getCartesParAttribut_trieParNom() {
        List<Card> darkCards = service.getCardsByAttributes(CardAttribute.DARK);

        assertEquals(3, darkCards.size());

        // Vérifie que c'est trié par nom
        String prevName = null;
        for (Card card : darkCards) {
            if (prevName != null) {
                assertTrue(card.name().compareTo(prevName) >= 0,
                        "Les cartes doivent être triées par nom");
            }
            prevName = card.name();
        }
    }

    @Test
    void getCartesParAttribut_retourneVideSiAttributNull() {
        assertTrue(service.getCardsByAttributes(null).isEmpty());
    }

    @Test
    void getStatistiquesParType_compteCorrectement() {
        Map<CardType, Long> stats = service.getStatisticsByTypes();

        assertEquals(2L, stats.get(CardType.SPELL));
        assertEquals(1L, stats.get(CardType.TRAP));
        assertEquals(2L, stats.get(CardType.EFFECT));
        assertEquals(2L, stats.get(CardType.NORMAL)); // Blue-Eyes + Red-Eyes
    }

    @Test
    void getTop3Monstres_retourneLesTroisPlusForts() {
        List<Card> top3 = service.getTop3Monsters();

        assertEquals(3, top3.size());
        assertEquals("Blue-Eyes White Dragon", top3.get(0).name());   // 3000
        assertEquals("Dark Magician", top3.get(1).name());           // 2500
        assertEquals("Red-Eyes Black Dragon", top3.get(2).name());   // 2400
    }

}