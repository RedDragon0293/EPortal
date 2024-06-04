package cn.reddragon.eportal.config;

import com.google.gson.JsonElement;

public abstract class AbstractConfig {
    public String name;

    public AbstractConfig(String name) {
        this.name = name;
    }

    public abstract JsonElement toJson();

    public abstract void fromJson(JsonElement element);
}
