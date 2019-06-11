package com.hwx.rx_chat.common.response;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.Objects;

public class LoginResponse implements Serializable {

    @SerializedName("status")
    private String status;

    @SerializedName("text")
    private String text;

    @SerializedName("username")
    private String username;

    @SerializedName("email")
    private String email;

    @SerializedName("token")
    private String token;

    @SerializedName("user_id")
    @JsonProperty("user_id")
    private String userId;

    @SerializedName("avatar_url")
    @JsonProperty("avatar_url")
    private String avatarUrl;

    @SerializedName("first_name")
    @JsonProperty("first_name")
    private String firstName;

    @SerializedName("last_name")
    @JsonProperty("last_name")
    private String lastName;

    @SerializedName("bio")
    @JsonProperty("bio")
    private String bio;

    public LoginResponse() {
    }

    public LoginResponse(String status, String text) {
        this.status = status;
        this.text = text;
    }

    public LoginResponse(String status, String text, String username, String email, String userId) {
        this.status = status;
        this.text = text;
        this.username = username;
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

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LoginResponse that = (LoginResponse) o;
        return Objects.equals(status, that.status) &&
                Objects.equals(text, that.text) &&
                Objects.equals(username, that.username) &&
                Objects.equals(email, that.email) &&
                Objects.equals(token, that.token) &&
                Objects.equals(userId, that.userId) &&
                Objects.equals(avatarUrl, that.avatarUrl) &&
                Objects.equals(firstName, that.firstName) &&
                Objects.equals(lastName, that.lastName) &&
                Objects.equals(bio, that.bio);
    }

    @Override
    public int hashCode() {
        return Objects.hash(status, text, username, email, token, userId, avatarUrl, firstName, lastName, bio);
    }

    @Override
    public String toString() {
        return "LoginResponse{" +
                "status='" + status + '\'' +
                ", text='" + text + '\'' +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", token='" + token + '\'' +
                ", userId='" + userId + '\'' +
                ", avatarUrl='" + avatarUrl + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", bio='" + bio + '\'' +
                '}';
    }

}
