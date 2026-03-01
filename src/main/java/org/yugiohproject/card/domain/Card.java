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
public class Card {

    private final int id;
    private final String name;
    private final String desc;
    private final CardType type;
    private final Integer atk;
    private final Integer def;
    private final Integer level;
    private final CardAttribute attribute;
    private final String race;


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
            CardType type,
            Integer atk,
            Integer def,
            Integer level,
            CardAttribute attribute,
            String race) {
        this.id = id;
        this.name = Objects.requireNonNull(name, "name is required");
        this.desc = Objects.requireNonNull(desc, "desc is required");
        this.type = Objects.requireNonNull(type, "type is required");
        this.atk = atk;
        this.def = def;
        this.level = level;
        this.attribute = attribute;
        this.race = race;

        // Validation métier de base
        if (type.isMonster()) {
            if (attribute == null) {
                throw new IllegalArgumentException("Un monstre doit avoir un attribut");
            }
            if (level == null || level < 1 || level > 12) {
                throw new IllegalArgumentException("Niveau invalide pour un monstre : " + level);
            }
        } else {
            // Magie / Piège
            if (attribute != null) {
                throw new IllegalArgumentException("Les cartes Magie/Piège n'ont pas d'attribut");
            }
            if (atk != null || def != null || level != null) {
                throw new IllegalArgumentException("Les cartes Magie/Piège n'ont ni ATK, ni DEF, ni niveau");
            }
        }

        if (atk != null && atk < 0) {
            throw new IllegalArgumentException("ATK ne peut pas être négative");
        }
        if (def != null && def < 0) {
            throw new IllegalArgumentException("DEF ne peut pas être négative");
        }
    }

    // ── Getters seulement (pas de setters) ────────────────────────

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public CardType getType() {
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

    public CardAttribute getAttribute() {
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
        var sb = new StringBuilder(name);

        if (attribute != null) {
            sb.append(" [").append(attribute).append("]");   // ← affiche TÉNÈBRES grâce au toString()
        }
        if (level != null) {
            sb.append(" ★").append(level);
        }
        if (atk != null) {
            sb.append(" ATK/").append(atk);
        }
        if (def != null) {
            sb.append(" DEF/").append(def);
        }
        if (race != null) {
            sb.append(" (").append(race).append(")");
        }
        if (type != null) {
            sb.append(" — ").append(type.getLabelFrançais());
        }

        return sb.toString();
    }
}


