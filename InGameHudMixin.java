package me.cat.client.mixin;

import me.cat.client.CatClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public class InGameHudMixin {
    @Inject(method = "render", at = @At("RETURN"))
    private void onRender(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        if (CatClient.moduleManager != null && !net.minecraft.client.MinecraftClient.getInstance().options.hudHidden) {
            CatClient.moduleManager.onRender2D(context, tickCounter.getTickDelta(false));
        }
    }
}
