package com.hwx.rx_chat_client.background.p2p;

public class StringUtils {

    public static String stringXOR (String a, String b) {
        if (a != null & b != null) {
            if (a.length() < b.length()) {
                b = b.substring(0, a.length());
            }

            if (b.length() < a.length()) {
                a = a.substring(0, b.length());
            }

            StringBuilder sb = new StringBuilder();
            for(int i = 0; i < a.length(); i++)
                sb.append((char)(a.charAt(i) + b.charAt(i % b.length())));
            return sb.toString();
        } else {
            return "null";
        }
    }

}
