package com.cat.visuals;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.CloudRenderMode;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.GraphicsMode;
import net.minecraft.client.option.ParticlesMode;

/** Модуль оптимизации: меняет обычные настройки графики и возвращает их при выключении. */
public final class FpsBoost {
    private FpsBoost() {}

    private static boolean applied;
    private static int lastPreset = -1;
    private static boolean lastUnlimit, lastVsync;

    private static int savedView, savedSim, savedBiome, savedMaxFps;
    private static ParticlesMode savedParticles;
    private static boolean savedShadows, savedVsync;
    private static CloudRenderMode savedClouds;
    private static double savedEntDist;
    private static GraphicsMode savedGraphics;

    public static void tick(MinecraftClient mc) {
        if (mc.options == null) return;
        boolean want = Module.FPSBOOST.enabled;
        int preset = Module.FPS_PRESET.idx;
        boolean un = Module.FPS_UNLIMIT.b;
        boolean vs = Module.FPS_VSYNC.b;
        if (want && (!applied || preset != lastPreset || un != lastUnlimit || vs != lastVsync)) {
            apply(mc, preset, un, vs);
        } else if (!want && applied) {
            restore(mc);
        }
    }

    private static void apply(MinecraftClient mc, int preset, boolean unlimited, boolean vsyncOff) {
        GameOptions o = mc.options;
        if (!applied) {
            savedView = o.getViewDistance().getValue();
            savedSim = o.getSimulationDistance().getValue();
            savedBiome = o.getBiomeBlendRadius().getValue();
            savedMaxFps = o.getMaxFps().getValue();
            savedParticles = o.getParticles().getValue();
            savedShadows = o.getEntityShadows().getValue();
            savedVsync = o.getEnableVsync().getValue();
            savedClouds = o.getCloudRenderMode().getValue();
            savedEntDist = o.getEntityDistanceScaling().getValue();
            savedGraphics = o.getGraphicsMode().getValue();
        }

        int view, sim, biome;
        ParticlesMode particles;
        CloudRenderMode clouds;
        double entDist;
        if (preset == 0) {          // Balanced
            view = 12; sim = 8; biome = 1;
            particles = ParticlesMode.DECREASED; clouds = CloudRenderMode.FAST; entDist = 0.75;
        } else if (preset == 2) {   // Potato
            view = 5; sim = 5; biome = 0;
            particles = ParticlesMode.MINIMAL; clouds = CloudRenderMode.OFF; entDist = 0.5;
        } else {                    // Max FPS
            view = 8; sim = 6; biome = 0;
            particles = ParticlesMode.MINIMAL; clouds = CloudRenderMode.OFF; entDist = 0.5;
        }

        o.getViewDistance().setValue(view);
        o.getSimulationDistance().setValue(sim);
        o.getBiomeBlendRadius().setValue(biome);
        o.getParticles().setValue(particles);
        o.getCloudRenderMode().setValue(clouds);
        o.getEntityDistanceScaling().setValue(entDist);
        o.getEntityShadows().setValue(false);
        o.getGraphicsMode().setValue(GraphicsMode.FAST);
        o.getMaxFps().setValue(unlimited ? 260 : savedMaxFps);
        o.getEnableVsync().setValue(vsyncOff ? false : savedVsync);

        applied = true;
        lastPreset = preset;
        lastUnlimit = unlimited;
        lastVsync = vsyncOff;
    }

    public static void restore(MinecraftClient mc) {
        if (!applied || mc.options == null) return;
        GameOptions o = mc.options;
        o.getViewDistance().setValue(savedView);
        o.getSimulationDistance().setValue(savedSim);
        o.getBiomeBlendRadius().setValue(savedBiome);
        o.getParticles().setValue(savedParticles);
        o.getCloudRenderMode().setValue(savedClouds);
        o.getEntityDistanceScaling().setValue(savedEntDist);
        o.getEntityShadows().setValue(savedShadows);
        o.getGraphicsMode().setValue(savedGraphics);
        o.getMaxFps().setValue(savedMaxFps);
        o.getEnableVsync().setValue(savedVsync);
        applied = false;
        lastPreset = -1;
    }
}
