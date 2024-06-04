package cn.reddragon.eportal.config;

import cn.reddragon.eportal.config.configs.AccountConfig;
import cn.reddragon.eportal.config.configs.NetTypeConfig;
import com.google.gson.*;
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
    }

    public static void loadConfigs() {
        try {
            JsonElement element = JsonParser.parseReader(new BufferedReader(new FileReader(configFile)));
            if (!element.isJsonNull()) {
                JsonObject object = element.getAsJsonObject();
                for (Entry<String, JsonElement> next : object.entrySet()) {
                    for (AbstractConfig config : configs) {
                        if (config.name.equals(next.getKey())) {
                            config.fromJson(next.getValue());
                            logger.info("成功加载配置文件 {}", config.name);
                            break;
                        }
                    }
                }
            }
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public static void saveConfigs() {
        JsonObject root = new JsonObject();
        for (AbstractConfig config : configs) {
            root.add(config.name, config.toJson());
            logger.info("成功保存配置文件 {}", config.name);
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