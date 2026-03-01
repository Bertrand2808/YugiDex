package org.yugiohproject.card.domain;

import java.util.Objects;

/**
 * Card class representing a YuGiOh! card.
 * <p>
 * Inspired from YGOPRODeck API.
 * </p>
 *
 * @author bertrandrenaudin
 */
public record Card(
        int id,
        String name,
        String desc,
        CardType type,
        Integer atk,
        Integer def,
        Integer level,
        CardAttribute attribute,
        String race) {

    /**
     * Public constructor for Card entity
     *
     * @param name      the name of the card
     * @param desc      the desc of the card
     * @param atk       the atk of the card
     * @param def       the def of the card
     * @param level     the level of the card
     * @param attribute the attribute of the card
     * @param race      the race of the card
     * @param type      the type of the card
     */
    public Card {
        name = Objects.requireNonNull(name, "Name is required");
        desc = Objects.requireNonNull(desc, "Description is required");
        type = Objects.requireNonNull(type, "CardType is required");

        if (type.isMonster()) {
            if (attribute == null) {
                throw new IllegalArgumentException(
                        "A Monster must have an attribute (" + name + ")");
            }
            if (level == null || level < 1 || level > 12) {
                throw new IllegalArgumentException(
                        "Invalid level for a Monster : " + level + " (" + name + ")");
            }
        } else {
            // Magie / Piège
            if (attribute != null) {
                throw new IllegalArgumentException(
                        "Magic/Trap cards does not have attribute (" + name + ")");
            }
            if (atk != null || def != null || level != null) {
                throw new IllegalArgumentException(
                        "Magic/Trap cards does not have atk, def nor level (" + name + ")");
            }
        }

        if (atk != null && atk < 0) {
            throw new IllegalArgumentException("ATK can not be negative (" + name + ")");
        }
        if (def != null && def < 0) {
            throw new IllegalArgumentException("DEF can not be negative (" + name + ")");
        }

        // Optionnel : normalisation (exemple : trim sur name)
        name = name.trim();
    }
}


