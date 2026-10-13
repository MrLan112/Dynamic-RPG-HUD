package com.mrlan123.dynamicrpghud;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.Gui;

import java.util.regex.Pattern;

/**
 * FPS - Display (Grayray75) draws its counter straight from a mixin into {@code Gui#render}, so
 * there is no method of its own to wrap. Its text is recognised instead: a line of the exact
 * form the mod produces, drawn from inside {@code Gui}, while the mod is installed. That single
 * draw call is then wrapped like any other element.
 */
public final class FpsDisplayHook {

    private static final Pattern LINE = Pattern.compile("^\\d+ FPS( \\(\\d+ min \\| \\d+ avg \\| \\d+ max\\))?$");
    private static final StackWalker WALKER = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE);

    private static Boolean loaded;
    private static boolean open;

    private FpsDisplayHook() {
    }

    private static boolean installed() {
        if (loaded == null) {
            loaded = FabricLoader.getInstance().isModLoaded("fpsdisplay");
        }
        return loaded;
    }

    static boolean isFpsLine(String text) {
        return text != null && LINE.matcher(text).matches();
    }

    private static boolean calledFromGui() {
        return WALKER.walk(s -> s.limit(8).anyMatch(f -> f.getDeclaringClass() == Gui.class));
    }

    /** @return false if the counter is hidden and the draw call must be skipped */
    public static boolean begin(String text) {
        if (open || !installed() || !isFpsLine(text) || !calledFromGui()) {
            return true;
        }
        if (!ExternalHud.begin(SupportedMod.FPS_DISPLAY)) {
            return false;
        }
        open = true;
        return true;
    }

    public static void end() {
        if (open) {
            open = false;
            ExternalHud.end();
        }
    }
}
