package me.cat.client.module;

import me.cat.client.module.modules.hud.*;
import me.cat.client.module.modules.visuals.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ModuleManager {
    private final List<Module> modules = new ArrayList<>();

    public ModuleManager() {
        add(new JumpCircles());
        add(new Trails());
        add(new TargetRing());
        add(new Watermark());
        add(new ArrayListModule());
        add(new Keystrokes());
        add(new HotkeysHUD());
    }

    private void add(Module m) {
        modules.add(m);
    }

    public List<Module> getModules() {
        return modules;
    }

    public List<Module> getModulesByCategory(Category c) {
        return modules.stream().filter(m -> m.getCategory() == c).collect(Collectors.toList());
    }

    public Module getModule(String name) {
        for (Module m : modules) {
            if (m.getName().equalsIgnoreCase(name)) return m;
        }
        return null;
    }

    public void onRender2D(net.minecraft.client.gui.DrawContext ctx, float tickDelta) {
        for (Module m : modules) if (m.isEnabled()) m.onRender2D(ctx, tickDelta);
    }
}
