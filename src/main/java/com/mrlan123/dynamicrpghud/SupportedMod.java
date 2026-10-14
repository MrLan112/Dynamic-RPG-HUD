package com.mrlan123.dynamicrpghud;

/**
 * Mods whose HUD this mod supports explicitly (nothing is guessed): each entry matches one render
 * hook in {@code mixin/compat}. The hooks only exist when the mod is installed, and nothing of
 * these mods is bundled or needed to build.
 */
public enum SupportedMod {
    JADE("jade", "Jade", Fallback.NONE, "jade"),
    WTHIT("wthit", "WTHIT", Fallback.NONE, "wthit"),
    XAERO_MINIMAP("xaero_minimap", "Xaero's Minimap", Fallback.TOP_RIGHT_SQUARE, "xaerominimap", "xaerominimapfair"),
    SODIUM_EXTRA("sodium_extra", "Sodium Extra", Fallback.NONE, "sodium-extra"),
    VOICE_CHAT("voice_chat", "Simple Voice Chat", Fallback.NONE, "voicechat"),
    MINIHUD("minihud", "MiniHUD", Fallback.NONE, "minihud"),
    FPS_DISPLAY("fps_display", "FPS - Display", Fallback.NONE, "fpsdisplay"),
    /** Draws through the vanilla Health / Armor elements, so it has no element of its own. */
    OVERFLOWING_BARS("overflowing_bars", "Overflowing Bars", Fallback.NONE, "overflowingbars");

    /** Box shown in the editor when the mod draws without the usual draw calls (raw OpenGL). */
    public enum Fallback {
        NONE,
        TOP_RIGHT_SQUARE
    }

    public final String key;
    public final String defaultName;
    public final Fallback fallback;
    public final String[] modIds;

    SupportedMod(String key, String defaultName, Fallback fallback, String... modIds) {
        this.key = key;
        this.defaultName = defaultName;
        this.fallback = fallback;
        this.modIds = modIds;
    }

    /** True if any of this mod's ids is in the list of loaded mods ({@code isLoaded} is the lookup). */
    public boolean matches(java.util.function.Predicate<String> isLoaded) {
        for (String id : modIds) {
            if (isLoaded.test(id)) {
                return true;
            }
        }
        return false;
    }
}
