package com.hwx.rx_chat.common.entity.rx;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

public class RxMessage implements Serializable {


    @SerializedName("id")
    private String id;

    @SerializedName("user_from_name")
    @JsonProperty("user_from_name")
    private String userFromName;

    @SerializedName("user_from_id")
    @JsonProperty("user_from_id")
    private String userFromId;

    @SerializedName("value")
    private String value;

    @SerializedName("date_sent")
    @JsonProperty("date_sent")
    private Date dateSent;

    @SerializedName("date_exp")
    @JsonProperty("date_exp")
    private Date dateExp;

    @SerializedName("is_expirable")
    @JsonProperty("is_expirable")
    private Boolean isExpirable;

    @SerializedName("id_dialog")
    @JsonProperty("id_dialog")
    private String idDialog;

    @SerializedName("is_edited")
    @JsonProperty("is_edited")
    private Boolean isEdited;

    @SerializedName("date_edited")
    @JsonProperty("date_edited")
    private Date dateEdited;

    @SerializedName("is_deleted")
    @JsonProperty("is_deleted")
    private Boolean isDeleted;

    @SerializedName("date_deleted")
    @JsonProperty("date_deleted")
    private Date dateDeleted;

    public RxMessage() {
    }

    public RxMessage(String id, String userFromName, String userFromId, String value, Date dateSent, String idDialog) {
        this.id = id;
        this.userFromName = userFromName;
        this.userFromId = userFromId;
        this.value = value;
        this.dateSent = dateSent;
        this.idDialog = idDialog;
        this.isDeleted = false;
        this.isEdited = false;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserFromName() {
        return userFromName;
    }

    public void setUserFromName(String userFromName) {
        this.userFromName = userFromName;
    }

    public String getUserFromId() {
        return userFromId;
    }

    public void setUserFromId(String userFromId) {
        this.userFromId = userFromId;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Date getDateSent() {
        return dateSent;
    }

    public void setDateSent(Date dateSent) {
        this.dateSent = dateSent;
    }

    public Date getDateExp() {
        return dateExp;
    }

    public void setDateExp(Date dateExp) {
        this.dateExp = dateExp;
    }

    public Boolean getExpirable() {
        return isExpirable;
    }

    public void setExpirable(Boolean expirable) {
        isExpirable = expirable;
    }

    public String getIdDialog() {
        return idDialog;
    }

    public void setIdDialog(String idDialog) {
        this.idDialog = idDialog;
    }

    public Boolean getEdited() {
        return isEdited;
    }

    public void setEdited(Boolean edited) {
        isEdited = edited;
    }

    public Date getDateEdited() {
        return dateEdited;
    }

    public void setDateEdited(Date dateEdited) {
        this.dateEdited = dateEdited;
    }

    public Boolean getDeleted() {
        return isDeleted;
    }

    public void setDeleted(Boolean deleted) {
        isDeleted = deleted;
    }

    public Date getDateDeleted() {
        return dateDeleted;
    }

    public void setDateDeleted(Date dateDeleted) {
        this.dateDeleted = dateDeleted;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RxMessage rxMessage = (RxMessage) o;
        return Objects.equals(id, rxMessage.id) &&
                Objects.equals(userFromName, rxMessage.userFromName) &&
                Objects.equals(userFromId, rxMessage.userFromId) &&
                Objects.equals(value, rxMessage.value) &&
                Objects.equals(dateSent, rxMessage.dateSent) &&
                Objects.equals(dateExp, rxMessage.dateExp) &&
                Objects.equals(isExpirable, rxMessage.isExpirable) &&
                Objects.equals(idDialog, rxMessage.idDialog) &&
                Objects.equals(isEdited, rxMessage.isEdited) &&
                Objects.equals(dateEdited, rxMessage.dateEdited) &&
                Objects.equals(isDeleted, rxMessage.isDeleted) &&
                Objects.equals(dateDeleted, rxMessage.dateDeleted);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, userFromName, userFromId, value, dateSent, dateExp, isExpirable, idDialog, isEdited, dateEdited, isDeleted, dateDeleted);
    }

    @Override
    public String toString() {
        return "RxMessage{" +
                "id='" + id + '\'' +
                ", userFromName='" + userFromName + '\'' +
                ", userFromId='" + userFromId + '\'' +
                ", value='" + value + '\'' +
                ", dateSent=" + dateSent +
                ", dateExp=" + dateExp +
                ", isExpirable=" + isExpirable +
                ", idDialog='" + idDialog + '\'' +
                ", isEdited=" + isEdited +
                ", dateEdited=" + dateEdited +
                ", isDeleted=" + isDeleted +
                ", dateDeleted=" + dateDeleted +
                '}';
    }

}
