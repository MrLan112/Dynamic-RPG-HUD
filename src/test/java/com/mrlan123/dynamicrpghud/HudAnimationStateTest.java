package com.mrlan123.dynamicrpghud;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class HudAnimationStateTest {

    @AfterEach
    void cleanup() {
        HudAnimationState.forceShown(true);
        for (HudPart p : HudPart.values()) {
            HudConfig.resetPart(p);
        }
        HudConfig.setAnimation(AnimationMode.FADE);
    }

    @Test
    void keptElementIsNeverAnimatedOrHidden() {
        for (AnimationMode mode : AnimationMode.values()) {
            HudConfig.setAnimation(mode);
            HudConfig.setKept(HudPart.HOTBAR, true);
            HudAnimationState.forceShown(false);
            for (int i = 0; i < 50; i++) {
                HudAnimationState.update(HudPart.HOTBAR, 400, 300);
            }
            assertEquals(1f, HudAnimationState.progress(HudPart.HOTBAR), mode.name());
            HudTransform.compute(HudPart.HOTBAR, 400, 300);
            assertTrue(HudTransform.visible);
            assertEquals(1f, HudTransform.alpha, mode.name());
            assertEquals(1f, HudTransform.scale, mode.name());
        }
    }

    @Test
    void unkeptElementHidesWhenStateIsHidden() {
        HudConfig.setAnimation(AnimationMode.INSTANT);
        HudAnimationState.forceShown(false);
        HudAnimationState.update(HudPart.XP_BAR, 400, 300);
        assertEquals(0f, HudAnimationState.progress(HudPart.XP_BAR));
    }
}
