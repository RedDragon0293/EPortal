package cn.reddragon.eportal.window;

import cn.reddragon.eportal.Main;
import cn.reddragon.eportal.account.Account;
import cn.reddragon.eportal.account.AccountManager;
import cn.reddragon.eportal.config.ConfigManager;
import cn.reddragon.eportal.config.configs.AccountConfig;
import cn.reddragon.eportal.config.configs.AutoReconnectConfig;
import cn.reddragon.eportal.config.configs.NetTypeConfig;
import cn.reddragon.eportal.utils.Authenticator;
import cn.reddragon.eportal.utils.LoginType;
import cn.reddragon.eportal.window.controllers.MainController;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ChoiceBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

public class MainWindow extends Application {
    public static MainController controller;
    public static final Thread askThread = new Thread(() -> {
        while (true) {
            try {
                //System.out.println("Heartbeat.");
                Authenticator.updateStatus();
                if (!Authenticator.online && AutoReconnectConfig.getAutoReconnect() && !Authenticator.error)
                    controller.onLoginButtonClick();
                Platform.runLater(controller::updateUI);
                if (Authenticator.error)
                    Thread.sleep(1000L);
                else
                    Thread.sleep(5000L);
            } catch (Exception e) {
                //throw new RuntimeException(e);
                Main.logger.error("监听线程出错!", e);
            }
        }
    });

    public static void main(String[] args) {
        askThread.setDaemon(true);
        Authenticator.checkOnline();
        try {
            if (Authenticator.online)
                Authenticator.updateUserIndex();
        } catch (NullPointerException e) {
            Main.logger.error("启动时出错!", e);
            System.exit(0);
        }
        launch();
        ConfigManager.saveConfigs();
    }

    @SuppressWarnings("unchecked")
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(MainWindow.class.getResource("hello-view.fxml"));
        Parent root = fxmlLoader.load();
        controller = fxmlLoader.getController();
        Scene scene = new Scene(root);
        stage.setTitle("EPortal");
        stage.setScene(scene);
        ConfigManager.loadConfigs();
        AccountWindow.init(stage);
        ChoiceBox<String> box = (ChoiceBox<String>) root.lookup("#typeSelector");
        for (LoginType it : LoginType.values())
            if (!Objects.equals(it.displayName, ""))
                box.getItems().add(it.displayName);
        ChoiceBox<String> box2 = (ChoiceBox<String>) root.lookup("#accountSelector");
        for (Account it : AccountManager.accounts)
            box2.getItems().add(it.name());
        if (NetTypeConfig.index != -1)
            box.setValue(box.getItems().get(NetTypeConfig.index));
        else
            box.setValue("");
        if (AccountConfig.index < AccountManager.accounts.size())
            box2.setValue(box2.getItems().get(AccountConfig.index));
        /*
        TextField name = (TextField) root.lookup("#userNameField");
        TextField pass = (TextField) root.lookup("#passwordField");
        if (!AccountManager.accounts.isEmpty()) {
            name.setText(AccountManager.accounts.get(0).getName());
            pass.setText(AccountManager.accounts.get(0).getPassword());
        }
        */
        askThread.start();
        stage.show();
    }
}