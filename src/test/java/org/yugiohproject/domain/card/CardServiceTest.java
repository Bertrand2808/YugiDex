package org.yugiohproject.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.yugiohproject.domain.card.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitaires pour {@link CardService}.
 * Utilise {@link MockCardRepository} comme source de données — aucune base de données requise.
 */
class CardServiceTest {

    private CardService service;

    /**
     * Initialise le service avec un MockCardRepository avant chaque test.
     */
    @BeforeEach
    void setUp() {
        CardRepository repo = new MockCardRepository();
        service = new CardService(repo);
    }

    /**
     * Vérifie que la recherche par nom retourne les cartes contenant "Dragon".
     */
    @Test
    void rechercherParNom_doitRetournerCarteAvecDragon() {
        List<Card> result = service.rechercherParNom("Dragon");
        assertFalse(result.isEmpty());
        assertTrue(result.stream().allMatch(c -> c.name().toLowerCase().contains("dragon")));
    }

    /**
     * Vérifie qu'une recherche de moins de 2 caractères lève une exception.
     */
    @Test
    void rechercherParNom_moinsDe2Caracteres_doitLeverException() {
        assertThrows(IllegalArgumentException.class, () -> service.rechercherParNom("D"));
    }

    /**
     * Vérifie que getMonstresParForce retourne uniquement les cartes avec ATK non nulle.
     */
    @Test
    void getMonstresParForce_doitRetournerUniquementCartesAvecAtk() {
        List<Card> result = service.getMonstresParForce();
        assertFalse(result.isEmpty());
        assertTrue(result.stream().allMatch(c -> c.atk() != null));
    }

    /**
     * Vérifie que getMonstresParForce retourne les cartes triées par ATK décroissante.
     */
    @Test
    void getMonstresParForce_doitEtreTireParAtkDecroissant() {
        List<Card> result = service.getMonstresParForce();
        for (int i = 0; i < result.size() - 1; i++) {
            assertTrue(result.get(i).atk() >= result.get(i + 1).atk(),
                    "Les monstres doivent être triés par ATK décroissante");
        }
    }

    /**
     * Vérifie que getCartesParAttribut retourne uniquement les cartes DARK.
     */
    @Test
    void getCartesParAttribut_doitRetournerCartesDark() {
        List<Card> result = service.getCartesParAttribut(Attribute.DARK);
        assertFalse(result.isEmpty());
        assertTrue(result.stream().allMatch(c -> c.attribut() == Attribute.DARK));
    }

    /**
     * Vérifie que getCartesParAttribut retourne les cartes triées par nom alphabétique.
     */
    @Test
    void getCartesParAttribut_doitEtreTireParNom() {
        List<Card> result = service.getCartesParAttribut(Attribute.DARK);
        for (int i = 0; i < result.size() - 1; i++) {
            assertTrue(result.get(i).name().compareTo(result.get(i + 1).name()) <= 0,
                    "Les cartes doivent être triées par nom");
        }
    }

    /**
     * Vérifie que getStatistiquesParType contient tous les types de cartes.
     */
    @Test
    void getStatistiquesParType_doitContenisTousTesTypes() {
        var stats = service.getStatistiquesParType();
        assertFalse(stats.isEmpty());
        assertTrue(stats.containsKey(Types.MONSTRE));
        assertTrue(stats.containsKey(Types.MAGIQUE));
        assertTrue(stats.containsKey(Types.PIEGE));
    }

    /**
     * Vérifie que getTop3Monstres retourne exactement 3 cartes.
     */
    @Test
    void getTop3Monstres_doitRetourner3Monstres() {
        List<Card> result = service.getTop3Monstres();
        assertEquals(3, result.size());
    }

    /**
     * Vérifie que getTop3Monstres retourne les cartes triées par ATK décroissante.
     */
    @Test
    void getTop3Monstres_doitEtreTireParAtkDecroissant() {
        List<Card> result = service.getTop3Monstres();
        for (int i = 0; i < result.size() - 1; i++) {
            assertTrue(result.get(i).atk() >= result.get(i + 1).atk(),
                    "Le top 3 doit être trié par ATK décroissante");
        }
    }
}