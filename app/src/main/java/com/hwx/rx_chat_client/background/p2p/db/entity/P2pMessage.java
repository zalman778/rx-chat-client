package com.hwx.rx_chat_client.background.p2p.db.entity;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

import com.hwx.rx_chat.common.entity.rx.RxMessage;

import java.util.Date;
import java.util.UUID;
@Entity(foreignKeys =
    @ForeignKey(entity = P2pDialog.class, parentColumns = "id", childColumns = "id_dialog")
)
public class P2pMessage {

    @PrimaryKey
    @NonNull
    private String id = UUID.randomUUID().toString();

    @ColumnInfo(name = "user_from_name")
    private String userFromName;

    @ColumnInfo(name = "user_from_id")
    private String userFromId;

    private String value;

    @ColumnInfo(name="date_sent")
    private Date dateSent;

    @ColumnInfo(name = "date_exp")
    private Date dateExp;

    @ColumnInfo(name = "is_expirable")
    private Boolean isExpirable;

    @ColumnInfo(name = "id_dialog")
    private String idDialog;

    @ColumnInfo(name = "date_edited")
    private Date dateEdited;

    @ColumnInfo(name = "is_deleted")
    private Boolean isDeleted;

    @ColumnInfo(name = "date_deleted")
    private Date dateDeleted;

    @ColumnInfo(name = "image_url")
    private String imageUrl;
    /*
        Картинка генерится и хранится от названия чата
     */

    public P2pMessage() {
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

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    @Override
    public String toString() {
        return "P2pMessage{" +
                "id='" + id + '\'' +
                ", userFromName='" + userFromName + '\'' +
                ", userFromId='" + userFromId + '\'' +
                ", value='" + value + '\'' +
                ", dateSent=" + dateSent +
                ", dateExp=" + dateExp +
                ", isExpirable=" + isExpirable +
                ", idDialog='" + idDialog + '\'' +
                ", dateEdited=" + dateEdited +
                ", isDeleted=" + isDeleted +
                ", dateDeleted=" + dateDeleted +
                ", imageUrl='" + imageUrl + '\'' +
                '}';
    }

    public P2pMessage(RxMessage rxMessage) {
        dateDeleted = rxMessage.getDateDeleted();
        dateEdited = rxMessage.getDateEdited();
        dateExp = rxMessage.getDateExp();
        dateSent = rxMessage.getDateSent();
        idDialog = rxMessage.getIdDialog();
        imageUrl = rxMessage.getImageUrl();
        isDeleted = rxMessage.getDeleted();
        isExpirable = rxMessage.getExpirable();
        userFromId = rxMessage.getUserFromId();
        userFromName = rxMessage.getUserFromName();
        value = rxMessage.getValue();
        id = rxMessage.getId();
    }

    public RxMessage toRxMessage() {
        RxMessage rxMessage = new RxMessage();
        rxMessage.setDateDeleted(dateDeleted);
        rxMessage.setDateEdited(dateEdited);
        rxMessage.setDateExp(dateExp);
        rxMessage.setDateSent(dateSent);
        rxMessage.setIdDialog(idDialog);
        rxMessage.setImageUrl(imageUrl);
        rxMessage.setDeleted(isDeleted);
        rxMessage.setExpirable(isExpirable);
        rxMessage.setUserFromId(userFromId);
        rxMessage.setUserFromName(userFromName);
        rxMessage.setValue(value);
        rxMessage.setId(id);
        return rxMessage;
    }
}
