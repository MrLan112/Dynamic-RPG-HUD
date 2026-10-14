package com.mrlan123.dynamicrpghud;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

import java.util.List;

/**
 * Flat, modern button used by all screens of the mod. Optional extras: a secondary line of
 * text, a radio dot, or a cross / check box on the left.
 */
public class FlatButton extends Button {

    public static final int NO_BOX = 0;
    public static final int BOX_CROSS = 1;
    public static final int BOX_CHECK = 2;

    private boolean accent;
    private Component subtitle;
    private boolean radio;
    private boolean selected;
    private int box = NO_BOX;

    public FlatButton(int x, int y, int w, int h, Component message, Button.OnPress onPress) {
        super(x, y, w, h, message, onPress, DEFAULT_NARRATION);
    }

    public FlatButton accent() {
        this.accent = true;
        return this;
    }

    public FlatButton subtitle(Component subtitle) {
        this.subtitle = subtitle;
        return this;
    }

    public FlatButton radio(boolean selected) {
        this.radio = true;
        this.selected = selected;
        return this;
    }

    public FlatButton box(int state) {
        this.box = state;
        return this;
    }

    @Override
    protected void renderContents(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        int x = this.getX();
        int y = this.getY();
        int w = this.getWidth();
        int h = this.getHeight();
        boolean hover = this.active && this.isHoveredOrFocused();

        int fill;
        if (!this.active) {
            fill = Theme.BTN_DISABLED;
        } else if (this.accent) {
            fill = hover ? Theme.ACCENT_HOVER : Theme.ACCENT_DARK;
        } else {
            fill = hover ? Theme.BTN_HOVER : Theme.BTN;
        }
        int border = (hover || (this.radio && this.selected)) ? Theme.ACCENT : Theme.BORDER;
        g.fill(x, y, x + w, y + h, border);
        g.fill(x + 1, y + 1, x + w - 1, y + h - 1, fill);

        Font font = Minecraft.getInstance().font;
        int textColor = this.active ? Theme.TEXT : Theme.TEXT_MUTED;
        int left = x + 8;

        if (this.box != NO_BOX) {
            int bs = Math.min(h - 6, 16);
            int by = y + (h - bs) / 2;
            Theme.field(g, left, by, bs, bs);
            Theme.glyph(g, this.box == BOX_CHECK ? Theme.CHECK : Theme.CROSS,
                    this.box == BOX_CHECK ? Theme.GOOD : Theme.BAD, left, by, bs, bs);
            left += bs + 8;
        } else if (this.radio) {
            int ry = y + (h - 10) / 2;
            Theme.field(g, left, ry, 10, 10);
            if (this.selected) {
                g.fill(left + 3, ry + 3, left + 7, ry + 7, Theme.ACCENT);
            }
            left += 18;
        }

        boolean sideIcon = this.box != NO_BOX || this.radio;
        int textW = Math.max(8, x + w - 6 - left);
        List<FormattedCharSequence> titleLines = font.split(this.getMessage(), sideIcon || this.subtitle != null ? textW : Math.max(8, w - 12));
        List<FormattedCharSequence> subLines = this.subtitle != null ? font.split(this.subtitle, textW) : List.of();
        int total = titleLines.size() * 10 + (subLines.isEmpty() ? 0 : 1 + subLines.size() * 10);
        int ty = total + 6 <= h ? y + (h - total) / 2 + 1 : y + 3;
        int bottom = y + h - 2;
        for (FormattedCharSequence line : titleLines) {
            if (ty + 9 > bottom) {
                break;
            }
            if (sideIcon || this.subtitle != null) {
                g.drawString(font, line, left, ty, textColor);
            } else {
                g.drawString(font, line, x + (w - font.width(line)) / 2, ty, textColor);
            }
            ty += 10;
        }
        if (!subLines.isEmpty()) {
            ty += 1;
            for (FormattedCharSequence line : subLines) {
                if (ty + 9 > bottom) {
                    break;
                }
                g.drawString(font, line, left, ty, Theme.TEXT_MUTED);
                ty += 10;
            }
        }
    }

    /** The label cut to {@code maxW} pixels with an ellipsis, so it never leaves the button. */
    public static FormattedCharSequence fit(Font font, Component text, int maxW) {
        if (font.width(text) <= maxW) {
            return text.getVisualOrderText();
        }
        int dots = font.width("...");
        return Component.literal(font.plainSubstrByWidth(text.getString(), Math.max(0, maxW - dots)) + "...")
                .getVisualOrderText();
    }

    /** Height a button with a wrapped subtitle needs (title line + subtitle lines + padding). */
    public static int heightFor(Font font, Component subtitle, int buttonWidth, boolean hasIcon, int minHeight) {
        return heightFor(font, Component.empty(), subtitle, buttonWidth, hasIcon, minHeight);
    }

    /**
     * Height of a button whose title and optional subtitle both wrap onto new lines instead of
     * being cut off. {@code hasIcon} is true for radio dots and check boxes.
     */
    public static int heightFor(Font font, Component title, Component subtitle, int buttonWidth, boolean hasIcon, int minHeight) {
        int iconW = hasIcon ? 18 : 0;
        int textW = Math.max(8, buttonWidth - 14 - iconW);
        int titleLines = Math.max(1, font.split(title, subtitle != null && subtitle != title ? textW : Math.max(8, buttonWidth - 12 - iconW)).size());
        boolean hasSub = subtitle != null && !subtitle.getString().isEmpty();
        int subLines = hasSub ? Math.max(1, font.split(subtitle, textW).size()) : 0;
        int total = titleLines * 10 + (hasSub ? 1 + subLines * 10 : 0);
        return Math.max(minHeight, total + 12);
    }

    /** Wraps a plain label into lines no wider than {@code maxW}. */
    public static List<FormattedCharSequence> wrap(Font font, Component text, int maxW) {
        return font.split(text, Math.max(8, maxW));
    }

    /** Draws a wrapped, centred paragraph; returns the y just below it. */
    public static int drawWrappedCentered(GuiGraphics g, Font font, Component text, int cx, int y, int maxW, int color) {
        for (FormattedCharSequence line : wrap(font, text, maxW)) {
            g.drawString(font, line, cx - font.width(line) / 2, y, color);
            y += 10;
        }
        return y;
    }

    /** Draws a wrapped paragraph at the left edge; returns the y just below it. */
    public static int drawWrapped(GuiGraphics g, Font font, Component text, int x, int y, int maxW, int color) {
        for (FormattedCharSequence line : wrap(font, text, maxW)) {
            g.drawString(font, line, x, y, color);
            y += 10;
        }
        return y;
    }

    /** Number of lines a wrapped paragraph takes. */
    public static int lines(Font font, Component text, int maxW) {
        return Math.max(1, wrap(font, text, maxW).size());
    }
}
