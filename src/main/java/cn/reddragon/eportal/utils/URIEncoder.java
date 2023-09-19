package cn.reddragon.eportal.utils;

import java.nio.charset.StandardCharsets;

public class URIEncoder {

    //public static final String ALLOWED_CHARS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789-_.!~*'()";

    public static String encodeURI(String str) {
        String isoStr = new String(str.getBytes(StandardCharsets.UTF_8), StandardCharsets.ISO_8859_1);
        char[] chars = isoStr.toCharArray();
        StringBuilder sb = new StringBuilder();
        for (char aChar : chars) {
            if ((aChar <= 'z' && aChar >= 'a') || (aChar <= 'Z' && aChar >= 'A')
                    || aChar == '-' || aChar == '_' || aChar == '.' || aChar == '!'
                    || aChar == '~' || aChar == '*' || aChar == '\'' || aChar == '('
                    || aChar == ')' || aChar == ';' || aChar == '/' || aChar == '?'
                    || aChar == ':' || aChar == '@' || aChar == '&' || aChar == '='
                    || aChar == '+' || aChar == '$' || aChar == ',' || aChar == '#'
                    || (aChar <= '9' && aChar >= '0')) {
                sb.append(aChar);
            } else {
                sb.append("%");
                sb.append(Integer.toHexString(aChar).toUpperCase());
            }
        }
        return sb.toString();
    }
/*
    public static String encodeURIComponent(String input) {
        if (null == input || input.trim().isEmpty()) {
            return input;
        }

        int l = input.length();
        StringBuilder o = new StringBuilder(l * 3);
        for (int i = 0; i < l; i++) {
            String e = input.substring(i, i + 1);
            if (!ALLOWED_CHARS.contains(e)) {
                byte[] b = e.getBytes(StandardCharsets.UTF_8);
                o.append(getHex(b));
                continue;
            }
            o.append(e);
        }
        return o.toString();
    }
*/
/*
    private static String getHex(byte[] buf) {
        StringBuilder o = new StringBuilder(buf.length * 3);
        for (byte b : buf) {
            int n = (int) b & 0xff;
            o.append("%");
            if (n < 0x10) {
                o.append("0");
            }
            o.append(Long.toString(n, 16).toUpperCase());
        }
        return o.toString();
    }*/
}