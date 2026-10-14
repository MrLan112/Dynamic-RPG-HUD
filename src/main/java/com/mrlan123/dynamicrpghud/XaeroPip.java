package com.mrlan123.dynamicrpghud;

import org.joml.Matrix3x2f;

/**
 * Minecraft 1.21.6+ draws Xaero's map as a picture-in-picture: the map is rendered to its own
 * texture and put on the screen later with an identity matrix, so the GUI pose does not move it
 * (only the minimap's text and frame follow). The transform of the minimap element is noted here
 * while its HUD hook runs and applied to that final blit by {@code XaeroPipMixin}.
 */
public final class XaeroPip {

    /** The only renderer that is moved: the minimap's own (Xaero's world map has another one). */
    public static final String RENDERER = "xaero.hud.minimap.render.MinimapPipRenderer";

    private static Matrix3x2f pose;
    private static int tint = -1;

    private XaeroPip() {
    }

    /** Reads the transform the HUD hook has just computed for the minimap element. */
    public static void capture() {
        float tx = HudTransform.tx;
        float ty = HudTransform.ty;
        float s = HudTransform.scale;
        float a = HudTransform.alpha;
        if (!HudTransform.visible || (tx == 0f && ty == 0f && s == 1f && a >= 1f)) {
            clear();
            return;
        }
        pose = new Matrix3x2f().translate(tx + HudTransform.cx, ty + HudTransform.cy).scale(s, s)
                .translate(-HudTransform.cx, -HudTransform.cy);
        tint = tintFor(a);
    }

    public static void clear() {
        pose = null;
        tint = -1;
    }

    /** Null when the minimap is where Xaero put it (nothing to change). */
    public static Matrix3x2f pose() {
        return pose;
    }

    /** Colour of the final blit: white, scaled by the fade (the blit uses premultiplied alpha). */
    public static int tint() {
        return tint;
    }

    static int tintFor(float alpha) {
        if (alpha >= 1f) {
            return -1;
        }
        int v = Math.max(0, Math.min(255, Math.round(alpha * 255f)));
        return (v << 24) | (v << 16) | (v << 8) | v;
    }

    public static boolean isMinimapRenderer(Object renderer) {
        return renderer != null && RENDERER.equals(renderer.getClass().getName());
    }
}
