package cn.reddragon.eportal.config.configs;

import cn.reddragon.eportal.account.Account;
import cn.reddragon.eportal.account.AccountManager;
import cn.reddragon.eportal.config.AbstractConfig;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class AccountConfig extends AbstractConfig {

    public AccountConfig() {
        super("Account");
    }

    @Override
    public JsonElement toJson() {
        JsonArray array = new JsonArray();
        for (Account it : AccountManager.accounts) {
            JsonObject object = new JsonObject();
            object.addProperty("Username", it.getName());
            object.addProperty("Password", it.getPassword());
            array.add(object);
        }
        return array;
    }

    @Override
    public void fromJson(JsonElement element) {
        JsonArray array = element.getAsJsonArray();
        for (JsonElement it : array) {
            JsonObject object = it.getAsJsonObject();
            AccountManager.addAccount(object.get("Username").getAsString(), object.get("Password").getAsString());
        }
    }
}
