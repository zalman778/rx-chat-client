package com.hwx.rx_chat.common.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.regex.Pattern;

public class SignupRequest implements Serializable {

    private String username;
    private String email;
    private String password;

    @SerializedName("password_confirm")
    @JsonProperty("password_confirm")
    private String passwordConfirm;

    public SignupRequest() {
    }

    public SignupRequest(String username, String email, String password, String passwordConfirm) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.passwordConfirm = passwordConfirm;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPasswordConfirm() {
        return passwordConfirm;
    }

    public void setPasswordConfirm(String passwordConfirm) {
        this.passwordConfirm = passwordConfirm;
    }

    public boolean isValid() {

        return email != null
                && Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE).matcher(email).matches()
                && username != null
                && Pattern.compile("[A-Za-z0-9_]+").matcher(username).matches()
                && username.length() > 6 && username.length() < 64
                && password != null
                && password.equals(passwordConfirm)
                && password.length() > 6 && password.length() < 64
                && Pattern.compile("[A-Za-z0-9_]+").matcher(password).matches();
    }

    public String getInvalidInfo() {
        if (email == null || !Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE).matcher(email).matches())
            return "check email";
        if (username == null || !Pattern.compile("[A-Za-z0-9_]+").matcher(username).matches() || username.length() <= 6 || username.length() > 64)
            return "username should be \"A-Za-z0-9_\", 7-64 symbols";

        if (password == null || !Pattern.compile("[A-Za-z0-9_]+").matcher(password).matches() || password.length() <= 6 || password.length() > 64)
            return "password should be \"A-Za-z0-9_\", 7-64 symbols";

        if (!password.equals(passwordConfirm)) {
            return "passwords do not match";
        }
        return null;
    }

    @Override
    public String toString() {
        return "SignupRequest{" +
                "username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", password='" + password + '\'' +
                ", passwordConfirm='" + passwordConfirm + '\'' +
                '}';
    }
}
