package com.mrlan123.dynamicrpghud;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class ConditionalHudTest {

    @Test
    void hotbarKeyPressIsSeenEvenForTheSameSlot() {
        HotbarKeys keys = new HotbarKeys();
        boolean[] none = new boolean[9];
        boolean[] one = new boolean[9];
        one[0] = true;
        assertFalse(keys.update(none));
        assertTrue(keys.update(one));   // pressed
        assertFalse(keys.update(one));  // still held: not a new press
        assertFalse(keys.update(none));
        assertTrue(keys.update(one));   // pressed again, slot 1 -> slot 1
    }

    @Test
    void airRuleUnderWater() {
        assertTrue(HudAnimationState.airWanted(true, false, true, true, true));   // shown at once
        assertFalse(HudAnimationState.airWanted(true, false, true, false, true)); // only after air is lost
        assertTrue(HudAnimationState.airWanted(true, true, true, false, true));
    }

    @Test
    void airCanBeSwitchedOffUnderWater() {
        assertFalse(HudAnimationState.airWanted(true, true, false, true, true));
        assertTrue(HudAnimationState.airWanted(false, true, false, true, true)); // refilling after the dive
    }

    @Test
    void airRuleAfterLeavingWater() {
        assertTrue(HudAnimationState.airWanted(false, true, true, true, true));    // keep until refilled
        assertFalse(HudAnimationState.airWanted(false, true, true, true, false));  // hide at once
        assertFalse(HudAnimationState.airWanted(false, false, true, true, true));  // full air: nothing to show
    }

    @Test
    void layersAndCrosshair() {
        assertEquals(1, HudPart.HEALTH.layer());
        assertEquals(1, HudPart.CROSSHAIR.layer());
        for (HudPart p : new HudPart[]{HudPart.AIR, HudPart.JUMP_BAR, HudPart.VEHICLE_HEALTH, HudPart.EFFECTS}) {
            assertEquals(2, p.layer());
        }
        assertFalse(HudPart.CROSSHAIR.isMovable());
        HudConfig.resetAll();
        assertTrue(HudConfig.isKept(HudPart.CROSSHAIR));      // never hides by default
        assertTrue(HudConfig.isDefault(HudPart.CROSSHAIR));
        HudConfig.setOffset(HudPart.CROSSHAIR, 50, 50);       // cannot be moved
        assertEquals(0, HudConfig.offsetX(HudPart.CROSSHAIR));
        assertEquals(0, HudConfig.offsetY(HudPart.CROSSHAIR));
        HudConfig.setScale(HudPart.CROSSHAIR, 2f);
        assertEquals(2f, HudConfig.scale(HudPart.CROSSHAIR));
        HudConfig.resetAll();
    }

    private static boolean near(float a, float b) {
        return Math.abs(a - b) < 1e-4f;
    }

    @Test
    void progressMovesAtTheSameConstantSpeedAndEasesSmoothly() {
        float p = 0f;
        for (int i = 0; i < 10; i++) {
            p = HudAnimationState.step(p, 1f, 0.01f);
        }
        assertTrue(near(0.1f / HudAnimationState.DURATION, p));
        assertEquals(0f, HudAnimationState.step(0.01f, 0f, 1f)); // never past the target
        assertEquals(0f, HudAnimationState.ease(0f));
        assertEquals(1f, HudAnimationState.ease(1f));
        assertTrue(near(0.5f, HudAnimationState.ease(0.5f)));
        assertTrue(HudAnimationState.ease(0.1f) < 0.1f); // gentle start
        assertTrue(HudAnimationState.ease(0.9f) > 0.9f); // gentle end
    }

    @Test
    void slidesLeaveTheWholeScreenForEveryElementAtTheSameSpeed() {
        int w = 400;
        int h = 300;
        // hidden: moved by a full screen (+ margin) in the direction, whatever the element is
        float[] down = HudTransform.slideOffset(AnimationMode.SLIDE, 1f, w, h);
        float[] up = HudTransform.slideOffset(AnimationMode.SLIDE_UP, 1f, w, h);
        float[] left = HudTransform.slideOffset(AnimationMode.SLIDE_LEFT, 1f, w, h);
        float[] right = HudTransform.slideOffset(AnimationMode.SLIDE_RIGHT, 1f, w, h);
        assertTrue(down[1] >= h && down[0] == 0f);
        assertTrue(up[1] <= -h && up[0] == 0f);
        assertTrue(left[0] <= -w && left[1] == 0f);
        assertTrue(right[0] >= w && right[1] == 0f);
        // in place: no offset; halfway is halfway
        assertTrue(near(0f, HudTransform.slideOffset(AnimationMode.SLIDE_LEFT, 0f, w, h)[0]));
        assertTrue(near(down[1] / 2f, HudTransform.slideOffset(AnimationMode.SLIDE, 0.5f, w, h)[1]));
    }
}
