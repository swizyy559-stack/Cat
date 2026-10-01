package com.cat.visuals;

import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/** Сохранение модулей и настроек в config/cat.properties. */
public final class Config {
    private Config() {}

    private static Path file() {
        return FabricLoader.getInstance().getConfigDir().resolve("cat.properties");
    }

    public static void load() {
        Path f = file();
        if (!Files.exists(f)) return;
        Properties p = new Properties();
        try (InputStream in = Files.newInputStream(f)) {
            p.load(in);
        } catch (IOException e) {
            return;
        }
        for (Module m : Module.ALL) {
            String en = p.getProperty(m.name + ".enabled");
            if (en != null) {
                m.enabled = Boolean.parseBoolean(en);
                m.anim = m.enabled ? 1f : 0f;
            }
            for (Setting s : m.settings) {
                String v = p.getProperty(m.name + "." + s.name);
                if (v != null) s.load(v);
            }
        }
    }

    public static void save() {
        Properties p = new Properties();
        for (Module m : Module.ALL) {
            p.setProperty(m.name + ".enabled", Boolean.toString(m.enabled));
            for (Setting s : m.settings) p.setProperty(m.name + "." + s.name, s.save());
        }
        try (OutputStream out = Files.newOutputStream(file())) {
            p.store(out, "Cat settings");
        } catch (IOException ignored) {
        }
    }
}
