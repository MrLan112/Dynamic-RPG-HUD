package com.mrlan123.dynamicrpghud;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class HudVisibilityTest {

    private static void idle(HudVisibility v, int ticks, boolean full) {
        for (int i = 0; i < ticks; i++) {
            v.tick(false, false, full);
        }
    }

    @Test
    void hidesOnlyAfterTimerWhenIdleAtFullStats() {
        HudVisibility v = new HudVisibility();
        v.tick(true, false, true);
        idle(v, HudVisibility.VISIBLE_TICKS - 1, true);
        assertTrue(v.shown(), "still inside the 80 tick window");
        idle(v, 2, true);
        assertFalse(v.shown());
    }

    @Test
    void revealResetsTimerToEightyTicks() {
        HudVisibility v = new HudVisibility();
        idle(v, 200, true);
        assertFalse(v.shown());
        v.tick(true, false, true);
        assertTrue(v.shown());
        assertEquals(HudVisibility.VISIBLE_TICKS, v.timer());
        idle(v, 40, true);
        v.tick(true, false, true); // another reveal restarts it
        assertEquals(HudVisibility.VISIBLE_TICKS, v.timer());
    }

    @Test
    void latchedRevealBetweenTicksIsNotLost() {
        HudVisibility v = new HudVisibility();
        idle(v, 200, true);
        assertFalse(v.shown());
        v.latchReveal();
        v.tick(false, false, true);
        assertTrue(v.shown());
        assertEquals(HudVisibility.VISIBLE_TICKS, v.timer());
    }

    @Test
    void noFlappingWhenWalkingStartStopWithLowHealth() {
        HudVisibility v = new HudVisibility();
        idle(v, 200, false);
        int changes = 0;
        boolean last = v.shown();
        // 400 ticks of stop-and-go: 3 ticks moving, 3 ticks still, health not full.
        for (int t = 0; t < 400; t++) {
            v.tick(false, (t / 3) % 2 == 0, false);
            if (v.shown() != last) {
                changes++;
                last = v.shown();
            }
        }
        // Old logic toggled every few ticks (~130 changes). Now it may only change slowly.
        assertTrue(changes <= 400 / HudVisibility.MIN_DWELL, "state changes: " + changes);
    }

    @Test
    void stateChangeWithoutRevealRespectsMinimumDwell() {
        HudVisibility v = new HudVisibility();
        idle(v, 200, true);
        assertFalse(v.shown());
        // health drops without a reveal event (e.g. dwell) -> must still wait the dwell
        v.tick(false, false, false);
        assertTrue(v.shown() || v.timer() == 0);
        for (int i = 0; i < HudVisibility.MIN_DWELL; i++) {
            v.tick(false, false, false);
        }
        assertTrue(v.shown());
    }

    @Test
    void movementIsDebounced() {
        HudVisibility v = new HudVisibility();
        idle(v, 200, true);
        v.tick(true, false, true);
        idle(v, HudVisibility.VISIBLE_TICKS + 5, true);
        assertFalse(v.shown());
    }

    @Test
    void resetRestoresInitialState() {
        HudVisibility v = new HudVisibility();
        idle(v, 300, true);
        v.reset();
        assertTrue(v.shown());
        assertEquals(HudVisibility.VISIBLE_TICKS, v.timer());
    }
}
