package com.mrlan123.dynamicrpghud;

/**
 * Every HUD element the mod can fade out, move and scale.
 * The order is the order used in the editor and in the config file.
 */
public enum HudPart implements HudElement {
    ARMOR("armor"),
    HEALTH("health"),
    HUNGER("hunger"),
    AIR("air"),
    HOTBAR("hotbar"),
    XP_BAR("xp_bar"),
    XP_LEVEL("xp_level"),
    CROSSHAIR("crosshair"),
    JUMP_BAR("jump_bar"),
    VEHICLE_HEALTH("vehicle_health"),
    EFFECTS("effects");

    /** The air bubbles are drawn inside renderPlayerHealth here and are wrapped call by call. */
    public static final boolean AIR_SUPPORTED = true;

    public static final int COUNT = values().length;

    public final String id;

    HudPart(String id) {
        this.id = id;
    }

    @Override
    public String id() {
        return id;
    }

    @Override
    public net.minecraft.network.chat.Component displayName() {
        return net.minecraft.network.chat.Component.translatable(langKey());
    }

    @Override
    public boolean isExternal() {
        return false;
    }

    /** Layer 2: elements that are only drawn in some situations (water, riding, effects). */
    @Override
    public int layer() {
        switch (this) {
            case AIR:
            case JUMP_BAR:
            case VEHICLE_HEALTH:
            case EFFECTS:
                return 2;
            default:
                return 1;
        }
    }

    @Override
    public boolean isMovable() {
        return this != CROSSHAIR;
    }

    /** True for elements that are drawn without hiding until the player turns hiding on. */
    public boolean staysVisibleByDefault() {
        return this == CROSSHAIR;
    }

    public String langKey() {
        return "dynamicrpghud.element." + id;
    }

    public boolean isSupported() {
        return this != AIR || AIR_SUPPORTED;
    }

    public static HudPart byId(String id) {
        for (HudPart p : values()) {
            if (p.id.equals(id)) {
                return p;
            }
        }
        return null;
    }
}
