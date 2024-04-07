package cn.reddragon.eportal;

import cn.reddragon.eportal.utils.HttpUtils;
import cn.reddragon.eportal.utils.IOUtils;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import javafx.application.Platform;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.NoRouteToHostException;
import java.net.SocketTimeoutException;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class Authenticator {
    public static final String ePortalUrl = "http://10.96.0.155/eportal/InterFace.do?method=";
    public static String userIndex = null;
    public static boolean online = false;
    public static LoginType type;

    public static HttpURLConnection login(String username, String password, String service, String queryString) {
        String content = "userId=" + username + "&password=" + password + "&service=" + service + "&queryString=" + queryString + "&operatorPwd=" + "&operatorUserId=" + "&validcode=" + "&passwordEncrypt=false";
        try {
            HttpURLConnection connection = HttpUtils.make(ePortalUrl + "login", "POST");
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
            PrintWriter out = new PrintWriter(connection.getOutputStream());
            out.print(content);
            out.flush();
            connection.connect();
            return connection;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void updateUserIndex() {
        if (!Authenticator.getOnline()) {
            userIndex = null;
        }
        try {
            HttpURLConnection connection = HttpUtils.make("http://10.96.0.155/eportal/redirectortosuccess.jsp", "GET");
            connection.setInstanceFollowRedirects(false);
            connection.connect();
            Map<String, List<String>> result = connection.getHeaderFields();
            String redirectLocation = result.get("Location").get(0);
            userIndex = redirectLocation.substring(redirectLocation.indexOf('=') + 1);
        } catch (IOException e) {
            e.printStackTrace();
            userIndex = null;
        }
    }

    public static HttpURLConnection getUserInfo() {
        String content = "userIndex=" + userIndex();
        try {
            HttpURLConnection connection = HttpUtils.make(ePortalUrl + "getOnlineUserInfo", "POST");
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
            PrintWriter out = new PrintWriter(connection.getOutputStream());
            out.print(content);
            out.flush();
            connection.connect();
            return connection;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    //经过测试，登出时不需要userIndex也能成功登出
    public static HttpURLConnection logout() {
        String content = "userIndex=" + userIndex();
        try {
            HttpURLConnection connection = HttpUtils.make(ePortalUrl + "logout", "POST");
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
            connection.setRequestProperty("Referer", "http://10.96.0.155/eportal/success.jsp?userIndex=" + userIndex());
            PrintWriter out = new PrintWriter(connection.getOutputStream());
            out.print(content);
            out.flush();
            connection.connect();
            return connection;
        } catch (IOException e) {
            return null;
        }
    }

    public static boolean getOnline() {
        return online;
    }

    public static String userIndex() {
        if (userIndex == null) {
            updateUserIndex();
        }
        return userIndex;
    }

    public static void checkOnline() {
        try {
            HttpURLConnection connection = HttpUtils.make("http://10.96.0.155/eportal/redirectortosuccess.jsp", "GET");
            connection.setInstanceFollowRedirects(false);
            connection.connect();
            Map<String, List<String>> result = connection.getHeaderFields();
            String redirectLocation = result.get("Location").get(0);
            if (Objects.equals(redirectLocation, "Http://123.123.123.123")) {
                online = false;
            } else online = redirectLocation.contains("http://10.96.0.155/eportal/./success.jsp?");
            if (HelloApplication.controller != null)
                Platform.runLater(() -> {
                    if (HelloApplication.controller.button.isDisabled())
                        HelloApplication.controller.resultText.setText("");
                    HelloApplication.controller.button.setDisable(false);
                });
        } catch (SocketTimeoutException e) {
            if (HelloApplication.controller != null) {
                Platform.runLater(() -> {
                    HelloApplication.controller.resultText.setText("Auth server connection timeout!");
                    HelloApplication.controller.button.setDisable(true);
                });
            }
            online = false;
        } catch (NoRouteToHostException e) {
            if (HelloApplication.controller != null) {
                Platform.runLater(() -> {
                    HelloApplication.controller.resultText.setText("No Internet connection!");
                    HelloApplication.controller.button.setDisable(true);
                });
            }
            online = false;
        } catch (IOException e) {
            e.printStackTrace();
            online = false;
        }
    }

    public static void updateSession() {
        new Thread(() -> {
            try {
                while (true) {
                    String r = updateSessionInternal();
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

    private static String updateSessionInternal() {
        HttpURLConnection connection = Authenticator.getUserInfo();
        if (connection == null) {
            return "fail";
        }
        JsonObject resultJson;
        try {
            resultJson = JsonParser.parseString(IOUtils.readText(connection.getInputStream())).getAsJsonObject();
            //System.out.println(resultJson.toString());
            String r = resultJson.get("result").getAsString();
            if (Objects.equals(r, "success")) {
                //更新userIndex
                Authenticator.userIndex = resultJson.get("userIndex").getAsString();
                // 获取当前用户
                Platform.runLater(() -> HelloApplication.controller.user.setText("User: " + resultJson.get("userName").getAsString() + " (" + resultJson.get("userId").getAsString() + ")"));
                // 获取运营商、剩余时间
                JsonArray ballArray = JsonParser.parseString(resultJson.get("ballInfo").getAsString()).getAsJsonArray();
                if (ballArray.get(1).getAsJsonObject().get("displayName").getAsString().equals("我的运营商")) {
                    for (LoginType it : LoginType.values()) {
                        if (it.authName.contains(ballArray.get(1).getAsJsonObject().get("value").getAsString())) {
                            Authenticator.type = it;
                        }
                    }
                    Platform.runLater(() -> HelloApplication.controller.remainLabel.setText("Time remaining: ∞"));
                    return r;
                }
                Authenticator.type = LoginType.WAN;
                int duration = ballArray.get(1).getAsJsonObject().get("value").getAsInt();
                StringBuilder sb = new StringBuilder();
                sb.append("Time remaining: ");
                //计算剩余时间
                int h = duration / 3600;
                int m = (duration % 3600) / 60;
                int s = (duration % 3600) % 60;
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
                Platform.runLater(() -> HelloApplication.controller.remainLabel.setText(sb.toString()));
            } else if (!Objects.equals(r, "wait")) {
                //Authenticator.type = LoginType.OFFLINE;
                Platform.runLater(() -> HelloApplication.controller.resultText.setText(resultJson.get("message").getAsString()));
            }
            return r;
        } catch (IOException e) {
            return "fail";
        }
    }
}
