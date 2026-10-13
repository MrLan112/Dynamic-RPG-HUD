package com.mrlan123.dynamicrpghud;

import net.minecraft.client.gui.GuiGraphics;

/** Matrix stack helpers (PoseStack, up to 1.21.5). */
final class PoseOps {
    private PoseOps() {
    }

    static void push(GuiGraphics g) {
        g.pose().pushPose();
    }

    static void pop(GuiGraphics g) {
        g.pose().popPose();
    }

    static void translate(GuiGraphics g, float x, float y) {
        g.pose().translate(x, y, 0.0f);
    }

    static void scale(GuiGraphics g, float s) {
        g.pose().scale(s, s, 1.0f);
    }
}
