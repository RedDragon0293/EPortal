package cn.reddragon.eportal;

import cn.reddragon.eportal.utils.HttpUtils;
import cn.reddragon.eportal.utils.IOUtils;
import cn.reddragon.eportal.utils.URIEncoder;
import com.google.gson.*;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.nio.charset.Charset;

public class HelloController {
    @FXML
    public ChoiceBox selector;
    @FXML
    private TextField userNameField;
    @FXML
    private TextField passwordField;
    @FXML
    private Label resultText;
    @FXML
    private Button button;

    @FXML
    protected void onLoginButtonClick() {
        if (Authenticator.isOnline()) {
            if (button.getText().equals("Login")) {
                resultText.setText("Already logged in!");
                button.setText("Logout");
            } else if (button.getText().equals("Logout")) {
                try {
                    HttpURLConnection connection = Authenticator.logout(Authenticator.userIndex);
                    if (connection == null) {
                        resultText.setText("Error!");
                        return;
                    }
                    JsonObject resultMessage = JsonParser.parseString(IOUtils.readText(connection.getInputStream())).getAsJsonObject();
                    System.out.println(resultMessage.toString());
                    String result = resultMessage.get("result").getAsString();
                    if (result.equals("success")) {
                        resultText.setText("success!" + resultMessage.get("message").getAsString());
                        button.setText("Login");
                    } else if (result.equals("fail")) {
                        resultText.setText("fail!" + resultMessage.get("message").getAsString());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
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
                return;
            }
            //获取结果
            JsonObject resultMessage = JsonParser.parseString(IOUtils.readText(loginConnection.getInputStream())).getAsJsonObject();
            System.out.println(resultMessage.toString());
            String result = resultMessage.get("result").getAsString();
            if (result.equals("fail")) {
                resultText.setText(resultMessage.get("message").getAsString());
            } else if (result.equals("success")) {
                resultText.setText("Success!" + resultMessage.get("message").getAsString());
                Authenticator.userIndex = resultMessage.get("userIndex").getAsString();
                button.setText("Logout");
                Config.save(username, password);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}