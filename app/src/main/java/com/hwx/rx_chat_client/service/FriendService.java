package com.hwx.rx_chat_client.service;

import com.hwx.rx_chat.common.response.DefaultResponse;

import java.util.Map;

import io.reactivex.Observable;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.HeaderMap;
import retrofit2.http.POST;
import retrofit2.http.Url;

public interface FriendService {

    @FormUrlEncoded
    @POST
    Observable<DefaultResponse> acceptFriendRequest(
              @Url String url
            , @HeaderMap Map<String, String> headers
            , @Field("requestId") String requestId
    );

    @FormUrlEncoded
    @POST
    Observable<DefaultResponse> rejectFriendRequest(
              @Url String url
            , @HeaderMap Map<String, String> headers
            , @Field("requestId") String requestId
    );

    @GET
    Observable<DefaultResponse> createFriendRequest(
              @Url String url
            , @HeaderMap Map<String, String> headers
    );
}
