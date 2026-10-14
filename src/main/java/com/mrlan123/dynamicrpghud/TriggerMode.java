package com.mrlan123.dynamicrpghud;

/** What decides when the HUD hides. */
public enum TriggerMode {
    AUTO("auto"),
    HOTKEY("hotkey");

    public final String id;

    TriggerMode(String id) {
        this.id = id;
    }

    public String nameKey() {
        return "dynamicrpghud.trigger." + id;
    }

    public String descKey() {
        return "dynamicrpghud.trigger." + id + ".desc";
    }

    public static TriggerMode byName(String name, TriggerMode fallback) {
        for (TriggerMode m : values()) {
            if (m.name().equalsIgnoreCase(name)) {
                return m;
            }
        }
        return fallback;
    }
}
