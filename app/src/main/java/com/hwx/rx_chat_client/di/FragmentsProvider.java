package com.hwx.rx_chat_client.di;

import com.hwx.rx_chat_client.fragment.DialogsFragment;
import com.hwx.rx_chat_client.fragment.FriendsFragment;
import com.hwx.rx_chat_client.fragment.HomeFragment;
import com.hwx.rx_chat_client.fragment.ProfileFragment;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;

@Module
public abstract class FragmentsProvider {

    @ContributesAndroidInjector()
    abstract DialogsFragment provideDialogsFragmentFactory();

    @ContributesAndroidInjector()
    abstract FriendsFragment provideFriendsFragmentFactory();

    @ContributesAndroidInjector()
    abstract HomeFragment provideHomeFragmentFactory();

    @ContributesAndroidInjector()
    abstract ProfileFragment provideProfileFragmentFactory();
}