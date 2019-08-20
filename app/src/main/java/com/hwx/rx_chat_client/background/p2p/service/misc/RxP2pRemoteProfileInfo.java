package com.hwx.rx_chat_client.background.p2p.service.misc;

public class RxP2pRemoteProfileInfo {
    private String avatarUrl;
    private String caption;

    public RxP2pRemoteProfileInfo(String avatarUrl, String caption) {
        this.avatarUrl = avatarUrl;
        this.caption = caption;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public String getCaption() {
        return caption;
    }
}
