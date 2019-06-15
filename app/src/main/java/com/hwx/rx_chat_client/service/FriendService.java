package com.hwx.rx_chat_client.service;

import com.hwx.rx_chat.common.response.DefaultResponse;
import com.hwx.rx_chat_client.Configuration;

import java.util.Map;

import io.reactivex.Observable;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.HeaderMap;
import retrofit2.http.POST;

public interface FriendService {

    @FormUrlEncoded
    @POST(Configuration.URL_FRIENDS_REQUEST_ACCEPT)
    Observable<DefaultResponse> acceptFriendRequest(
              @HeaderMap Map<String, String> headers
            , @Field("requestId") String requestId
    );

    @FormUrlEncoded
    @POST(Configuration.URL_FRIENDS_REQUEST_REJECT)
    Observable<DefaultResponse> rejectFriendRequest(
              @HeaderMap Map<String, String> headers
            , @Field("requestId") String requestId
    );
}
