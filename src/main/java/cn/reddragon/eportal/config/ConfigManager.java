package cn.reddragon.eportal.config;

import cn.reddragon.eportal.config.configs.AccountConfig;
import cn.reddragon.eportal.config.configs.AutoReconnectConfig;
import cn.reddragon.eportal.config.configs.NetTypeConfig;
import com.google.gson.*;
import javafx.scene.control.Alert;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.util.ArrayList;
import java.util.Map.Entry;

public class ConfigManager {
    public static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private static final File configFile = new File("EPortal.json");
    private static final ArrayList<AbstractConfig> configs = new ArrayList<>();
    private static final Logger logger = LogManager.getLogger("ConfigManager");
    private static final int configVersion = 2;

    static {
        if (!configFile.exists()) {
            try {
                logger.info("未找到配置文件. 新建配置文件.");
                configFile.createNewFile();
                saveConfigs();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        configs.add(new AccountConfig());
        configs.add(new NetTypeConfig());
        configs.add(new AutoReconnectConfig());
    }

    public static void loadConfigs() {
        try {
            JsonElement element = JsonParser.parseReader(new BufferedReader(new FileReader(configFile)));
            if (!element.isJsonNull()) {
                JsonObject object = element.getAsJsonObject();
                for (Entry<String, JsonElement> next : object.entrySet()) {
                    if (next.getKey().equals("ConfigVersion") && next.getValue().getAsInt() < configVersion) {
                        logger.warn("配置文件版本过旧!");
                        Alert alert = new Alert(Alert.AlertType.WARNING);
                        alert.setTitle("警告");
                        alert.setHeaderText(null);
                        alert.setContentText("配置文件版本过旧! 这可能导致配置加载错误. 由于软件会在退出时保存配置文件, 请考虑在软件退出前备份旧版配置文件.");
                        alert.showAndWait();
                    }
                    for (AbstractConfig config : configs) {
                        if (config.name.equals(next.getKey())) {
                            config.fromJson(next.getValue());
                            logger.info("成功加载配置文件 {}.", config.name);
                            break;
                        }
                    }
                }
            } else {
                logger.warn("配置文件损坏!");
            }
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public static void saveConfigs() {
        JsonObject root = new JsonObject();
        root.addProperty("ConfigVersion", configVersion);
        for (AbstractConfig config : configs) {
            root.add(config.name, config.toJson());
            logger.info("成功保存配置文件 {}.", config.name);
        }
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(configFile));
            writer.write(gson.toJson(root));
            writer.flush();
            writer.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
