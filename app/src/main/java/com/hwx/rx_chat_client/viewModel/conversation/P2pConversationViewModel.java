package com.hwx.rx_chat_client.viewModel.conversation;

import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.util.Log;
import android.view.View;

import com.hwx.rx_chat.common.entity.rx.RxMessage;
import com.hwx.rx_chat.common.object.rx.RxObject;
import com.hwx.rx_chat.common.object.rx.types.EventType;
import com.hwx.rx_chat_client.background.p2p.StringUtils;
import com.hwx.rx_chat_client.background.p2p.db.P2pDatabase;
import com.hwx.rx_chat_client.background.p2p.db.entity.P2pMessage;
import com.hwx.rx_chat_client.background.p2p.db.service.P2pDbService;
import com.hwx.rx_chat_client.background.p2p.object.RxP2PObject;
import com.hwx.rx_chat_client.background.p2p.object.type.ObjectType;
import com.hwx.rx_chat_client.background.p2p.service.RxP2PService;
import com.hwx.rx_chat_client.util.ResourceProvider;
import com.hwx.rx_chat_client.util.SharedPreferencesProvider;
import com.squareup.picasso.Picasso;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

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

    private CompositeDisposable compositeDisposable = new CompositeDisposable();


    private MutableLiveData<Integer> isMessagesVisible = new MutableLiveData<>();
    private MutableLiveData<Boolean> isMessagesLoading = new MutableLiveData<>();
    private MutableLiveData<String> lvSendPanelText = new MutableLiveData<>();
    private MutableLiveData<String> lvEditMessageOriginalText = new MutableLiveData<>();

    private MutableLiveData<Integer> lvEditMessageVisibility = new MutableLiveData<>();



    private PublishSubject<RxP2PObject> psRxMessageRecievedAction = PublishSubject.create();
    private PublishSubject<List<RxMessage>> psRxMessagesListLoadedFromLocalDbAction = PublishSubject.create();
    private PublishSubject<String> psPerformRollbackMessageSwipe = PublishSubject.create();
    private PublishSubject<String> psProfileSelectedAction = PublishSubject.create();
    private PublishSubject<String> psDialogInfoAction = PublishSubject.create();


    private String idDialog;

    private HashSet<String> uniqueMessagesIdSet = new HashSet<>();

    private Boolean isEditingMessage = false;
    private RxMessage editableMessage;


    private RxP2PService rxP2PService;

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

        userId = sharedPreferencesProvider.getSharedPreferences("localPref", 0).getString("user_id", "");
        profileAvatarUrl = sharedPreferencesProvider.getSharedPreferences("localPref", 0).getString("profileAvatarUrl", "");

        profileAvatarUrl = profileAvatarUrl != null ? profileAvatarUrl.replace("api/image/", "") : null;

        Log.w("AVX", "got profileAvatarUrl = "+profileAvatarUrl);
    }

    public void setRxP2PService(RxP2PService rxP2PService) {
        this.rxP2PService = rxP2PService;
        subscribeP2pPublishers();
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
                                psRxMessageRecievedAction.onNext(rxP2PObject);
                                p2PDbService.asyncInsertMessage(new P2pMessage(rxP2PObject.getMessage()));
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


    public void setRemoteProfileId(String remoteProfileId) {
        this.remoteProfileId = remoteProfileId;

        idDialog = StringUtils.stringXOR(userId, remoteProfileId);
        Log.w("AVX", "p2pConvVm: got dialogId = "+idDialog+" from "+userId+" and "+remoteProfileId);

        subscribeDbMessages();
    }

    //subscribing on messages in local db for local history
    private void subscribeDbMessages() {
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

    public MutableLiveData<String> getLvEditMessageOriginalText() {
        return lvEditMessageOriginalText;
    }

    public MutableLiveData<Integer> getLvEditMessageVisibility() {
        return lvEditMessageVisibility;
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

            Log.w("AVX", "sending rx message:"+rxMessage.getValue());

            rxObj = rxP2PService.sendRxP2PObject(remoteProfileId, rxObj);

            //make sent message as recieved for showing it back on screen:
            psRxMessageRecievedAction.onNext(rxObj);
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
