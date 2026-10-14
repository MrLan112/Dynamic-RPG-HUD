package com.mrlan123.dynamicrpghud.mixin.compat;

import com.mrlan123.dynamicrpghud.XaeroPip;
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import net.minecraft.client.gui.render.state.BlitRenderState;
import org.joml.Matrix3x2f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

/**
 * Puts the picture of Xaero's Minimap where the element is: the vanilla picture-in-picture blit
 * ({@code PictureInPictureRenderer.blitTexture}) is submitted with an identity matrix, so the
 * minimap's transform (see {@link XaeroPip}) is applied to that one blit. Only the minimap's own
 * renderer is touched; if anything differs the blit is left exactly as it was.
 */
@Mixin(PictureInPictureRenderer.class)
public abstract class XaeroPipMixin {

    @ModifyArg(method = "blitTexture",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/render/state/GuiRenderState;submitBlitToCurrentLayer(Lnet/minecraft/client/gui/render/state/BlitRenderState;)V"),
            index = 0, require = 0)
    private BlitRenderState dynamicrpghud$moveXaeroMap(BlitRenderState blit) {
        try {
            Matrix3x2f m = XaeroPip.pose();
            if (m == null || !XaeroPip.isMinimapRenderer(this)) {
                return blit;
            }
            return new BlitRenderState(blit.pipeline(), blit.textureSetup(), new Matrix3x2f(m).mul(blit.pose()),
                    blit.x0(), blit.y0(), blit.x1(), blit.y1(), blit.u0(), blit.u1(), blit.v0(), blit.v1(),
                    XaeroPip.tint(), blit.scissorArea());
        } catch (RuntimeException | LinkageError e) {
            return blit;
        }
    }
}
