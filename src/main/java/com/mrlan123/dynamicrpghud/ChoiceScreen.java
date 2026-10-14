package com.mrlan123.dynamicrpghud;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.function.IntConsumer;

/** A list of mutually exclusive options (radio rows with a description), used for animation and trigger. */
public class ChoiceScreen extends Screen {

    public static final class Option {
        final Component label;
        final Component description;

        public Option(Component label, Component description) {
            this.label = label;
            this.description = description;
        }
    }

    private final Screen parent;
    private final Component hint;
    private final Option[] options;
    private final IntConsumer onSelect;
    private int selected;
    private int titleY;

    public ChoiceScreen(Screen parent, Component title, Component hint, Option[] options, int selected, IntConsumer onSelect) {
        super(title);
        this.parent = parent;
        this.hint = hint;
        this.options = options;
        this.selected = selected;
        this.onSelect = onSelect;
    }

    @Override
    protected void init() {
        int bw = Math.min(300, this.width - 40);
        int x = (this.width - bw) / 2;
        int gap = 5;
        int[] rowH = new int[options.length];
        int hintLines = FlatButton.lines(this.font, this.hint, this.width - 20);
        int totalH = 36 + (hintLines - 1) * 10 + 24 + 4;
        for (int i = 0; i < options.length; i++) {
            rowH[i] = FlatButton.heightFor(this.font, options[i].label, options[i].description, bw, true, 32);
            totalH += rowH[i] + gap;
        }
        int top = Math.max(6, (this.height - totalH) / 2);
        this.titleY = top;
        int y = top + 36 + (hintLines - 1) * 10;

        for (int i = 0; i < options.length; i++) {
            final int index = i;
            this.addRenderableWidget(new FlatButton(x, y, bw, rowH[i], options[i].label, b -> {
                this.selected = index;
                this.onSelect.accept(index);
                this.clearWidgets();
                this.init();
            }).radio(i == this.selected).subtitle(options[i].description));
            y += rowH[i] + gap;
        }

        this.addRenderableWidget(new FlatButton(x, y + 4, bw, 24,
                Component.translatable("dynamicrpghud.screen.back"), b -> this.onClose()).accent());
    }

    @Override
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        HudPose.resetColor();
        Theme.dim(graphics, this.width, this.height);
        graphics.drawCenteredString(this.font, this.title, this.width / 2, this.titleY + 4, Theme.TEXT);
        int below = FlatButton.drawWrappedCentered(graphics, this.font, this.hint, this.width / 2, this.titleY + 17, this.width - 20, Theme.TEXT_MUTED);
        graphics.fill(this.width / 2 - 40, below + 1, this.width / 2 + 40, below + 3, Theme.ACCENT);
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(this.parent);
    }
}
