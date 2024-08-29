package cn.reddragon.eportal.config.configs;

import cn.reddragon.eportal.config.AbstractConfig;
import cn.reddragon.eportal.window.MainWindow;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import javafx.scene.control.CheckMenuItem;

public class AutoReconnectConfig extends AbstractConfig {
    public AutoReconnectConfig() {
        super("AutoReconnect");
    }

    public static boolean getAutoReconnect() {
        return ((CheckMenuItem) MainWindow.controller.menuBar.getMenus().get(0).getItems().get(0)).isSelected();
    }

    public static void setAutoReconnect(boolean b) {
        ((CheckMenuItem) MainWindow.controller.menuBar.getMenus().get(0).getItems().get(0)).setSelected(b);
    }

    @Override
    public JsonElement toJson() {
        return new JsonPrimitive(getAutoReconnect());
    }

    @Override
    public void fromJson(JsonElement element) {
        setAutoReconnect(element.getAsBoolean());
    }
}
