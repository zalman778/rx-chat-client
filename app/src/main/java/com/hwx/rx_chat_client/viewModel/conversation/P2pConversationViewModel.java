package com.hwx.rx_chat_client.viewModel.conversation;

import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.os.Handler;
import android.os.Looper;
import android.util.Base64;
import android.util.Log;
import android.view.View;

import com.hwx.rx_chat.common.entity.rx.RxMessage;
import com.hwx.rx_chat.common.object.rx.RxObject;
import com.hwx.rx_chat.common.object.rx.types.EventType;
import com.hwx.rx_chat_client.Configuration;
import com.hwx.rx_chat_client.background.p2p.StringUtils;
import com.hwx.rx_chat_client.background.p2p.db.P2pDatabase;
import com.hwx.rx_chat_client.background.p2p.db.entity.P2pMessage;
import com.hwx.rx_chat_client.background.p2p.db.service.P2pDbService;
import com.hwx.rx_chat_client.background.p2p.object.RxP2PObject;
import com.hwx.rx_chat_client.background.p2p.object.type.ObjectType;
import com.hwx.rx_chat_client.background.p2p.service.RxP2PService;
import com.hwx.rx_chat_client.background.p2p.service.misc.RxP2pRemoteProfileInfo;
import com.hwx.rx_chat_client.background.service.RxService;
import com.hwx.rx_chat_client.util.ResourceProvider;
import com.hwx.rx_chat_client.util.SharedPreferencesProvider;
import com.squareup.picasso.Picasso;

import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.inject.Inject;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;

public class P2pConversationViewModel  extends ViewModel {

    private ResourceProvider resourceProvider;
    private SharedPreferencesProvider sharedPreferencesProvider;
    private Picasso picasso;
    private P2pDatabase p2pDatabase;
    private P2pDbService p2PDbService;

    private String userId;
    private String profileAvatarUrl;
    private String remoteProfileId;
    private String idDialog;

    private CompositeDisposable compositeDisposable = new CompositeDisposable();


    private MutableLiveData<Integer> isMessagesVisible = new MutableLiveData<>();
    private MutableLiveData<Boolean> isMessagesLoading = new MutableLiveData<>();
    private MutableLiveData<String> lvSendPanelText = new MutableLiveData<>();
    private MutableLiveData<String> lvEditMessageOriginalText = new MutableLiveData<>();
    private MutableLiveData<Integer> lvEditMessageVisibility = new MutableLiveData<>();
    private MutableLiveData<Boolean> lvEnabledBtnSend = new MutableLiveData<>();


    private PublishSubject<RxP2PObject> psRxMessageRecievedAction = PublishSubject.create();
    private PublishSubject<List<RxMessage>> psRxMessagesListLoadedFromLocalDbAction = PublishSubject.create();
    private PublishSubject<String> psPerformRollbackMessageSwipe = PublishSubject.create();
    private PublishSubject<String> psProfileSelectedAction = PublishSubject.create();

    //toolbar:
    private MutableLiveData<String> lvRemoteProfileCaption = new MutableLiveData<>();
    private MutableLiveData<Integer> lvVisibilityConnectingProgress = new MutableLiveData<>();
    private PublishSubject<String> psDialogCaptionRefreshAction = PublishSubject.create();

    private Boolean isEditingMessage = false;
    private RxMessage editableMessage;


    private RxP2PService rxP2PService;
    private RxService rxService;

    @Inject
    public P2pConversationViewModel(
              ResourceProvider resourceProvider
            , SharedPreferencesProvider sharedPreferencesProvider
            , Picasso picasso
            , P2pDatabase p2pDatabase
            , P2pDbService p2PDbService
    ) {

        this.resourceProvider = resourceProvider;
        this.sharedPreferencesProvider = sharedPreferencesProvider;
        this.picasso = picasso;
        this.p2pDatabase = p2pDatabase;
        this.p2PDbService = p2PDbService;

        isMessagesVisible.setValue(View.VISIBLE);
        isMessagesLoading.setValue(false);
        lvEditMessageVisibility.setValue(View.GONE);

        lvEnabledBtnSend.setValue(false);

        //toolbar:
//        lvRemoteProfileAvatarUrl.setValue("");
        lvRemoteProfileCaption.setValue("no data at the moment");
        lvVisibilityConnectingProgress.setValue(View.VISIBLE);

        //self
        userId = sharedPreferencesProvider.getSharedPreferences("localPref", 0).getString("user_id", "");
        profileAvatarUrl = sharedPreferencesProvider.getSharedPreferences("localPref", 0).getString("profileAvatarUrl", "");
        profileAvatarUrl = profileAvatarUrl != null ? profileAvatarUrl.replace("api/image/", "") : null;


    }





    public void setRemoteProfileId(String pRemoteProfileId) {
        remoteProfileId = pRemoteProfileId;
        idDialog = StringUtils.stringXOR(userId, remoteProfileId);
    }

    public void setRxP2PService(RxP2PService rxP2PService) {
        this.rxP2PService = rxP2PService;

        subscribeRxP2pPublishers();
        establishConnection();
    }

    public void setRxService(RxService rxService) {
        this.rxService = rxService;
        subscribeRxServiceSocketInfoResponse();
    }

    /*
               смотрим на наличие открытых пайпов и информации о собседенике
                   * если есть, то подписываемся на пайпы и профит
                   * если нет, то инциируем отправляем серверу запрос на получение данных, подписываемся
                        на ответ,как придет - начинаем request channel и получаем данные о диалоге
    */
    private void establishConnection() {
        if (rxP2PService.isEstablished(remoteProfileId)) {
            subscribeP2pPublishers();
            unlockDialogUI();
            updateDialogUI();

        } else {
            sendRemoteProfileRequest();
        }


    }

    //метод разблокирует кнопку отправки и поле ввода сообщений
    private void unlockDialogUI() {
        lvEnabledBtnSend.setValue(true);
        lvVisibilityConnectingProgress.setValue(View.GONE);
    }

    //метод обновляет инфо о диалоге - ставит картинку и название
    private void updateDialogUI() {


        RxP2pRemoteProfileInfo info = rxP2PService.getRemoteProfileInfo(remoteProfileId);
        if (info != null) {
            psDialogCaptionRefreshAction.onNext(info.getCaption());
        }
    }

    private void sendRemoteProfileRequest() {
        new Handler(Looper.getMainLooper()).postDelayed(()-> {
            RxObject rxRequest = new RxObject(com.hwx.rx_chat.common.object.rx.types.ObjectType.REQUEST_IP, remoteProfileId);
            rxService.sendRxObject(rxRequest);
        }, 500);
    }

    /*
        подписываемся на овтет от сервера - инфо о сокете собеседника
     */
    private void subscribeRxServiceSocketInfoResponse() {
        compositeDisposable.add(
                rxService.getPpRxProcessor()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .filter(rxObject->rxObject.getEventType().equals(EventType.FRIEND_SOCKET_INFO)
                                && rxObject.getObjectType().equals(com.hwx.rx_chat.common.object.rx.types.ObjectType.EVENT))
                        .subscribe(
                                rxObject -> actionRequestChannel((String) rxObject.getValue(), (String) rxObject.getObjectId())
                                , err-> Log.e("AVX", "err", err))
        );
    }

    private void actionRequestChannel(String profileSocketInfo, String remoteProfileId) {
        rxP2PService.requestChannelByProfileInfo(profileSocketInfo, remoteProfileId);
    }


    //метод
    private void subscribeP2pPublishers() {
        compositeDisposable.add(
            rxP2PService
                .getPipeHolder(remoteProfileId)
                .getRxPipe()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(rxP2PObject -> {

                        if (rxP2PObject.getObjectType() == ObjectType.MESSAGE) {

                            //decrypting -->>
                            SecretKeySpec secretKeySpec = rxP2PService.getPipeHolder(remoteProfileId).getSecretKey();

                            byte[] encodedBytes = Base64.decode(rxP2PObject.getMessage().getValue(), Base64.DEFAULT);
                            Cipher aliceCipher = Cipher.getInstance("AES/CBC/PKCS5Padding");

                            IvParameterSpec iv = new IvParameterSpec(Configuration.AES_INIT_VECTOR.getBytes("UTF-8"));
                            aliceCipher.init(Cipher.DECRYPT_MODE, secretKeySpec, iv);
                            byte[] recovered = aliceCipher.doFinal(encodedBytes);
                            rxP2PObject.getMessage().setValue(new String(recovered));
                            //<<-- decrypted

                            psRxMessageRecievedAction.onNext(rxP2PObject);
                            p2PDbService.asyncInsertMessage(
                                    new P2pMessage(rxP2PObject.getMessage()), userId, remoteProfileId
                            );
                        }
                    }
                     , err-> Log.e("AVX", "err", err)
                )
        );
    }

    public Picasso getPicasso() {
        return picasso;
    }

    public ResourceProvider getResourceProvider() {
        return resourceProvider;
    }

    public PublishSubject<String> getPsProfileSelectedAction() {
        return psProfileSelectedAction;
    }




    //subscribing on messages in local db for local history
    private void subscribeRxP2pPublishers() {
        compositeDisposable.add(
            p2pDatabase
                .p2pMessageDao()
                .getAllByDialogId(idDialog)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(dbMsgList ->{
                    List<RxMessage> rxMessageList =
                            dbMsgList.stream().map(P2pMessage::toRxMessage).collect(Collectors.toList());

                    psRxMessagesListLoadedFromLocalDbAction.onNext(rxMessageList);

                } , err-> Log.e("AVX", "err", err))
        );

        compositeDisposable.add(
                rxP2PService
                        .getPsWelcomeHandshakeCompletedAction()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(val->{
                            subscribeP2pPublishers();
                            updateDialogUI();
                            unlockDialogUI();
                        },  err-> Log.e("AVX", "err", err))
        );
    }

    public PublishSubject<String> getPsPerformRollbackMessageSwipe() {
        return psPerformRollbackMessageSwipe;
    }

    public MutableLiveData<Integer> getIsMessagesVisible() {
        return isMessagesVisible;
    }

    public MutableLiveData<Boolean> getIsMessagesLoading() {
        return isMessagesLoading;
    }

    public MutableLiveData<String> getLvSendPanelText() {
        return lvSendPanelText;
    }

    public MutableLiveData<Boolean> getLvEnabledBtnSend() {
        return lvEnabledBtnSend;
    }

    public MutableLiveData<String> getLvEditMessageOriginalText() {
        return lvEditMessageOriginalText;
    }

    public MutableLiveData<Integer> getLvEditMessageVisibility() {
        return lvEditMessageVisibility;
    }

    public PublishSubject<String> getPsDialogCaptionRefreshAction() {
        return psDialogCaptionRefreshAction;
    }

    public MutableLiveData<String> getLvRemoteProfileCaption() {
        return lvRemoteProfileCaption;
    }

    public MutableLiveData<Integer> getLvVisibilityConnectingProgress() {
        return lvVisibilityConnectingProgress;
    }

    public PublishSubject<RxP2PObject> getPsRxMessageRecievedAction() {
        return psRxMessageRecievedAction;
    }

    public PublishSubject<List<RxMessage>> getPsRxMessagesListLoadedFromLocalDbAction() {
        return psRxMessagesListLoadedFromLocalDbAction;
    }


    public String getIdDialog() {
        return idDialog;
    }

    //кнопка отправки сообщения
    public void onBtnSend(View view) {
        if (isEditingMessage) {
            editableMessage.setValue(lvSendPanelText.getValue());



            RxP2PObject rxP2PObject = new RxP2PObject(ObjectType.ACTION_EDIT_MESSAGE_REQUEST, editableMessage);
            editableMessage = null;

            rxP2PService.sendRxP2PObject(remoteProfileId, rxP2PObject);


            //closing everything
            lvEditMessageVisibility.setValue(View.GONE);
            lvEditMessageOriginalText.setValue("");
            lvSendPanelText.setValue("");
            isEditingMessage = false;


        } else {
            String msgText = lvSendPanelText.getValue();
            if (msgText != null && !msgText.isEmpty()) {
                lvSendPanelText.setValue("");


                RxMessage rxMessage = new RxMessage(
                        UUID.randomUUID().toString()
                        , sharedPreferencesProvider.getSharedPreferences("localPref", 0).getString("username", "")
                        , sharedPreferencesProvider.getSharedPreferences("localPref", 0).getString("user_id", "")
                        , msgText
                        , new Date()
                        , idDialog
                );
                rxMessage.setImageUrl(profileAvatarUrl);

                RxP2PObject rxObj = new RxP2PObject(ObjectType.MESSAGE, rxMessage);

                Log.w("AVX", "sending rx message:" + rxMessage.getValue());

                String messageIdDialog = rxObj.getMessage().getIdDialog();
                if (messageIdDialog == null || messageIdDialog.isEmpty()) {
                    messageIdDialog = StringUtils.stringXOR(remoteProfileId, userId);
                    rxObj.getMessage().setIdDialog(messageIdDialog);
                }

                String realMessage = rxObj.getMessage().getValue();
                rxP2PService.sendRxP2PObject(remoteProfileId, rxObj);

                rxObj.getMessage().setValue(realMessage);
                psRxMessageRecievedAction.onNext(rxObj);

            }
        }
    }

    public void onBtnCloseEdit(View view) {

    }

    public void onRefreshMessages() {

    }

    //метод удаления сообщения - отправляем собеседнику запрос на удаление сообщения
    public void sendRxEventMessageDelete(String msgId) {
        RxP2PObject rxP2PObject = new RxP2PObject();
        rxP2PObject.setObjectType(ObjectType.ACTION_REMOVE_MESSAGE_REQUEST);
        rxP2PObject.setValue(msgId);

        rxP2PService.sendRxP2PObject(remoteProfileId, rxP2PObject);
    }

    public void openMessageEditBox(RxMessage rxMsg) {
        lvEditMessageVisibility.setValue(View.VISIBLE);
        lvEditMessageOriginalText.setValue("Оригинальный текст: "+rxMsg.getValue());
        lvSendPanelText.setValue(rxMsg.getValue());
        editableMessage = rxMsg;
        isEditingMessage = true;
    }
}
