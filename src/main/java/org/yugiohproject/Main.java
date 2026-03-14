package org.yugiohproject;

import org.yugiohproject.card.adapter.outbound.persistence.CardRepository;
import org.yugiohproject.card.adapter.outbound.persistence.MockCardRepositoryImpl;
import org.yugiohproject.card.application.CardService;
import org.yugiohproject.card.domain.Card;
import org.yugiohproject.card.domain.CardType;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Main class of the YugiDex project
 *
 * @author bertrandrenaudin
 */
public class Main {

    public static void main(String[] args) {
        CardService service = new CardService(new MockCardRepositoryImpl());

        System.out.println("=== Recherche \"Dragon\" ===");
        List<Card> dragons = service.findByName("Dragon");
        System.out.println(dragons.size() + " résultats trouvés");
        dragons.forEach(c ->
                System.out.println(c.name() + " [" + c.attribute() + "] ATK:" + c.atk())
        );

        System.out.println("\n=== Top 3 ATK ===");
        service.getTop3Monsters().forEach(c ->
                System.out.printf("  %s — %d%n", c.name(), c.atk())
        );

        System.out.println("\n=== Statistiques ===");
        Map<CardType, Long> stats = service.getStatisticsByTypes();
        stats.forEach((type, count) ->
                System.out.printf("%s : %d cartes%n", type, count)
        );
    }
}
