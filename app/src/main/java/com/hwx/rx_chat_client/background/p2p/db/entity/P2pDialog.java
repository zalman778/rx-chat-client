package com.hwx.rx_chat_client.background.p2p.db.entity;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

import com.hwx.rx_chat.common.response.DialogResponse;

import java.util.Date;
import java.util.UUID;

@Entity
public class P2pDialog {

    @PrimaryKey
    @NonNull
    private String id = UUID.randomUUID().toString();

    private String caption;

    @ColumnInfo(name = "date_created")
    private Date dateCreated;

    @ColumnInfo(name = "remote_profile_id")
    private String remoteProfileId;

    @ColumnInfo(name = "profile_id")
    private String profileId;



    public P2pDialog() {
    }

    @NonNull
    public String getId() {
        return id;
    }

    public void setId(@NonNull String id) {
        this.id = id;
    }

    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    public Date getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(Date dateCreated) {
        this.dateCreated = dateCreated;
    }

    public String getRemoteProfileId() {
        return remoteProfileId;
    }

    public void setRemoteProfileId(String remoteProfileId) {
        this.remoteProfileId = remoteProfileId;
    }

    public String getProfileId() {
        return profileId;
    }

    public void setProfileId(String profileId) {
        this.profileId = profileId;
    }

    public DialogResponse toDialogResponse() {
        DialogResponse dialogResponse = new DialogResponse();
        dialogResponse.setDialogName(caption);
        dialogResponse.setDialogId(id);
        dialogResponse.setPrivate(true);
        dialogResponse.setRemoteProfileId(remoteProfileId);
        return dialogResponse;
    }

}
