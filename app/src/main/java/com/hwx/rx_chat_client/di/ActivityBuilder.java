package com.hwx.rx_chat_client.di;

import com.hwx.rx_chat_client.view.HomeActivity;
import com.hwx.rx_chat_client.view.LoginActivity;
import com.hwx.rx_chat_client.view.SignupActivity;
import com.hwx.rx_chat_client.view.dialog.ConversationActivity;
import com.hwx.rx_chat_client.view.dialog.CreateDialogActivity;
import com.hwx.rx_chat_client.view.dialog.DialogProfileActivity;
import com.hwx.rx_chat_client.view.friend.AddFriendActivity;
import com.hwx.rx_chat_client.view.friend.ProfileActivity;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;

@Module
public abstract class ActivityBuilder {

    @ContributesAndroidInjector()
    abstract ConversationActivity bindConversationActivity();

    @ContributesAndroidInjector()
    abstract CreateDialogActivity bindCreateDialogActivity();

    @ContributesAndroidInjector()
    abstract DialogProfileActivity bindDialogProfileActivity();

    @ContributesAndroidInjector()
    abstract AddFriendActivity bindAddFriendActivity();

    @ContributesAndroidInjector()
    abstract ProfileActivity bindProfileActivity();

    @ContributesAndroidInjector(modules = FragmentsProvider.class)
    abstract HomeActivity bindHomeActivity();

    @ContributesAndroidInjector()
    abstract LoginActivity bindLoginActivity();

    @ContributesAndroidInjector()
    abstract SignupActivity bindSignupActivity();

}
