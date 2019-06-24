package com.hwx.rx_chat.common.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.Objects;

public class UserDetailsResponse implements Serializable {

    private String id;

    @SerializedName("image_url")
    @JsonProperty("image_url")
    private String imageUrl;

    private String firstname;

    private String lastname;

    private String username;

    private String bio;

    @SerializedName("is_available_send_friend_request")
    @JsonProperty("is_available_send_friend_request")
    private Boolean isAvailableSendFriendRequest;

    public UserDetailsResponse() {
    }

    public UserDetailsResponse(String id, String imageUrl, String firstname, String lastname, String username, String bio, Boolean isAvailableSendFriendRequest) {
        this.id = id;
        this.imageUrl = imageUrl;
        this.firstname = firstname;
        this.lastname = lastname;
        this.username = username;
        this.bio = bio;
        this.isAvailableSendFriendRequest = isAvailableSendFriendRequest;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public Boolean getAvailableSendFriendRequest() {
        return isAvailableSendFriendRequest;
    }

    public void setAvailableSendFriendRequest(Boolean availableSendFriendRequest) {
        isAvailableSendFriendRequest = availableSendFriendRequest;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserDetailsResponse that = (UserDetailsResponse) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(imageUrl, that.imageUrl) &&
                Objects.equals(firstname, that.firstname) &&
                Objects.equals(lastname, that.lastname) &&
                Objects.equals(username, that.username) &&
                Objects.equals(bio, that.bio) &&
                Objects.equals(isAvailableSendFriendRequest, that.isAvailableSendFriendRequest);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, imageUrl, firstname, lastname, username, bio, isAvailableSendFriendRequest);
    }

    @Override
    public String toString() {
        return "UserDetailsResponse{" +
                "id='" + id + '\'' +
                ", imageUrl='" + imageUrl + '\'' +
                ", firstname='" + firstname + '\'' +
                ", lastname='" + lastname + '\'' +
                ", username='" + username + '\'' +
                ", bio='" + bio + '\'' +
                ", isAvailableSendFriendRequest=" + isAvailableSendFriendRequest +
                '}';
    }
}
