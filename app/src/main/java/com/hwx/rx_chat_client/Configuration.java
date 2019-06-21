package com.hwx.rx_chat_client;

import android.os.Build;
import android.util.Log;

import com.hwx.rx_chat_client.util.NetworkUtil;

public class Configuration {
    //Rsocket config:
    public static final int RSOCKET_PORT = 7878;
    public static final int RSOCKET_TICK_PERIOD = 42;
    public static final int RSOCKET_ACK_PERIOD = 60;
    public static final int RSOCKET_MISSED_ACKS = 10;


    //public static final int RSOCKET_CLIENT_SERVER_PORT = 8113;
    //avd:
    public static final String IP = BuildConfig.ServerIpAddr;


    public static final String URL_SERVER = "http://"+IP+":8081/";

    public static final String HTTPS_SERVER_URL = "https://"+IP+":8443/";

    public static final String IMAGE_PREFIX = "api/image/";

    public static final String URL_LOGIN_REQUEST = HTTPS_SERVER_URL + "api/login";
    public static final String URL_SIGNUP_USER = HTTPS_SERVER_URL + "api/signup";
    public static final String URL_USERS_SEARCH = HTTPS_SERVER_URL + "api/users/search";
    public static final String URL_GET_PROFILE_INFO = HTTPS_SERVER_URL + "api/profile";


    public static final String URL_DIALOGS_LIST = HTTPS_SERVER_URL + "api/dialogs";
    public static final String URL_DIALOGS_FIND_OR_CREATE = HTTPS_SERVER_URL + "api/dialog/find_or_create";
    public static final String URL_DIALOGS_CREATE = HTTPS_SERVER_URL + "api/dialog/create";
    public static final String URL_DIALOG_PROFILE = HTTPS_SERVER_URL + "api/dialog/info";

    public static final String URL_MESSAGES_LIST = HTTPS_SERVER_URL + "api/messages";

    public static final String URL_FRIENDS_LIST = HTTPS_SERVER_URL + "api/friends";
    public static final String URL_FRIENDS_REQUEST_CREATE = HTTPS_SERVER_URL + "api/friends/request/create";
    public static final String URL_FRIENDS_REQUEST_ACCEPT = HTTPS_SERVER_URL + "api/friends/request/accept";
    public static final String URL_FRIENDS_REQUEST_REJECT = HTTPS_SERVER_URL + "api/friends/request/reject";



    public static final String URL_UPLOAD_PROFILE_PIC = HTTPS_SERVER_URL + "api/profile/upload_avatar";
    public static final String URL_UPLOAD_PROFILE_BIO = HTTPS_SERVER_URL + "api/profile/update_bio";


    public static final Integer MONGO_TIMEZONE_CORRECTION_HRS = 3;


    public static Boolean IS_TEST_STANDS = true;

    //security
    public static String CLIENT_CERT_PASS =  BuildConfig.ClientCertPass;
    public static String CLEINT_NETTY_KEYSTORE_PASS = BuildConfig.ClientNettyKeystorePass;


    public static String getIpV4() {
        if (IS_TEST_STANDS) {
            //Log.i("AVX", "build all = "+ Build.FINGERPRINT +"; "+Build.HARDWARE +"; "+Build.HOST+"; "+Build.TAGS+"; "+Build.PRODUCT+": "+Build.DEVICE);

            return NetworkUtil.getIPAddress(true);
        } else {
            return NetworkUtil.getIPAddress(true);
        }
    }

    public static Integer getPort() {
        if (IS_TEST_STANDS)
            return 5554;
        else
            return 7778;

    }

}
