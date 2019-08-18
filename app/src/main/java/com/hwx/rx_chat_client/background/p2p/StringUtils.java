package com.hwx.rx_chat_client.background.p2p;

import android.util.Base64;

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

            return Base64.encodeToString(sb.toString().getBytes(), Base64.DEFAULT);
        } else {
            return "null";
        }
    }

}
