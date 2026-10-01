package me.cat.client.module;

import me.cat.client.language.LangManager;
import me.cat.client.module.settings.Setting;
import net.minecraft.client.MinecraftClient;

import java.util.ArrayList;
import java.util.List;

public abstract class Module {
    protected static final MinecraftClient mc = MinecraftClient.getInstance();
    private final String nameKey;
    private final Category category;
    private final List<Setting<?>> settings = new ArrayList<>();
    private int key = -1;
    private boolean enabled = false;

    public Module(String nameKey, Category category) {
        this.nameKey = nameKey;
        this.category = category;
    }

    public String getName() {
        return LangManager.get(nameKey + ".name");
    }

    public String getDescription() {
        return LangManager.get(nameKey + ".desc");
    }

    public Category getCategory() {
        return category;
    }

    public List<Setting<?>> getSettings() {
        return settings;
    }

    public int getKey() {
        return key;
    }

    public void setKey(int key) {
        this.key = key;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void toggle() {
        enabled = !enabled;
        if (enabled) onEnable();
        else onDisable();
    }

    public void setEnabled(boolean enabled) {
        if (this.enabled != enabled) toggle();
    }

    protected void onEnable() {
    }

    protected void onDisable() {
    }

    public void onTick() {
    }

    public void onRender2D(net.minecraft.client.gui.DrawContext context, float tickDelta) {
    }

    public void onRender3D(net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext context) {
    }

    protected void addSetting(Setting<?> setting) {
        settings.add(setting);
    }
}
