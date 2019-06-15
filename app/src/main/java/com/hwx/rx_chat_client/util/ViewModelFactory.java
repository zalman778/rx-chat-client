package com.hwx.rx_chat_client.util;

import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.support.annotation.NonNull;

import com.hwx.rx_chat_client.rsocket.ChatSocket;
import com.hwx.rx_chat_client.rsocket.SocketServer;
import com.hwx.rx_chat_client.service.ChatRepository;
import com.hwx.rx_chat_client.service.DialogRepository;
import com.hwx.rx_chat_client.service.FriendRepository;
import com.hwx.rx_chat_client.viewModel.HomeViewModel;
import com.hwx.rx_chat_client.viewModel.LoginViewModel;
import com.hwx.rx_chat_client.viewModel.SignupViewModel;
import com.hwx.rx_chat_client.viewModel.conversation.ConversationViewModel;
import com.hwx.rx_chat_client.viewModel.friend.AddFriendViewModel;
import com.hwx.rx_chat_client.viewModel.friend.ProfileViewModel;
import com.squareup.picasso.Picasso;

import javax.inject.Inject;

public class ViewModelFactory implements ViewModelProvider.Factory {

    private ChatRepository chatRepository;
    private FriendRepository friendRepository;
    private DialogRepository dialogRepository;
    private ResourceProvider resourceProvider;
    private SharedPreferencesProvider sharedPreferencesProvider;
    private ChatSocket chatSocket;
    private SocketServer socketServer;
    private Picasso picasso;


    @Inject
    public ViewModelFactory(
            ChatRepository chatRepository
            , FriendRepository friendRepository
            , DialogRepository dialogRepository, ResourceProvider resourceProvider
            , SharedPreferencesProvider sharedPreferencesProvider
            , ChatSocket chatSocket
            , SocketServer socketServer
            , Picasso picasso

    ) {
        this.chatRepository = chatRepository;
        this.friendRepository = friendRepository;
        this.dialogRepository = dialogRepository;
        this.resourceProvider = resourceProvider;
        this.sharedPreferencesProvider = sharedPreferencesProvider;
        this.chatSocket = chatSocket;
        this.socketServer = socketServer;
        this.picasso = picasso;
    }


    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(LoginViewModel.class)) {
            return (T) new LoginViewModel(chatRepository);
        }
        if (modelClass.isAssignableFrom(HomeViewModel.class)) {
            return (T) new HomeViewModel(chatRepository, friendRepository, resourceProvider, sharedPreferencesProvider, picasso);
        }
        if (modelClass.isAssignableFrom(ConversationViewModel.class)) {
            return (T) new ConversationViewModel(chatRepository, resourceProvider, sharedPreferencesProvider, chatSocket, picasso);
        }
        if (modelClass.isAssignableFrom(SignupViewModel.class)) {
            return (T) new SignupViewModel(chatRepository, resourceProvider, sharedPreferencesProvider);
        }
        if (modelClass.isAssignableFrom(AddFriendViewModel.class)) {
            return (T) new AddFriendViewModel(friendRepository, chatRepository, sharedPreferencesProvider, picasso);
        }
        if (modelClass.isAssignableFrom(ProfileViewModel.class)) {
            return (T) new ProfileViewModel(sharedPreferencesProvider, chatRepository, dialogRepository, picasso);
        }
        throw new IllegalArgumentException("Unknown class name");
    }
}
