package cn.reddragon.eportal;

import cn.reddragon.eportal.config.ConfigManager;
import cn.reddragon.eportal.utils.Authenticator;

public class Main {
    public static void main(String[] args) {
        Thread.currentThread().setName("EPortal Main");
        EPortal.askThread.setDaemon(true);
        Authenticator.checkOnline();
        try {
            if (Authenticator.online) {
                Authenticator.updateUserIndex();
            }
        } catch (NullPointerException e) {
            EPortal.logger.error("启动时出错!", e);
            System.exit(0);
        }
        EPortal.launch();
        ConfigManager.saveConfigs();
    }
}
