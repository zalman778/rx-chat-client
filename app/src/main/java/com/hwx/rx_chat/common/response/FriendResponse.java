package com.hwx.rx_chat.common.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.Objects;

public class FriendResponse implements Serializable {
    @SerializedName("user_id")
    @JsonProperty("user_id")
    private String userId;

    private String username;

    @SerializedName("image_url")
    @JsonProperty("image_url")
    private String imageUrl;

    private Boolean accepted;

    @SerializedName("request_id")
    @JsonProperty("request_id")
    private String requestId;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Boolean getAccepted() {
        return accepted;
    }

    public void setAccepted(Boolean accepted) {
        this.accepted = accepted;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public FriendResponse() {
    }

    public FriendResponse(String userId, String username, String imageUrl, Boolean accepted, String requestId) {
        this.userId = userId;
        this.username = username;
        this.imageUrl = imageUrl;
        this.accepted = accepted;
        this.requestId = requestId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FriendResponse that = (FriendResponse) o;
        return Objects.equals(userId, that.userId) &&
                Objects.equals(username, that.username) &&
                Objects.equals(imageUrl, that.imageUrl) &&
                Objects.equals(accepted, that.accepted) &&
                Objects.equals(requestId, that.requestId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, username, imageUrl, accepted, requestId);
    }

    @Override
    public String toString() {
        return "FriendResponse{" +
                "userId='" + userId + '\'' +
                ", username='" + username + '\'' +
                ", imageUrl='" + imageUrl + '\'' +
                ", accepted=" + accepted +
                ", requestId='" + requestId + '\'' +
                '}';
    }
}
