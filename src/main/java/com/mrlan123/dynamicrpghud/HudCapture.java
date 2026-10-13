package com.mrlan123.dynamicrpghud;

import net.minecraft.client.gui.GuiGraphics;
import org.joml.Vector3f;

/**
 * Measures what another mod's HUD callback draws. While a callback runs, the draw calls of
 * {@code GuiGraphics} report their corners here; they are taken through the current matrix and
 * the mod's own transform is undone, so the result is the box where the mod draws by itself.
 * Only measured while the element is fully shown (the transform is stable then).
 */
public final class HudCapture {

    private static HudElement current;
    private static boolean armed;
    private static int guiW;
    private static int guiH;
    private static float cx, cy, tx, ty, scale;
    private static float minX, minY, maxX, maxY;
    private static boolean any;
    private static final Vector3f TMP = new Vector3f();

    private HudCapture() {
    }

    /** Called right after the element's transform has been computed. */
    static void begin(HudElement part, int w, int h, boolean fullyShown) {
        if (!(part instanceof ExternalElement) && part != HudPart.EFFECTS) {
            return;
        }
        if (current != null) {
            end();
        }
        current = part;
        armed = fullyShown && HudTransform.scale > 0.05f;
        guiW = w;
        guiH = h;
        cx = HudTransform.cx;
        cy = HudTransform.cy;
        tx = HudTransform.tx;
        ty = HudTransform.ty;
        scale = HudTransform.scale;
        minX = minY = Float.MAX_VALUE;
        maxX = maxY = -Float.MAX_VALUE;
        any = false;
    }

    static void end() {
        HudElement e = current;
        current = null;
        if (e != null && armed && any) {
            int l = (int) Math.floor(minX);
            int t = (int) Math.floor(minY);
            int w = (int) Math.ceil(maxX) - l;
            int h = (int) Math.ceil(maxY) - t;
            if (e instanceof ExternalElement) {
                ((ExternalElement) e).setRect(guiW, guiH, l, t, w, h);
            } else {
                // The status effects have no fixed box: measure it so the pivot is their real middle.
                HudGeometry.set((HudPart) e, guiW, guiH, l, t, w, h);
            }
        }
        armed = false;
    }

    public static boolean active() {
        return armed;
    }

    /** Reports a rectangle drawn in the callback's own coordinates. */
    public static void rect(GuiGraphics g, float x1, float y1, float x2, float y2) {
        if (!armed) {
            return;
        }
        point(g, x1, y1);
        point(g, x2, y2);
    }

    private static void point(GuiGraphics g, float x, float y) {
        TMP.set(x, y, 0f);
        g.pose().last().pose().transformPosition(TMP);
        addScreenPoint(TMP.x, TMP.y);
    }

    /** Adds a point given in screen coordinates; undoes this element's own transform. */
    static void addScreenPoint(float sx, float sy) {
        // Undo this element's own transform: screen = (native - c) * scale + c + t.
        float nx = (sx - cx - tx) / scale + cx;
        float ny = (sy - cy - ty) / scale + cy;
        if (nx < minX) minX = nx;
        if (nx > maxX) maxX = nx;
        if (ny < minY) minY = ny;
        if (ny > maxY) maxY = ny;
        any = true;
    }
}
