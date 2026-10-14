package com.mrlan123.dynamicrpghud;

import net.minecraft.network.chat.Component;

/**
 * The HUD of one supported mod (see {@link SupportedMod}). Its box is measured from what it
 * draws; for mods that draw without the usual draw calls a fixed fallback box is used so it can
 * still be dragged.
 */
public final class ExternalElement implements HudElement {

    private final String id;
    private final String modName;
    private final int number;
    private SupportedMod.Fallback fallback = SupportedMod.Fallback.NONE;

    private final int[] rect = new int[4];
    private boolean hasRect;
    private boolean drawn;
    private int stampW = -1;
    private int stampH = -1;

    ExternalElement(String id, String modName, int number) {
        this.id = id;
        this.modName = modName;
        this.number = number;
    }

    ExternalElement withFallback(SupportedMod.Fallback fallback) {
        this.fallback = fallback;
        return this;
    }

    @Override
    public String id() {
        return id;
    }

    @Override
    public Component displayName() {
        if (number <= 1) {
            return Component.translatable("dynamicrpghud.external.name", modName);
        }
        return Component.translatable("dynamicrpghud.external.name.n", modName, number);
    }

    @Override
    public boolean isExternal() {
        return true;
    }

    /** The mod's render hook ran at least once, so the element is worth listing. */
    void markDrawn() {
        drawn = true;
    }

    boolean hasDrawn() {
        return drawn;
    }

    void setRect(int guiW, int guiH, int left, int top, int w, int h) {
        rect[0] = left;
        rect[1] = top;
        rect[2] = Math.max(1, w);
        rect[3] = Math.max(1, h);
        hasRect = true;
        stampW = guiW;
        stampH = guiH;
    }

    /** The native box {left, top, w, h}: measured, else the fallback once drawn, else null. */
    int[] rect(int guiW, int guiH) {
        if (hasRect && guiW == stampW && guiH == stampH) {
            return rect;
        }
        if (drawn && fallback == SupportedMod.Fallback.TOP_RIGHT_SQUARE) {
            return new int[]{guiW - 5 - 90, 5, 90, 90};
        }
        return null;
    }
}
