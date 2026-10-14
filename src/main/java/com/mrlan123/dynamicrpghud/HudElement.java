package com.mrlan123.dynamicrpghud;

import net.minecraft.network.chat.Component;

/** Anything the mod can move, scale and hide: a vanilla element ({@link HudPart}) or another mod's HUD. */
public interface HudElement {

    /** Stable key used in the config file ("health", "ext:somemod"...). */
    String id();

    Component displayName();

    /** True for HUD drawn by another mod; those never hide unless the player asks for it. */
    boolean isExternal();

    /** Editor layer: 1 = the always-drawn HUD (and other mods' HUD), 2 = HUD that shows up only sometimes. */
    default int layer() {
        return 1;
    }

    /** False for elements that may be scaled and hidden but not moved (the crosshair). */
    default boolean isMovable() {
        return true;
    }
}
