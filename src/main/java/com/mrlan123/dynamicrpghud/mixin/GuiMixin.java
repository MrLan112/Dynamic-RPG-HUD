package com.mrlan123.dynamicrpghud.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.mrlan123.dynamicrpghud.HudAnimationState;
import com.mrlan123.dynamicrpghud.HudConfig;
import com.mrlan123.dynamicrpghud.HudGeometry;
import com.mrlan123.dynamicrpghud.HudPart;
import com.mrlan123.dynamicrpghud.HudPose;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.contextualbar.ContextualBarRenderer;
import net.minecraft.client.gui.contextualbar.JumpableVehicleBarRenderer;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Wraps each vanilla HUD element the mod owns in its own animated transform
 * (Minecraft 1.21.9 - 1.21.11).
 * <p>
 * Same approach as the 1.21.1 build: MixinExtras {@code @WrapMethod} around the vanilla method, the
 * transform is pushed and popped in try/finally, so another mod that replaces or hooks the same
 * element can never leave a matrix behind. The experience bar and level are not methods of
 * {@code Gui} any more: the contextual bar (experience bar, or the horse jump bar while riding) and
 * the level number are wrapped at their call sites in {@code renderHotbarAndDecorations}. Signatures were checked against the Fabric API 1.21.11 sources and the
 * Parchment mappings of 1.21.9, 1.21.10 and 1.21.11.
 */
@Mixin(Gui.class)
public abstract class GuiMixin {

    /** Frame bracket: other mods' HUD hooks get the frame's graphics, and nothing may outlive the frame. */
    @WrapMethod(method = "render")
    private void dynamicrpghud$frame(GuiGraphics g, DeltaTracker dt, Operation<Void> original) {
        HudPose.setFrame(g);
        HudAnimationState.advanceAll();
        try {
            original.call(g, dt);
        } finally {
            HudPose.releaseAll();
            HudPose.setFrame(null);
        }
    }

    @WrapMethod(method = "renderItemHotbar")
    private void dynamicrpghud$hotbar(GuiGraphics g, DeltaTracker dt, Operation<Void> original) {
        HudAnimationState.observeFrame();
        HudPose.run(g, HudPart.HOTBAR, () -> original.call(g, dt));
    }

    @WrapMethod(method = "renderHearts")
    private void dynamicrpghud$hearts(GuiGraphics g, Player p, int x, int y, int lineHeight, int regenIndex,
                                      float maxHealth, int lastHealth, int health, int absorption,
                                      boolean blink, Operation<Void> original) {
        int[] r = HudGeometry.heartsRect(x, y, lineHeight, maxHealth, absorption);
        HudGeometry.set(HudPart.HEALTH, g.guiWidth(), g.guiHeight(), r[0], r[1], r[2], r[3]);
        HudPose.run(g, HudPart.HEALTH, () -> original.call(g, p, x, y, lineHeight, regenIndex, maxHealth,
                lastHealth, health, absorption, blink));
    }

    /** {@code renderArmor} is static in vanilla, so this handler is too. */
    @WrapMethod(method = "renderArmor")
    private static void dynamicrpghud$armor(GuiGraphics g, Player p, int y, int heartRows, int lineHeight, int x,
                                            Operation<Void> original) {
        int rows = HudGeometry.armorLiftRows(heartRows, HudConfig.isInPlace(HudPart.ARMOR),
                HudConfig.isInPlace(HudPart.HEALTH));
        int[] r = HudGeometry.armorRect(x, y, rows, lineHeight);
        HudGeometry.set(HudPart.ARMOR, g.guiWidth(), g.guiHeight(), r[0], r[1], r[2], r[3]);
        HudPose.run(g, HudPart.ARMOR, () -> original.call(g, p, y, rows, lineHeight, x));
    }

    @WrapMethod(method = "renderFood")
    private void dynamicrpghud$food(GuiGraphics g, Player p, int y, int rightX, Operation<Void> original) {
        int[] r = HudGeometry.foodRect(rightX, y);
        HudGeometry.set(HudPart.HUNGER, g.guiWidth(), g.guiHeight(), r[0], r[1], r[2], r[3]);
        HudPose.run(g, HudPart.HUNGER, () -> original.call(g, p, y, rightX));
    }

    @WrapMethod(method = "renderAirBubbles")
    private void dynamicrpghud$air(GuiGraphics g, Player p, int vehicleMaxHealth, int y, int x,
                                   Operation<Void> original) {
        int[] r = HudGeometry.foodRect(x, y);
        HudGeometry.set(HudPart.AIR, g.guiWidth(), g.guiHeight(), r[0], r[1], r[2], r[3]);
        HudPose.run(g, HudPart.AIR, () -> original.call(g, p, vehicleMaxHealth, y, x));
    }

    @WrapMethod(method = "renderCrosshair")
    private void dynamicrpghud$crosshair(GuiGraphics g, DeltaTracker dt, Operation<Void> original) {
        HudPose.run(g, HudPart.CROSSHAIR, () -> original.call(g, dt));
    }

    @WrapMethod(method = "renderEffects")
    private void dynamicrpghud$effects(GuiGraphics g, DeltaTracker dt, Operation<Void> original) {
        HudPose.run(g, HudPart.EFFECTS, () -> original.call(g, dt));
    }

    /** The health of the mount (replaces the hunger bar while riding). */
    @WrapMethod(method = "renderVehicleHealth")
    private void dynamicrpghud$vehicleHealth(GuiGraphics g, Operation<Void> original) {
        HudPose.run(g, HudPart.VEHICLE_HEALTH, () -> original.call(g));
    }

    /**
     * The contextual bar: its empty background and its fill are two separate calls, both wrapped so
     * they move together. The renderer type tells the experience bar from the horse jump bar. Not
     * required: if a call is not found that bar simply stays vanilla.
     */
    @WrapOperation(method = "renderHotbarAndDecorations",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/contextualbar/ContextualBarRenderer;renderBackground(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/client/DeltaTracker;)V"),
            require = 0)
    private void dynamicrpghud$barBackground(ContextualBarRenderer bar, GuiGraphics g, DeltaTracker dt,
                                             Operation<Void> original) {
        HudPose.run(g, partOf(bar), () -> original.call(bar, g, dt));
    }

    @WrapOperation(method = "renderHotbarAndDecorations",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/contextualbar/ContextualBarRenderer;render(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/client/DeltaTracker;)V"),
            require = 0)
    private void dynamicrpghud$barFill(ContextualBarRenderer bar, GuiGraphics g, DeltaTracker dt,
                                       Operation<Void> original) {
        HudPose.run(g, partOf(bar), () -> original.call(bar, g, dt));
    }

    @WrapOperation(method = "renderHotbarAndDecorations",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/contextualbar/ContextualBarRenderer;renderExperienceLevel(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/client/gui/Font;I)V"),
            require = 0)
    private void dynamicrpghud$xpLevel(GuiGraphics g, Font font, int level, Operation<Void> original) {
        HudPose.run(g, HudPart.XP_LEVEL, () -> original.call(g, font, level));
    }

    private static HudPart partOf(ContextualBarRenderer bar) {
        return bar instanceof JumpableVehicleBarRenderer ? HudPart.JUMP_BAR : HudPart.XP_BAR;
    }
}
