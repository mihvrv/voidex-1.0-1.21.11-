package dev.voidex;

import dev.voidex.hud.CpsTracker;
import dev.voidex.hud.HudModule;
import dev.voidex.ui.HudEditorScreen;
import dev.voidex.ui.VoidexMenuScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

public class VoidexClient implements ClientModInitializer {
    public static final KeyBinding.Category CATEGORY = KeyBinding.Category.create(Identifier.of("voidex", "main"));
    public static KeyBinding menuKey;
    public static KeyBinding zoomKey;

    @Override
    public void onInitializeClient() {
        VoidexConfig.load();

        menuKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.voidex.menu", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_RIGHT_SHIFT, CATEGORY));
        zoomKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.voidex.zoom", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_C, CATEGORY));

        ClientTickEvents.END_CLIENT_TICK.register(mc -> {
            while (menuKey.wasPressed()) {
                if (mc.currentScreen == null) mc.setScreen(new VoidexMenuScreen(null));
            }
            Zoom.update(zoomKey.isPressed() && mc.currentScreen == null);
        });

        HudElementRegistry.attachElementBefore(VanillaHudElements.CHAT, Identifier.of("voidex", "hud"), (ctx, tickCounter) -> {
            MinecraftClient mc = MinecraftClient.getInstance();
            if (mc.currentScreen == null) CpsTracker.poll(mc.getWindow().getHandle());
            if (mc.options.hudHidden) return;
            if (mc.currentScreen instanceof HudEditorScreen) return;
            for (HudModule m : VoidexConfig.modules) {
                if (m.enabled) m.render(ctx, mc);
            }
        });
    }
}
