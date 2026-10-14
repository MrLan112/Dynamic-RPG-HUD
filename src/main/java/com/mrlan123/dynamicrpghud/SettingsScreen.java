package com.mrlan123.dynamicrpghud;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/** Main settings screen of Dynamic RPG HUD (opened from Mod Menu or the "open settings" key). */
public class SettingsScreen extends Screen {

    private final Screen parent;
    private int titleY;
    private int headerH;
    private int footerY;

    public SettingsScreen(Screen parent) {
        super(Component.translatable("dynamicrpghud.screen.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int bw = Math.min(260, this.width - 40);
        int x = (this.width - bw) / 2;
        int half = (bw - 6) / 2;
        Component configure = Component.translatable("dynamicrpghud.screen.configure");
        Component animTitle = Component.translatable("dynamicrpghud.screen.animation");
        Component animValue = Component.translatable(HudConfig.animation().nameKey());
        Component trigTitle = Component.translatable("dynamicrpghud.screen.trigger");
        Component trigValue = Component.translatable(HudConfig.trigger().nameKey());
        Component autoTitle = Component.translatable("dynamicrpghud.screen.auto");
        Component autoDesc = Component.translatable("dynamicrpghud.screen.auto.desc");
        Component resetTitle = Component.translatable("dynamicrpghud.screen.reset");
        Component doneTitle = Component.translatable("dynamicrpghud.screen.done");

        int cfgH = FlatButton.heightFor(this.font, configure, null, bw, false, 24);
        int animH = FlatButton.heightFor(this.font, animTitle, animValue, bw, false, 30);
        int trigH = FlatButton.heightFor(this.font, trigTitle, trigValue, bw, false, 30);
        int autoH = FlatButton.heightFor(this.font, autoTitle, autoDesc, bw, false, 30);
        int lastH = Math.max(FlatButton.heightFor(this.font, resetTitle, null, half, false, 24),
                FlatButton.heightFor(this.font, doneTitle, null, half, false, 24));
        int headerH = 40 + (FlatButton.lines(this.font, Component.translatable("dynamicrpghud.screen.subtitle"), this.width - 20) - 1) * 10;
        int footerLines = FlatButton.lines(this.font, Component.translatable("dynamicrpghud.screen.footer"), this.width - 20);
        int totalH = headerH + cfgH + 6 + animH + 6 + trigH + 6 + autoH + 10 + lastH + footerLines * 10 + 6;
        int top = Math.max(6, (this.height - totalH) / 2);
        this.titleY = top;
        this.headerH = headerH;
        int y = top + headerH;

        this.addRenderableWidget(new FlatButton(x, y, bw, cfgH, configure,
                b -> this.minecraft.setScreen(new ElementEditorScreen(this))).accent());
        y += cfgH + 6;

        this.addRenderableWidget(new FlatButton(x, y, bw, animH, animTitle,
                b -> this.minecraft.setScreen(animationScreen())).subtitle(animValue));
        y += animH + 6;

        this.addRenderableWidget(new FlatButton(x, y, bw, trigH, trigTitle,
                b -> this.minecraft.setScreen(triggerScreen())).subtitle(trigValue));
        y += trigH + 6;

        this.addRenderableWidget(new FlatButton(x, y, bw, autoH, autoTitle,
                b -> this.minecraft.setScreen(new AutoModeScreen(this))).subtitle(autoDesc));
        y += autoH + 10;

        this.addRenderableWidget(new FlatButton(x, y, half, lastH, resetTitle, b ->
                this.minecraft.setScreen(new ConfirmDialog(this,
                        Component.translatable("dynamicrpghud.confirm.everything.title"),
                        Component.translatable("dynamicrpghud.confirm.everything.message"),
                        () -> {
                            HudConfig.resetAll();
                            HudConfig.save();
                        }))));
        this.addRenderableWidget(new FlatButton(x + half + 6, y, half, lastH, doneTitle, b -> this.onClose()).accent());
        this.footerY = y + lastH + 8;
    }

    private Screen animationScreen() {
        AnimationMode[] modes = AnimationMode.values();
        ChoiceScreen.Option[] options = new ChoiceScreen.Option[modes.length];
        for (int i = 0; i < modes.length; i++) {
            options[i] = new ChoiceScreen.Option(Component.translatable(modes[i].nameKey()),
                    Component.translatable(modes[i].descKey()));
        }
        return new ChoiceScreen(this, Component.translatable("dynamicrpghud.screen.animation"),
                Component.translatable("dynamicrpghud.screen.animation.hint"),
                options, HudConfig.animation().ordinal(), i -> {
            HudConfig.setAnimation(modes[i]);
            HudConfig.save();
        });
    }

    private Screen triggerScreen() {
        TriggerMode[] modes = TriggerMode.values();
        ChoiceScreen.Option[] options = new ChoiceScreen.Option[modes.length];
        for (int i = 0; i < modes.length; i++) {
            options[i] = new ChoiceScreen.Option(Component.translatable(modes[i].nameKey()),
                    Component.translatable(modes[i].descKey()));
        }
        return new ChoiceScreen(this, Component.translatable("dynamicrpghud.screen.trigger"),
                Component.translatable("dynamicrpghud.screen.trigger.hint"),
                options, HudConfig.trigger().ordinal(), i -> {
            HudConfig.setTrigger(modes[i]);
            HudConfig.save();
        });
    }

    @Override
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        // Drawn in render() instead, so it works the same on every Minecraft version.
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        HudPose.resetColor();
        Theme.dim(graphics, this.width, this.height);

        graphics.drawCenteredString(this.font, this.title, this.width / 2, this.titleY + 4, Theme.TEXT);
        int below = FlatButton.drawWrappedCentered(graphics, this.font, Component.translatable("dynamicrpghud.screen.subtitle"),
                this.width / 2, this.titleY + 17, this.width - 20, Theme.TEXT_MUTED);
        int lineW = 60;
        graphics.fill(this.width / 2 - lineW, below + 2, this.width / 2 + lineW, below + 4, Theme.ACCENT);

        super.render(graphics, mouseX, mouseY, partialTick);

        FlatButton.drawWrappedCentered(graphics, this.font, Component.translatable("dynamicrpghud.screen.footer"),
                this.width / 2, this.footerY, this.width - 20, Theme.TEXT_MUTED);
    }

    @Override
    public void onClose() {
        HudConfig.save();
        this.minecraft.setScreen(this.parent);
    }
}
