package cn.reddragon.eportal.config.configs;

import cn.reddragon.eportal.EPortal;
import cn.reddragon.eportal.config.AbstractConfig;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import javafx.scene.control.ChoiceBox;

public class NetTypeConfig extends AbstractConfig {
    private final ChoiceBox<String> selector = EPortal.controller.selector;

    public NetTypeConfig() {
        super("NetType");
    }

    @Override
    public JsonElement toJson() {
        return new JsonPrimitive(EPortal.controller.selector.getItems().indexOf(EPortal.controller.selector.getValue()));
    }

    @Override
    public void fromJson(JsonElement element) {
        if (element.getAsInt() != -1)
            selector.setValue(selector.getItems().get(element.getAsInt()));
        else
            selector.setValue("");
    }
}
