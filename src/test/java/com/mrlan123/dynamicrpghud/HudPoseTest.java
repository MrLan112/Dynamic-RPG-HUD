package com.mrlan123.dynamicrpghud;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

/** The transform must never outlive the vanilla call, even when it throws (bugs 1-7, 12). */
class HudPoseTest {

    static final class FakeStack implements HudPose.Stack {
        int depth, pushes, maxDepth;
        float alpha = 1f;
        float lastScale = 1f;

        public void push() {
            depth++;
            pushes++;
            maxDepth = Math.max(maxDepth, depth);
        }

        public void pop() {
            depth--;
        }

        public void translate(float x, float y) {
        }

        public void scale(float s) {
            lastScale = s;
        }

        public void setAlpha(float a) {
            alpha = a;
        }
    }

    @AfterEach
    void cleanup() {
        for (HudPart p : HudPart.values()) {
            HudConfig.resetPart(p);
        }
    }

    @Test
    void identityTouchesNothing() {
        FakeStack st = new FakeStack();
        boolean[] ran = {false};
        HudPose.runWith(st, 400, 300, HudPart.HOTBAR, () -> ran[0] = true);
        assertTrue(ran[0]);
        assertEquals(0, st.pushes);
    }

    @Test
    void customPositionIsPoppedAfterwards() {
        FakeStack st = new FakeStack();
        HudConfig.setOffset(HudPart.HOTBAR, 10, -20);
        HudPose.runWith(st, 400, 300, HudPart.HOTBAR, () -> assertEquals(1, st.depth));
        assertEquals(0, st.depth);
        assertEquals(1, st.pushes);
    }

    @Test
    void popAndColourRestoredEvenIfTheVanillaCallThrows() {
        FakeStack st = new FakeStack();
        HudConfig.setOffset(HudPart.HEALTH, 5, 5);
        HudConfig.setScale(HudPart.HEALTH, 2f);
        assertThrows(IllegalStateException.class, () -> HudPose.runWith(st, 400, 300, HudPart.HEALTH, () -> {
            throw new IllegalStateException("another mod crashed");
        }));
        assertEquals(0, st.depth);
        assertEquals(1f, st.alpha);
    }

    @Test
    void everyElementIsBalancedAndNeverNests() {
        FakeStack st = new FakeStack();
        for (HudPart p : HudPart.values()) {
            HudConfig.setOffset(p, 3, 4);
            HudPose.runWith(st, 400, 300, p, () -> { });
            assertEquals(0, st.depth, p.name());
        }
        assertEquals(1, st.maxDepth);
    }

    @Test
    void shrinkPivotsAroundTheLiveRectCentre() {
        HudGeometry.clear();
        HudGeometry.set(HudPart.HEALTH, 400, 300, 109, 243, 81, 27); // three rows
        HudPose.runWith(new FakeStack(), 400, 300, HudPart.HEALTH, () -> { });
        assertEquals(109 + 81 / 2f, HudTransform.cx);
        assertEquals(243 + 27 / 2f, HudTransform.cy);
        HudGeometry.clear();
    }
}
