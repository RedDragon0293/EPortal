package cn.reddragon.eportal;

import cn.reddragon.eportal.utils.HttpUtils;
import cn.reddragon.eportal.utils.IOUtils;
import cn.reddragon.eportal.utils.URIEncoder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
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

public class HelloController {
    @FXML
    public ChoiceBox selector;
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
        sb.append("Status: ");
        if (Authenticator.getOnline()) {
            button.setText("Logout");
            sb.append("Online ");
            if (Authenticator.type == LoginType.CHINAMOBILE)
                sb.append("(ChinaMobile)");
            else
                sb.append("(WAN)");
        } else {
            button.setText("Login");
            sb.append("Offline");
            user.setText("User: null");
        }
        statusLabel.setText(sb.toString());
    }

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
                    resultText.setText(resultMessage.get("result").getAsString() + ":" + resultMessage.get("message").getAsString());
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
            //serviceString = URIEncoder.encodeURI(URIEncoder.encodeURI("校园网(Campus NET)"));
        } else if (mode.equals("ChinaMobile")) {
            serviceString = URIEncoder.encodeURI(URIEncoder.encodeURI("中国移动(CMCC NET)"));
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
                Config.save(username, password, (byte) selector.getItems().indexOf(mode));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        button.setDisable(false);
    }
}