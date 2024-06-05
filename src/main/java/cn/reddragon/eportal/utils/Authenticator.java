package cn.reddragon.eportal.utils;

import cn.reddragon.eportal.EPortal;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import javafx.application.Platform;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class Authenticator {
    public static final String ePortalUrl = "http://10.96.0.155/eportal";
    public static final String ePortalInterFaceUrl = ePortalUrl + "/InterFace.do?method=";
    private static final Logger logger = LogManager.getLogger("Authenticator");
    public static String userIndex = null;
    public static boolean online = false;
    public static LoginType type = null;
    public static boolean error = false;

    public static HttpURLConnection login(String username, String password, String service, String queryString) {
        String content = "userId=" + username + "&password=" + password + "&service=" + service + "&queryString=" + queryString + "&operatorPwd=" + "&operatorUserId=" + "&validcode=" + "&passwordEncrypt=false";
        try {
            HttpURLConnection connection = HttpUtils.make(ePortalInterFaceUrl + "login", "POST");
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
            PrintWriter out = new PrintWriter(connection.getOutputStream());
            out.print(content);
            out.flush();
            connection.connect();
            return connection;
        } catch (IOException e) {
            logger.error("登录时出错!", e);
            return null;
        }
    }

    public static void updateUserIndex() {
        if (!online) {
            userIndex = null;
        }
        try {
            HttpURLConnection connection = HttpUtils.make(ePortalUrl + "/redirectortosuccess.jsp", "GET");
            connection.setInstanceFollowRedirects(false);
            connection.connect();
            Map<String, List<String>> result = connection.getHeaderFields();
            String redirectLocation = result.get("Location").get(0);
            userIndex = redirectLocation.substring(redirectLocation.indexOf('=') + 1);
        } catch (IOException e) {
            logger.error("更新 UserIndex 时出错!", e);
            userIndex = null;
        }
    }

    public static HttpURLConnection getUserInfo() {
        String content = "userIndex=" + userIndex();
        try {
            HttpURLConnection connection = HttpUtils.make(ePortalInterFaceUrl + "getOnlineUserInfo", "POST");
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
            PrintWriter out = new PrintWriter(connection.getOutputStream());
            out.print(content);
            out.flush();
            connection.connect();
            return connection;
        } catch (IOException e) {
            logger.error("更新用户数据时出错!", e);
            return null;
        }
    }

    //经过测试，登出时不需要userIndex也能成功登出
    public static HttpURLConnection logout() {
        String content = "userIndex=" + userIndex();
        try {
            HttpURLConnection connection = HttpUtils.make(ePortalInterFaceUrl + "logout", "POST");
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
            connection.setRequestProperty("Referer", ePortalUrl + "/success.jsp?userIndex=" + userIndex());
            PrintWriter out = new PrintWriter(connection.getOutputStream());
            out.print(content);
            out.flush();
            connection.connect();
            return connection;
        } catch (IOException e) {
            return null;
        }
    }

    public static String userIndex() {
        if (userIndex == null) {
            updateUserIndex();
        }
        return userIndex;
    }

    public static void checkOnline() {
        try {
            HttpURLConnection connection = HttpUtils.make(ePortalUrl + "/redirectortosuccess.jsp", "GET");
            connection.setInstanceFollowRedirects(false);
            connection.connect();
            if (connection.getResponseCode() != HttpURLConnection.HTTP_MOVED_TEMP) {
                if (EPortal.controller != null) {
                    Platform.runLater(() -> EPortal.controller.resultText.setText("错误: 与认证服务器通信时出错!"));
                    error = true;
                }
                return;
            }
            Map<String, List<String>> result = connection.getHeaderFields();
            String redirectLocation = result.get("Location").get(0);
            // 判断是否在线
            if ("Http://123.123.123.123".equalsIgnoreCase(redirectLocation)) {
                online = false;
            } else online = redirectLocation.contains(ePortalUrl + "/./success.jsp?");

            if (EPortal.controller != null && error) {
                Platform.runLater(() -> EPortal.controller.resultText.setText(""));
                error = false;
            }
        } catch (SocketTimeoutException e) {
            logger.error("无法连接到认证服务器! {}", e.getMessage());
            if (EPortal.controller != null) {
                Platform.runLater(() -> EPortal.controller.resultText.setText("错误: 无法连接到认证服务器!"));
                error = true;
            }
            online = false;
        } catch (SocketException e) {
            logger.error("客户端网络错误! {}", e.getMessage());
            if (EPortal.controller != null) {
                Platform.runLater(() -> EPortal.controller.resultText.setText("错误: 无网络连接!"));
                error = true;
            }
            online = false;
        } catch (IOException | NullPointerException e) {
            logger.error("更新在线状态时出错!", e);
            online = false;
        }
    }

    public static void updateStatus() {
        checkOnline();
        if (online) {
            updateUserIndex();
            updateSession();
        } else {
            Authenticator.type = null;
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
                        EPortal.controller.updateUI();
                        break;
                    } else {
                        break;
                    }
                }
            } catch (Exception e) {
                logger.error("更新 Session 时出错!", e);
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
                // 设置当前用户
                Platform.runLater(() -> EPortal.controller.user.setText("当前用户: " + resultJson.get("userName").getAsString() + " (" + resultJson.get("userId").getAsString() + ")"));
                // 设置运营商、剩余时间
                JsonArray ballArray = JsonParser.parseString(resultJson.get("ballInfo").getAsString()).getAsJsonArray();
                if (ballArray.get(1).getAsJsonObject().get("displayName").getAsString().equals("我的运营商")) {
                    for (LoginType it : LoginType.values()) {
                        if (it.authName.contains(ballArray.get(1).getAsJsonObject().get("value").getAsString())) {
                            Authenticator.type = it;
                        }
                    }
                    Platform.runLater(() -> EPortal.controller.remainLabel.setText("剩余时长: ∞"));
                    return r;
                }
                Authenticator.type = LoginType.WAN;
                int duration = ballArray.get(1).getAsJsonObject().get("value").getAsInt();
                StringBuilder sb = new StringBuilder();
                sb.append("剩余时长: ");
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
                Platform.runLater(() -> EPortal.controller.remainLabel.setText(sb.toString()));
            } else if (!Objects.equals(r, "wait")) {
                //Authenticator.type = LoginType.OFFLINE;
                Platform.runLater(() -> EPortal.controller.resultText.setText(resultJson.get("message").getAsString()));
            }
            return r;
        } catch (IOException e) {
            return "fail";
        }
    }
}
