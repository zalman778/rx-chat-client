package com.hwx.rx_chat_client.p2p.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.hwx.rx_chat.common.object.rx.RxObject;
import com.hwx.rx_chat_client.di.RxP2PModule;
import com.hwx.rx_chat_client.di.DaggerRxP2PServiceComponent;

import javax.inject.Inject;

import io.reactivex.subjects.ReplaySubject;

public class RxP2PService extends Service {

    private static String LOG_TAG = "AVX";

    private ReplaySubject<RxObject> rsRxObjectsRecieved = ReplaySubject.create();

    public RxP2PService() {
    }

    @Inject
    RSocketP2PController rSocketP2PController;

    @Inject
    RSocketP2PClient rSocketP2PClient;

    @Override
    public void onStart(Intent intent, int startId) {

        Log.i(LOG_TAG, "RxP2PService onStart");
        super.onStart(intent, startId);
    }

    @Override
    public void onCreate() {
        Log.i(LOG_TAG, "RxP2PService onCreate");
       // super.onCreate();

        DaggerRxP2PServiceComponent
                .builder()
                .rxP2PModule(new RxP2PModule(this))
                .build()
                .injectRxP2PService(this);

    }


    @Override
    public IBinder onBind(Intent intent) {
        Log.i(LOG_TAG,"RxP2PService onBind");
        return null;
    }

    public ReplaySubject<RxObject> getRsRxObjectsRecieved() {
        return rsRxObjectsRecieved;
    }



    @Override
    public void onRebind(Intent intent) {
        Log.i(LOG_TAG, "onRebind");
        super.onRebind(intent);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.i(LOG_TAG, "onUnbind");
        return true;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(LOG_TAG, "onDestroy");
    }


    private Integer val = 0;
    public Integer getValue() {
        return val++;
    }
}
