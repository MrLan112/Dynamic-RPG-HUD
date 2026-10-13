package com.mrlan123.dynamicrpghud.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mrlan123.dynamicrpghud.HudPose;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Makes the items of the hotbar fade like everything else. Solid (block) items are drawn with an
 * opaque render type: the translucency of the fade (the shader colour) has no effect on them, so
 * they stayed fully visible until the very end of the fade and then vanished at once. While the
 * HUD is faded, the item draw inside the HUD frame uses a translucent render type instead.
 * Optional: if the call is not found the items keep their old behaviour.
 */
@Mixin(ItemRenderer.class)
public abstract class ItemRendererFadeMixin {

    @WrapOperation(method = "render",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/ItemBlockRenderTypes;getRenderType(Lnet/minecraft/world/item/ItemStack;Z)Lnet/minecraft/client/renderer/RenderType;"),
            require = 0)
    private RenderType dynamicrpghud$fadeItemType(ItemStack stack, boolean cull, Operation<RenderType> original) {
        RenderType type = original.call(stack, cull);
        try {
            if (HudPose.inFrame() && RenderSystem.getShaderColor()[3] < 0.999f) {
                return Sheets.translucentCullBlockSheet();
            }
        } catch (RuntimeException e) {
            // keep the vanilla type
        }
        return type;
    }
}
