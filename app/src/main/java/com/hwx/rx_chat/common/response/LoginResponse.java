package com.hwx.rx_chat.common.response;


import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.Objects;

public class LoginResponse implements Serializable {

    @SerializedName("status")
    private String status;

    @SerializedName("text")
    private String text;

    @SerializedName("name")
    private String name;

    @SerializedName("email")
    private String email;

    @SerializedName("token")
    private String token;

    @SerializedName("user_id")
    private String userId;

    public LoginResponse(String status, String text) {
        this.status = status;
        this.text = text;
    }

    public LoginResponse(String status, String text, String name, String email, String userId) {
        this.status = status;
        this.text = text;
        this.name = name;
        this.email = email;
        this.userId = userId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LoginResponse that = (LoginResponse) o;
        return Objects.equals(status, that.status) &&
                Objects.equals(text, that.text) &&
                Objects.equals(name, that.name) &&
                Objects.equals(email, that.email) &&
                Objects.equals(token, that.token) &&
                Objects.equals(userId, that.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(status, text, name, email, token, userId);
    }

    @Override
    public String toString() {
        return "LoginResponse{" +
                "status='" + status + '\'' +
                ", text='" + text + '\'' +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", token='" + token + '\'' +
                ", userId='" + userId + '\'' +
                '}';
    }
}
