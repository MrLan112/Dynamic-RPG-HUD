package com.mrlan123.dynamicrpghud;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

/**
 * Draws a HUD element with its real vanilla textures (used by the editor in the main menu,
 * where there is no running game to show the real HUD). Applies the element's saved
 * position and scale, so it matches the box the user is editing.
 */
public final class EditorPreview {

    private EditorPreview() {
    }

    public static void draw(GuiGraphics g, HudPart part, int w, int h, Font font) {
        int[] r = HudLayout.rect(part, w, h);
        float cx = r[0] + r[2] / 2f;
        float cy = r[1] + r[3] / 2f;

        PoseOps.push(g);
        PoseOps.translate(g, HudConfig.offsetX(part) + cx, HudConfig.offsetY(part) + cy);
        PoseOps.scale(g, HudConfig.scale(part));
        PoseOps.translate(g, -cx, -cy);

        switch (part) {
            case ARMOR:
                for (int i = 0; i < 10; i++) {
                    Sprites.draw(g, "hud/armor_full", r[0] + i * 8, r[1], 9, 9);
                }
                break;
            case HEALTH:
                for (int i = 0; i < 10; i++) {
                    Sprites.draw(g, "hud/heart/container", r[0] + i * 8, r[1], 9, 9);
                    Sprites.draw(g, "hud/heart/full", r[0] + i * 8, r[1], 9, 9);
                }
                break;
            case HUNGER:
                for (int i = 0; i < 10; i++) {
                    Sprites.draw(g, "hud/food_empty", r[0] + i * 8, r[1], 9, 9);
                    Sprites.draw(g, "hud/food_full", r[0] + i * 8, r[1], 9, 9);
                }
                break;
            case AIR:
                for (int i = 0; i < 10; i++) {
                    Sprites.draw(g, "hud/air", r[0] + i * 8, r[1], 9, 9);
                }
                break;
            case HOTBAR:
                Sprites.draw(g, "hud/hotbar", r[0], r[1], 182, 22);
                Sprites.draw(g, "hud/hotbar_selection", r[0] - 1, r[1] - 1, 24, 23);
                break;
            case XP_BAR:
                Sprites.draw(g, "hud/experience_bar_background", r[0], r[1], 182, 5);
                Sprites.draw(g, "hud/experience_bar_progress", r[0], r[1], 182, 5);
                break;
            case CROSSHAIR:
                Sprites.draw(g, "hud/crosshair", r[0], r[1], 15, 15);
                break;
            case JUMP_BAR:
                Sprites.draw(g, "hud/jump_bar_background", r[0], r[1], 182, 5);
                Sprites.draw(g, "hud/jump_bar_progress", r[0], r[1], 182, 5);
                break;
            case VEHICLE_HEALTH:
                for (int i = 0; i < 10; i++) {
                    Sprites.draw(g, "hud/heart/vehicle_container", r[0] + i * 8, r[1], 9, 9);
                    Sprites.draw(g, "hud/heart/vehicle_full", r[0] + i * 8, r[1], 9, 9);
                }
                break;
            case EFFECTS:
                for (int i = 0; i < 3; i++) {
                    Sprites.draw(g, "hud/effect_background", r[0] + 50 - i * 25, r[1], 24, 24);
                }
                break;
            case XP_LEVEL:
            default:
                String text = "30";
                int x = r[0] + r[2] / 2 - font.width(text) / 2;
                int y = r[1] + 1;
                g.drawString(font, text, x + 1, y, 0xFF000000);
                g.drawString(font, text, x - 1, y, 0xFF000000);
                g.drawString(font, text, x, y + 1, 0xFF000000);
                g.drawString(font, text, x, y - 1, 0xFF000000);
                g.drawString(font, text, x, y, 0xFF80FF20);
                break;
        }

        PoseOps.pop(g);
    }
}
