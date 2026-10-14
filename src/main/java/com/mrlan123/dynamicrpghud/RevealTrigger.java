package com.mrlan123.dynamicrpghud;

/**
 * Everything that can make the HUD appear in Automatic Mode. Each one can be switched on or off
 * on the Automatic Mode screen. {@link #defaultOn} is the value of a fresh install.
 */
public enum RevealTrigger {
    DAMAGE("damage", true),
    HEAL("heal", true),
    DROWNING("drowning", true),
    FIRE_DAMAGE("fire_damage", true),
    BURNING("burning", true),
    HUNGER("hunger", true),
    HEALTH("health", false),
    ARMOR("armor", true),
    EXPERIENCE("experience", false),
    ITEM_SWITCH("item_switch", true),
    HOTBAR_SCROLL("hotbar_scroll", true),
    HOTBAR_KEY("hotbar_key", true),
    ATTACK("attack", false),
    INTERACT("interact", false),
    JUMP("jump", false),
    SPRINT("sprint", false),
    SNEAK("sneak", false),
    PICKUP("pickup", false),
    USE_ITEM("use_item", false),
    INVENTORY("inventory", true),
    EFFECT("effect", true);

    public final String id;
    public final boolean defaultOn;

    RevealTrigger(String id, boolean defaultOn) {
        this.id = id;
        this.defaultOn = defaultOn;
    }

    public String nameKey() {
        return "dynamicrpghud.reveal." + id;
    }

    public static RevealTrigger byId(String id) {
        for (RevealTrigger t : values()) {
            if (t.id.equalsIgnoreCase(id) || t.name().equalsIgnoreCase(id)) {
                return t;
            }
        }
        return null;
    }
}
