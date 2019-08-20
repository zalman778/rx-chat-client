package com.hwx.rx_chat_client.background.p2p.service;

import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hwx.rx_chat.common.entity.rx.RxMessage;
import com.hwx.rx_chat_client.background.p2p.StringUtils;
import com.hwx.rx_chat_client.background.p2p.db.P2pDatabase;
import com.hwx.rx_chat_client.background.p2p.db.entity.P2pMessage;
import com.hwx.rx_chat_client.background.p2p.db.service.P2pDbService;
import com.hwx.rx_chat_client.background.p2p.object.PipeHolder;
import com.hwx.rx_chat_client.background.p2p.object.RxP2PController;
import com.hwx.rx_chat_client.background.p2p.object.RxP2PObject;
import com.hwx.rx_chat_client.background.p2p.object.type.ObjectType;
import com.hwx.rx_chat_client.background.p2p.service.misc.RxP2pRemoteProfileInfo;
import com.hwx.rx_chat_client.util.SharedPreferencesProvider;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import dagger.android.DaggerService;
import io.netty.handler.ssl.SslContext;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;

public class RxP2PService extends DaggerService {

    private static String LOG_TAG = "AVX";
    public static final int DEFAULT_PORT = 6000;

    private String profileId;

    private RxP2PServiceClient rxP2PServiceClient;
    private RxP2PController rxP2PController;

    private HashMap<String, CompositeDisposable> disposablesMap = new HashMap<>();

    private Map<String, PipeHolder> pipesMap = new HashMap<>();
    private Map<String, RxP2pRemoteProfileInfo> remoteProfilesMap = new HashMap<>();
    private PublishSubject<RxP2PObject> rxObj = PublishSubject.create();

    //actions for vievModels:
    private PublishSubject<String> psRemoveMessageAction = PublishSubject.create();
    private PublishSubject<RxMessage> psEditMessageAction = PublishSubject.create();

    @Inject
    ObjectMapper objectMapper;

    @Inject
    SslContext sslContext;

    @Inject
    P2pDatabase p2pDatabase;

    @Inject
    P2pDbService p2PDbService;

    @Inject
    SharedPreferencesProvider sharedPreferencesProvider;

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
        метод проверяет - установлено ли соеднинение - по мапе пайпов, из not disposed
     */
    public boolean isEstablished(String remoteProfileId) {
        boolean check =       getPipeHolder(remoteProfileId) != null
                && !getPipeHolder(remoteProfileId).getTxPipe().hasComplete()
                && !getPipeHolder(remoteProfileId).getRxPipe().hasComplete();

        return check;
    }

    //метод сохраяняет инфо о собеседнике
    public void saveEstablishedConnectionInfo(
              String remoteProfileId
            , String profileCaption
            , String profileAvatarUrl
    ) {
       RxP2pRemoteProfileInfo info = new RxP2pRemoteProfileInfo(profileAvatarUrl, profileCaption);
       remoteProfilesMap.put(remoteProfileId, info);
    }

    public RxP2pRemoteProfileInfo getRemoteProfileInfo(String remoteProfileId) {
        return remoteProfilesMap.get(remoteProfileId);
    }

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

    public RxP2PObject sendRxP2PObject(String remoteProfileId, RxP2PObject rxP2PObject) {
        getPipeHolder(remoteProfileId).getTxPipe().onNext(rxP2PObject);



        if (rxP2PObject.getObjectType().equals(ObjectType.MESSAGE)) {

            String messageIdDialog = rxP2PObject.getMessage().getIdDialog();
            if (messageIdDialog == null || messageIdDialog.isEmpty()) {
                messageIdDialog = StringUtils.stringXOR(remoteProfileId, profileId);
                rxP2PObject.getMessage().setIdDialog(messageIdDialog);

            }

            p2PDbService.asyncInsertMessage(new P2pMessage(rxP2PObject.getMessage()), profileId, remoteProfileId);
        }
        return rxP2PObject;
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

    public PublishSubject<String> getPsRemoveMessageAction() {
        return psRemoveMessageAction;
    }

    public PublishSubject<RxMessage> getPsEditMessageAction() {
        return psEditMessageAction;
    }

    // *****************************
    // end public API
    // *****************************


    /*
        Отправляем запрос на p2p чат собеседнику:
        вкладываем в запрос: avatarUrl ->rxP2pObj.valueId, caption->value
     */
    private void sendProfileIdRequest(String remoteProfileId) {
        new Handler(Looper.getMainLooper()).postDelayed(()-> {
            RxP2PObject rxP2PObject = new RxP2PObject(ObjectType.PROFILE_ID_REQUEST, profileId);
            String profileAvatarUrl = sharedPreferencesProvider.getSharedPreferences("localPref", 0).getString("profileAvatarUrl", "");
            profileAvatarUrl = profileAvatarUrl != null ? profileAvatarUrl.replace("api/image/", "") : null;
            rxP2PObject.setValueId(profileAvatarUrl);

            sendRxP2PObject(remoteProfileId, rxP2PObject);
            Log.i(LOG_TAG, "send profielRequest :"+rxP2PObject.toString());

            subscribeP2pRemoteCommands(remoteProfileId);
        }, 2000);

    }

    private void subscribeP2pRemoteCommands(String remoteProfileId) {
        new Handler(Looper.getMainLooper()).postDelayed(()-> {
                if (!disposablesMap.containsKey(remoteProfileId))
                    disposablesMap.put(remoteProfileId, new CompositeDisposable());

                disposablesMap.get(remoteProfileId).add(
                        getPipeHolder(remoteProfileId)
                            .getRxPipe()
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .filter(rxP2PObject ->
                                        rxP2PObject.getObjectType() == ObjectType.ACTION_REMOVE_MESSAGE_REQUEST
                                    ||  rxP2PObject.getObjectType() == ObjectType.ACTION_REMOVE_MESSAGE_RESPONSE
                                    ||  rxP2PObject.getObjectType() == ObjectType.ACTION_EDIT_MESSAGE_REQUEST
                                    ||  rxP2PObject.getObjectType() == ObjectType.ACTION_EDIT_MESSAGE_RESPONSE
                            )
                            .subscribe(rxP2PObject -> {
                                Log.w("AVX", "got rxP2Pobj " + rxP2PObject.hashCode());
                                Log.w("AVX", rxP2PObject.toString());
                                //обрабатываем события:
                                if (rxP2PObject.getObjectType() == ObjectType.ACTION_REMOVE_MESSAGE_REQUEST) {
                                    String messageId = rxP2PObject.getValue();

                                    p2PDbService.asyncDeleteMessage(messageId);

                                    RxP2PObject objToSend = new RxP2PObject(
                                            ObjectType.ACTION_REMOVE_MESSAGE_RESPONSE, messageId
                                    );

                                    getPipeHolder(remoteProfileId).getTxPipe().onNext(objToSend);
                                    psRemoveMessageAction.onNext(messageId);

                                } else if (rxP2PObject.getObjectType() == ObjectType.ACTION_REMOVE_MESSAGE_RESPONSE) {
                                    p2PDbService.asyncDeleteMessage(rxP2PObject.getValue());
                                    psRemoveMessageAction.onNext(rxP2PObject.getValue());

                                } else if (rxP2PObject.getObjectType() == ObjectType.ACTION_EDIT_MESSAGE_REQUEST) {

                                    RxMessage rxMessage = rxP2PObject.getMessage();
                                    rxMessage.setEdited(true);
                                    rxMessage.setDateEdited(new Date());
                                    p2PDbService.asyncUpdateMessage(new P2pMessage(rxMessage));

                                    RxP2PObject objToSend = new RxP2PObject(
                                            ObjectType.ACTION_EDIT_MESSAGE_RESPONSE, rxP2PObject.getMessage()
                                    );

                                    getPipeHolder(remoteProfileId).getTxPipe().onNext(objToSend);
                                    psEditMessageAction.onNext(rxP2PObject.getMessage());

                                } else if (rxP2PObject.getObjectType() == ObjectType.ACTION_EDIT_MESSAGE_RESPONSE) {
                                    p2PDbService.asyncUpdateMessage(new P2pMessage(rxP2PObject.getMessage()));
                                    psEditMessageAction.onNext(rxP2PObject.getMessage());

                                } else {
//                                    getPipeHolder(remoteProfileId).getRxPipe().onNext(rxP2PObject);
                                }
                            }, err-> Log.e("AVX", "err", err))

                );
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

        rxP2PController = new RxP2PController(objectMapper, rxObj, profileId, pipesMap, sharedPreferencesProvider);

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
