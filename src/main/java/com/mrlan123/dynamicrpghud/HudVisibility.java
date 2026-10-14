package com.mrlan123.dynamicrpghud;

/**
 * Pure (Minecraft-free) state machine that decides whether the HUD is shown in AUTO mode.
 * <p>
 * Rules: any reveal event (slot change, held item change, damage, hunger loss, new effect) shows
 * the HUD at once and restarts the 80 tick timer. When the timer is over, the HUD hides while the
 * player is moving or at full health and food. Two guards remove the flapping of the old logic:
 * movement is debounced (it counts for {@link #MOVE_HOLD} ticks after the last step), and a
 * non-reveal change of state needs the previous state to have lasted {@link #MIN_DWELL} ticks.
 * Reveal events can be latched between ticks, so a fast slot scroll is never lost.
 */
public final class HudVisibility {

    public static final int VISIBLE_TICKS = 80;
    public static final int MOVE_HOLD = 10;
    public static final int MIN_DWELL = 20;

    private int visibleTicks = VISIBLE_TICKS;
    private boolean shown = true;
    private int timer = VISIBLE_TICKS;
    private int moveHold;
    private int dwell = MIN_DWELL;
    private boolean pending;

    public boolean shown() {
        return shown;
    }

    /** How many ticks the HUD stays after the last reveal event. */
    public void setVisibleTicks(int ticks) {
        visibleTicks = Math.max(1, ticks);
        if (timer > visibleTicks) {
            timer = visibleTicks;
        }
    }

    public int timer() {
        return timer;
    }

    /** Remembers a reveal event seen between two ticks (e.g. in the render thread). */
    public void latchReveal() {
        pending = true;
    }

    /** Back to the initial state (no player, a screen is open, hotkey mode...). */
    public void reset() {
        shown = true;
        timer = visibleTicks;
        moveHold = 0;
        dwell = MIN_DWELL;
        pending = false;
    }

    public void tick(boolean reveal, boolean movedRaw, boolean fullStats) {
        boolean revealed = reveal || pending;
        pending = false;
        if (revealed) {
            timer = visibleTicks;
        } else if (timer > 0) {
            timer--;
        }
        if (movedRaw) {
            moveHold = MOVE_HOLD;
        } else if (moveHold > 0) {
            moveHold--;
        }
        boolean moving = moveHold > 0;
        boolean want = timer > 0 || !(moving || fullStats);
        if (dwell < MIN_DWELL) {
            dwell++;
        }
        if (want != shown && (revealed || dwell >= MIN_DWELL)) {
            shown = want;
            dwell = 0;
        }
    }
}
