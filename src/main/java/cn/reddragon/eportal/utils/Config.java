package cn.reddragon.eportal.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

public class Config {
    private static final File configFile = new File("EPortal.dll");
    private static boolean saved = true;

    public static String[] read() throws IOException {
        if (!saved) {
            return new String[]{"", "", "1"};
        }
        try {
            FileInputStream stream = new FileInputStream(configFile);
            int length = stream.read();
            if (length == -1) {
                return new String[]{"", "", "1"};
            }
            byte[] allBytes = stream.readAllBytes();
            if (allBytes.length != length) {
                return new String[]{"", "", "1"};
            }
            int index = 0;
            int usernameLength = allBytes[index];
            index++;
            byte[] b1 = Arrays.copyOfRange(allBytes, index, index + usernameLength);
            index += usernameLength;
            ArrayList<Byte> b2 = new ArrayList<>();
            for (int i = usernameLength - 1; i >= 0; i--) {
                b2.add((byte) ~b1[i]);
            }
            for (int i = 0; i < b2.size(); i++) {
                b1[i] = b2.get(i);
            }
            String username = new String(b1);
            int passwordLength = allBytes[index];
            index++;
            b1 = Arrays.copyOfRange(allBytes, index, index + passwordLength);
            index += passwordLength;
            b2.clear();
            for (int i = passwordLength - 1; i >= 0; i--) {
                b2.add((byte) ~b1[i]);
            }
            for (int i = 0; i < b2.size(); i++) {
                b1[i] = b2.get(i);
            }
            String password = new String(b1);
            byte netType = allBytes[index];
            stream.close();
            return new String[]{username, password, String.valueOf(netType)};
        } catch (Exception e) {
            e.printStackTrace();
            FileOutputStream stream = new FileOutputStream(configFile);
            stream.close();
            return new String[]{"", "", "1"};
        }
    }

    public static void save(String username, String password, byte netType) {
        int len1 = username.length();
        int len2 = password.length();
        try {
            FileOutputStream stream = new FileOutputStream(configFile);
            stream.write(len1 + len2 + 1 + 2);
            stream.write(len1);
            StringBuilder sb = new StringBuilder(username);
            byte[] b1 = sb.reverse().toString().getBytes();
            ArrayList<Byte> b2 = new ArrayList<>();
            for (byte value : b1) {
                b2.add((byte) ~value);
            }
            for (byte i : b2) {
                stream.write(i);
            }
            stream.write(len2);
            sb = new StringBuilder(password);
            b1 = sb.reverse().toString().getBytes();
            b2.clear();
            for (byte value : b1) {
                b2.add((byte) ~value);
            }
            for (byte i : b2) {
                stream.write(i);
            }
            stream.write(netType);
            stream.flush();
            stream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static {
        if (!configFile.exists()) {
            try {
                configFile.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            saved = false;
        }
    }
}
