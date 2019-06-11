package com.hwx.rx_chat_client.service;

import com.hwx.rx_chat.common.entity.rx.RxMessage;
import com.hwx.rx_chat.common.request.ProfileInfoUpdateRequest;
import com.hwx.rx_chat.common.request.SignupRequest;
import com.hwx.rx_chat.common.response.DefaultResponse;
import com.hwx.rx_chat.common.response.DialogResponse;
import com.hwx.rx_chat.common.response.LoginResponse;

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

    public Observable<List<DialogResponse>> getDialogList(Map<String, String> headersMap, String userId) {
        return chatService.getDialogList(headersMap, userId);
    }

    public Observable<List<RxMessage>> getMessageList(Map<String, String> headersMap, String dialogId) {
        return chatService.getMessageList(headersMap, dialogId);
    }

    public Observable<DefaultResponse> signUpUser(SignupRequest signupRequest) {
        return chatService.signUpUser(signupRequest);
    }

    public Observable<DefaultResponse> updateProfilePic(Map<String, String> headersMap, MultipartBody.Part img) {
        return chatService.updateProfilePic(headersMap, img);
    }

    public Observable<DefaultResponse> updateProfileBio(Map<String, String> headersMap, ProfileInfoUpdateRequest profileInfoUpdateRequest) {
        return chatService.updateProfileBio(headersMap, profileInfoUpdateRequest);
    }


}
