package cn.reddragon.eportal.window.controllers;

import cn.reddragon.eportal.Main;
import cn.reddragon.eportal.account.Account;
import cn.reddragon.eportal.account.AccountManager;
import cn.reddragon.eportal.utils.*;
import cn.reddragon.eportal.window.AccountWindow;
import cn.reddragon.eportal.window.MainWindow;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuBar;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Objects;

public class MainController {
    @FXML
    public ChoiceBox<String> typeSelector;
    @FXML
    public ChoiceBox<String> accountSelector;
    @FXML
    public Label resultText;
    @FXML
    public Button button;
    @FXML
    public Label remainLabel;
    @FXML
    public Label statusLabel;
    @FXML
    public Label user;
    @FXML
    public MenuBar menuBar;

    public void updateUI() {
        StringBuilder sb = new StringBuilder();
        sb.append("状态: ");
        if (Authenticator.online) {
            sb.append("在线 ");
            if (Authenticator.type == null) {
                sb.append("(...)");
            } else
                for (LoginType it : LoginType.values()) {
                    if (Objects.equals(it, Authenticator.type)) {
                        sb.append('(');
                        sb.append(it.displayName);
                        sb.append(')');
                        break;
                    }
                }
        } else {
            sb.append("离线");
        }
        if (Platform.isFxApplicationThread()) {
            statusLabel.setText(sb.toString());
            button.setDisable(Authenticator.error);
            if (Authenticator.online)
                button.setText("登出");
            else {
                button.setText("登录");
                user.setText("当前用户: null");
                remainLabel.setText("剩余时长:");
            }
        } else Platform.runLater(() -> {
            statusLabel.setText(sb.toString());
            button.setDisable(Authenticator.error);
            if (Authenticator.online)
                button.setText("登出");
            else {
                button.setText("登录");
                user.setText("当前用户: null");
                remainLabel.setText("剩余时长:");
            }
        });
    }

    @FXML
    public void onOpenAccountWindow() {
        AccountWindow.open();
    }

    @FXML
    public void onOpenLog() {
        try {
            Desktop desktop = Desktop.getDesktop();
            desktop.open(new File("logs\\latest.log"));
        } catch (Exception e) {
            Main.logger.error("打开日志失败!", e);
        }
    }

    @FXML
    public void openAboutDialog() {
        Alert dialog = new Alert(Alert.AlertType.INFORMATION);
        dialog.setTitle("关于");
        dialog.setHeaderText(null);
        VBox vbox = new VBox();
        Label label1 = new Label(Main.name + " version " + Main.version);
        vbox.getChildren().add(label1);
        Hyperlink link = new Hyperlink("https://github.com/RedDragon0293/EPortal");
        link.setOnAction(e -> {
            Desktop desktop = Desktop.getDesktop();
            try {
                desktop.browse(new URI("https://github.com/RedDragon0293/EPortal"));
            } catch (IOException | URISyntaxException ex) {
                Main.logger.error(ex);
            }
        });
        Label label2 = new Label("开源地址: ");
        //label2.setLabelFor(link);
        label2.setGraphic(link);
        label2.setContentDisplay(ContentDisplay.RIGHT);
        vbox.getChildren().add(label2);
        //vbox.getChildren().add(link);
        dialog.getDialogPane().setContent(vbox);
        dialog.initOwner(MainWindow.fatherStage);
        dialog.initModality(Modality.WINDOW_MODAL);
        dialog.showAndWait();
    }

    @FXML
    public void onLoginButtonClick() {
        button.setDisable(true);
        if (Authenticator.online) {
            if (button.getText().equals("登录")) {
                resultText.setText("当前设备已登录!");
                button.setText("登出");
            } else if (button.getText().equals("登出")) {
                new Thread(() -> {
                    HttpURLConnection connection = null;
                    try {
                        Main.logger.info("尝试发出登出命令.");
                        connection = Authenticator.logout();
                        if (connection == null) {
                            Platform.runLater(() -> {
                                resultText.setText("Error!");
                                button.setDisable(false);
                            });
                            return;
                        }
                        JsonObject resultMessage = JsonParser.parseString(IOUtils.readText(connection.getInputStream())).getAsJsonObject();
                        //System.out.println(resultMessage.toString());
                        Main.logger.info("服务器返回的是: {}", resultMessage);
                        Platform.runLater(() -> resultText.setText(resultMessage.get("result").getAsString() + ":" + resultMessage.get("message").getAsString()));
                        Authenticator.updateStatus();
                        Platform.runLater(this::updateUI);
                    } catch (Exception e) {
                        Main.logger.error("登出线程出错!", e);
                        Platform.runLater(() -> resultText.setText(e.getMessage()));
                    } finally {
                        if (connection != null) {
                            connection.disconnect();
                        }
                    }
                    Platform.runLater(() -> button.setDisable(false));
                }).start();

            }
            //button.setDisable(false);
            return;
        }
        if (accountSelector.getValue().isBlank()) {
            return;
        }
        Account account = AccountManager.getAccount(accountSelector.getValue());
        if (account == null) {
            Main.logger.error("尝试登录时账号错误! selector: {}", accountSelector.getValue());
            resultText.setText("账号错误!");
            return;
        }
        String username = account.name();
        String password = account.password();
        String userId = URIEncoder.encodeURI(URIEncoder.encodeURI(username));
        String pw = URIEncoder.encodeURI(URIEncoder.encodeURI(password));
        String mode = typeSelector.getValue();
        String serviceString = Arrays.stream(LoginType.values()).filter(it -> Objects.equals(it.displayName, mode)).findFirst().map(it -> URIEncoder.encodeURI(URIEncoder.encodeURI(it.authName))).orElse("");
        new Thread(() -> {
            try {
                Main.logger.info("尝试发出登录命令.");
                //获取queryString
                HttpURLConnection connection = HttpUtils.make("http://123.123.123.123", "GET");
                connection.setInstanceFollowRedirects(false);
                connection.connect();
                InputStreamReader reader = new InputStreamReader(connection.getInputStream());
                StringBuilder sb = new StringBuilder();
                int c = reader.read();
                boolean b = false;
                while (c != -1) {
                    if (b) {
                        sb.append((char) c);
                    }
                    if (c == '\'') {
                        b = !b;
                    }
                    c = reader.read();
                }
                sb.deleteCharAt(sb.length() - 1);
                sb.delete(0, sb.indexOf("?") + 1);
                //登录
                HttpURLConnection loginConnection = Authenticator.login(userId, pw, serviceString, URLEncoder.encode(URLEncoder.encode(sb.toString(), Charset.defaultCharset()), Charset.defaultCharset()));
                if (loginConnection == null) {
                    Platform.runLater(() -> {
                        resultText.setText("Error!");
                        button.setDisable(false);
                    });
                    return;
                }
                //获取结果
                JsonObject resultMessage = JsonParser.parseString(IOUtils.readText(loginConnection.getInputStream())).getAsJsonObject();
                //System.out.println(resultMessage.toString());
                Main.logger.info("服务器返回的是: {}", resultMessage);
                String result = resultMessage.get("result").getAsString();
                Platform.runLater(() -> resultText.setText(resultMessage.get("result").getAsString() + ":" + resultMessage.get("message").getAsString()));
                if (result.equals("success")) {
                    Authenticator.userIndex = resultMessage.get("userIndex").getAsString();
                }
            } catch (IOException e) {
                Main.logger.error("登录线程出错!", e);
                Platform.runLater(() -> resultText.setText(e.getMessage()));
            }
            Authenticator.updateStatus();
            Platform.runLater(() -> {
                button.setDisable(false);
                updateUI();
            });
        }).start();
    }
}