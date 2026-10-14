package com.mrlan123.dynamicrpghud;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * All user settings, stored in a single human-readable file: config/dynamicrpghud.json.
 * To share a setup, send that file; the receiver drops it into their config folder
 * (replacing the old one) and restarts the game.
 */
public final class HudConfig {

    public static final int CONFIG_VERSION = 2;
    public static final float MIN_SCALE = 0.25f;
    public static final float MAX_SCALE = 4.0f;
    public static final int MAX_OFFSET = 2000;

    public static final String EXTERNAL_PREFIX = "ext:";

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    /** Settings of one element. External (other mods') elements default to "stay visible". */
    private static final class Entry {
        boolean kept;
        int x;
        int y;
        float scale = 1.0f;
        AnimationMode animation; // null = use the global animation
    }

    private static final Map<String, Entry> ENTRIES = new LinkedHashMap<>();

    private static AnimationMode animation = AnimationMode.FADE;
    private static TriggerMode trigger = TriggerMode.AUTO;

    public static final int DEFAULT_DELAY_TICKS = 80;
    public static final int MIN_DELAY_TICKS = 10;
    public static final int MAX_DELAY_TICKS = 1200;
    private static int delayTicks = DEFAULT_DELAY_TICKS;

    /** Air bubbles: show them at all while the head is under water. */
    private static boolean airShowUnderwater = true;
    /** Air bubbles: show them as soon as the head is under water (else only once air is lost). */
    private static boolean airShowOnSubmerge = true;
    /** Air bubbles: after leaving the water keep them until the air is refilled (else hide at once). */
    private static boolean airHideAfterRefill = true;
    private static final java.util.EnumMap<RevealTrigger, Boolean> REVEAL = new java.util.EnumMap<>(RevealTrigger.class);

    static {
        resetAll();
    }

    private HudConfig() {
    }

    public static Path file() {
        return FabricLoader.getInstance().getConfigDir().resolve("dynamicrpghud.json");
    }

    // ---- accessors ----------------------------------------------------------------------------

    public static AnimationMode animation() {
        return animation;
    }

    public static void setAnimation(AnimationMode mode) {
        animation = mode;
    }

    public static TriggerMode trigger() {
        return trigger;
    }

    public static void setTrigger(TriggerMode mode) {
        trigger = mode;
    }

    /** How long the HUD stays after the last event, in ticks (20 per second). */
    public static int delayTicks() {
        return delayTicks;
    }

    public static void setDelayTicks(int ticks) {
        delayTicks = clampInt(ticks, MIN_DELAY_TICKS, MAX_DELAY_TICKS);
    }

    public static boolean airShowUnderwater() {
        return airShowUnderwater;
    }

    public static void setAirShowUnderwater(boolean on) {
        airShowUnderwater = on;
    }

    public static boolean airShowOnSubmerge() {
        return airShowOnSubmerge;
    }

    public static void setAirShowOnSubmerge(boolean on) {
        airShowOnSubmerge = on;
    }

    public static boolean airHideAfterRefill() {
        return airHideAfterRefill;
    }

    public static void setAirHideAfterRefill(boolean on) {
        airHideAfterRefill = on;
    }

    public static boolean revealOn(RevealTrigger t) {
        Boolean v = REVEAL.get(t);
        return v != null ? v : t.defaultOn;
    }

    public static void setRevealOn(RevealTrigger t, boolean on) {
        REVEAL.put(t, on);
    }

    /** Back to the default delay and the default set of events. */
    public static void resetAutoMode() {
        delayTicks = DEFAULT_DELAY_TICKS;
        REVEAL.clear();
    }

    private static Entry entry(String id) {
        Entry e = ENTRIES.get(id);
        if (e == null) {
            e = new Entry();
            e.kept = defaultKept(id);
            ENTRIES.put(id, e);
        }
        return e;
    }

    private static boolean defaultKept(String id) {
        if (id.startsWith(EXTERNAL_PREFIX)) {
            return true;
        }
        HudPart part = HudPart.byId(id);
        return part != null && part.staysVisibleByDefault();
    }

    private static Entry entry(HudElement p) {
        return entry(p.id());
    }

    public static boolean isKept(HudElement p) {
        return entry(p).kept;
    }

    public static void setKept(HudElement p, boolean kept) {
        entry(p).kept = kept;
    }

    public static int offsetX(HudElement p) {
        return p.isMovable() ? entry(p).x : 0;
    }

    public static int offsetY(HudElement p) {
        return p.isMovable() ? entry(p).y : 0;
    }

    public static void setOffset(HudElement p, int x, int y) {
        if (!p.isMovable()) {
            return;
        }
        Entry e = entry(p);
        e.x = clampInt(x, -MAX_OFFSET, MAX_OFFSET);
        e.y = clampInt(y, -MAX_OFFSET, MAX_OFFSET);
    }

    public static float scale(HudElement p) {
        return entry(p).scale;
    }

    public static void setScale(HudElement p, float scale) {
        entry(p).scale = Math.max(MIN_SCALE, Math.min(MAX_SCALE, scale));
    }

    /** This element's own hide animation, or null when it follows the global one. */
    public static AnimationMode elementAnimation(HudElement p) {
        return entry(p).animation;
    }

    public static void setElementAnimation(HudElement p, AnimationMode mode) {
        entry(p).animation = mode;
    }

    /** The animation actually used for this element. */
    public static AnimationMode animationFor(HudElement p) {
        AnimationMode own = entry(p).animation;
        return own != null ? own : animation;
    }

    /** True when the user has not moved the element (scale and animation do not count). */
    public static boolean isInPlace(HudElement p) {
        return offsetX(p) == 0 && offsetY(p) == 0;
    }

    public static boolean isDefault(HudElement p) {
        Entry e = entry(p);
        return e.kept == defaultKept(p.id()) && e.x == 0 && e.y == 0 && e.scale == 1.0f && e.animation == null;
    }

    public static void resetPart(HudElement p) {
        ENTRIES.remove(p.id());
        if (p == HudPart.AIR) {
            airShowUnderwater = true;
            airShowOnSubmerge = true;
            airHideAfterRefill = true;
        }
    }

    /** Positions and scales only (keeps animation / trigger / stay-visible flags). */
    public static void resetLayout() {
        for (Entry e : ENTRIES.values()) {
            e.x = 0;
            e.y = 0;
            e.scale = 1.0f;
        }
    }

    public static void resetAll() {
        ENTRIES.clear();
        airShowUnderwater = true;
        airShowOnSubmerge = true;
        airHideAfterRefill = true;
        animation = AnimationMode.FADE;
        trigger = TriggerMode.AUTO;
        resetAutoMode();
    }

    private static int clampInt(int v, int lo, int hi) {
        return Math.max(lo, Math.min(hi, v));
    }

    // ---- file ---------------------------------------------------------------------------------

    public static void load() {
        Path path = file();
        if (!Files.isRegularFile(path)) {
            return;
        }
        try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            JsonElement rootEl = JsonParser.parseReader(reader);
            if (rootEl == null || !rootEl.isJsonObject()) {
                return;
            }
            JsonObject root = rootEl.getAsJsonObject();
            resetAll();

            if (root.has("animation") && root.get("animation").isJsonPrimitive()) {
                animation = AnimationMode.byName(root.get("animation").getAsString(), AnimationMode.FADE);
            }
            if (root.has("trigger") && root.get("trigger").isJsonPrimitive()) {
                trigger = TriggerMode.byName(root.get("trigger").getAsString(), TriggerMode.AUTO);
            }

            if (root.has("hideDelaySeconds") && root.get("hideDelaySeconds").isJsonPrimitive()) {
                setDelayTicks(Math.round(root.get("hideDelaySeconds").getAsFloat() * 20f));
            }
            if (root.has("airShowUnderwater") && root.get("airShowUnderwater").isJsonPrimitive()) {
                airShowUnderwater = root.get("airShowUnderwater").getAsBoolean();
            }
            if (root.has("airShowOnSubmerge") && root.get("airShowOnSubmerge").isJsonPrimitive()) {
                airShowOnSubmerge = root.get("airShowOnSubmerge").getAsBoolean();
            }
            if (root.has("airHideAfterRefill") && root.get("airHideAfterRefill").isJsonPrimitive()) {
                airHideAfterRefill = root.get("airHideAfterRefill").getAsBoolean();
            }
            JsonElement when = root.get("showHudWhen");
            if (when != null && when.isJsonObject()) {
                for (Map.Entry<String, JsonElement> item : when.getAsJsonObject().entrySet()) {
                    RevealTrigger t = RevealTrigger.byId(item.getKey());
                    if (t != null && item.getValue().isJsonPrimitive()) {
                        REVEAL.put(t, item.getValue().getAsBoolean());
                    }
                }
            }

            // Version 1 files only had {"keepVisible": {"<id>": true}}.
            JsonElement legacy = root.get("keepVisible");
            if (legacy != null && legacy.isJsonObject()) {
                for (HudPart p : HudPart.values()) {
                    JsonElement v = legacy.getAsJsonObject().get(p.id);
                    if (v != null && v.isJsonPrimitive()) {
                        setKept(p, v.getAsBoolean());
                    }
                }
            }

            JsonElement elements = root.get("elements");
            if (elements != null && elements.isJsonObject()) {
                for (Map.Entry<String, JsonElement> item : elements.getAsJsonObject().entrySet()) {
                    if (!item.getValue().isJsonObject()) {
                        continue;
                    }
                    JsonObject o = item.getValue().getAsJsonObject();
                    Entry e = entry(item.getKey());
                    if (o.has("stayVisible")) {
                        e.kept = o.get("stayVisible").getAsBoolean();
                    }
                    int x = o.has("x") ? o.get("x").getAsInt() : 0;
                    int y = o.has("y") ? o.get("y").getAsInt() : 0;
                    e.x = clampInt(x, -MAX_OFFSET, MAX_OFFSET);
                    e.y = clampInt(y, -MAX_OFFSET, MAX_OFFSET);
                    if (o.has("scale")) {
                        e.scale = Math.max(MIN_SCALE, Math.min(MAX_SCALE, o.get("scale").getAsFloat()));
                    }
                    if (o.has("animation") && o.get("animation").isJsonPrimitive()) {
                        e.animation = AnimationMode.byName(o.get("animation").getAsString(), null);
                    }
                }
            }
        } catch (IOException | RuntimeException e) {
            // Broken file: fall back to what was loaded so far; it is rewritten on the next save.
        }
    }

    public static void save() {
        JsonObject root = new JsonObject();
        root.addProperty("configVersion", CONFIG_VERSION);
        root.addProperty("animation", animation.name());
        root.addProperty("trigger", trigger.name());
        root.addProperty("hideDelaySeconds", delayTicks / 20f);
        root.addProperty("airShowUnderwater", airShowUnderwater);
        root.addProperty("airShowOnSubmerge", airShowOnSubmerge);
        root.addProperty("airHideAfterRefill", airHideAfterRefill);
        JsonObject when = new JsonObject();
        for (RevealTrigger t : RevealTrigger.values()) {
            when.addProperty(t.id, revealOn(t));
        }
        root.add("showHudWhen", when);

        for (HudPart p : HudPart.values()) {
            entry(p); // vanilla elements are always written
        }
        JsonObject elements = new JsonObject();
        for (Map.Entry<String, Entry> item : ENTRIES.entrySet()) {
            Entry e = item.getValue();
            JsonObject o = new JsonObject();
            o.addProperty("stayVisible", e.kept);
            o.addProperty("x", e.x);
            o.addProperty("y", e.y);
            o.addProperty("scale", Math.round(e.scale * 100f) / 100f);
            if (e.animation != null) {
                o.addProperty("animation", e.animation.name());
            }
            elements.add(item.getKey(), o);
        }
        root.add("elements", elements);

        try {
            Path path = file();
            Files.createDirectories(path.getParent());
            Path tmp = path.resolveSibling("dynamicrpghud.json.tmp");
            try (Writer writer = Files.newBufferedWriter(tmp, StandardCharsets.UTF_8)) {
                GSON.toJson(root, writer);
            }
            Files.move(tmp, path, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            // Not fatal: the settings simply will not persist.
        }
    }
}
