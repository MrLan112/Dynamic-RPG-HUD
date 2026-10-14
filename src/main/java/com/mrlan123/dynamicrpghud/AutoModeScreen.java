package com.mrlan123.dynamicrpghud;

import net.minecraft.client.input.MouseButtonEvent;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Automatic Mode settings: how long the HUD stays after the last event and which events
 * make it appear. The list of events scrolls with the mouse wheel.
 */
public class AutoModeScreen extends Screen {

    private final Screen parent;
    private final List<FlatButton> rows = new ArrayList<>();
    private final List<RevealTrigger> rowTriggers = new ArrayList<>();
    private FlatSlider delaySlider;

    private int bw;
    private int x;
    private int titleY;
    private int delayY;
    private int listTop;
    private int listBottom;
    private int contentH;
    private int scroll;
    private int bottomY;
    private int rowH;
    private int bottomH;

    public AutoModeScreen(Screen parent) {
        super(Component.translatable("dynamicrpghud.auto.title"));
        this.parent = parent;
    }

    private static float seconds(int ticks) {
        return ticks / 20f;
    }

    private Component delayLabel() {
        return Component.translatable("dynamicrpghud.auto.delay", String.format(java.util.Locale.ROOT, "%.1f", seconds(HudConfig.delayTicks())));
    }

    @Override
    protected void init() {
        this.rows.clear();
        this.rowTriggers.clear();
        this.bw = Math.min(300, this.width - 30);
        this.x = (this.width - this.bw) / 2;
        int inner = this.bw - 24;
        int top = 6;
        this.titleY = top;

        int y = top + 16;
        y = y + FlatButton.lines(this.font, Component.translatable("dynamicrpghud.auto.hint"), this.width - 20) * 10 + 8;
        this.delayY = y;
        int delayLines = FlatButton.lines(this.font, this.delayLabel(), this.bw);
        y += delayLines * 10 + 4;
        this.delaySlider = new FlatSlider(this.x, y, this.bw, 14, 0.5, 30.0, seconds(HudConfig.delayTicks()), v -> {
            HudConfig.setDelayTicks(Math.round((float) (Math.round(v * 2.0) / 2.0) * 20f));
        });
        this.addRenderableWidget(this.delaySlider);
        y += 14 + 8;
        this.sectionY = y;
        y += FlatButton.lines(this.font, Component.translatable("dynamicrpghud.auto.when"), this.bw) * 10 + 4;

        int half = (this.bw - 12) / 3;
        this.bottomH = 24;
        for (String key : new String[]{"dynamicrpghud.auto.all_on", "dynamicrpghud.auto.all_off", "dynamicrpghud.auto.defaults", "dynamicrpghud.screen.back"}) {
            this.bottomH = Math.max(this.bottomH, FlatButton.heightFor(this.font, Component.translatable(key), null, half, false, 24));
        }
        this.bottomY = this.height - this.bottomH - 6;
        this.listTop = y;
        this.listBottom = this.bottomY - 8 - this.bottomH - 4;

        int w = this.bw - 4;
        this.rowH = 22;
        for (RevealTrigger t : RevealTrigger.values()) {
            this.rowH = Math.max(this.rowH, FlatButton.heightFor(this.font, Component.translatable(t.nameKey()), null, w - 24, false, 22));
        }
        int ry = 0;
        for (RevealTrigger t : RevealTrigger.values()) {
            FlatButton b = new FlatButton(this.x, 0, w, this.rowH, Component.translatable(t.nameKey()), btn -> {
                HudConfig.setRevealOn(t, !HudConfig.revealOn(t));
                this.syncRows();
            });
            b.box(HudConfig.revealOn(t) ? FlatButton.BOX_CHECK : FlatButton.BOX_CROSS);
            this.rows.add(b);
            this.rowTriggers.add(t);
            ry += this.rowH + 3;
        }
        this.contentH = ry;
        this.clampScroll();

        int by = this.bottomY - this.bottomH - 4 - 0;
        int third = (this.bw - 12) / 3;
        this.addRenderableWidget(new FlatButton(this.x, this.bottomY - this.bottomH - 4, third, this.bottomH,
                Component.translatable("dynamicrpghud.auto.all_on"), b -> this.setAll(true)));
        this.addRenderableWidget(new FlatButton(this.x + third + 6, this.bottomY - this.bottomH - 4, third, this.bottomH,
                Component.translatable("dynamicrpghud.auto.all_off"), b -> this.setAll(false)));
        this.addRenderableWidget(new FlatButton(this.x + 2 * (third + 6), this.bottomY - this.bottomH - 4, third, this.bottomH,
                Component.translatable("dynamicrpghud.auto.defaults"), b -> {
            HudConfig.resetAutoMode();
            this.clearWidgets();
            this.init();
        }));
        this.addRenderableWidget(new FlatButton(this.x, this.bottomY, this.bw, this.bottomH,
                Component.translatable("dynamicrpghud.screen.back"), b -> this.onClose()).accent());
        this.syncRows();
    }

    private int sectionY;

    private void setAll(boolean on) {
        for (RevealTrigger t : RevealTrigger.values()) {
            HudConfig.setRevealOn(t, on);
        }
        this.syncRows();
    }

    private void syncRows() {
        // The check boxes are redrawn from the config, so rebuild only their state.
        for (int i = 0; i < this.rows.size(); i++) {
            this.rows.set(i, rebuild(this.rows.get(i), this.rowTriggers.get(i)));
        }
    }

    private FlatButton rebuild(FlatButton old, RevealTrigger t) {
        FlatButton b = new FlatButton(old.getX(), old.getY(), old.getWidth(), old.getHeight(), Component.translatable(t.nameKey()), btn -> {
            HudConfig.setRevealOn(t, !HudConfig.revealOn(t));
            this.syncRows();
        });
        b.box(HudConfig.revealOn(t) ? FlatButton.BOX_CHECK : FlatButton.BOX_CROSS);
        return b;
    }

    private int viewH() {
        return Math.max(20, this.listBottom - this.listTop);
    }

    private void clampScroll() {
        int max = Math.max(0, this.contentH - viewH());
        this.scroll = Math.max(0, Math.min(max, this.scroll));
    }

    private boolean inList(double mx, double my) {
        return mx >= this.x && mx < this.x + this.bw && my >= this.listTop && my < this.listBottom;
    }

    @Override
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        HudPose.resetColor();
        Theme.dim(g, this.width, this.height);
        g.drawCenteredString(this.font, this.title, this.width / 2, this.titleY + 4, Theme.TEXT);
        FlatButton.drawWrappedCentered(g, this.font, Component.translatable("dynamicrpghud.auto.hint"),
                this.width / 2, this.titleY + 17, this.width - 20, Theme.TEXT_MUTED);
        FlatButton.drawWrapped(g, this.font, this.delayLabel(), this.x, this.delayY, this.bw, Theme.TEXT);
        FlatButton.drawWrapped(g, this.font, Component.translatable("dynamicrpghud.auto.when"), this.x, this.sectionY, this.bw, Theme.ACCENT);

        super.render(g, mouseX, mouseY, partialTick);

        g.enableScissor(this.x - 2, this.listTop, this.x + this.bw + 2, this.listBottom);
        int y = this.listTop - this.scroll;
        boolean over = inList(mouseX, mouseY);
        for (FlatButton b : this.rows) {
            b.setX(this.x);
            b.setY(y);
            b.render(g, over ? mouseX : -1000, over ? mouseY : -1000, partialTick);
            y += this.rowH + 3;
        }
        g.disableScissor();

        if (this.contentH > viewH()) {
            int trackH = viewH();
            int knobH = Math.max(14, trackH * trackH / this.contentH);
            int maxScroll = this.contentH - trackH;
            int knobY = this.listTop + (maxScroll == 0 ? 0 : (trackH - knobH) * this.scroll / maxScroll);
            g.fill(this.x + this.bw + 3, this.listTop, this.x + this.bw + 5, this.listBottom, Theme.BORDER);
            g.fill(this.x + this.bw + 3, knobY, this.x + this.bw + 5, knobY + knobH, Theme.ACCENT);
        }
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        double mx = event.x();
        double my = event.y();
        if (inList(mx, my)) {
            for (FlatButton b : this.rows) {
                if (my >= b.getY() && my < b.getY() + b.getHeight() && b.mouseClicked(event, doubleClick)) {
                    return true;
                }
            }
            return true;
        }
        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double scrollX, double scrollY) {
        if (this.contentH > viewH()) {
            this.scroll -= (int) Math.signum(scrollY) * 24;
            this.clampScroll();
            return true;
        }
        return super.mouseScrolled(mx, my, scrollX, scrollY);
    }

    @Override
    public void onClose() {
        HudConfig.save();
        this.minecraft.setScreen(this.parent);
    }
}
