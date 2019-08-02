package com.hwx.rx_chat_client.di;

import com.hwx.rx_chat_client.RxChatApplication;

import javax.inject.Singleton;

import dagger.BindsInstance;
import dagger.Component;
import dagger.android.AndroidInjector;
import dagger.android.support.AndroidSupportInjectionModule;

@Component(modules = {
        UtilsModule.class,
        ServicesBuilder.class,
        AndroidSupportInjectionModule.class,
        ActivityBuilder.class,
        ViewModelModule.class
})
@Singleton
public interface AppComponent extends AndroidInjector<RxChatApplication> {

    @Component.Builder
    interface Builder {
        @BindsInstance
        Builder application(RxChatApplication application);
        Builder utilsModule(UtilsModule um);
        AppComponent build();
    }

    void inject(RxChatApplication rxChatApplication);
}
