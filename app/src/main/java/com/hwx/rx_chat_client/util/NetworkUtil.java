package com.hwx.rx_chat_client.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.hwx.rx_chat_client.BuildConfig;
import com.hwx.rx_chat_client.Configuration;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.Collections;
import java.util.List;

public class NetworkUtil {

    public static Boolean hasNetwork(Context context) {
        Boolean isConnected = false;
//        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
//        NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
//        if (activeNetwork != null && activeNetwork.isConnected())
//            isConnected = true;
//        return isConnected;
        try {
            Socket socket = new Socket();
            SocketAddress socketAddress = new InetSocketAddress(BuildConfig.ServerIpAddr, Configuration.HTTPS_SERVER_PORT);
            socket.connect(socketAddress, 2000);
            socket.close();
            isConnected = true;
        } catch (IOException e) {
        }
        return isConnected;

    }

    public static String getIPAddress(boolean useIPv4) {
        try {
            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface intf : interfaces) {
                List<InetAddress> addrs = Collections.list(intf.getInetAddresses());
                for (InetAddress addr : addrs) {
                    if (!addr.isLoopbackAddress()) {
                        String sAddr = addr.getHostAddress();
                        //boolean isIPv4 = InetAddressUtils.isIPv4Address(sAddr);
                        boolean isIPv4 = sAddr.indexOf(':')<0;

                        if (useIPv4) {
                            if (isIPv4)
                                return sAddr;
                        } else {
                            if (!isIPv4) {
                                int delim = sAddr.indexOf('%'); // drop ip6 zone suffix
                                return delim<0 ? sAddr.toUpperCase() : sAddr.substring(0, delim).toUpperCase();
                            }
                        }
                    }
                }
            }
        } catch (Exception ignored) { } // for now eat exceptions
        return "";
    }

}
