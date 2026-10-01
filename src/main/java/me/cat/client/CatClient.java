package me.cat.client;

import me.cat.client.config.Config;
import me.cat.client.gui.ClickGui;
import me.cat.client.language.LangManager;
import me.cat.client.module.Module;
import me.cat.client.module.ModuleManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class CatClient implements ClientModInitializer {
    public static final String NAME = "Cat";
    public static final String VERSION = "2.0";
    public static MinecraftClient mc;

    public static ModuleManager moduleManager;
    public static Config config;
    public static LangManager langManager;

    private KeyBinding guiKey;

    @Override
    public void onInitializeClient() {
        mc = MinecraftClient.getInstance();
        langManager = new LangManager();
        moduleManager = new ModuleManager();
        config = new Config();
        config.load();

        guiKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.cat.openGui",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_RIGHT_SHIFT,
                "category.cat"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (guiKey.wasPressed()) {
                mc.setScreen(new ClickGui());
            }
            for (Module m : moduleManager.getModules()) {
                if (m.isEnabled()) m.onTick();
            }
        });

        WorldRenderEvents.LAST.register(context -> {
            for (Module m : moduleManager.getModules()) {
                if (m.isEnabled()) m.onRender3D(context);
            }
        });

        Runtime.getRuntime().addShutdownHook(new Thread(() -> config.save()));
    }
}
