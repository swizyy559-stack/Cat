package me.cat.client.config;

import com.google.gson.*;
import me.cat.client.CatClient;
import me.cat.client.module.Module;
import me.cat.client.module.settings.*;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

public class Config {
    private final File file;
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public Config() {
        File dir = new File("cat");
        if (!dir.exists()) dir.mkdir();
        file = new File(dir, "config.json");
    }

    public void save() {
        JsonObject obj = new JsonObject();
        for (Module m : CatClient.moduleManager.getModules()) {
            JsonObject mObj = new JsonObject();
            mObj.addProperty("enabled", m.isEnabled());
            mObj.addProperty("key", m.getKey());
            JsonObject sObj = new JsonObject();
            for (Setting<?> s : m.getSettings()) {
                if (s instanceof BooleanSetting)
                    sObj.addProperty(s.getName(), ((BooleanSetting) s).getValue());
                else if (s instanceof NumberSetting)
                    sObj.addProperty(s.getName(), ((NumberSetting) s).getValue());
                else if (s instanceof ColorSetting)
                    sObj.addProperty(s.getName(), ((ColorSetting) s).getValue());
            }
            mObj.add("settings", sObj);
            obj.add(m.getName(), mObj);
        }
        try (FileWriter writer = new FileWriter(file)) {
            gson.toJson(obj, writer);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void load() {
        if (!file.exists()) return;
        try (FileReader reader = new FileReader(file)) {
            JsonObject obj = JsonParser.parseReader(reader).getAsJsonObject();
            for (Module m : CatClient.moduleManager.getModules()) {
                if (!obj.has(m.getName())) continue;
                JsonObject mObj = obj.getAsJsonObject(m.getName());
                if (mObj.has("enabled")) m.setEnabled(mObj.get("enabled").getAsBoolean());
                if (mObj.has("key")) m.setKey(mObj.get("key").getAsInt());
                if (mObj.has("settings")) {
                    JsonObject sObj = mObj.getAsJsonObject("settings");
                    for (Setting<?> s : m.getSettings()) {
                        if (!sObj.has(s.getName())) continue;
                        JsonElement el = sObj.get(s.getName());
                        if (s instanceof BooleanSetting && el.isJsonPrimitive())
                            ((BooleanSetting) s).setValue(el.getAsBoolean());
                        else if (s instanceof NumberSetting && el.isJsonPrimitive())
                            ((NumberSetting) s).setValue(el.getAsDouble());
                        else if (s instanceof ColorSetting && el.isJsonPrimitive())
                            ((ColorSetting) s).setValue(el.getAsInt());
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
