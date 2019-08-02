package com.hwx.rx_chat_client.di;

import com.hwx.rx_chat_client.p2p.service.RxP2PService;

import javax.inject.Singleton;

import dagger.Component;

@Component(modules = RxP2PModule.class)
@Singleton
public interface RxP2PServiceComponent {
    void injectRxP2PService(RxP2PService rxP2PService);

}
