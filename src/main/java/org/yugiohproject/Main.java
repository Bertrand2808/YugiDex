package org.yugiohproject;

import org.yugiohproject.card.adapter.outbound.persistence.CardRepository;
import org.yugiohproject.card.adapter.outbound.persistence.MockCardRepositoryImpl;
import org.yugiohproject.card.domain.Card;
import java.util.List;
import java.util.Optional;

/**
 * Main class of the YugiDex project
 *
 * @author bertrandrenaudin
 */
public class Main {

    public static void main(String[] args) {
        System.out.println("=== YugiDex démarré ===\n");
        // Instantiate the repository
        CardRepository repository = new MockCardRepositoryImpl();
        List<Card> cardList = repository.findAll();
        System.out.println(cardList.toArray().length);
        Optional<Card> darkMagician = repository.findById(89631139);
        System.out.println("GREP darkMagician by id : " + darkMagician.toString());
        List<Card> dragonCardList = repository.findByName("Dragon");
        System.out.println("Grep dragon cards by partial name :");
        for (Card card : dragonCardList) {
            System.out.println(card);
        }
    }
}
