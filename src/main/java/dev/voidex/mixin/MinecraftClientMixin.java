package dev.voidex.mixin;

import dev.voidex.VoidexConfig;
import dev.voidex.ui.VoidexTitleScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {
    @ModifyVariable(method = "setScreen", at = @At("HEAD"), argsOnly = true)
    private Screen voidex$replaceTitle(Screen screen) {
        if (VoidexConfig.customTitle && screen != null && screen.getClass() == TitleScreen.class) {
            return new VoidexTitleScreen();
        }
        return screen;
    }
}
