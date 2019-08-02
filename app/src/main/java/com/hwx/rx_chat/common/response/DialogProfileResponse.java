package com.hwx.rx_chat.common.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class DialogProfileResponse implements Serializable {

    @SerializedName("dialog_id")
    @JsonProperty("dialog_id")
    private String dialogId;

    @SerializedName("dialog_name")
    @JsonProperty("dialog_name")
    private String dialogName;

    @SerializedName("chat_image")
    @JsonProperty("chat_image")
    private String chatImage;

    @SerializedName("friend_list")
    @JsonProperty("friend_list")
    private List<FriendResponse> friendList = new ArrayList<>();

    @SerializedName("creator_id")
    @JsonProperty("creator_id")
    private String creatorId;

    @SerializedName("creator_username")
    @JsonProperty("creator_username")
    private String creatorUsername;

    public String getDialogId() {
        return dialogId;
    }

    public void setDialogId(String dialogId) {
        this.dialogId = dialogId;
    }

    public String getDialogName() {
        return dialogName;
    }

    public void setDialogName(String dialogName) {
        this.dialogName = dialogName;
    }

    public String getChatImage() {
        return chatImage;
    }

    public void setChatImage(String chatImage) {
        this.chatImage = chatImage;
    }

    public List<FriendResponse> getFriendList() {
        return friendList;
    }

    public void setFriendList(List<FriendResponse> friendList) {
        this.friendList = friendList;
    }

    public String getCreatorId() {
        return creatorId;
    }

    public void setCreatorId(String creatorId) {
        this.creatorId = creatorId;
    }

    public String getCreatorUsername() {
        return creatorUsername;
    }

    public void setCreatorUsername(String creatorUsername) {
        this.creatorUsername = creatorUsername;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DialogProfileResponse that = (DialogProfileResponse) o;
        return Objects.equals(dialogId, that.dialogId) &&
                Objects.equals(dialogName, that.dialogName) &&
                Objects.equals(chatImage, that.chatImage) &&
                Objects.equals(friendList, that.friendList) &&
                Objects.equals(creatorId, that.creatorId) &&
                Objects.equals(creatorUsername, that.creatorUsername);
    }

    @Override
    public int hashCode() {
        return Objects.hash(dialogId, dialogName, chatImage, friendList, creatorId, creatorUsername);
    }
}
