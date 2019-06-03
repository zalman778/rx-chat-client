package com.hwx.rx_chat.common.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

public class DialogResponse implements Serializable {

    @SerializedName("dialog_id")
    @JsonProperty("dialog_id")
    private String dialogId;

    @SerializedName("dialog_name")
    @JsonProperty("dialog_name")
    private String dialogName;

    @SerializedName("last_date")
    @JsonProperty("last_date")
    private Date lastDate;

    @SerializedName("last_user")
    @JsonProperty("last_user")
    private String lastUser;

    @SerializedName("last_message")
    @JsonProperty("last_message")
    private String lastMessage;

    @SerializedName("chat_image")
    @JsonProperty("chat_image")
    private String chatImage;

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

    public Date getLastDate() {
        return lastDate;
    }

    public void setLastDate(Date lastDate) {
        this.lastDate = lastDate;
    }

    public String getLastUser() {
        return lastUser;
    }



    public void setLastUser(String lastUser) {
        this.lastUser = lastUser;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public String getChatImage() {
        return chatImage;
    }

    public void setChatImage(String chatImage) {
        this.chatImage = chatImage;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DialogResponse that = (DialogResponse) o;
        return Objects.equals(dialogId, that.dialogId) &&
                Objects.equals(dialogName, that.dialogName) &&
                Objects.equals(lastDate, that.lastDate) &&
                Objects.equals(lastUser, that.lastUser) &&
                Objects.equals(lastMessage, that.lastMessage) &&
                Objects.equals(chatImage, that.chatImage);
    }

    @Override
    public int hashCode() {
        return Objects.hash(dialogId, dialogName, lastDate, lastUser, lastMessage, chatImage);
    }

    @Override
    public String toString() {
        return "DialogResponse{" +
                "dialogId='" + dialogId + '\'' +
                ", dialogName='" + dialogName + '\'' +
                ", lastDate=" + lastDate +
                ", lastUser='" + lastUser + '\'' +
                ", lastMessage='" + lastMessage + '\'' +
                ", chatImage='" + chatImage + '\'' +
                '}';
    }
}
