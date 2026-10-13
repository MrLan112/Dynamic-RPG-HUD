package com.mrlan123.dynamicrpghud.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.mrlan123.dynamicrpghud.HudConfig;
import com.mrlan123.dynamicrpghud.HudGeometry;
import com.mrlan123.dynamicrpghud.HudPart;
import com.mrlan123.dynamicrpghud.HudPose;
import com.mrlan123.dynamicrpghud.HudAnimationState;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.player.Player;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.PlayerRideableJumping;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Wraps each vanilla HUD element the mod owns in its own animated transform (Minecraft 1.21.1).
 * <p>
 * Uses MixinExtras {@code @WrapMethod} (bundled with Fabric Loader 0.16.9+): the transform is
 * pushed and popped around the whole vanilla call inside try/finally, so other mods that cancel
 * or hook the same methods (Overflowing Bars via Puzzles Lib, etc.) can never leave a push
 * behind. Handler staticness mirrors the target method, as WrapMethod requires.
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
                                      float maxHealth, int health, int displayHealth, int absorption,
                                      boolean blink, Operation<Void> original) {
        int[] r = HudGeometry.heartsRect(x, y, lineHeight, maxHealth, absorption);
        HudGeometry.set(HudPart.HEALTH, g.guiWidth(), g.guiHeight(), r[0], r[1], r[2], r[3]);
        HudPose.run(g, HudPart.HEALTH, () -> original.call(g, p, x, y, lineHeight, regenIndex, maxHealth,
                health, displayHealth, absorption, blink));
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

    /** The horse jump bar (it replaces the experience bar while riding). */
    @WrapMethod(method = "renderJumpMeter")
    private void dynamicrpghud$jumpBar(PlayerRideableJumping rideable, GuiGraphics g, int x, Operation<Void> original) {
        HudPose.run(g, HudPart.JUMP_BAR, () -> original.call(rideable, g, x));
    }

    /**
     * The air bubbles are drawn inside {@code renderPlayerHealth} (no method of their own), so each
     * bubble sprite call is wrapped. The sprites are told apart by their vanilla paths; every other
     * sprite goes through untouched. Not required: if the call is not found the bubbles stay vanilla.
     */
    @WrapOperation(method = "renderPlayerHealth",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/GuiGraphics;blitSprite(Lnet/minecraft/resources/ResourceLocation;IIII)V"),
            require = 0)
    private void dynamicrpghud$airBubble(GuiGraphics g, ResourceLocation sprite, int x, int y, int w, int h,
                                         Operation<Void> original) {
        if (!isAirSprite(sprite)) {
            original.call(g, sprite, x, y, w, h);
            return;
        }
        HudGeometry.set(HudPart.AIR, g.guiWidth(), g.guiHeight(), g.guiWidth() / 2 + 10, y, 81, 9);
        HudPose.run(g, HudPart.AIR, () -> original.call(g, sprite, x, y, w, h));
    }

    private static boolean isAirSprite(ResourceLocation sprite) {
        if (sprite == null) {
            return false;
        }
        String path = sprite.getPath();
        return path.equals("hud/air") || path.equals("hud/air_bursting");
    }

    @WrapMethod(method = "renderExperienceBar")
    private void dynamicrpghud$xpBar(GuiGraphics g, int x, Operation<Void> original) {
        HudPose.run(g, HudPart.XP_BAR, () -> original.call(g, x));
    }

    @WrapMethod(method = "renderExperienceLevel")
    private void dynamicrpghud$xpLevel(GuiGraphics g, DeltaTracker dt, Operation<Void> original) {
        HudPose.run(g, HudPart.XP_LEVEL, () -> original.call(g, dt));
    }
}
