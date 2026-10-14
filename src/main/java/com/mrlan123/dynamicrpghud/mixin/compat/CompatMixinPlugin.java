package com.mrlan123.dynamicrpghud.mixin.compat;

import com.mrlan123.dynamicrpghud.SupportedMod;
import net.fabricmc.loader.api.FabricLoader;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

/** Applies each hook only when its mod is installed. */
public final class CompatMixinPlugin implements IMixinConfigPlugin {

    private static SupportedMod modFor(String mixinClassName) {
        if (mixinClassName.endsWith("JadeMixin")) return SupportedMod.JADE;
        if (mixinClassName.endsWith("WthitMixin")) return SupportedMod.WTHIT;
        if (mixinClassName.endsWith("XaeroMinimapMixin") || mixinClassName.endsWith("XaeroPipMixin")) return SupportedMod.XAERO_MINIMAP;
        if (mixinClassName.endsWith("SodiumExtraMixin") || mixinClassName.endsWith("SodiumExtraHudMixin")) return SupportedMod.SODIUM_EXTRA;
        if (mixinClassName.endsWith("OverflowingBarsMixin")) return SupportedMod.OVERFLOWING_BARS;
        if (mixinClassName.endsWith("VoiceChatMixin")) return SupportedMod.VOICE_CHAT;
        if (mixinClassName.endsWith("MiniHudMixin")) return SupportedMod.MINIHUD;
        return null;
    }

    @Override
    public void onLoad(String mixinPackage) {
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        SupportedMod mod = modFor(mixinClassName);
        if (mod == null) {
            // GuiGraphicsMixin (measuring): only useful when some supported mod is installed.
            for (SupportedMod m : SupportedMod.values()) {
                if (m.matches(id -> FabricLoader.getInstance().isModLoaded(id))) {
                    return true;
                }
            }
            return false;
        }
        return mod.matches(id -> FabricLoader.getInstance().isModLoaded(id));
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {
    }

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }
}
