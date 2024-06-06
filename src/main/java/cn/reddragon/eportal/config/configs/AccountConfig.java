package cn.reddragon.eportal.config.configs;

import cn.reddragon.eportal.account.Account;
import cn.reddragon.eportal.account.AccountManager;
import cn.reddragon.eportal.config.AbstractConfig;
import cn.reddragon.eportal.window.MainWindow;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class AccountConfig extends AbstractConfig {
    public static int index;
    public AccountConfig() {
        super("Account");
    }

    @Override
    public JsonElement toJson() {
        JsonObject o = new JsonObject();
        o.addProperty("Selector", MainWindow.controller.accountSelector.getItems().indexOf(MainWindow.controller.accountSelector.getValue()));
        JsonArray array = new JsonArray();
        for (Account it : AccountManager.accounts) {
            JsonObject object = new JsonObject();
            object.addProperty("Username", it.name());
            object.addProperty("Password", it.password());
            array.add(object);
        }
        o.add("Data", array);
        return o;
    }

    @Override
    public void fromJson(JsonElement element) {
        JsonObject o = element.getAsJsonObject();
        index = o.get("Selector").getAsInt();
        JsonArray array = o.get("Data").getAsJsonArray();
        for (JsonElement it : array) {
            JsonObject object = it.getAsJsonObject();
            AccountManager.addAccount(object.get("Username").getAsString(), object.get("Password").getAsString());
        }
    }
}
