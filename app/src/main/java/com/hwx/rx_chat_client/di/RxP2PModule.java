package com.hwx.rx_chat_client.di;

import com.hwx.rx_chat_client.p2p.service.RxP2PService;

import dagger.Module;
import dagger.Provides;

@Module
public class RxP2PModule {

    RxP2PService rxP2PService;

    public RxP2PModule(RxP2PService rxP2PService) {
        this.rxP2PService = rxP2PService;
    }

    @Provides
    RxP2PService provideRxService() {
        return rxP2PService;
    }
}
