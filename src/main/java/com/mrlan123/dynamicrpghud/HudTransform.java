package com.mrlan123.dynamicrpghud;

/**
 * Pure math: combines the user's per-element position / scale with the
 * current hide animation into one transform. Written into static fields so
 * nothing is allocated per frame (client thread only).
 * <p>
 * The element is first scaled around the centre of its vanilla bounding box,
 * then translated by (tx, ty).
 */
public final class HudTransform {

    public static float cx;
    public static float cy;
    public static float tx;
    public static float ty;
    public static float scale;
    public static float alpha;
    /** False when the element is fully hidden and need not be drawn at all. */
    public static boolean visible;

    private HudTransform() {
    }

    /** True when the element is exactly where vanilla puts it: nothing needs to be touched. */
    public static boolean isIdentity() {
        return tx == 0f && ty == 0f && scale == 1f && alpha >= 1f;
    }

    /** Extra pixels beyond the screen edge so the element is fully out of sight when hidden. */
    private static final float MARGIN = 4f;

    /** Extra offset {dx, dy} of a slide animation; {@code away} is 1 when hidden and 0 in place. */
    static float[] slideOffset(AnimationMode mode, float away, int guiW, int guiH) {
        switch (mode) {
            case SLIDE:
                return new float[]{0f, away * (guiH + MARGIN)};
            case SLIDE_UP:
                return new float[]{0f, -away * (guiH + MARGIN)};
            case SLIDE_LEFT:
                return new float[]{-away * (guiW + MARGIN), 0f};
            case SLIDE_RIGHT:
                return new float[]{away * (guiW + MARGIN), 0f};
            default:
                return new float[]{0f, 0f};
        }
    }

    public static void compute(HudElement part, int guiW, int guiH) {
        HudAnimationState.update(part, guiW, guiH);
        float raw = HudAnimationState.progress(part);
        float e = HudAnimationState.ease(raw);
        int[] r = HudLayout.rect(part, guiW, guiH);
        // Drawn from the first moment of the appearance on and until the very end of the hiding.
        boolean drawn = raw > 0.001f;
        if (r == null) {
            // Another mod's element that has not been measured yet: draw it untouched this frame.
            cx = cy = tx = ty = 0f;
            scale = 1f;
            alpha = 1f;
            visible = drawn;
            return;
        }

        cx = r[0] + r[2] / 2f;
        cy = r[1] + r[3] / 2f;
        tx = HudConfig.offsetX(part);
        ty = HudConfig.offsetY(part);
        scale = HudConfig.scale(part);
        alpha = 1f;
        visible = drawn;

        if (raw < 1f) {
            float away = 1f - e; // 1 = fully hidden, 0 = in place
            switch (HudConfig.animationFor(part)) {
                case SLIDE:
                case SLIDE_UP:
                case SLIDE_LEFT:
                case SLIDE_RIGHT:
                    // The same distance for every element (a whole screen): each one is fully out
                    // of sight when hidden, and all of them move at the same speed.
                    float[] d = slideOffset(HudConfig.animationFor(part), away, guiW, guiH);
                    tx += d[0];
                    ty += d[1];
                    break;
                case FADE:
                    alpha = e;
                    break;
                case SHRINK:
                    // Scaled around the middle of the element itself.
                    scale *= Math.max(e, 0.0001f);
                    break;
                case INSTANT:
                default:
                    break;
            }
        }
    }
}
