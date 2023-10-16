package cn.reddragon.eportal;

import cn.reddragon.eportal.utils.HttpUtils;

import java.awt.*;
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

    public static void getUserIndex() {
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
            getUserIndex();
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
        } catch (SocketTimeoutException e) {
            if (SystemTray.isSupported()) {
                SystemTray tray = SystemTray.getSystemTray();
                Image image = Toolkit.getDefaultToolkit().createImage("icon.png");
                TrayIcon icon = new TrayIcon(image, "Error");
                icon.setImageAutoSize(true);
                try {
                    tray.add(icon);
                } catch (AWTException e1) {
                    throw new RuntimeException(e1);
                }
                icon.displayMessage("Error", "Connection timeout!", TrayIcon.MessageType.ERROR);
                tray.remove(icon);
                //System.exit(0);
            }
            online = false;
        } catch (NoRouteToHostException e) {
            if (SystemTray.isSupported()) {
                SystemTray tray = SystemTray.getSystemTray();
                Image image = Toolkit.getDefaultToolkit().createImage("icon.png");
                TrayIcon icon = new TrayIcon(image, "Error");
                icon.setImageAutoSize(true);
                try {
                    tray.add(icon);
                } catch (AWTException e1) {
                    throw new RuntimeException(e1);
                }
                icon.displayMessage("Error", "No Internet connection!", TrayIcon.MessageType.ERROR);
                tray.remove(icon);
                //System.exit(0);
            }
            online = false;
        } catch (IOException e) {
            e.printStackTrace();
            online = false;
        }
    }
}
