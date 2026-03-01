package org.yugiohproject.domain.card;

import java.util.Objects;

/**
 * Card class representing a YuGiOh! card.
 * <p>
 * Inspired from YGOPRODeck API.
 * </p>
 *
 * @author bertrandrenaudin
 */
public class Card {

    private int id;
    private String name;
    private String desc;
    private Integer atk;
    private Integer def;
    private Integer level;
    private String attribute;
    private String race;
    private String type;


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
    public Card(
            int id,
            String name,
            String desc,
            Integer atk,
            Integer def,
            Integer level,
            String attribute,
            String race,
            String type) {
        this.id = id;
        this.name = Objects.requireNonNull(name, "name is required");
        this.desc = Objects.requireNonNull(desc, "desc is required");
        this.atk = atk;
        this.def = def;
        this.level = level;
        this.attribute = attribute;
        this.race = race;
        this.type = Objects.requireNonNull(type, "type is required");

        // Validation métier légère
        if (atk != null && atk < 0) {
            throw new IllegalArgumentException("ATK cannot be negative: " + atk);
        }
        if (def != null && def < 0) {
            throw new IllegalArgumentException("DEF cannot be negative: " + def);
        }
    }

    // ── Getters seulement (pas de setters) ────────────────────────

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public String getDesc() {
        return desc;
    }

    public Integer getAtk() {
        return atk;
    }

    public Integer getDef() {
        return def;
    }

    public Integer getLevel() {
        return level;
    }

    public String getAttribute() {
        return attribute;
    }

    public String getRace() {
        return race;
    }

    /**
     * Format lisible et agréable pour un joueur humain.
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(name);

        if (attribute != null) {
            sb.append(" [").append(attribute).append("]");
        }
        if (level != null) {
            sb.append(" ★").append(level);
        }
        if (atk != null) {
            sb.append(" ATK:").append(atk);
        }
        if (def != null) {
            sb.append(" DEF:").append(def);
        }
        if (race != null) {
            sb.append(" (").append(race).append(")");
        }

        return sb.toString();
    }
}


