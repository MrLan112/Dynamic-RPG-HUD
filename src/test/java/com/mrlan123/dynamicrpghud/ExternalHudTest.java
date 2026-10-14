package com.mrlan123.dynamicrpghud;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

/** Other mods' HUD: defaults, measuring, per-element animation (no Minecraft needed). */
class ExternalHudTest {

    private final ExternalElement elem = new ExternalElement("ext:testmod", "Test Mod", 1);

    @AfterEach
    void cleanup() {
        HudConfig.resetPart(elem);
        HudConfig.resetPart(HudPart.HOTBAR);
        HudConfig.setAnimation(AnimationMode.FADE);
    }

    @Test
    void externalHudDoesNotHideByDefaultButVanillaDoes() {
        assertTrue(HudConfig.isKept(elem));
        assertFalse(HudConfig.isKept(HudPart.HOTBAR));
        assertTrue(HudConfig.isDefault(elem));
    }

    @Test
    void ownAnimationOverridesGlobalOnlyForThatElement() {
        HudConfig.setAnimation(AnimationMode.FADE);
        HudConfig.setElementAnimation(HudPart.HOTBAR, AnimationMode.SLIDE);
        assertEquals(AnimationMode.SLIDE, HudConfig.animationFor(HudPart.HOTBAR));
        assertEquals(AnimationMode.FADE, HudConfig.animationFor(HudPart.HEALTH));
        HudConfig.setElementAnimation(HudPart.HOTBAR, null);
        assertEquals(AnimationMode.FADE, HudConfig.animationFor(HudPart.HOTBAR));
    }

    @Test
    void slideAndShrinkCanCoexistOnDifferentElements() {
        HudConfig.setAnimation(AnimationMode.INSTANT);
        HudConfig.setElementAnimation(HudPart.XP_BAR, AnimationMode.SHRINK);
        HudAnimationState.forceShown(false);
        HudAnimationState.update(HudPart.XP_BAR, 400, 300);
        HudAnimationState.update(HudPart.HEALTH, 400, 300);
        // global INSTANT snaps HEALTH to hidden, XP_BAR (shrink) was freshly initialised so it also snaps;
        assertEquals(0f, HudAnimationState.progress(HudPart.HEALTH));
        HudAnimationState.forceShown(true);
        HudConfig.resetPart(HudPart.XP_BAR);
    }

    @Test
    void unmeasuredExternalElementIsDrawnUntouched() {
        HudConfig.setOffset(elem, 40, 40);
        HudTransform.compute(elem, 400, 300);
        assertTrue(HudTransform.visible);
        assertTrue(HudTransform.isIdentity());
    }

    @Test
    void measuredRectIsOnlyValidForTheSameGuiSize() {
        elem.setRect(400, 300, 10, 20, 50, 12);
        assertArrayEquals(new int[]{10, 20, 50, 12}, HudLayout.rect(elem, 400, 300));
        assertNull(HudLayout.rect(elem, 500, 300));
    }

    @Test
    void captureUndoesTheElementsOwnTransform() {
        elem.setRect(400, 300, 100, 100, 40, 20); // native box, centre (120, 110)
        HudConfig.setOffset(elem, 30, -10);
        HudConfig.setScale(elem, 2f);
        HudTransform.compute(elem, 400, 300);
        HudCapture.begin(elem, 400, 300, true);
        // screen corners of the native box after the transform: (x - cx) * 2 + cx + 30 ...
        HudCapture.addScreenPoint((100 - 120) * 2 + 120 + 30, (100 - 110) * 2 + 110 - 10);
        HudCapture.addScreenPoint((140 - 120) * 2 + 120 + 30, (120 - 110) * 2 + 110 - 10);
        HudCapture.end();
        assertArrayEquals(new int[]{100, 100, 40, 20}, HudLayout.rect(elem, 400, 300));
    }

    @Test
    void nothingIsMeasuredWhileTheElementIsFading() {
        elem.setRect(400, 300, 100, 100, 40, 20);
        HudTransform.compute(elem, 400, 300);
        HudCapture.begin(elem, 400, 300, false);
        HudCapture.addScreenPoint(0, 0);
        HudCapture.addScreenPoint(5, 5);
        HudCapture.end();
        assertArrayEquals(new int[]{100, 100, 40, 20}, HudLayout.rect(elem, 400, 300));
    }

    @Test
    void supportedModsMatchOnlyTheirOwnIds() {
        assertTrue(SupportedMod.JADE.matches(id -> id.equals("jade")));
        assertFalse(SupportedMod.JADE.matches(id -> id.equals("wthit")));
        assertTrue(SupportedMod.XAERO_MINIMAP.matches(id -> id.equals("xaerominimapfair")));
        assertFalse(SupportedMod.MINIHUD.matches(id -> false));
    }

    @Test
    void rawGlModsGetAFallbackBoxOnlyAfterTheirHookRan() {
        ExternalElement xaero = new ExternalElement("ext:xaero_minimap", "Xaero", 1)
                .withFallback(SupportedMod.Fallback.TOP_RIGHT_SQUARE);
        assertNull(HudLayout.rect(xaero, 400, 300));
        xaero.markDrawn();
        assertArrayEquals(new int[]{305, 5, 90, 90}, HudLayout.rect(xaero, 400, 300));
    }

    @Test
    void hookFramesAreBalancedAndReleasedAtFrameEnd() {
        HudConfig.setOffset(elem, 5, 5);
        elem.setRect(400, 300, 10, 10, 20, 20);
        HudPoseTest.FakeStack st = new HudPoseTest.FakeStack();
        assertTrue(HudPose.beginWith(st, 400, 300, elem));
        // a second hook on the same element must not transform twice
        assertTrue(HudPose.beginWith(st, 400, 300, elem));
        assertEquals(1, st.depth);
        assertEquals(2, HudPose.openFrames());
        HudPose.end();
        assertEquals(1, st.depth);
        HudPose.releaseAll(); // e.g. another mod cancelled before our end hook ran
        assertEquals(0, st.depth);
        assertEquals(0, HudPose.openFrames());
    }

    @Test
    void hiddenElementIsNotOpenedSoItsMethodCanBeCancelled() {
        HudConfig.setKept(elem, false);
        HudConfig.setAnimation(AnimationMode.INSTANT);
        HudAnimationState.forceShown(false);
        HudPoseTest.FakeStack st = new HudPoseTest.FakeStack();
        assertFalse(HudPose.beginWith(st, 400, 300, elem));
        assertEquals(0, HudPose.openFrames());
        HudAnimationState.forceShown(true);
    }
}
