package com.hwx.rx_chat_client.repository;

import com.hwx.rx_chat.common.entity.rx.RxMessage;
import com.hwx.rx_chat.common.request.ProfileInfoUpdateRequest;
import com.hwx.rx_chat.common.request.SignupRequest;
import com.hwx.rx_chat.common.response.DefaultResponse;
import com.hwx.rx_chat.common.response.DialogResponse;
import com.hwx.rx_chat.common.response.FriendResponse;
import com.hwx.rx_chat.common.response.LoginResponse;
import com.hwx.rx_chat.common.response.UserDetailsResponse;
import com.hwx.rx_chat_client.service.ChatService;

import java.util.List;
import java.util.Map;

import io.reactivex.Observable;
import okhttp3.MultipartBody;
import retrofit2.Response;


public class ChatRepository {
    private ChatService chatService;

    public ChatRepository(ChatService chatService) {
        this.chatService = chatService;
    }

    public Observable<LoginResponse> authorize(String username, String password) {
        return chatService.authorize(username, password);
    }

    public Observable<Response<LoginResponse>> authorizeWithResponse(String username, String password) {
        return chatService.authorizeWithResponse(username, password);
    }

    public Observable<DefaultResponse> signUpUser(SignupRequest signupRequest) {
        return chatService.signUpUser(signupRequest);
    }

    public Observable<List<FriendResponse>> searchUsers(String url, Map<String, String> headersMap) {
        return chatService.searchUsers(url, headersMap);
    }

    public Observable<List<DialogResponse>> getDialogList(String url, Map<String, String> headersMap) {
        return chatService.getDialogList(url, headersMap);
    }

    public Observable<List<FriendResponse>> getFriendList(String url, Map<String, String> headersMap) {
        return chatService.getFriendsList(url, headersMap);
    }

    public Observable<UserDetailsResponse> getProfileInfo(String url, Map<String, String> headersMap) {
        return chatService.getProfileInfo(url, headersMap);
    }

    public Observable<List<RxMessage>> getMessageList(String url, Map<String, String> headersMap) {
        return chatService.getMessageList(url, headersMap);
    }



    public Observable<DefaultResponse> updateProfilePic(Map<String, String> headersMap, MultipartBody.Part img) {
        return chatService.updateProfilePic(headersMap, img);
    }

    public Observable<DefaultResponse> updateProfileBio(Map<String, String> headersMap, ProfileInfoUpdateRequest profileInfoUpdateRequest) {
        return chatService.updateProfileBio(headersMap, profileInfoUpdateRequest);
    }


}
