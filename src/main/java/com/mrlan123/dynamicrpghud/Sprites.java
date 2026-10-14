package com.mrlan123.dynamicrpghud;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;

/** Draws a vanilla GUI sprite (the blit call differs between Minecraft versions). */
final class Sprites {
    private Sprites() {
    }

    static void draw(GuiGraphics g, String path, int x, int y, int w, int h) {
        Identifier id = Identifier.withDefaultNamespace(path);
        g.blitSprite(RenderPipelines.GUI_TEXTURED, id, x, y, w, h);
    }
}
