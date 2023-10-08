package cn.reddragon.eportal.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class IOUtils {
    public static String readText(InputStream stream) {
        try {
            InputStreamReader reader = new InputStreamReader(stream, StandardCharsets.UTF_8);
            BufferedReader bufferedReader = new BufferedReader(reader);
            return bufferedReader.readLine();
            /*StringBuilder sb = new StringBuilder();
            int ch = reader.read();
            while (ch != -1) {
                sb.append((char) ch);
                ch = reader.read();
            }
            return sb.toString();*/
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }
}
