package com.mrlan123.dynamicrpghud;

import net.minecraft.client.input.MouseButtonEvent;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

import java.util.ArrayList;
import java.util.List;

/**
 * Layout editor: every HUD element is shown as a box over the dimmed game. Drag a box to move
 * it; click it to open a small panel with its X / Y position, scale and "stay visible" switch.
 * X / Y are offsets from the vanilla position (0 / 0 = default), so a shared config file looks
 * the same on every screen size.
 */
public class ElementEditorScreen extends Screen {

    private static final int PANEL_W = 176;
    private int panelH = 188;
    private int yName, yPosLbl, yXY, yScLbl, yScRow, yKeep, hKeep, yAnim, hAnim, yReset, hReset;
    private int nameL, posL, scL;

    private final Screen parent;
    private final List<HudElement> parts = new ArrayList<>();

    /** The layer on show: 1 = main HUD (default), 2 = HUD that is only drawn in some situations. */
    private int layer = 1;
    private int yAir0, hAir0, yAir1, hAir1, yAir2, hAir2;

    private HudElement selected;
    private boolean dragging;
    private double grabMouseX;
    private double grabMouseY;
    private int grabOffsetX;
    private int grabOffsetY;

    // layout cache
    private int bannerBottom;
    private int panelX;
    private int panelY;

    // overlay widgets
    private EditBox xBox;
    private EditBox yBox;
    private EditBox scaleBox;
    private FlatSlider scaleSlider;
    private boolean updating;

    public ElementEditorScreen(Screen parent) {
        super(Component.translatable("dynamicrpghud.editor.title"));
        this.parent = parent;
        for (HudPart p : HudPart.values()) {
            if (p.isSupported()) {
                this.parts.add(p);
            }
        }
    }

    // =============================================================================================
    // Widgets
    // =============================================================================================

    @Override
    protected void init() {
        this.addMeasuredExternals();
        List<FormattedCharSequence> lines = this.font.split(Component.translatable("dynamicrpghud.editor.banner"), this.width - 40);
        this.bannerBottom = 12 + 14 + lines.size() * 10 + 8;

        int by = this.height - 28;
        int bw = Math.min(150, (this.width - 30) / 2);
        for (HudElement p : this.parts) {
            keepOnScreen(p);
        }
        this.addRenderableWidget(new FlatButton(10, by, bw, 20,
                Component.translatable("dynamicrpghud.editor.reset_all"), b ->
                this.minecraft.setScreen(new ConfirmDialog(this,
                        Component.translatable("dynamicrpghud.confirm.positions.title"),
                        Component.translatable("dynamicrpghud.confirm.positions.message"),
                        () -> {
                            HudConfig.resetLayout();
                            HudConfig.save();
                        }))));
        this.addRenderableWidget(new FlatButton(10 + bw + 6, by, 20, 20,
                Component.literal(Integer.toString(this.layer)), b -> {
            this.layer = this.layer == 1 ? 2 : 1;
            this.selected = null;
            this.dragging = false;
            this.refresh();
        }));
        this.addRenderableWidget(new FlatButton(this.width - 10 - bw, by, bw, 20,
                Component.translatable("dynamicrpghud.screen.done"), b -> this.onClose()).accent());

        if (this.selected != null && !this.dragging) {
            this.buildPanel();
        }
    }

    /** Other mods' HUD that has drawn something is editable too (found while the game was running). */
    private void addMeasuredExternals() {
        for (ExternalElement e : ExternalHud.measured(this.width, this.height)) {
            if (!this.parts.contains(e)) {
                this.parts.add(e);
            }
        }
    }

    private boolean inLayer(HudElement p) {
        return p.layer() == this.layer;
    }

    private void refresh() {
        this.clearWidgets();
        this.init();
    }

    /** Computes the rows of the settings panel; labels that do not fit wrap onto more lines. */
    private void layoutPanel(HudElement part) {
        int w = PANEL_W - 20;
        Component animName = HudConfig.elementAnimation(part) == null
                ? Component.translatable("dynamicrpghud.animation.default")
                : Component.translatable(HudConfig.elementAnimation(part).nameKey());
        this.nameL = Math.min(3, FlatButton.lines(this.font, part.displayName(), w));
        this.posL = FlatButton.lines(this.font, Component.translatable("dynamicrpghud.editor.position"), w);
        this.scL = FlatButton.lines(this.font, Component.translatable("dynamicrpghud.editor.scale"), w);
        this.yName = 8;
        this.yPosLbl = 8 + this.nameL * 10 + 6;
        if (part.isMovable()) {
            this.yXY = this.yPosLbl + this.posL * 10 + 4;
            this.yScLbl = this.yXY + 28;
        } else {
            this.yXY = this.yPosLbl; // the crosshair cannot be moved: no position rows
            this.yScLbl = this.yPosLbl;
        }
        this.yScRow = this.yScLbl + this.scL * 10 + 2;
        this.yKeep = this.yScRow + 28;
        this.hKeep = FlatButton.heightFor(this.font, Component.translatable("dynamicrpghud.editor.stay_visible"), null, w - 24, false, 22);
        this.yAnim = this.yKeep + this.hKeep + 4;
        this.hAnim = FlatButton.heightFor(this.font, Component.translatable("dynamicrpghud.editor.animation"), animName, w, false, 30);
        int next = this.yAnim + this.hAnim + 4;
        if (part == HudPart.AIR) {
            this.yAir0 = next;
            this.hAir0 = FlatButton.heightFor(this.font, Component.translatable("dynamicrpghud.editor.air_underwater"), null, w - 24, false, 22);
            this.yAir1 = this.yAir0 + this.hAir0 + 4;
            this.hAir1 = FlatButton.heightFor(this.font, Component.translatable("dynamicrpghud.editor.air_submerge"), null, w - 24, false, 22);
            this.yAir2 = this.yAir1 + this.hAir1 + 4;
            this.hAir2 = FlatButton.heightFor(this.font, Component.translatable("dynamicrpghud.editor.air_refill"), null, w - 24, false, 22);
            next = this.yAir2 + this.hAir2 + 4;
        }
        this.yReset = next;
        this.hReset = FlatButton.heightFor(this.font, Component.translatable("dynamicrpghud.editor.reset_element"), null, w, false, 18);
        this.panelH = this.yReset + this.hReset + 4;
    }

    private void buildPanel() {
        final HudElement part = this.selected;
        int[] pb = bounds(part);
        int bx1 = pb[0];
        int by1 = pb[1];
        int bx2 = pb[0] + pb[2];
        int by2 = pb[1] + pb[3];

        this.layoutPanel(part);
        int minY = this.bannerBottom + 4;
        int maxY = this.height - 34 - this.panelH;
        if (maxY < minY) {
            maxY = minY;
        }
        int px;
        int py = clamp((by1 + by2) / 2 - this.panelH / 2, minY, maxY);
        if (bx2 + 8 + PANEL_W <= this.width - 4) {
            px = bx2 + 8;
        } else if (bx1 - 8 - PANEL_W >= 4) {
            px = bx1 - 8 - PANEL_W;
        } else {
            px = clamp((bx1 + bx2) / 2 - PANEL_W / 2, 4, this.width - PANEL_W - 4);
            py = clamp(by1 - this.panelH - 10, minY, maxY);
        }
        this.panelX = px;
        this.panelY = py;

        // Position (not for the crosshair)
        if (part.isMovable()) {
        this.xBox = this.numberBox(px + 20, py + this.yXY, 62, Integer.toString(HudConfig.offsetX(part)), "-?\\d{0,5}");
        this.xBox.setResponder(s -> {
            if (!this.updating) {
                Integer v = parseInt(s);
                if (v != null) {
                    HudConfig.setOffset(part, v, HudConfig.offsetY(part));
                    this.clampAndSync(part);
                }
            }
        });
        this.yBox = this.numberBox(px + 100, py + this.yXY, 62, Integer.toString(HudConfig.offsetY(part)), "-?\\d{0,5}");
        this.yBox.setResponder(s -> {
            if (!this.updating) {
                Integer v = parseInt(s);
                if (v != null) {
                    HudConfig.setOffset(part, HudConfig.offsetX(part), v);
                    this.clampAndSync(part);
                }
            }
        });
        } else {
            this.xBox = null;
            this.yBox = null;
        }

        // Scale
        int percent = Math.round(HudConfig.scale(part) * 100f);
        this.scaleSlider = new FlatSlider(px + 10, py + this.yScRow + 4, 100, 14, 0.25, 2.0, HudConfig.scale(part), v -> {
            if (!this.updating) {
                float s = Math.round(v * 20.0) / 20.0f;
                HudConfig.setScale(part, s);
                this.updating = true;
                this.scaleBox.setValue(Integer.toString(Math.round(s * 100f)));
                this.updating = false;
                this.clampAndSync(part);
            }
        });
        this.addRenderableWidget(this.scaleSlider);
        this.scaleBox = this.numberBox(px + 116, py + this.yScRow, 42, Integer.toString(percent), "\\d{0,3}");
        this.scaleBox.setResponder(s -> {
            if (!this.updating) {
                Integer v = parseInt(s);
                if (v != null && v > 0) {
                    float scale = v / 100f;
                    HudConfig.setScale(part, scale);
                    this.updating = true;
                    this.scaleSlider.setActual(HudConfig.scale(part));
                    this.updating = false;
                    this.clampAndSync(part);
                }
            }
        });

        // Stay visible
        this.addRenderableWidget(new FlatButton(px + 10, py + this.yKeep, PANEL_W - 20, this.hKeep,
                Component.translatable("dynamicrpghud.editor.stay_visible"), b -> {
            HudConfig.setKept(part, !HudConfig.isKept(part));
            HudConfig.save();
            this.refresh();
        }).box(HudConfig.isKept(part) ? FlatButton.BOX_CHECK : FlatButton.BOX_CROSS));

        // Per-element hide animation
        AnimationMode own = HudConfig.elementAnimation(part);
        Component animName = own == null
                ? Component.translatable("dynamicrpghud.animation.default")
                : Component.translatable(own.nameKey());
        this.addRenderableWidget(new FlatButton(px + 10, py + this.yAnim, PANEL_W - 20, this.hAnim,
                Component.translatable("dynamicrpghud.editor.animation"),
                b -> this.minecraft.setScreen(this.animationScreen(part)))
                .subtitle(animName));

        if (part == HudPart.AIR) {
            this.addRenderableWidget(new FlatButton(px + 10, py + this.yAir0, PANEL_W - 20, this.hAir0,
                    Component.translatable("dynamicrpghud.editor.air_underwater"), b -> {
                HudConfig.setAirShowUnderwater(!HudConfig.airShowUnderwater());
                HudConfig.save();
                this.refresh();
            }).box(HudConfig.airShowUnderwater() ? FlatButton.BOX_CHECK : FlatButton.BOX_CROSS));
            this.addRenderableWidget(new FlatButton(px + 10, py + this.yAir1, PANEL_W - 20, this.hAir1,
                    Component.translatable("dynamicrpghud.editor.air_submerge"), b -> {
                HudConfig.setAirShowOnSubmerge(!HudConfig.airShowOnSubmerge());
                HudConfig.save();
                this.refresh();
            }).box(HudConfig.airShowOnSubmerge() ? FlatButton.BOX_CHECK : FlatButton.BOX_CROSS));
            this.addRenderableWidget(new FlatButton(px + 10, py + this.yAir2, PANEL_W - 20, this.hAir2,
                    Component.translatable("dynamicrpghud.editor.air_refill"), b -> {
                HudConfig.setAirHideAfterRefill(!HudConfig.airHideAfterRefill());
                HudConfig.save();
                this.refresh();
            }).box(HudConfig.airHideAfterRefill() ? FlatButton.BOX_CHECK : FlatButton.BOX_CROSS));
        }

        // Reset element
        this.addRenderableWidget(new FlatButton(px + 10, py + this.yReset, PANEL_W - 20, this.hReset,
                Component.translatable("dynamicrpghud.editor.reset_element"), b -> {
            HudConfig.resetPart(part);
            HudConfig.save();
            this.refresh();
        }));
    }

    private Screen animationScreen(HudElement part) {
        AnimationMode[] modes = AnimationMode.values();
        ChoiceScreen.Option[] options = new ChoiceScreen.Option[modes.length + 1];
        options[0] = new ChoiceScreen.Option(Component.translatable("dynamicrpghud.animation.default"),
                Component.translatable("dynamicrpghud.animation.default.desc"));
        for (int i = 0; i < modes.length; i++) {
            options[i + 1] = new ChoiceScreen.Option(Component.translatable(modes[i].nameKey()),
                    Component.translatable(modes[i].descKey()));
        }
        AnimationMode own = HudConfig.elementAnimation(part);
        return new ChoiceScreen(this, part.displayName(),
                Component.translatable("dynamicrpghud.editor.animation.hint"),
                options, own == null ? 0 : own.ordinal() + 1, i -> {
            HudConfig.setElementAnimation(part, i == 0 ? null : modes[i - 1]);
            HudConfig.save();
        });
    }

    /** Keeps the element on screen after a typed change and shows the corrected X / Y in the fields. */
    private void clampAndSync(HudElement part) {
        int ox = HudConfig.offsetX(part);
        int oy = HudConfig.offsetY(part);
        keepOnScreen(part);
        if ((ox != HudConfig.offsetX(part) || oy != HudConfig.offsetY(part)) && this.xBox != null && this.yBox != null) {
            this.updating = true;
            this.xBox.setValue(Integer.toString(HudConfig.offsetX(part)));
            this.yBox.setValue(Integer.toString(HudConfig.offsetY(part)));
            this.updating = false;
        }
    }

    private EditBox numberBox(int x, int y, int w, String value, String regex) {
        EditBox box = new EditBox(this.font, x + 5, y + 5, w - 10, 10, Component.empty());
        box.setBordered(false);
        box.setMaxLength(6);
        box.setFilter(s -> s.isEmpty() || s.equals("-") || s.matches(regex));
        box.setValue(value);
        this.addRenderableWidget(box);
        return box;
    }

    private static Integer parseInt(String s) {
        try {
            return Integer.valueOf(s);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static int clamp(int v, int lo, int hi) {
        return Math.max(lo, Math.min(hi, v));
    }

    // =============================================================================================
    // Geometry
    // =============================================================================================

    /** Current on-screen box {x, y, w, h} of an element (vanilla box + user offset and scale). */
    private int[] bounds(HudElement part) {
        int[] r = HudLayout.rect(part, this.width, this.height);
        if (r == null) {
            r = new int[]{this.width / 2, this.height / 2, 1, 1};
        }
        float s = HudConfig.scale(part);
        float cx = r[0] + r[2] / 2f + HudConfig.offsetX(part);
        float cy = r[1] + r[3] / 2f + HudConfig.offsetY(part);
        float w = r[2] * s;
        float h = r[3] * s;
        return new int[]{Math.round(cx - w / 2f), Math.round(cy - h / 2f), Math.max(1, Math.round(w)), Math.max(1, Math.round(h))};
    }

    /** Clickable area: the box, but never smaller than 12x12 so thin bars are easy to grab. */
    private int[] hitBox(HudElement part) {
        int[] b = bounds(part);
        int w = Math.max(b[2], 12);
        int h = Math.max(b[3], 12);
        return new int[]{b[0] - (w - b[2]) / 2, b[1] - (h - b[3]) / 2, w, h};
    }

    private HudElement partAt(double mx, double my) {
        for (int i = this.parts.size() - 1; i >= 0; i--) {
            HudElement p = this.parts.get(i);
            if (!inLayer(p)) {
                continue;
            }
            int[] h = hitBox(p);
            if (mx >= h[0] && mx < h[0] + h[2] && my >= h[1] && my < h[1] + h[3]) {
                return p;
            }
        }
        return null;
    }

    private boolean overPanel(double mx, double my) {
        return this.selected != null && !this.dragging
                && mx >= this.panelX && mx < this.panelX + PANEL_W
                && my >= this.panelY && my < this.panelY + this.panelH;
    }

    // =============================================================================================
    // Rendering
    // =============================================================================================

    @Override
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        // Drawn in render() instead, so it works the same on every Minecraft version.
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        HudPose.resetColor();
        Theme.dim(g, this.width, this.height);
        if (this.minecraft.level == null) {
            // Main menu: there is no real HUD behind the screen, so draw the real textures.
            for (HudElement p : this.parts) {
                if (p instanceof HudPart && inLayer(p)) {
                    EditorPreview.draw(g, (HudPart) p, this.width, this.height, this.font);
                }
            }
        }
        this.drawBanner(g);
        this.drawBoxes(g, mouseX, mouseY);
        this.drawLayerLabel(g);
        if (this.selected != null && !this.dragging) {
            this.drawPanelBackground(g);
        }
        super.render(g, mouseX, mouseY, partialTick);
    }

    /** Names the layer on show, to the right of the layer button. */
    private void drawLayerLabel(GuiGraphics g) {
        int bw = Math.min(150, (this.width - 30) / 2);
        int x = 10 + bw + 6 + 20 + 6;
        int room = this.width - 10 - bw - 6 - x;
        Component label = Component.translatable(this.layer == 1
                ? "dynamicrpghud.editor.layer1" : "dynamicrpghud.editor.layer2");
        List<FormattedCharSequence> lines = FlatButton.wrap(this.font, label, Math.max(40, room));
        int n = Math.min(2, lines.size());
        int y = this.height - 28 + 10 - n * 5;
        for (int i = 0; i < n; i++) {
            g.drawString(this.font, lines.get(i), x, y + i * 10, Theme.TEXT_MUTED);
        }
    }

    private void drawBanner(GuiGraphics g) {
        g.fill(0, 0, this.width, this.bannerBottom, 0xE60B0D14);
        g.fill(0, this.bannerBottom, this.width, this.bannerBottom + 1, Theme.ACCENT);
        g.drawCenteredString(this.font, this.title, this.width / 2, 10, Theme.TEXT);
        List<FormattedCharSequence> lines = this.font.split(Component.translatable("dynamicrpghud.editor.banner"), this.width - 40);
        int y = 24;
        for (FormattedCharSequence line : lines) {
            g.drawString(this.font, line, (this.width - this.font.width(line)) / 2, y, Theme.TEXT_MUTED);
            y += 10;
        }
    }

    private void drawBoxes(GuiGraphics g, int mouseX, int mouseY) {
        HudElement hover = this.dragging ? this.selected : partAt(mouseX, mouseY);
        for (HudElement p : this.parts) {
            if (!inLayer(p)) {
                continue;
            }
            int[] b = bounds(p);
            boolean sel = p == this.selected;
            boolean hov = p == hover;
            boolean kept = HudConfig.isKept(p);

            int fill = sel ? 0x554DA3FF : hov ? 0x442F6FB5 : 0x2A9AA4B5;
            int line = sel ? Theme.ACCENT : hov ? Theme.ACCENT_HOVER : 0xFF8D96AA;
            g.fill(b[0], b[1], b[0] + b[2], b[1] + b[3], fill);
            Theme.outline(g, b[0], b[1], b[0] + b[2], b[1] + b[3], line);

            // Name tag above the box (below it if there is no room); long names wrap to 2 lines.
            Component name = p.displayName();
            java.util.List<net.minecraft.util.FormattedCharSequence> tagLines = FlatButton.wrap(this.font, name, 118);
            int nLines = Math.max(1, Math.min(2, tagLines.size()));
            int tw = 0;
            for (int i = 0; i < nLines && i < tagLines.size(); i++) {
                tw = Math.max(tw, this.font.width(tagLines.get(i)));
            }
            int tagW = tw + 8 + (kept ? 10 : 0);
            int tagH = 4 + nLines * 10;
            int tagX = clamp(b[0] + b[2] / 2 - tagW / 2, 2, this.width - tagW - 2);
            int tagY = b[1] - tagH - 1;
            if (tagY < this.bannerBottom + 3) {
                tagY = b[1] + b[3] + 3;
            }
            g.fill(tagX, tagY, tagX + tagW, tagY + tagH, sel ? Theme.ACCENT_DARK : 0xDD10131C);
            for (int i = 0; i < nLines && i < tagLines.size(); i++) {
                g.drawString(this.font, tagLines.get(i), tagX + 4, tagY + 2 + i * 10, Theme.TEXT);
            }
            if (kept) {
                Theme.glyph(g, Theme.CHECK, Theme.GOOD, tagX + tw + 6, tagY + 2, 8, 8);
            }
        }
    }

    private void drawPanelBackground(GuiGraphics g) {
        int px = this.panelX;
        int py = this.panelY;
        Theme.panel(g, px, py, PANEL_W, this.panelH);

        int w = PANEL_W - 20;
        int ny = py + this.yName;
        int shown = 0;
        for (net.minecraft.util.FormattedCharSequence line : FlatButton.wrap(this.font, this.selected.displayName(), w)) {
            if (shown++ >= this.nameL) {
                break;
            }
            g.drawString(this.font, line, px + 10, ny, Theme.TEXT);
            ny += 10;
        }
        if (this.selected.isMovable()) {
            FlatButton.drawWrapped(g, this.font, Component.translatable("dynamicrpghud.editor.position"), px + 10, py + this.yPosLbl, w, Theme.TEXT_MUTED);
            g.drawString(this.font, "X", px + 10, py + this.yXY + 5, Theme.TEXT);
            g.drawString(this.font, "Y", px + 90, py + this.yXY + 5, Theme.TEXT);
            Theme.field(g, px + 20, py + this.yXY, 62, 18);
            Theme.field(g, px + 100, py + this.yXY, 62, 18);
        }

        FlatButton.drawWrapped(g, this.font, Component.translatable("dynamicrpghud.editor.scale"), px + 10, py + this.yScLbl, w, Theme.TEXT_MUTED);
        Theme.field(g, px + 116, py + this.yScRow, 42, 18);
        g.drawString(this.font, "%", px + 162, py + this.yScRow + 5, Theme.TEXT_MUTED);
    }

    // =============================================================================================
    // Mouse
    // =============================================================================================

    /** Starts dragging when an element is hit (elements win over everything except the open panel). */
    private boolean onPress(double mx, double my, int button) {
        if (button != 0 || overPanel(mx, my)) {
            return false;
        }
        HudElement hit = partAt(mx, my);
        if (hit == null) {
            return false;
        }
        this.selected = hit;
        this.dragging = hit.isMovable();
        this.grabMouseX = mx;
        this.grabMouseY = my;
        this.grabOffsetX = HudConfig.offsetX(hit);
        this.grabOffsetY = HudConfig.offsetY(hit);
        this.refresh();
        return true;
    }

    /** A click that hit nothing: closes the panel. */
    private boolean onBlankPress(double mx, double my, int button) {
        if (button == 0 && this.selected != null && !overPanel(mx, my)) {
            this.selected = null;
            this.refresh();
            return true;
        }
        return false;
    }

    private boolean onDrag(double mx, double my) {
        if (!this.dragging || this.selected == null) {
            return false;
        }
        int nx = this.grabOffsetX + (int) Math.round(mx - this.grabMouseX);
        int ny = this.grabOffsetY + (int) Math.round(my - this.grabMouseY);
        HudConfig.setOffset(this.selected, nx, ny);
        keepOnScreen(this.selected);
        return true;
    }

    /** Clamps the element's offset so its whole (scaled) box stays inside the screen. */
    private void keepOnScreen(HudElement part) {
        int[] r = HudLayout.rect(part, this.width, this.height);
        if (r == null) {
            return;
        }
        float s = HudConfig.scale(part);
        float halfW = r[2] * s / 2f;
        float halfH = r[3] * s / 2f;
        float cx = r[0] + r[2] / 2f;
        float cy = r[1] + r[3] / 2f;
        int minX = (int) Math.ceil(halfW - cx);
        int maxX = (int) Math.floor(this.width - halfW - cx);
        int minY = (int) Math.ceil(halfH - cy);
        int maxY = (int) Math.floor(this.height - halfH - cy);
        if (minX > maxX) {
            minX = maxX = (minX + maxX) / 2;
        }
        if (minY > maxY) {
            minY = maxY = (minY + maxY) / 2;
        }
        HudConfig.setOffset(part, clamp(HudConfig.offsetX(part), minX, maxX), clamp(HudConfig.offsetY(part), minY, maxY));
    }

    private boolean onRelease() {
        if (!this.dragging) {
            return false;
        }
        this.dragging = false;
        HudConfig.save();
        this.refresh();
        return true;
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (onPress(event.x(), event.y(), event.button())) {
            return true;
        }
        if (super.mouseClicked(event, doubleClick)) {
            return true;
        }
        return onBlankPress(event.x(), event.y(), event.button());
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
        if (onDrag(event.x(), event.y())) {
            return true;
        }
        return super.mouseDragged(event, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        boolean ended = onRelease();
        return super.mouseReleased(event) || ended;
    }

    @Override
    public void onClose() {
        HudConfig.save();
        this.minecraft.setScreen(this.parent);
    }
}
