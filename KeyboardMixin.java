package me.cat.client.mixin;

import me.cat.client.CatClient;
import me.cat.client.module.Module;
import net.minecraft.client.Keyboard;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Keyboard.class)
public class KeyboardMixin {
    @Inject(method = "onKey", at = @At("HEAD"))
    private void onKey(long window, int key, int scancode, int action, int modifiers, CallbackInfo ci) {
        if (action == 1 && MinecraftClient.getInstance().currentScreen == null && CatClient.moduleManager != null) {
            for (Module m : CatClient.moduleManager.getModules()) {
                if (m.getKey() == key) m.toggle();
            }
        }
    }
}
