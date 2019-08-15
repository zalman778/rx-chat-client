package com.hwx.rx_chat_client.background.p2p.service;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hwx.rx_chat_client.background.p2p.db.P2pDatabase;
import com.hwx.rx_chat_client.background.p2p.db.entity.P2pMessage;
import com.hwx.rx_chat_client.background.p2p.db.service.P2pDbService;
import com.hwx.rx_chat_client.background.p2p.object.PipeHolder;
import com.hwx.rx_chat_client.background.p2p.object.RxP2PController;
import com.hwx.rx_chat_client.background.p2p.object.RxP2PObject;
import com.hwx.rx_chat_client.background.p2p.object.type.ObjectType;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import dagger.android.DaggerService;
import io.netty.handler.ssl.SslContext;
import io.reactivex.disposables.Disposable;
import io.reactivex.subjects.PublishSubject;

public class RxP2PService extends DaggerService {

    private static String LOG_TAG = "AVX";
    public static final int DEFAULT_PORT = 6000;

    private String profileId;

    private RxP2PServiceClient rxP2PServiceClient;
    private RxP2PController rxP2PController;

    private HashMap<String, Disposable> disposablesMap = new HashMap<>();

    private Map<String, PipeHolder> pipesMap = new HashMap<>();
    private PublishSubject<RxP2PObject> rxObj = PublishSubject.create();

    @Inject
    ObjectMapper objectMapper;

    @Inject
    SslContext sslContext;

    @Inject
    P2pDatabase p2pDatabase;

    @Inject
    P2pDbService p2PDbService;

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

    public PublishSubject<RxP2PObject> getPublisher() {
        return rxObj;
    }

    // *****************************
    // public API:
    // *****************************
    /*
        return true if such connection exists
        otherwise it creates such connection and send requesting package, should be subbed outside
        for response package
     */
    public boolean requestChannelByProfileInfo(String profileSocketInfo, String remoteProfileId) {
        if (getPipeHolder(remoteProfileId) == null) {
            rxP2PServiceClient.openConnection(profileSocketInfo, remoteProfileId);
            Log.i(LOG_TAG, "RxP2PService openned connection for ip = " + profileSocketInfo +" and remoteProfileId = "+remoteProfileId);

            rxP2PServiceClient.requestChannel(remoteProfileId);

            Log.i(LOG_TAG, "RxP2PService requested channel " + profileSocketInfo);

            sendProfileIdRequest(remoteProfileId);
            return false;
        } else {
            return true;
        }

    }




    public void sendRxP2PObject(String profileId, RxP2PObject rxP2PObject) {
        getPipeHolder(profileId).getTxPipe().onNext(rxP2PObject);
        if (rxP2PObject.getObjectType().equals(ObjectType.MESSAGE)) {
            p2PDbService.asyncInsertMessage(new P2pMessage(rxP2PObject.getMessage()));
        }
    }

    public PublishSubject<RxP2PObject> getRxObj() {
        return rxObj;
    }

    public void setProfileId(String profileId) {
        this.profileId = profileId;
    }

    public PipeHolder getPipeHolder(String profileId) {
        return pipesMap.get(profileId);
    }

    // *****************************
    // end public API
    // *****************************

    private void sendProfileIdRequest(String remoteProfileId) {
        new Handler(Looper.getMainLooper()).postDelayed(()-> {
            RxP2PObject rxP2PObject = new RxP2PObject(ObjectType.PROFILE_ID_REQUEST, profileId);
            sendRxP2PObject(remoteProfileId, rxP2PObject);

            Log.i(LOG_TAG, "send profielRequest :"+rxP2PObject.toString());


        }, 2000);

    }


    @Override
    public void onStart(Intent intent, int startId) {

        Log.i(LOG_TAG, "RxP2PService onStart");
        super.onStart(intent, startId);
    }

    @Override
    public void onCreate() {
        super.onCreate();


        Log.i(LOG_TAG, "RxP2PService onCreate");
        rxP2PServiceClient = new RxP2PServiceClient(sslContext, objectMapper, pipesMap);
        rxP2PController = new RxP2PController(objectMapper, rxObj, profileId, pipesMap);

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
