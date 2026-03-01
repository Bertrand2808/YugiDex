package org.yugiohproject.card.adapter.outbound.persistence;

import org.yugiohproject.card.domain.Card;
import org.yugiohproject.card.domain.CardAttribute;
import org.yugiohproject.card.domain.CardType;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * In memory Implementation of the Card Repository
 * Use for local tests without API calls.
 */
public class MockCardRepositoryImpl implements CardRepository{

    private final List<Card> cards = new ArrayList<>();

    /**
     * Public constructor that implement CardRepository with 7 cards
     */
    public MockCardRepositoryImpl() {
        cards.add(new Card(
                89631139,
                "Dark Magician",
                "The ultimate wizard in terms of attack and defense.",
                CardType.EFFECT,
                2500, 2100, 7,
                CardAttribute.DARK,
                "Spellcaster"
        ));

        cards.add(new Card(
                89631140,  // id fictif pour Blue-Eyes
                "Blue-Eyes White Dragon",
                "This legendary dragon is a powerful engine of destruction.",
                CardType.NORMAL,
                3000, 2500, 8,
                CardAttribute.LIGHT,
                "Dragon"
        ));

        cards.add(new Card(
                33396948,
                "Exodia the Forbidden One",
                "If you have all five pieces of Exodia...",
                CardType.EFFECT,
                1000, 1000, 3,
                CardAttribute.DARK,
                "Spellcaster"
        ));

        cards.add(new Card(
                55144522,
                "Pot of Greed",
                "Draw 2 cards.",
                CardType.SPELL,
                null, null, null,
                null,
                "Normal"
        ));

        cards.add(new Card(
                53582587,
                "Mirror Force",
                "When an opponent's monster declares an attack: Destroy all Attack Position monsters your opponent controls.",
                CardType.TRAP,
                null, null, null,
                null,
                "Normal"
        ));

        cards.add(new Card(
                83764718,
                "Monster Reborn",
                "Target 1 monster in either player's GY; Special Summon it.",
                CardType.SPELL,
                null, null, null,
                null,
                "Normal"
        ));

        // Bonus : une carte supplémentaire pour plus de variété
        cards.add(new Card(
                70903634,
                "Red-Eyes Black Dragon",
                "A ferocious dragon with a deadly attack.",
                CardType.NORMAL,
                2400, 2000, 7,
                CardAttribute.DARK,
                "Dragon"
        ));
    }

    @Override
    public List<Card> findAll(){
        return new ArrayList<>(cards);
    }

    @Override
    public Optional<Card> findById(int id){
        // On parcourt la liste des cartes et on récupère la première carte avec l'id correspondant à celui recherché
        return cards.stream().filter(card -> card.id() == id).findFirst();
    }

    @Override
    public List<Card> findByName(String partialName){
        String partialNameLower = partialName.toLowerCase().trim();
        return cards.stream().filter(
                card -> card.name().toLowerCase().contains(partialNameLower))
                .collect(Collectors.toList());
    }

    @Override
    public List<Card> findByType(CardType type) {
        return cards.stream()
                .filter(card -> card.type() == type)   // == car enum
                .collect(Collectors.toList());
    }

    @Override
    public List<Card> findByAttribute(CardAttribute attribute){
        return cards.stream().filter(card -> card.attribute().equals(attribute)).collect(Collectors.toList());
    }


}
