package com.hwx.rx_chat_client.di;

import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;

import com.hwx.rx_chat_client.ui.ViewModelFactory;
import com.hwx.rx_chat_client.viewModel.HomeViewModel;
import com.hwx.rx_chat_client.viewModel.LoginViewModel;
import com.hwx.rx_chat_client.viewModel.SignupViewModel;
import com.hwx.rx_chat_client.viewModel.conversation.ConversationViewModel;
import com.hwx.rx_chat_client.viewModel.conversation.CreateDialogViewModel;
import com.hwx.rx_chat_client.viewModel.conversation.DialogProfileViewModel;
import com.hwx.rx_chat_client.viewModel.conversation.P2pConversationViewModel;
import com.hwx.rx_chat_client.viewModel.friend.AddFriendViewModel;
import com.hwx.rx_chat_client.viewModel.friend.ProfileViewModel;

import dagger.Binds;
import dagger.Module;
import dagger.multibindings.IntoMap;

@Module
public abstract class ViewModelModule {

    @Binds
    @IntoMap
    @ViewModelKey(ConversationViewModel.class)
    abstract ViewModel bindConversationViewModel(ConversationViewModel conversationViewModel);

    @Binds
    @IntoMap
    @ViewModelKey(P2pConversationViewModel.class)
    abstract ViewModel bindP2pConversationViewModel(P2pConversationViewModel p2pConversationViewModel);

    @Binds
    @IntoMap
    @ViewModelKey(CreateDialogViewModel.class)
    abstract ViewModel bindCreateDialogViewModel(CreateDialogViewModel createDialogViewModel);

    @Binds
    @IntoMap
    @ViewModelKey(DialogProfileViewModel.class)
    abstract ViewModel vindDialogProfileViewModel(DialogProfileViewModel dialogProfileViewModel);

    @Binds
    @IntoMap
    @ViewModelKey(AddFriendViewModel.class)
    abstract ViewModel bindAddFriendViewModel(AddFriendViewModel addFriendViewModel);

    @Binds
    @IntoMap
    @ViewModelKey(ProfileViewModel.class)
    abstract ViewModel bindProfileViewModel(ProfileViewModel profileViewModel);

    @Binds
    @IntoMap
    @ViewModelKey(HomeViewModel.class)
    abstract ViewModel bindHomeViewModel(HomeViewModel homeViewModel);

    @Binds
    @IntoMap
    @ViewModelKey(LoginViewModel.class)
    abstract ViewModel bindLoginViewModel(LoginViewModel loginViewModel);

    @Binds
    @IntoMap
    @ViewModelKey(SignupViewModel.class)
    abstract ViewModel bindSignupViewModel(SignupViewModel signupViewModel);

    @Binds
    abstract ViewModelProvider.Factory bindViewModelFactory(ViewModelFactory factory);

}
