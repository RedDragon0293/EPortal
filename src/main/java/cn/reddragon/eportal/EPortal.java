package cn.reddragon.eportal;

import cn.reddragon.eportal.account.AccountManager;
import cn.reddragon.eportal.config.ConfigManager;
import cn.reddragon.eportal.config.configs.AutoReconnectConfig;
import cn.reddragon.eportal.controllers.MainController;
import cn.reddragon.eportal.utils.Authenticator;
import cn.reddragon.eportal.utils.LoginType;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.Objects;

public class EPortal extends Application {
    public static MainController controller;
    public static Logger logger = LogManager.getLogger("EPortal");
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
                logger.error("监听线程出错!", e);
            }
        }
    });

    @SuppressWarnings("unchecked")
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(EPortal.class.getResource("hello-view.fxml"));
        Parent root = fxmlLoader.load();
        controller = fxmlLoader.getController();
        Scene scene = new Scene(root);
        ChoiceBox<String> box = (ChoiceBox<String>) root.lookup("#selector");
        //box.getItems().addAll("LAN", "WAN", "ChinaMobile");
        for (LoginType it : LoginType.values()) {
            if (!Objects.equals(it.displayName, "")) {
                box.getItems().add(it.displayName);
            }
        }
        stage.setTitle("EPortal");
        stage.setScene(scene);
        stage.setMinWidth(root.prefWidth(-1));
        stage.setMinHeight(root.prefHeight(-1));
        //String[] config = ConfigManager.read();
        ConfigManager.loadConfigs();
        TextField name = (TextField) root.lookup("#userNameField");
        TextField pass = (TextField) root.lookup("#passwordField");
        if (!AccountManager.accounts.isEmpty()) {
            name.setText(AccountManager.accounts.get(0).getName());
            pass.setText(AccountManager.accounts.get(0).getPassword());
        }
        askThread.start();
        stage.show();
    }
}