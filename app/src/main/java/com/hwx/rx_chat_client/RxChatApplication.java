package com.hwx.rx_chat_client;

import android.app.Application;
import android.content.Context;

import com.hwx.rx_chat_client.di.AppComponent;
import com.hwx.rx_chat_client.di.DaggerAppComponent;
import com.hwx.rx_chat_client.di.UtilsModule;
import com.squareup.leakcanary.LeakCanary;


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

        if (LeakCanary.isInAnalyzerProcess(this)) {
            // This process is dedicated to LeakCanary for heap analysis.
            // You should not init your app in this process.
            return;
        }
        LeakCanary.install(this);
    }

    public AppComponent getAppComponent() {
        return appComponent;
    }

    @Override
    protected void attachBaseContext(Context context) {
        super.attachBaseContext(context);
    }

}
