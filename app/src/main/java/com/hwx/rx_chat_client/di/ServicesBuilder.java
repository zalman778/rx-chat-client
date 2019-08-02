package com.hwx.rx_chat_client.di;

import com.hwx.rx_chat_client.background.service.RxService;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;

@Module
abstract class ServicesBuilder {

    @ContributesAndroidInjector
    abstract RxService provideRxService();

}
