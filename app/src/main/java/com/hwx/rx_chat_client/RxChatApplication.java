package com.hwx.rx_chat_client;

import android.app.Activity;
import android.app.Application;
import android.app.Service;
import android.content.Context;

import com.hwx.rx_chat_client.di.AppInjector;
import com.squareup.leakcanary.LeakCanary;

import javax.inject.Inject;

import dagger.android.DispatchingAndroidInjector;
import dagger.android.HasActivityInjector;
import dagger.android.HasServiceInjector;


public class RxChatApplication extends Application
                               implements HasActivityInjector, HasServiceInjector {

    @Inject
    DispatchingAndroidInjector<Activity> activityInjector;
    @Inject
    DispatchingAndroidInjector<Service> serviceInjector;

    Context context;

    @Override
    public void onCreate() {
        super.onCreate();


       // appComponent.inject(this);
        AppInjector.init(this);

        context = this;

        if (LeakCanary.isInAnalyzerProcess(this)) {
            return;
        }
        LeakCanary.install(this);
    }

    @Override
    protected void attachBaseContext(Context context) {
        super.attachBaseContext(context);
    }

    @Override
    public DispatchingAndroidInjector<Activity> activityInjector() {
        return activityInjector;
    }

    @Override
    public DispatchingAndroidInjector<Service> serviceInjector() {
        return serviceInjector;
    }
}
