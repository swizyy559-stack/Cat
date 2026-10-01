package me.cat.client.language;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class LangManager {
    public enum Language {
        RU("ru"),
        EN("en");

        public final String code;

        Language(String code) {
            this.code = code;
        }
    }

    private static Language current = Language.RU;
    private static final Map<String, Map<String, String>> maps = new HashMap<>();
    private static final Gson gson = new Gson();

    public LangManager() {
        loadLang(Language.RU, "/assets/cat/lang/ru.json");
        loadLang(Language.EN, "/assets/cat/lang/en.json");
    }

    private void loadLang(Language lang, String path) {
        try (InputStreamReader reader = new InputStreamReader(
                getClass().getResourceAsStream(path), StandardCharsets.UTF_8)) {
            Map<String, String> map = gson.fromJson(reader, new TypeToken<Map<String, String>>() {
            }.getType());
            maps.put(lang.code, map);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String get(String key) {
        Map<String, String> map = maps.get(current.code);
        if (map != null && map.containsKey(key)) return map.get(key);
        return key;
    }

    public static void setLanguage(Language lang) {
        current = lang;
    }

    public static Language getCurrent() {
        return current;
    }
}
