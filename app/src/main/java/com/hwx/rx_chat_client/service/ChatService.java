package com.hwx.rx_chat_client.service;

import com.hwx.rx_chat.common.entity.rx.RxMessage;
import com.hwx.rx_chat.common.request.SignupRequest;
import com.hwx.rx_chat.common.response.DefaultResponse;
import com.hwx.rx_chat.common.response.DialogResponse;
import com.hwx.rx_chat.common.response.LoginResponse;
import com.hwx.rx_chat_client.Configuration;

import java.util.List;
import java.util.Map;

import io.reactivex.Observable;
import retrofit2.Response;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.HeaderMap;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface ChatService {

    @FormUrlEncoded
    @POST(Configuration.URL_LOGIN_REQUEST)
    Observable<LoginResponse> authorize(
              @Field("username") String username
            , @Field("passwordHash") String password
    );

    @FormUrlEncoded
    @POST(Configuration.URL_LOGIN_REQUEST)
    Observable<Response<LoginResponse>> authorizeWithResponse(
              @Field("username") String username
            , @Field("passwordHash") String password
    );

    @FormUrlEncoded
    @POST(Configuration.URL_DIALOGS_LIST)
    Observable<List<DialogResponse>> getDialogList(
              @HeaderMap Map<String, String> headers
            , @Field("userId") String userId
    );

    @FormUrlEncoded
    @POST(Configuration.URL_MESSAGES_LIST)
    Observable<List<RxMessage>> getMessageList(
              @HeaderMap Map<String, String> headers
            , @Field("dialogId") String dialogId
    );

//    @FormUrlEncoded
//    @Headers("Content-Type: application/json")
    @POST(Configuration.URL_SIGNUP_USER)
    Observable<DefaultResponse> signUpUser(
            @Body SignupRequest signupRequest
    );



}
