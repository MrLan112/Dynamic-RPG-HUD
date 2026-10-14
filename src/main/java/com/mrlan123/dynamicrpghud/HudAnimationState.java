package com.mrlan123.dynamicrpghud;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.player.LocalPlayer;

/**
 * Decides whether the HUD is "shown" or "hidden" and eases every element's
 * visibility progress (0 = hidden, 1 = shown) towards that state.
 * <p>
 * Trigger mode AUTO: hides when idle at full health/hunger or while moving, reveals on the events
 * the player enabled on the Automatic Mode screen, then stays for the configured delay (4 s default).
 * Trigger mode HOTKEY: only the toggle key changes the state.
 * Elements marked "stay visible" never hide.
 */
public final class HudAnimationState {

    public static final int VISIBLE_TICKS = HudVisibility.VISIBLE_TICKS;
    /** Every element needs exactly this long (seconds) to go from fully shown to fully hidden. */
    public static final float DURATION = 0.45f;

    private static final HudVisibility visibility = new HudVisibility();
    private static boolean shown = true;
    private static boolean manualShown = true;

    private static final class State {
        HudElement element;
        float progress = 1.0f;
        long lastNanos = -1L;
    }

    private static final java.util.Map<String, State> STATES = new java.util.HashMap<>();

    private static State state(HudElement part) {
        State st = STATES.computeIfAbsent(part.id(), k -> new State());
        st.element = part;
        return st;
    }

    /** Translucency multiplier read by the Minecraft 1.21.6+ sprite mixin. */
    public static float fadeAlpha = 1.0f;

    /** Air bubbles / horse jump bar: shown by their own rules, not by the global state. */
    private static boolean airWanted = true;
    private static boolean jumpWanted = true;
    private static final HotbarKeys hotbarKeys = new HotbarKeys();

    private static boolean initialized = false;
    private static PlayerSnapshot last = null;
    private static int lastFrameSlot = -1;
    private static int lastFrameItem = 0;
    private static double lastX = Double.NaN;
    private static double lastZ = Double.NaN;

    private HudAnimationState() {
    }

    public static boolean isShown() {
        return shown;
    }

    /** Test hook: sets the global shown flag. */
    static void forceShown(boolean value) {
        shown = value;
    }

    /** Called by the toggle key (hotkey trigger mode). */
    public static void toggleManual() {
        manualShown = !manualShown;
    }

    // =============================================================================================
    // Tick logic
    // =============================================================================================

    public static void onClientTick(Minecraft client) {
        LocalPlayer player = client.player;

        if (player == null) {
            airWanted = true;
            jumpWanted = true;
            hotbarKeys.reset();
            shown = true;
            manualShown = true;
            visibility.reset();
            initialized = false;
            last = null;
            lastX = Double.NaN;
            HudGeometry.clear();
            return;
        }

        updateConditional(client, player);

        if (isOwnScreen(client.screen)) {
            // Never hide the HUD behind this mod's own screens (the real HUD is the editor preview).
            shown = true;
            visibility.reset();
            initialized = false;
            last = null;
            lastX = Double.NaN;
            return;
        }

        if (HudConfig.trigger() == TriggerMode.HOTKEY) {
            shown = manualShown;
            initialized = false;
            last = null;
            return;
        }

        visibility.setVisibleTicks(HudConfig.delayTicks());

        PlayerSnapshot now = PlayerSnapshot.of(client, player);
        if (!initialized || last == null) {
            initialized = true;
            last = now;
            lastFrameSlot = now.slot;
            lastFrameItem = now.heldItem;
        }

        boolean reveal = anyEnabled(RevealDetector.detect(last, now));
        if (pollHotbarKeys(client) && HudConfig.revealOn(RevealTrigger.HOTBAR_KEY)) {
            reveal = true;
        }
        last = now;
        if (client.screen instanceof AbstractContainerScreen && HudConfig.revealOn(RevealTrigger.INVENTORY)) {
            reveal = true;
        }

        boolean moved = updateMoving(player);
        visibility.tick(reveal, moved, isFullHealthAndFood(player));
        shown = visibility.shown();
    }

    /** Reads the nine hotbar keys; true when one of them was pressed since the last read. */
    private static boolean pollHotbarKeys(Minecraft mc) {
        if (mc.options == null || mc.options.keyHotbarSlots == null) {
            return false;
        }
        boolean[] now = new boolean[9];
        for (int i = 0; i < now.length && i < mc.options.keyHotbarSlots.length; i++) {
            now[i] = mc.options.keyHotbarSlots[i].isDown();
        }
        return hotbarKeys.update(now);
    }

    /** Decides whether the air bubbles and the horse jump bar are wanted right now. */
    private static void updateConditional(Minecraft client, LocalPlayer player) {
        if (player == null || isOwnScreen(client.screen)) {
            airWanted = true;
            jumpWanted = true;
            return;
        }
        airWanted = airWanted(player.isEyeInFluid(net.minecraft.tags.FluidTags.WATER),
                player.getAirSupply() < player.getMaxAirSupply(),
                HudConfig.airShowUnderwater(), HudConfig.airShowOnSubmerge(), HudConfig.airHideAfterRefill());
        jumpWanted = player.getVehicle() instanceof net.minecraft.world.entity.PlayerRideableJumping
                && client.options != null && client.options.keyJump.isDown();
    }

    /**
     * The air bubbles rule. Under water: not at all, or shown at once, or only after air has been lost. Out of the
     * water: kept until the air is full again, or hidden at once. (Vanilla draws them for both.)
     */
    static boolean airWanted(boolean inWater, boolean airLost, boolean showUnderwater,
                             boolean showOnSubmerge, boolean hideAfterRefill) {
        if (inWater) {
            return showUnderwater && (showOnSubmerge || airLost);
        }
        return hideAfterRefill && airLost;
    }

    private static boolean anyEnabled(java.util.Set<RevealTrigger> events) {
        for (RevealTrigger t : events) {
            if (HudConfig.revealOn(t)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isOwnScreen(net.minecraft.client.gui.screens.Screen screen) {
        return screen != null && screen.getClass().getName().startsWith("com.mrlan123.dynamicrpghud.");
    }

    /**
     * Called once per rendered frame from the hotbar wrapper: a slot or held item change that
     * happens between two ticks (fast scrolling) is latched instead of being lost.
     */
    public static void observeFrame() {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null || !initialized || mc.screen != null) {
            return;
        }
        int slot = player.getInventory().getSelectedSlot();
        net.minecraft.world.item.ItemStack held = player.getMainHandItem();
        int item = held == null || held.isEmpty() ? 0 : System.identityHashCode(held.getItem());
        boolean scrolled = slot != lastFrameSlot && HudConfig.revealOn(RevealTrigger.HOTBAR_SCROLL);
        boolean switched = item != lastFrameItem && HudConfig.revealOn(RevealTrigger.ITEM_SWITCH);
        lastFrameSlot = slot;
        lastFrameItem = item;
        if (pollHotbarKeys(mc) && HudConfig.revealOn(RevealTrigger.HOTBAR_KEY)) {
            visibility.latchReveal();
        }
        if (scrolled || switched) {
            visibility.latchReveal();
        }
    }

    private static boolean updateMoving(LocalPlayer player) {
        double x = player.getX();
        double z = player.getZ();
        boolean moved = false;
        if (!Double.isNaN(lastX)) {
            double dx = x - lastX;
            double dz = z - lastZ;
            moved = (dx * dx + dz * dz) > 0.0004;
        }
        lastX = x;
        lastZ = z;
        return moved || player.isSprinting();
    }

    private static boolean isFullHealthAndFood(LocalPlayer player) {
        return player.getHealth() >= player.getMaxHealth() && player.getFoodData().getFoodLevel() >= 20;
    }

    // =============================================================================================
    // Per-frame easing
    // =============================================================================================

    /** Where an element's progress is heading: 1 = shown, 0 = hidden. */
    static float targetFor(HudElement part) {
        if (HudConfig.isKept(part)) {
            return 1f;
        }
        if (part == HudPart.AIR) {
            return airWanted ? 1f : 0f;
        }
        if (part == HudPart.JUMP_BAR) {
            return jumpWanted ? 1f : 0f;
        }
        return shown ? 1f : 0f;
    }

    /**
     * Once per rendered frame (from the Gui.render wrapper): moves EVERY known element towards its
     * target, also those that are not drawn this frame (another mod's HUD under a screen). Without
     * this such an element kept its old progress and, when drawn again, jumped through a long frame
     * step: it slid in late and at a different speed.
     */
    public static void advanceAll() {
        for (State st : STATES.values()) {
            if (st.element != null) {
                advance(st, st.element);
            }
        }
    }

    /** Advances the visibility progress of one element; call right before drawing it. */
    public static void update(HudElement part, int guiW, int guiH) {
        advance(state(part), part);
    }

    private static void advance(State st, HudElement part) {
        long now = System.nanoTime();
        if (HudConfig.isKept(part)) {
            // "Stay visible" elements are never animated: fully shown at once, always.
            st.progress = 1f;
            st.lastNanos = now;
            return;
        }
        float target = targetFor(part);

        if (st.lastNanos < 0 || HudConfig.animationFor(part) == AnimationMode.INSTANT) {
            st.progress = target;
        } else {
            float dt = (float) ((now - st.lastNanos) / 1_000_000_000.0);
            dt = Math.max(0f, Math.min(0.1f, dt));
            st.progress = step(st.progress, target, dt);
        }
        st.lastNanos = now;
    }

    /** One time step: constant speed (1 / DURATION per second), never past the target. */
    static float step(float progress, float target, float dtSeconds) {
        float d = dtSeconds / DURATION;
        if (progress < target) {
            return Math.min(target, progress + d);
        }
        return Math.max(target, progress - d);
    }

    /** Ease in and out (cubic): gentle start and end, same curve for every element. */
    public static float ease(float p) {
        if (p <= 0f) {
            return 0f;
        }
        if (p >= 1f) {
            return 1f;
        }
        return p < 0.5f ? 4f * p * p * p : 1f - (float) Math.pow(-2f * p + 2f, 3) / 2f;
    }

    /** The visual progress of an element: its time-linear progress put through {@link #ease}. */
    public static float eased(HudElement part) {
        return ease(progress(part));
    }

    public static float progress(HudElement part) {
        return state(part).progress;
    }

    /** Same for text colours: an alpha of 0 means fully opaque there. */
    public static int applyTextAlpha(int color) {
        float a = fadeAlpha;
        if (a >= 1.0f) {
            return color;
        }
        int base = (color >>> 24) & 0xFF;
        if (base == 0) {
            base = 255;
        }
        int alpha = Math.max(1, (int) (base * Math.max(0f, a)));
        return (alpha << 24) | (color & 0x00FFFFFF);
    }

    /** Multiplies the alpha channel of an ARGB colour by the current fade alpha. */
    public static int applyAlpha(int color) {
        float a = fadeAlpha;
        if (a >= 1.0f) {
            return color;
        }
        int alpha = (int) (((color >>> 24) & 0xFF) * Math.max(0f, a));
        return (alpha << 24) | (color & 0x00FFFFFF);
    }
}
