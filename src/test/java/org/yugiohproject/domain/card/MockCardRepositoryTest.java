package org.yugiohproject.domain.card;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitaires pour {@link MockCardRepository}.
 * Vérifie le bon fonctionnement de toutes les méthodes de recherche.
 */
class MockCardRepositoryTest {

    MockCardRepository repository;

    /**
     * Initialise un nouveau repository avant chaque test.
     */
    @BeforeEach
    void setUp() {
        repository = new MockCardRepository();
    }

    /**
     * Vérifie que findAll retourne bien les 6 cartes du mock.
     */
    @Test
    void findAll_doitRetourner6Cartes() {
        assertEquals(6, repository.findAll().size());
    }

    /**
     * Vérifie que findById retourne la bonne carte sur un ID existant.
     */
    @Test
    void findById_idExistant_doitRetournerLaCarte() {
        assertTrue(repository.findById(4).isPresent());
        assertEquals("Dark Magician", repository.findById(4).get().name());
    }

    /**
     * Vérifie que findById retourne Optional.empty() sur un ID inconnu.
     */
    @Test
    void findById_idInconnu_doitRetournerEmpty() {
        assertTrue(repository.findById(99).isEmpty());
    }

    /**
     * Vérifie que findByName retourne les cartes contenant "Dragon".
     */
    @Test
    void findByName_doitRetournerCarteAvecDragon() {
        var result = repository.findByName("Dragon");
        assertFalse(result.isEmpty());
        assertTrue(result.stream().allMatch(c -> c.name().toLowerCase().contains("dragon")));
    }

    /**
     * Vérifie que findByName est insensible à la casse.
     */
    @Test
    void findByName_isCaseInsensitive() {
        var result = repository.findByName("dragon");
        assertFalse(result.isEmpty());
        assertTrue(result.stream().allMatch(c -> c.name().toLowerCase().contains("dragon")));
    }

    /**
     * Vérifie que findByType retourne uniquement les monstres.
     */
    @Test
    void findByType_doitRetournerLesMonstres() {
        var result = repository.findByType(Types.MONSTRE);
        assertFalse(result.isEmpty());
        assertTrue(result.stream().allMatch(c -> c.type() == Types.MONSTRE));
    }

    /**
     * Vérifie que findByType retourne uniquement le type demandé.
     */
    @Test
    void findByType_returnOnlyRequestedType() {
        var result = repository.findByType(Types.MAGIQUE);
        assertFalse(result.isEmpty());
        assertTrue(result.stream().allMatch(c -> c.type() == Types.MAGIQUE));
    }

    /**
     * Vérifie que findByAttribute retourne uniquement les cartes DARK.
     */
    @Test
    void findByAttribute_doitRetournerCartesDark() {
        var result = repository.findByAttribute(Attribute.DARK);
        assertFalse(result.isEmpty());
        assertTrue(result.stream().allMatch(c -> c.attribut() == Attribute.DARK));
    }
}