package cn.reddragon.eportal.controller;

import cn.reddragon.eportal.utils.*;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Objects;

public class MainController {
    @FXML
    public ChoiceBox<String> selector;
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
    private TextField userNameField;
    @FXML
    private TextField passwordField;

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
                        connection = Authenticator.logout();
                        if (connection == null) {
                            Platform.runLater(() -> {
                                resultText.setText("Error!");
                                button.setDisable(false);
                            });
                            return;
                        }
                        JsonObject resultMessage = JsonParser.parseString(IOUtils.readText(connection.getInputStream())).getAsJsonObject();
                        System.out.println(resultMessage.toString());
                        Platform.runLater(() -> resultText.setText(resultMessage.get("result").getAsString() + ":" + resultMessage.get("message").getAsString()));
                        Authenticator.updateStatus();
                        Platform.runLater(this::updateUI);
                    } catch (Exception e) {
                        e.printStackTrace();
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
        String username = userNameField.getText();
        if (username.isBlank()) {
            resultText.setText("请输入用户名!");
            return;
        }
        String password = passwordField.getText();
        if (password.isBlank()) {
            resultText.setText("请输入密码!");
            return;
        }
        String userId = URIEncoder.encodeURI(URIEncoder.encodeURI(username));
        String pw = URIEncoder.encodeURI(URIEncoder.encodeURI(password));
        String mode = selector.getValue();
        String serviceString = Arrays.stream(LoginType.values()).filter(it -> Objects.equals(it.displayName, mode)).findFirst().map(it -> URIEncoder.encodeURI(URIEncoder.encodeURI(it.authName))).orElse("");
        new Thread(() -> {
            try {
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
                System.out.println(resultMessage.toString());
                String result = resultMessage.get("result").getAsString();
                Platform.runLater(() -> resultText.setText(resultMessage.get("result").getAsString() + ":" + resultMessage.get("message").getAsString()));
                if (result.equals("success")) {
                    Authenticator.userIndex = resultMessage.get("userIndex").getAsString();
                    Config.save(username, password, (byte) selector.getItems().indexOf(mode));
                }
            } catch (IOException e) {
                e.printStackTrace();
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