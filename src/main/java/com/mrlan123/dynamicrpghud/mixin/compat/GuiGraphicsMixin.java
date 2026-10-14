package com.mrlan123.dynamicrpghud.mixin.compat;

import com.mrlan123.dynamicrpghud.FpsDisplayHook;
import com.mrlan123.dynamicrpghud.HudCapture;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Reports the corners of what is drawn to {@link HudCapture} (only does anything while another
 * mod's HUD callback is running). Every injection is optional ({@code require = 0}): a missing
 * one only makes the measured box smaller, it never breaks the game. Targets are the public
 * GuiGraphics draw methods of 1.21.9 - 1.21.11 (descriptors from the Parchment mappings).
 */
@Mixin(GuiGraphics.class)
public abstract class GuiGraphicsMixin {

    private void dynamicrpghud$box(int x1, int y1, int x2, int y2) {
        if (HudCapture.active()) {
            HudCapture.rect((GuiGraphics) (Object) this, x1, y1, x2, y2);
        }
    }

    @Inject(method = "fill(Lcom/mojang/blaze3d/pipeline/RenderPipeline;IIIII)V", at = @At("HEAD"), require = 0)
    private void dynamicrpghud$fill(@SuppressWarnings("unused") com.mojang.blaze3d.pipeline.RenderPipeline pipeline,
                                    int x1, int y1, int x2, int y2, int color, CallbackInfo ci) {
        dynamicrpghud$box(x1, y1, x2, y2);
    }

    @Inject(method = "blitSprite(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/resources/ResourceLocation;IIIII)V",
            at = @At("HEAD"), require = 0)
    private void dynamicrpghud$sprite(com.mojang.blaze3d.pipeline.RenderPipeline pipeline, net.minecraft.resources.ResourceLocation id,
                                      int x, int y, int w, int h, int color, CallbackInfo ci) {
        dynamicrpghud$box(x, y, x + w, y + h);
    }

    @Inject(method = "blitSprite(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/resources/ResourceLocation;IIIIIIIII)V",
            at = @At("HEAD"), require = 0)
    private void dynamicrpghud$spritePart(com.mojang.blaze3d.pipeline.RenderPipeline pipeline, net.minecraft.resources.ResourceLocation id,
                                          int spriteW, int spriteH, int u, int v, int x, int y, int w, int h,
                                          int color, CallbackInfo ci) {
        dynamicrpghud$box(x, y, x + w, y + h);
    }

    @Inject(method = "innerBlit(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/resources/ResourceLocation;IIIIFFFFI)V",
            at = @At("HEAD"), require = 0)
    private void dynamicrpghud$blit(com.mojang.blaze3d.pipeline.RenderPipeline pipeline, net.minecraft.resources.ResourceLocation id,
                                    int x1, int x2, int y1, int y2, float u0, float u1, float v0, float v1,
                                    int color, CallbackInfo ci) {
        dynamicrpghud$box(x1, y1, x2, y2);
    }

    @Inject(method = "drawString(Lnet/minecraft/client/gui/Font;Ljava/lang/String;IIIZ)V", at = @At("HEAD"),
            cancellable = true, require = 0)
    private void dynamicrpghud$text(Font font, String text, int x, int y, int color, boolean shadow, CallbackInfo ci) {
        if (!FpsDisplayHook.begin(text)) { // FPS - Display's counter, hidden by the player
            ci.cancel();
            return;
        }
        if (HudCapture.active() && text != null) {
            dynamicrpghud$box(x, y, x + font.width(text), y + 9);
        }
    }

    @Inject(method = "drawString(Lnet/minecraft/client/gui/Font;Ljava/lang/String;IIIZ)V", at = @At("RETURN"), require = 0)
    private void dynamicrpghud$textEnd(Font font, String text, int x, int y, int color, boolean shadow, CallbackInfo ci) {
        FpsDisplayHook.end();
    }

    @Inject(method = "drawString(Lnet/minecraft/client/gui/Font;Lnet/minecraft/network/chat/Component;IIIZ)V",
            at = @At("HEAD"), require = 0)
    private void dynamicrpghud$textComponent(Font font, Component text, int x, int y, int color, boolean shadow,
                                             CallbackInfo ci) {
        if (HudCapture.active() && text != null) {
            dynamicrpghud$box(x, y, x + font.width(text), y + 9);
        }
    }

    @Inject(method = "drawString(Lnet/minecraft/client/gui/Font;Lnet/minecraft/util/FormattedCharSequence;IIIZ)V",
            at = @At("HEAD"), require = 0)
    private void dynamicrpghud$textSeq(Font font, FormattedCharSequence text, int x, int y, int color, boolean shadow,
                                       CallbackInfo ci) {
        if (HudCapture.active() && text != null) {
            dynamicrpghud$box(x, y, x + font.width(text), y + 9);
        }
    }

    @Inject(method = "renderItem(Lnet/minecraft/world/item/ItemStack;II)V", at = @At("HEAD"), require = 0)
    private void dynamicrpghud$item(ItemStack stack, int x, int y, CallbackInfo ci) {
        dynamicrpghud$box(x, y, x + 16, y + 16);
    }

    @Inject(method = "renderItem(Lnet/minecraft/world/item/ItemStack;III)V", at = @At("HEAD"), require = 0)
    private void dynamicrpghud$itemSeed(ItemStack stack, int x, int y, int seed, CallbackInfo ci) {
        dynamicrpghud$box(x, y, x + 16, y + 16);
    }
}
