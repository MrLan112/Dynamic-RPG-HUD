package com.mrlan123.dynamicrpghud;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

/** Colours and tiny drawing helpers shared by every screen of the mod. */
public final class Theme {

    public static final int ACCENT = 0xFF4DA3FF;
    public static final int ACCENT_HOVER = 0xFF6DB6FF;
    public static final int ACCENT_DARK = 0xFF2F6FB5;
    public static final int PANEL = 0xF0151821;
    public static final int PANEL_HEADER = 0xFF1C2030;
    public static final int BORDER = 0xFF3A4054;
    public static final int BTN = 0xFF242938;
    public static final int BTN_HOVER = 0xFF30374C;
    public static final int BTN_DISABLED = 0xFF1A1D28;
    public static final int FIELD = 0xFF0E1018;
    public static final int TEXT = 0xFFE8ECF4;
    public static final int TEXT_MUTED = 0xFF8D96AA;
    public static final int GOOD = 0xFF4AE28A;
    public static final int BAD = 0xFFE2564A;

    public static final String[] CROSS = {
            "X.....X",
            ".X...X.",
            "..X.X..",
            "...X...",
            "..X.X..",
            ".X...X.",
            "X.....X"
    };
    public static final String[] CHECK = {
            "......X",
            ".....XX",
            "X...XX.",
            "XX.XX..",
            ".XXX...",
            "..X...."
    };

    private Theme() {
    }

    /** Semi-transparent dim over the running game (so the world and HUD stay visible), solid when no world is loaded. */
    public static void dim(GuiGraphics g, int w, int h) {
        if (Minecraft.getInstance().level != null) {
            g.fill(0, 0, w, h, 0xA0090B12);
        } else {
            g.fill(0, 0, w, h, 0xFF0E1118);
        }
    }

    public static void outline(GuiGraphics g, int x1, int y1, int x2, int y2, int color) {
        g.fill(x1, y1, x2, y1 + 1, color);
        g.fill(x1, y2 - 1, x2, y2, color);
        g.fill(x1, y1, x1 + 1, y2, color);
        g.fill(x2 - 1, y1, x2, y2, color);
    }

    /** Dark panel with a border and an optional accent line on top. */
    public static void panel(GuiGraphics g, int x, int y, int w, int h) {
        g.fill(x, y, x + w, y + h, BORDER);
        g.fill(x + 1, y + 1, x + w - 1, y + h - 1, PANEL);
        g.fill(x + 1, y + 1, x + w - 1, y + 3, ACCENT);
    }

    public static void field(GuiGraphics g, int x, int y, int w, int h) {
        g.fill(x, y, x + w, y + h, BORDER);
        g.fill(x + 1, y + 1, x + w - 1, y + h - 1, FIELD);
    }

    /** Draws a pixel-art glyph ('X' = lit pixel) centred inside a box. */
    public static void glyph(GuiGraphics g, String[] rows, int color, int boxLeft, int boxTop, int boxW, int boxH) {
        int scale = (boxW >= 18 && boxH >= 18) ? 2 : 1;
        int w = rows[0].length() * scale;
        int h = rows.length * scale;
        int left = boxLeft + (boxW - w) / 2;
        int top = boxTop + (boxH - h) / 2;
        for (int y = 0; y < rows.length; y++) {
            for (int x = 0; x < rows[y].length(); x++) {
                if (rows[y].charAt(x) == 'X') {
                    int px = left + x * scale;
                    int py = top + y * scale;
                    g.fill(px, py, px + scale, py + scale, color);
                }
            }
        }
    }
}
