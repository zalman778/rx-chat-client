package com.hwx.rx_chat_client.background.service;

import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hwx.rx_chat.common.object.rx.RxObject;
import com.hwx.rx_chat.common.object.rx.types.ObjectType;
import com.hwx.rx_chat.common.object.rx.types.SettingType;
import com.hwx.rx_chat_client.rsocket.RxServiceClient;

import javax.inject.Inject;

import dagger.android.DaggerService;
import io.netty.handler.ssl.SslContext;
import io.reactivex.subjects.PublishSubject;

/*
    Фоновый сервис для общения с сервером.
 */
public class RxService extends DaggerService {

    private static String LOG_TAG = "AVX";
    private RxServiceClient rxServiceClient;

    public RxService() {
    }

    @Inject
    ObjectMapper objectMapper;

    @Inject
    SslContext sslContext;



    public class RxServiceBinder extends Binder {
        public RxService getService() {
            return RxService.this;
        }
    }

    private final IBinder serviceBinder = new RxServiceBinder();

    @Override
    public IBinder onBind(Intent arg0) {
        return serviceBinder;
    }

    @Override
    public void onStart(Intent intent, int startId) {

        Log.i(LOG_TAG, "RxService onStart");
        super.onStart(intent, startId);
    }

    @Override
    public void onCreate() {
        Log.i(LOG_TAG, "RxService onCreate");
        super.onCreate();

        rxServiceClient = new RxServiceClient(sslContext, objectMapper);
        rxServiceClient.connect();
        rxServiceClient.requestChannel();

        Log.i(LOG_TAG, "RxService created rxClient");

    }

    //********** public API:



    public void sendRxObject(RxObject rxObject) {
        Log.w(LOG_TAG, "sent rxObj =" +rxObject);
        rxServiceClient.getTxProcessor().onNext(rxObject);
    }

    public PublishSubject<RxObject> getPpRxProcessor() {
        return rxServiceClient.getPsRxMessage();
    }



    //********** public API:


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return this.START_STICKY;
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
