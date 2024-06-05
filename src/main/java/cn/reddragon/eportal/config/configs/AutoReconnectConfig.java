package cn.reddragon.eportal.config.configs;

import cn.reddragon.eportal.EPortal;
import cn.reddragon.eportal.config.AbstractConfig;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import javafx.scene.control.CheckMenuItem;

public class AutoReconnectConfig extends AbstractConfig {
    public AutoReconnectConfig() {
        super("AutoReconnect");
    }

    public static boolean getAutoReconnect() {
        return ((CheckMenuItem) EPortal.controller.menuBar.getMenus().get(0).getItems().get(0)).isSelected();
    }

    @Override
    public JsonElement toJson() {
        return new JsonPrimitive(getAutoReconnect());
    }

    @Override
    public void fromJson(JsonElement element) {
        ((CheckMenuItem) EPortal.controller.menuBar.getMenus().get(0).getItems().get(0)).setSelected(element.getAsBoolean());
    }
}
