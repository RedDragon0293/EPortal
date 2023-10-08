package cn.reddragon.eportal;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.awt.*;
import java.io.IOException;

public class HelloApplication extends Application {
    private static Label statusLabel;
    private static Button button;
    public static final Thread askThread = new Thread(() -> {
        while (true) {
            try {
                Thread.sleep(2500L);
            } catch (InterruptedException e) {
                //throw new RuntimeException(e);
            }
            Platform.runLater(() -> {
                Authenticator.checkOnline();
                updateStatus();
            });
        }
    });

    public static void main(String[] args) {
        Authenticator.checkOnline();
        try {
            if (Authenticator.getOnline()) {
                System.out.println("Already connected!");
                if (SystemTray.isSupported()) {
                    SystemTray tray = SystemTray.getSystemTray();
                    Image image = Toolkit.getDefaultToolkit().createImage("icon.png");
                    TrayIcon icon = new TrayIcon(image, "Error");
                    icon.setImageAutoSize(true);
                    tray.add(icon);
                    icon.displayMessage("Error", "You have already logged in!", TrayIcon.MessageType.ERROR);
                    tray.remove(icon);
                }
                Authenticator.getUserIndex();
                //System.exit(0);
            } else {
                System.out.println("Ready");
            }
            //System.out.println(result);
        } catch (NullPointerException | AWTException e) {
            e.printStackTrace();
            System.exit(0);
        }
        launch();
        System.exit(0);
    }

    public static void updateStatus() {
        statusLabel.setText("Current status:" + (Authenticator.getOnline() ? "Online" : "Offline"));
        if (Authenticator.getOnline()) {
            button.setText("Logout");
        } else {
            button.setText("Login");
        }
    }

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("hello-view.fxml"));
        Parent root = fxmlLoader.load();
        Scene scene = new Scene(root, 320, 240);
        ChoiceBox box = (ChoiceBox) root.lookup("#selector");
        box.getItems().addAll("LAN", "WAN");
        box.setValue("WAN");
        statusLabel = (Label) root.lookup("#statusLabel");
        button = (Button) root.lookup("#button");
        stage.setTitle("EPortal");
        stage.setScene(scene);
        stage.show();
        String[] config = Config.read();
        TextField name = (TextField) root.lookup("#userNameField");
        TextField pass = (TextField) root.lookup("#passwordField");
        name.setText(config[0]);
        pass.setText(config[1]);
        updateStatus();
        askThread.start();
        Authenticator.checkOnline();
        if (!Authenticator.getOnline()) {
            button.getOnAction().handle(new ActionEvent());
        }
    }
}