package com.hwx.rx_chat_client.background.p2p.service;

import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hwx.rx_chat.common.entity.rx.RxMessage;
import com.hwx.rx_chat_client.background.p2p.object.RxP2PController;
import com.hwx.rx_chat_client.background.p2p.object.RxP2PObject;
import com.hwx.rx_chat_client.background.p2p.object.type.ObjectType;

import java.util.Date;

import javax.inject.Inject;

import dagger.android.DaggerService;
import io.netty.handler.ssl.SslContext;
import io.reactivex.subjects.PublishSubject;

public class RxP2PService extends DaggerService {

    private static String LOG_TAG = "AVX";
    public static final int DEFAULT_PORT = 6000;

    private RxP2PServiceClient rxP2PServiceClient;
    private RxP2PController rxP2PController;

    //recieves all rxP2P objects from all inbox requestChannel..

    private PublishSubject<RxP2PObject> rxObj = PublishSubject.create();

    @Inject
    ObjectMapper objectMapper;

    @Inject
    SslContext sslContext;

    public class RxP2PServiceBinder extends Binder {
        public RxP2PService getService() {
            return RxP2PService.this;
        }
    }

    private final IBinder serviceBinder = new RxP2PService.RxP2PServiceBinder();

    @Override
    public IBinder onBind(Intent arg0) {
        return serviceBinder;
    }


    public RxP2PService() {
    }

    // public API:
    public void requestChannelByProfileInfo(String profileSocketInfo, String profileId) {
        rxP2PServiceClient.openConnection(profileSocketInfo, profileId);
        Log.i(LOG_TAG, "RxP2PService openned connection for ip = "+profileSocketInfo);

        rxP2PServiceClient.requestChannel(profileId);

        Log.i(LOG_TAG, "RxP2PService requested channel "+profileSocketInfo);

        sendTestData(profileId);



    }

    private void sendTestData(String profileId) {
        new Handler(Looper.getMainLooper()).postDelayed(()-> {
            RxP2PObject rxP2PObject = new RxP2PObject();
            RxMessage rxMessage = new RxMessage();
            rxMessage.setValue("test text");
            rxMessage.setUserFromName("alex");
            rxMessage.setDateSent(new Date());

            rxP2PObject.setMessage(rxMessage);
            rxP2PObject.setObjectType(ObjectType.MESSAGE);

            sendRxP2PObject(profileId, rxP2PObject);
            Log.i(LOG_TAG, "RxP2PService send test rxObj "+rxP2PObject);

            sendTestData(profileId);
        }, 1000);
    }

    public void sendRxP2PObject(String profileId, RxP2PObject rxP2PObject) {
        rxP2PServiceClient.getTxProcessor(profileId).onNext(rxP2PObject);
    }

    public PublishSubject<RxP2PObject> getRxObj() {
        return rxObj;
    }

    // end public API


    @Override
    public void onStart(Intent intent, int startId) {

        Log.i(LOG_TAG, "RxP2PService onStart");
        super.onStart(intent, startId);
    }

    @Override
    public void onCreate() {
        super.onCreate();


        Log.i(LOG_TAG, "RxP2PService onCreate");
        rxP2PServiceClient = new RxP2PServiceClient(sslContext, objectMapper);
        rxP2PController = new RxP2PController(objectMapper, rxObj);

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
}
