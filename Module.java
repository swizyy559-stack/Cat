package com.cat.visuals;

import java.util.ArrayList;
import java.util.List;

public class Module {
    public final String name;
    public final String desc;
    public final String category;
    public boolean enabled;
    public float anim;
    public final List<Setting> settings;

    public Module(String name, String desc, String category, boolean enabled, Setting... settings) {
        this.name = name;
        this.desc = desc;
        this.category = category;
        this.enabled = enabled;
        this.anim = enabled ? 1f : 0f;
        this.settings = List.of(settings);
    }

    public void toggle() { enabled = !enabled; }

    // ---------- настройки ----------
    private static final String[] COLORS = {"Rainbow", "Orange", "Cyan", "Pink", "White"};

    public static final Setting JUMP_COLOR = Setting.mode("Color", 0, COLORS);
    public static final Setting JUMP_SIZE  = Setting.slider("Size", 0.5, 4.0, 0.1, 1.8);
    public static final Setting JUMP_TIME  = Setting.slider("Duration", 0.4, 2.0, 0.1, 0.9);

    public static final Setting TRAIL_COLOR  = Setting.mode("Color", 1, COLORS);
    public static final Setting TRAIL_LEN    = Setting.slider("Length", 0.3, 2.0, 0.1, 0.8);
    public static final Setting TRAIL_HEIGHT = Setting.slider("Height", 0.5, 2.5, 0.1, 1.8);

    public static final Setting TARGET_COLOR = Setting.mode("Color", 1, COLORS);
    public static final Setting TARGET_SPEED = Setting.slider("Speed", 0.5, 3.0, 0.1, 1.0);
    public static final Setting TARGET_TIME  = Setting.slider("Show time", 1, 6, 1, 3);

    public static final Setting HIT_TYPE  = Setting.mode("Type", 0, "Sparkle", "Crit", "Hearts", "Flame", "End Rod");
    public static final Setting HIT_COUNT = Setting.slider("Amount", 5, 60, 5, 20);

    public static final Setting HALO_COLOR  = Setting.mode("Color", 0, COLORS);
    public static final Setting HALO_HEIGHT = Setting.slider("Height", 1.9, 2.8, 0.1, 2.3);
    public static final Setting HALO_SIZE   = Setting.slider("Size", 0.2, 0.7, 0.05, 0.35);

    public static final Setting ZOOM_FOV = Setting.slider("FOV", 20, 70, 5, 30);

    public static final Setting CROSS_STYLE = Setting.mode("Style", 0, "Ticks", "Ring");
    public static final Setting CROSS_SIZE  = Setting.slider("Size", 2, 12, 1, 5);
    public static final Setting CROSS_COLOR = Setting.mode("Color", 1, COLORS);
    public static final Setting CROSS_DYN   = Setting.bool("Dynamic", true);

    public static final Setting WM_PING    = Setting.bool("Show ping", true);
    public static final Setting AL_RAINBOW = Setting.bool("Rainbow", true);
    public static final Setting AL_BG      = Setting.bool("Background", true);
    public static final Setting KS_MOUSE   = Setting.bool("Show mouse", true);
    public static final Setting CO_DIR     = Setting.bool("Direction", true);
    public static final Setting AR_DUR     = Setting.bool("Durability", true);

    public static final Setting FPS_PRESET = Setting.mode("Preset", 1, "Balanced", "Max FPS", "Potato");
    public static final Setting FPS_UNLIMIT = Setting.bool("Unlimited FPS", true);
    public static final Setting FPS_VSYNC   = Setting.bool("VSync off", true);

    public static final Setting THEME_COLOR = Setting.mode("Accent", 0, "Orange", "Blue", "Pink", "Green", "Purple");
    public static final Setting THEME_ALPHA = Setting.slider("Panel opacity", 0.4, 0.95, 0.05, 0.82);

    // ---------- модули ----------
    public static final Module JUMPCIRCLES = new Module("JumpCircles", "Кольцо при прыжке", "Visuals", true, JUMP_COLOR, JUMP_SIZE, JUMP_TIME);
    public static final Module TRAILS      = new Module("Trails", "Лента за игроком", "Visuals", true, TRAIL_COLOR, TRAIL_LEN, TRAIL_HEIGHT);
    public static final Module TARGETRING  = new Module("TargetRing", "Кольцо на цели", "Visuals", true, TARGET_COLOR, TARGET_SPEED, TARGET_TIME);
    public static final Module HITEFFECTS  = new Module("HitEffects", "Эффект при ударе", "Visuals", true, HIT_TYPE, HIT_COUNT);
    public static final Module HALO        = new Module("Halo", "Нимб над головой", "Visuals", true, HALO_COLOR, HALO_HEIGHT, HALO_SIZE);
    public static final Module ZOOM        = new Module("Zoom", "Приближение (клавиша C)", "Visuals", true, ZOOM_FOV);
    public static final Module CROSSHAIRFX = new Module("CrosshairFX", "Динамичный прицел", "Visuals", true, CROSS_STYLE, CROSS_SIZE, CROSS_COLOR, CROSS_DYN);

    public static final Module WATERMARK  = new Module("Watermark", "Плашка Cat", "HUD", true, WM_PING);
    public static final Module HOTKEYS    = new Module("Hotkeys", "Панель биндов", "HUD", true);
    public static final Module ARRAYLIST  = new Module("ArrayList", "Список модулей", "HUD", true, AL_RAINBOW, AL_BG);
    public static final Module KEYSTROKES = new Module("Keystrokes", "WASD и мышь", "HUD", true, KS_MOUSE);
    public static final Module COORDS     = new Module("Coordinates", "Координаты", "HUD", true, CO_DIR);
    public static final Module ARMORHUD   = new Module("ArmorHUD", "Броня на экране", "HUD", true, AR_DUR);
    public static final Module CPS        = new Module("CPS", "Клики в секунду", "HUD", true);
    public static final Module SPEED      = new Module("Speed", "Скорость блоки/сек", "HUD", true);

    public static final Module FPSBOOST = new Module("FPS Boost", "Настройки для FPS", "Performance", false, FPS_PRESET, FPS_UNLIMIT, FPS_VSYNC);

    public static final Module THEME = new Module("Theme", "Цвета меню", "Settings", true, THEME_COLOR, THEME_ALPHA);

    public static final List<Module> ALL = List.of(
            JUMPCIRCLES, TRAILS, TARGETRING, HITEFFECTS, HALO, ZOOM, CROSSHAIRFX,
            WATERMARK, HOTKEYS, ARRAYLIST, KEYSTROKES, COORDS, ARMORHUD, CPS, SPEED,
            FPSBOOST, THEME);

    public static final List<String> CATEGORIES = List.of("Visuals", "HUD", "Performance", "Settings");

    public static List<Module> in(String category) {
        List<Module> out = new ArrayList<>();
        for (Module m : ALL) if (m.category.equals(category)) out.add(m);
        return out;
    }
}
