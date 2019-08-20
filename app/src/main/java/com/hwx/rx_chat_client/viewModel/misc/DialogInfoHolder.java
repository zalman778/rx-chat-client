package com.hwx.rx_chat_client.viewModel.misc;

public class DialogInfoHolder {
    private String id;
    private boolean isPrivate;
    private String remoteProfileId;

    public DialogInfoHolder(String id, boolean isPrivate, String remoteProfileId) {
        this.id = id;
        this.isPrivate = isPrivate;
        this.remoteProfileId = remoteProfileId;
    }

    public String getId() {
        return id;
    }

    public boolean isPrivate() {
        return isPrivate;
    }

    public String getRemoteProfileId() {
        return remoteProfileId;
    }
}
