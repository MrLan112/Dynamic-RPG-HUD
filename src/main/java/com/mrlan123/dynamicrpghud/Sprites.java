package com.mrlan123.dynamicrpghud;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

/** Draws a vanilla GUI sprite (the blit call differs between Minecraft versions). */
final class Sprites {
    private Sprites() {
    }

    static void draw(GuiGraphics g, String path, int x, int y, int w, int h) {
        ResourceLocation id = ResourceLocation.withDefaultNamespace(path);
        g.blitSprite(id, x, y, w, h);
    }
}
