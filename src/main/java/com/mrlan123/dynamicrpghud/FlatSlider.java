package com.mrlan123.dynamicrpghud;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.network.chat.Component;

import java.util.function.DoubleConsumer;

/** Slim modern slider over a real value range. */
public class FlatSlider extends AbstractSliderButton {

    private final double min;
    private final double max;
    private final DoubleConsumer onChange;

    public FlatSlider(int x, int y, int w, int h, double min, double max, double current, DoubleConsumer onChange) {
        super(x, y, w, h, Component.empty(), normalize(current, min, max));
        this.min = min;
        this.max = max;
        this.onChange = onChange;
    }

    private static double normalize(double v, double min, double max) {
        return Math.max(0.0, Math.min(1.0, (v - min) / (max - min)));
    }

    /** Moves the knob without firing the change callback. */
    public void setActual(double v) {
        this.value = normalize(v, this.min, this.max);
    }

    @Override
    protected void updateMessage() {
        this.setMessage(Component.empty());
    }

    @Override
    protected void applyValue() {
        if (this.onChange != null) {
            this.onChange.accept(this.min + (this.max - this.min) * this.value);
        }
    }

    @Override
    public void renderWidget(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        int x = this.getX();
        int y = this.getY();
        int w = this.getWidth();
        int h = this.getHeight();
        boolean hover = this.isHoveredOrFocused();

        int trackY = y + h / 2 - 2;
        g.fill(x, trackY, x + w, trackY + 4, Theme.BORDER);
        g.fill(x + 1, trackY + 1, x + w - 1, trackY + 3, Theme.FIELD);

        int knobW = 6;
        int knobX = x + (int) Math.round(this.value * (w - knobW));
        g.fill(x + 1, trackY + 1, knobX + knobW / 2, trackY + 3, Theme.ACCENT_DARK);
        g.fill(knobX, y, knobX + knobW, y + h, hover ? Theme.ACCENT_HOVER : Theme.ACCENT);
    }
}
