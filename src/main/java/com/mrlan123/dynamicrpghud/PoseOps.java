package com.mrlan123.dynamicrpghud;

import net.minecraft.client.gui.GuiGraphics;

/** Matrix stack helpers (Matrix3x2fStack, 1.21.6 and newer). */
final class PoseOps {
    private PoseOps() {
    }

    static void push(GuiGraphics g) {
        g.pose().pushMatrix();
    }

    static void pop(GuiGraphics g) {
        g.pose().popMatrix();
    }

    static void translate(GuiGraphics g, float x, float y) {
        g.pose().translate(x, y);
    }

    static void scale(GuiGraphics g, float s) {
        g.pose().scale(s, s);
    }
}
