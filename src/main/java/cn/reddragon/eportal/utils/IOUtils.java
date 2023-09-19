package cn.reddragon.eportal.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class IOUtils {
    public static String readText(InputStream stream) {
        try {
            InputStreamReader reader = new InputStreamReader(stream);
            StringBuilder sb = new StringBuilder();
            int ch = reader.read();
            while (ch != -1) {
                sb.append((char) ch);
                ch = reader.read();
            }
            return sb.toString();
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }
}
