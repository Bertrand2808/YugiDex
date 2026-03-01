package org.yugiohproject.card.domain;

/**
 * Enumeration for Card Attribute (monster card only)
 * <p>
 *  Related to the 7 attributes from TCG/OCG.
 * </p>
 * @author bertrandrenaudin
 */
public enum CardAttribute {
    DARK("TÉNÈBRES"),
    LIGHT("LUMIÈRES"),
    FIRE("FEU"),
    WATER("EAU"),
    EARTH("TERRE"),
    WIND("VENT"),
    DIVINE("DIVIN");

    private final String labelFr;

    CardAttribute(String labelFr) {
        this.labelFr = labelFr;
    }

    /**
     * @return the french official name from the cards
     */
    public String labelFr() {
        return labelFr;
    }

    /**
      * @return directly the french label on .toString()
     */
    @Override
    public String toString() {
        return labelFr;
    }
}
