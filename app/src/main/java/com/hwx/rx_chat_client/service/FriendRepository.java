package com.hwx.rx_chat_client.service;

import com.hwx.rx_chat.common.response.DefaultResponse;

import java.util.Map;

import io.reactivex.Observable;

public class FriendRepository {
    private FriendService friendService;

    public FriendRepository(FriendService friendService) {
        this.friendService = friendService;
    }

    public Observable<DefaultResponse> acceptFriendRequest(
              Map<String, String> headers
            , String requestId
    ) {
        return friendService.acceptFriendRequest(headers, requestId);
    }

    public Observable<DefaultResponse> rejectFriendRequest(
            Map<String, String> headers
            , String requestId
    ) {
        return friendService.rejectFriendRequest(headers, requestId);
    }
}
