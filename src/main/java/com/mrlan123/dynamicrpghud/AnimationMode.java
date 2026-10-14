package com.mrlan123.dynamicrpghud;

/** How the HUD leaves the screen. */
public enum AnimationMode {
    SLIDE("slide"),
    SLIDE_UP("slide_up"),
    SLIDE_LEFT("slide_left"),
    SLIDE_RIGHT("slide_right"),
    FADE("fade"),
    INSTANT("instant"),
    SHRINK("shrink");

    public final String id;

    AnimationMode(String id) {
        this.id = id;
    }

    public String nameKey() {
        return "dynamicrpghud.animation." + id;
    }

    public String descKey() {
        return "dynamicrpghud.animation." + id + ".desc";
    }

    public static AnimationMode byName(String name, AnimationMode fallback) {
        for (AnimationMode m : values()) {
            if (m.name().equalsIgnoreCase(name)) {
                return m;
            }
        }
        return fallback;
    }
}
