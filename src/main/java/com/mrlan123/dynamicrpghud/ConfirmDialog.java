package com.mrlan123.dynamicrpghud;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

import java.util.List;

/** Small "are you sure?" pop-up used before any reset. */
public class ConfirmDialog extends Screen {

    private final Screen parent;
    private final Component message;
    private final Runnable onConfirm;

    private int panelX;
    private int panelY;
    private int panelW;
    private int panelH;

    public ConfirmDialog(Screen parent, Component title, Component message, Runnable onConfirm) {
        super(title);
        this.parent = parent;
        this.message = message;
        this.onConfirm = onConfirm;
    }

    @Override
    protected void init() {
        this.panelW = Math.min(280, this.width - 30);
        List<FormattedCharSequence> lines = this.font.split(this.message, this.panelW - 24);
        this.panelH = 16 + 14 + lines.size() * 10 + 14 + 24 + 12;
        this.panelX = (this.width - this.panelW) / 2;
        this.panelY = (this.height - this.panelH) / 2;

        int by = this.panelY + this.panelH - 12 - 22;
        int half = (this.panelW - 24 - 6) / 2;
        this.addRenderableWidget(new FlatButton(this.panelX + 12, by, half, 22,
                Component.translatable("dynamicrpghud.confirm.cancel"), b -> this.onClose()));
        this.addRenderableWidget(new FlatButton(this.panelX + 12 + half + 6, by, half, 22,
                Component.translatable("dynamicrpghud.confirm.yes"), b -> {
            this.onConfirm.run();
            this.minecraft.setScreen(this.parent);
        }).accent());
    }

    @Override
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        HudPose.resetColor();
        Theme.dim(g, this.width, this.height);
        Theme.panel(g, this.panelX, this.panelY, this.panelW, this.panelH);
        g.drawCenteredString(this.font, this.title, this.width / 2, this.panelY + 12, Theme.TEXT);
        List<FormattedCharSequence> lines = this.font.split(this.message, this.panelW - 24);
        int y = this.panelY + 28;
        for (FormattedCharSequence line : lines) {
            g.drawString(this.font, line, (this.width - this.font.width(line)) / 2, y, Theme.TEXT_MUTED);
            y += 10;
        }
        super.render(g, mouseX, mouseY, partialTick);
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(this.parent);
    }
}
