package com.hwx.rx_chat_client.di;

import com.hwx.rx_chat_client.view.ConversationActivity;
import com.hwx.rx_chat_client.view.HomeActivity;
import com.hwx.rx_chat_client.view.LoginActivity;
import com.hwx.rx_chat_client.view.SignupActivity;

import javax.inject.Singleton;

import dagger.Component;

@Component(modules = {UtilsModule.class})
@Singleton
public interface AppComponent {

    void doInjectLoginActivity(LoginActivity loginActivity);
    void doInjectHomeActivity(HomeActivity mainActivity);
    void doInjectConversationActivity(ConversationActivity conversationActivity);
    void doInjectSignupRepository(SignupActivity signupActivity);
}
