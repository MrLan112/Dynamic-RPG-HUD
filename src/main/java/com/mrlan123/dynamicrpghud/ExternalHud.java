package com.mrlan123.dynamicrpghud;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/** The elements of the supported mods and the begin / end calls the render hooks use. */
public final class ExternalHud {

    private static final Map<SupportedMod, ExternalElement> ELEMENTS = new EnumMap<>(SupportedMod.class);

    private ExternalHud() {
    }

    public static synchronized ExternalElement element(SupportedMod mod) {
        ExternalElement e = ELEMENTS.get(mod);
        if (e == null) {
            e = new ExternalElement(HudConfig.EXTERNAL_PREFIX + mod.key, nameOf(mod), 1).withFallback(mod.fallback);
            ELEMENTS.put(mod, e);
        }
        return e;
    }

    private static String nameOf(SupportedMod mod) {
        try {
            for (String id : mod.modIds) {
                java.util.Optional<ModContainer> c = FabricLoader.getInstance().getModContainer(id);
                if (c.isPresent()) {
                    return c.get().getMetadata().getName();
                }
            }
        } catch (RuntimeException e) {
            // fall through to the built-in name
        }
        return mod.defaultName;
    }

    /** Elements that have a box for this GUI size (the editor lists these). */
    public static synchronized List<ExternalElement> measured(int guiW, int guiH) {
        List<ExternalElement> out = new ArrayList<>();
        for (ExternalElement e : ELEMENTS.values()) {
            if (e.rect(guiW, guiH) != null) {
                out.add(e);
            }
        }
        return out;
    }

    /**
     * Called by a mod's render hook before the mod draws its HUD.
     *
     * @return false if the element is hidden and the mod's draw call must be skipped
     */
    public static boolean begin(SupportedMod mod) {
        ExternalElement e = element(mod);
        e.markDrawn();
        return HudPose.begin(e);
    }

    /** Called by the hook after the mod has drawn. */
    public static void end() {
        HudPose.end();
    }
}
