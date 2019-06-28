package com.hwx.rx_chat_client.service;

import com.hwx.rx_chat.common.entity.rx.RxMessage;
import com.hwx.rx_chat.common.request.ProfileInfoUpdateRequest;
import com.hwx.rx_chat.common.request.SignupRequest;
import com.hwx.rx_chat.common.response.DefaultResponse;
import com.hwx.rx_chat.common.response.DialogResponse;
import com.hwx.rx_chat.common.response.FriendResponse;
import com.hwx.rx_chat.common.response.LoginResponse;
import com.hwx.rx_chat.common.response.UserDetailsResponse;
import com.hwx.rx_chat_client.Configuration;

import java.util.List;
import java.util.Map;

import io.reactivex.Observable;
import okhttp3.MultipartBody;
import retrofit2.Response;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.HeaderMap;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Url;

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

    @POST(Configuration.URL_SIGNUP_USER)
    Observable<DefaultResponse> signUpUser(
            @Body SignupRequest signupRequest
    );

    @GET
    Observable<List<FriendResponse>> searchUsers(
              @Url String url
            , @HeaderMap Map<String, String> headers
    );

    @GET
    Observable<UserDetailsResponse> getProfileInfo(
              @Url String url
            , @HeaderMap Map<String, String> headers
    );

    @GET
    Observable<List<DialogResponse>> getDialogList(
              @Url String url
            , @HeaderMap Map<String, String> headers
    );

    @GET
    Observable<List<FriendResponse>> getFriendsList(
              @Url String url
            , @HeaderMap Map<String, String> headers
    );

    @GET
    Observable<List<RxMessage>> getMessageList(
              @Url String url
            , @HeaderMap Map<String, String> headers
    );

    @POST(Configuration.URL_UPLOAD_PROFILE_PIC)
    @Multipart
    Observable<DefaultResponse> updateProfilePic(
             @HeaderMap Map<String, String> headers
           , @Part MultipartBody.Part img
    );

    @POST(Configuration.URL_UPLOAD_PROFILE_BIO)
    Observable<DefaultResponse> updateProfileBio(
              @HeaderMap Map<String, String> headers
            , @Body ProfileInfoUpdateRequest profileInfoUpdateRequest
    );
}
