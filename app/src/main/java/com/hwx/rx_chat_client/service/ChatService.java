package com.hwx.rx_chat_client.service;

import com.hwx.rx_chat.common.entity.rx.RxMessage;
import com.hwx.rx_chat.common.request.ProfileInfoUpdateRequest;
import com.hwx.rx_chat.common.request.SignupRequest;
import com.hwx.rx_chat.common.response.DefaultResponse;
import com.hwx.rx_chat.common.response.DialogResponse;
import com.hwx.rx_chat.common.response.FriendResponse;
import com.hwx.rx_chat.common.response.LoginResponse;
import com.hwx.rx_chat.common.response.UserDetailsResponse;

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
    @POST
    Observable<LoginResponse> authorize(
              @Url String url
            , @Field("username") String username
            , @Field("passwordHash") String password
    );

    @FormUrlEncoded
    @POST
    Observable<Response<LoginResponse>> authorizeWithResponse(
              @Url String url
            , @Field("username") String username
            , @Field("passwordHash") String password
    );

    @POST
    Observable<DefaultResponse> signUpUser(
              @Url String url
            , @Body SignupRequest signupRequest
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

    @POST
    @Multipart
    Observable<DefaultResponse> updateProfilePic(
              @Url String url
            , @HeaderMap Map<String, String> headers
            , @Part MultipartBody.Part img
    );

    @POST
    Observable<DefaultResponse> updateProfileBio(
              @Url String url
            , @HeaderMap Map<String, String> headers
            , @Body ProfileInfoUpdateRequest profileInfoUpdateRequest
    );
}
