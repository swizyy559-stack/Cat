package com.cat.visuals;

import java.util.ArrayList;
import java.util.List;

public class Module {
    public final String name;
    public final String desc;
    public final String category;
    public boolean enabled;
    public float anim;

    public Module(String name, String desc, String category, boolean enabled) {
        this.name = name;
        this.desc = desc;
        this.category = category;
        this.enabled = enabled;
        this.anim = enabled ? 1f : 0f;
    }

    public void toggle() { enabled = !enabled; }

    public static final Module JUMPCIRCLES = new Module("JumpCircles", "Кольцо при прыжке", "Visuals", true);
    public static final Module TRAILS      = new Module("Trails", "Шлейф за игроком", "Visuals", true);
    public static final Module TARGETRING  = new Module("TargetRing", "Кольцо на цели", "Visuals", true);

    public static final Module WATERMARK   = new Module("Watermark", "Плашка Cat", "HUD", true);
    public static final Module HOTKEYS     = new Module("Hotkeys", "Панель биндов", "HUD", true);
    public static final Module ARRAYLIST   = new Module("ArrayList", "Список модулей", "HUD", true);
    public static final Module KEYSTROKES  = new Module("Keystrokes", "WASD и мышь", "HUD", true);

    public static final List<Module> ALL = List.of(
            JUMPCIRCLES, TRAILS, TARGETRING, WATERMARK, HOTKEYS, ARRAYLIST, KEYSTROKES);

    public static final List<String> CATEGORIES = List.of("Visuals", "HUD");

    public static List<Module> in(String category) {
        List<Module> out = new ArrayList<>();
        for (Module m : ALL) if (m.category.equals(category)) out.add(m);
        return out;
    }
}
