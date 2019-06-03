package com.hwx.rx_chat_client;

import android.app.Application;
import android.content.Context;

import com.hwx.rx_chat_client.di.AppComponent;
import com.hwx.rx_chat_client.di.DaggerAppComponent;
import com.hwx.rx_chat_client.di.UtilsModule;


public class RxChatApplication extends Application {

    AppComponent appComponent;
    Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        context = this;
        appComponent = DaggerAppComponent
                .builder()
                .utilsModule(new UtilsModule(this))
                .build();
    }

    public AppComponent getAppComponent() {
        return appComponent;
    }

    @Override
    protected void attachBaseContext(Context context) {
        super.attachBaseContext(context);
    }

}
