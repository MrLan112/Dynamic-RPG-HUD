package com.mrlan123.dynamicrpghud;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;

/**
 * Dynamic RPG HUD - client entrypoint.
 * Registers the two key bindings, the tick hook and (1.21.6+) the HUD element wrappers.
 */
public class DynamicRpgHud implements ClientModInitializer {

    public static final String MOD_ID = "dynamicrpghud";

    private static KeyMapping openSettingsKey;
    private static KeyMapping toggleHudKey;

    @Override
    public void onInitializeClient() {
        HudConfig.load();

        String category = "key.category.dynamicrpghud.main";

        // Unbound by default; the player can pick a key under Controls.
        openSettingsKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key.dynamicrpghud.open_settings",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_UNKNOWN,
                category));

        // Used in the "hotkey" trigger mode. Default: H.
        toggleHudKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key.dynamicrpghud.toggle_hud",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_H,
                category));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (toggleHudKey.consumeClick()) {
                if (client.screen == null && HudConfig.trigger() == TriggerMode.HOTKEY) {
                    HudAnimationState.toggleManual();
                }
            }
            while (openSettingsKey.consumeClick()) {
                if (client.screen == null) {
                    client.setScreen(new SettingsScreen(null));
                }
            }
            HudAnimationState.onClientTick(client);
        });
    }
}
