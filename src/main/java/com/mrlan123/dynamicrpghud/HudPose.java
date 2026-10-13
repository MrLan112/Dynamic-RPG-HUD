package com.mrlan123.dynamicrpghud;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;

/**
 * Applies an element's transform to the GUI matrix stack (PoseStack, 1.21.1).
 * <p>
 * The transform lives only inside {@link #run}: push, draw, and pop in a {@code finally}. It is
 * driven from a MixinExtras {@code @WrapMethod}, so the pop cannot be skipped when another mod
 * cancels or throws inside the vanilla method (the old HEAD/RETURN pair could leak, which shifted
 * every later HUD element and the settings screens, since they share one GuiGraphics).
 */
public final class HudPose {

    private HudPose() {
    }

    // ---- begin / end form, for hooks that cannot wrap a whole method (other mods' HUD) ---------

    private static final class Frame {
        String id;
        Stack st;
        boolean pushed;
        boolean tinted;
        boolean captured;
    }

    private static final Frame[] FRAMES = new Frame[8];
    private static int depth;
    private static GuiGraphics frameGraphics;

    /** The graphics of the HUD frame being drawn (set by the Gui.render wrapper), or null. */
    public static void setFrame(GuiGraphics g) {
        frameGraphics = g;
    }

    /** True while the HUD of the game is being drawn (not in-world item rendering, not menus). */
    public static boolean inFrame() {
        return frameGraphics != null;
    }

    /** @return false if the element is hidden and the caller must skip drawing it */
    public static boolean begin(HudElement part) {
        GuiGraphics g = frameGraphics;
        return beginWith(g == null ? null : stackOf(g), g == null ? 0 : g.guiWidth(), g == null ? 0 : g.guiHeight(), part);
    }

    /**
     * Opens a frame for a vanilla element around code of another mod that draws it (for example
     * Overflowing Bars' bars). It never cancels: when the element is hidden the drawing is
     * collapsed to nothing instead, so the mod's own layout bookkeeping still runs.
     */
    public static void beginPart(HudPart part) {
        GuiGraphics g = frameGraphics;
        beginWith(g == null ? null : stackOf(g), g == null ? 0 : g.guiWidth(), g == null ? 0 : g.guiHeight(), part, true);
    }

    static boolean beginWith(Stack st, int guiW, int guiH, HudElement part) {
        return beginWith(st, guiW, guiH, part, false);
    }

    /** True while a hook has this element's frame open (its transform is in effect). */
    public static boolean isOpen(HudElement part) {
        return isOpen(part.id());
    }

    private static boolean isOpen(String id) {
        for (int i = 0; i < depth; i++) {
            if (id.equals(FRAMES[i].id)) {
                return true;
            }
        }
        return false;
    }

    static boolean beginWith(Stack st, int guiW, int guiH, HudElement part, boolean collapseWhenHidden) {
        if (depth >= FRAMES.length) {
            releaseAll();
        }
        Frame f = new Frame();
        f.st = st;
        f.id = part.id();
        if (st == null || isOpen(f.id)) {
            // Outside the HUD frame, or the same element is already open (a mod hooked twice):
            // nothing more to transform, but keep begin / end balanced.
            f.id = null;
            FRAMES[depth++] = f;
            return true;
        }
        HudTransform.compute(part, guiW, guiH);
        if (!HudTransform.visible) {
            if (!collapseWhenHidden) {
                return false;
            }
            st.push();
            f.pushed = true;
            st.translate(HudTransform.cx, HudTransform.cy);
            st.scale(0.0001f);
            st.translate(-HudTransform.cx, -HudTransform.cy);
            FRAMES[depth++] = f;
            return true;
        }
        HudCapture.begin(part, guiW, guiH, HudAnimationState.progress(part) >= 1f);
        f.captured = true;
        if (!HudTransform.isIdentity()) {
            st.push();
            f.pushed = true;
            st.translate(HudTransform.tx + HudTransform.cx, HudTransform.ty + HudTransform.cy);
            st.scale(HudTransform.scale);
            st.translate(-HudTransform.cx, -HudTransform.cy);
            if (HudTransform.alpha < 1.0f) {
                st.setAlpha(HudTransform.alpha);
                f.tinted = true;
            }
        }
        FRAMES[depth++] = f;
        return true;
    }

    public static void end() {
        if (depth == 0) {
            return;
        }
        Frame f = FRAMES[--depth];
        FRAMES[depth] = null;
        try {
            if (f.tinted) {
                f.st.setAlpha(1.0f);
            }
        } finally {
            try {
                if (f.pushed) {
                    f.st.pop();
                }
            } finally {
                if (f.captured) {
                    HudCapture.end();
                }
            }
        }
    }

    /** Safety net at the end of every HUD frame: nothing a hook started may outlive the frame. */
    public static void releaseAll() {
        while (depth > 0) {
            end();
        }
    }

    static int openFrames() {
        return depth;
    }

    /** The few matrix / colour operations the transform needs (lets the logic be unit-tested). */
    interface Stack {
        void push();

        void pop();

        void translate(float x, float y);

        void scale(float s);

        void setAlpha(float a);
    }

    /** Draws one vanilla element with its transform; {@code body} is the original vanilla call. */
    public static void run(GuiGraphics g, HudElement part, Runnable body) {
        runWith(stackOf(g), g.guiWidth(), g.guiHeight(), part, body);
    }

    private static Stack stackOf(GuiGraphics g) {
        return new Stack() {
            @Override
            public void push() {
                g.pose().pushPose();
            }

            @Override
            public void pop() {
                g.pose().popPose();
            }

            @Override
            public void translate(float x, float y) {
                g.pose().translate(x, y, 0.0f);
            }

            @Override
            public void scale(float s) {
                g.pose().scale(s, s, 1.0f);
            }

            @Override
            public void setAlpha(float a) {
                // The fade is multiplied into EVERY shader colour call while it is set (see
                // RenderSystemFadeMixin), so code that resets the colour inside a faded element
                // (vanilla and other mods do) can no longer make the element jump back to opaque.
                HudAnimationState.fadeAlpha = a;
                if (a < 1.0f) {
                    RenderSystem.enableBlend();
                }
                RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
            }
        };
    }

    static void runWith(Stack st, int guiW, int guiH, HudElement part, Runnable body) {
        HudTransform.compute(part, guiW, guiH);
        if (!HudTransform.visible) {
            return;
        }
        HudCapture.begin(part, guiW, guiH, HudAnimationState.progress(part) >= 1f);
        try {
            runTransformed(st, body);
        } finally {
            HudCapture.end();
        }
    }

    private static void runTransformed(Stack st, Runnable body) {
        if (HudTransform.isIdentity()) {
            body.run();
            return;
        }
        float cx = HudTransform.cx;
        float cy = HudTransform.cy;
        float tx = HudTransform.tx;
        float ty = HudTransform.ty;
        float scale = HudTransform.scale;
        float alpha = HudTransform.alpha;
        st.push();
        boolean tinted = false;
        try {
            st.translate(tx + cx, ty + cy);
            st.scale(scale);
            st.translate(-cx, -cy);
            if (alpha < 1.0f) {
                st.setAlpha(alpha);
                tinted = true;
            }
            body.run();
        } finally {
            if (tinted) {
                st.setAlpha(1.0f);
            }
            st.pop();
        }
    }

    /** Called at the start of every mod screen: no HUD tint may leak into the menus. */
    public static void resetColor() {
        HudAnimationState.fadeAlpha = 1.0f;
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
    }
}
