package cn.reddragon.eportal.config.configs;

import cn.reddragon.eportal.config.AbstractConfig;
import cn.reddragon.eportal.window.MainWindow;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

public class NetTypeConfig extends AbstractConfig {
    public static int index = -1;

    public NetTypeConfig() {
        super("NetType");
    }

    @Override
    public JsonElement toJson() {
        return new JsonPrimitive(MainWindow.controller.typeSelector.getItems().indexOf(MainWindow.controller.typeSelector.getValue()));
    }

    @Override
    public void fromJson(JsonElement element) {
        index = element.getAsInt();
    }
}
