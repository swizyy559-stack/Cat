package me.cat.client.module.settings;

public abstract class Setting<T> {
    private final String nameKey;
    private T value;

    public Setting(String nameKey, T value) {
        this.nameKey = nameKey;
        this.value = value;
    }

    public String getName() {
        return me.cat.client.language.LangManager.get(nameKey);
    }

    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        this.value = value;
    }
}
