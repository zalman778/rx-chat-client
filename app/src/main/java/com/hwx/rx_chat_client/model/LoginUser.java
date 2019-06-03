package com.hwx.rx_chat_client.model;

import android.util.Patterns;

public class LoginUser {

    private String strEmailAddress;
    private String strPassword;

    public LoginUser(String EmailAddress, String Password) {
        strEmailAddress = EmailAddress;
        strPassword = Password;
    }

    public String getStrEmailAddress() {
        return strEmailAddress;
    }

    public String getStrPassword() {
        return strPassword;
    }

    public boolean isEmailValid() {
        return Patterns.EMAIL_ADDRESS.matcher(getStrEmailAddress()).matches();
    }

    public boolean isPasswordLengthGreaterThan6() {
        return getStrPassword().length() > 6;
    }

}
