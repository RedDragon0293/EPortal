package cn.reddragon.eportal;

import cn.reddragon.eportal.utils.HttpUtils;
import cn.reddragon.eportal.utils.IOUtils;
import cn.reddragon.eportal.utils.URIEncoder;
import com.google.gson.JsonArray;
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
import java.util.Objects;

public class HelloController {
    @FXML
    public ChoiceBox selector;
    @FXML
    private TextField userNameField;
    @FXML
    private TextField passwordField;
    @FXML
    public Label resultText;
    @FXML
    public Button button;
    @FXML
    private Label remainLabel;

    @FXML
    protected void onLoginButtonClick() {
        if (Authenticator.getOnline()) {
            button.setDisable(true);
            if (button.getText().equals("Login")) {
                resultText.setText("Already logged in!");
                button.setText("Logout");
            } else if (button.getText().equals("Logout")) {
                try {
                    HttpURLConnection connection = Authenticator.logout();
                    if (connection == null) {
                        resultText.setText("Error!");
                        button.setDisable(false);
                        return;
                    }
                    JsonObject resultMessage = JsonParser.parseString(IOUtils.readText(connection.getInputStream())).getAsJsonObject();
                    System.out.println(resultMessage.toString());
                    //String result = resultMessage.get("result").getAsString();
                    resultText.setText(resultMessage.get("result").getAsString() + ":" + resultMessage.get("message").getAsString());
                   /* if (result.equals("success")) {
                        resultText.setText("success!" + resultMessage.get("message").getAsString());
                        button.setText("Login");
                    } else if (result.equals("fail")) {
                        resultText.setText("fail!" + resultMessage.get("message").getAsString());
                    }*/
                    if (resultMessage.get("result").getAsString().equals("success")) {
                        remainLabel.setText("Time remaining: No user logon");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            button.setDisable(false);
            return;
        }
        String username = userNameField.getText();
        if (username.isBlank()) {
            resultText.setText("Please enter your username!");
            return;
        }
        String password = passwordField.getText();
        if (password.isBlank()) {
            resultText.setText("Please enter your password!");
        }
        button.setDisable(true);
        String userId = URIEncoder.encodeURI(URIEncoder.encodeURI(username));
        String pw = URIEncoder.encodeURI(URIEncoder.encodeURI(password));
        String mode = (String) selector.getValue();
        String serviceString = "";
        if (mode.equals("LAN")) {
            serviceString = URIEncoder.encodeURI(URIEncoder.encodeURI("校园内网服务(in-campus NET)"));
        } else if (mode.equals("WAN")) {
            serviceString = URIEncoder.encodeURI(URIEncoder.encodeURI("校园外网服务(out-campus NET)"));
        }
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
                resultText.setText("Error!");
                button.setDisable(false);
                return;
            }
            //获取结果
            JsonObject resultMessage = JsonParser.parseString(IOUtils.readText(loginConnection.getInputStream())).getAsJsonObject();
            System.out.println(resultMessage.toString());
            String result = resultMessage.get("result").getAsString();
            resultText.setText(resultMessage.get("result").getAsString() + ":" + resultMessage.get("message").getAsString());
            if (result.equals("success")) {
                Authenticator.userIndex = resultMessage.get("userIndex").getAsString();
                Config.save(username, password);
                onRemainLabelClick();
            }
            /*if (result.equals("fail")) {
                resultText.setText(resultMessage.get("message").getAsString());
            } else if (result.equals("success")) {
                resultText.setText("Success!" + resultMessage.get("message").getAsString());
                Authenticator.userIndex = resultMessage.get("userIndex").getAsString();
                button.setText("Logout");
                Config.save(username, password);
            }*/
        } catch (IOException e) {
            e.printStackTrace();
        }
        button.setDisable(false);
    }

    @FXML
    protected void onRemainLabelClick() throws IOException {
        remainLabel.setText("Time Remaining: ");
        new Thread(() -> {
            try {
                while (true) {
                    String r = updateRemainDuration();
                    if (Objects.equals(r, "wait")) {
                        Thread.sleep(1000);
                    } else if (Objects.equals(r, "success")) {
                        break;
                    } else {
                        break;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    protected String updateRemainDuration() {
        HttpURLConnection connection = Authenticator.getUserInfo();
        if (connection == null) {
            return "fail";
        }
        JsonObject resultJson;
        try {
            resultJson = JsonParser.parseString(IOUtils.readText(connection.getInputStream())).getAsJsonObject();
            System.out.println(resultJson.toString());
            String r = resultJson.get("result").getAsString();
            if (Objects.equals(r, "success")) {
                JsonArray ballArray = JsonParser.parseString(resultJson.get("ballInfo").getAsString()).getAsJsonArray();
                int time = ballArray.get(1).getAsJsonObject().get("value").getAsInt();
                StringBuilder sb = new StringBuilder();
                sb.append("Time remaining: ");
                if (time == -1) {
                    sb.append("Infinite");
                } else {
                    int h = time / 3600;
                    int m = (time % 3600) / 60;
                    int s = (time % 3600) % 60;
                    if (h > 0) {
                        sb.append(h).append("h ");
                        if (s > 0) {
                            sb.append(m).append("m ");
                            sb.append(s).append("s");
                        } else if (m > 0) {
                            sb.append(m).append("m");
                        }
                    } else {
                        if (m > 0) {
                            sb.append(m).append("m ");
                        }
                        if (s > 0) {
                            sb.append(s).append("s");
                        }
                    }
                }
                Platform.runLater(() -> remainLabel.setText(sb.toString()));
            } else if (!Objects.equals(r, "wait")) {
                StringBuilder sb = new StringBuilder();
                sb.append("Time remaining: ").append(r);
                Platform.runLater(() -> {
                    remainLabel.setText(sb.toString());
                    resultText.setText(resultJson.get("message").getAsString());
                });
            }
            return resultJson.get("result").getAsString();
        } catch (IOException e) {
            return "fail";
        }
    }
}