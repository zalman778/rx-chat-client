package com.hwx.rx_chat_client.viewModel.conversation;

import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.content.SharedPreferences;
import android.os.Handler;
import android.util.Log;
import android.view.View;

import com.hwx.rx_chat.common.entity.rx.RxMessage;
import com.hwx.rx_chat.common.object.rx.RxObject;
import com.hwx.rx_chat.common.object.rx.types.EventType;
import com.hwx.rx_chat.common.object.rx.types.ObjectType;
import com.hwx.rx_chat.common.object.rx.types.SettingType;
import com.hwx.rx_chat.common.response.DialogProfileResponse;
import com.hwx.rx_chat.common.response.UserDetailsResponse;
import com.hwx.rx_chat_client.Configuration;
import com.hwx.rx_chat_client.repository.DialogRepository;
import com.hwx.rx_chat_client.rsocket.ChatSocket;
import com.hwx.rx_chat_client.repository.ChatRepository;
import com.hwx.rx_chat_client.util.ResourceProvider;
import com.hwx.rx_chat_client.util.SharedPreferencesProvider;
import com.squareup.picasso.Picasso;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;

public class ConversationViewModel extends ViewModel {

    @Override
    protected void onCleared() {
        super.onCleared();
        disposables.dispose();
    }

    private ChatRepository chatRepository;
    private DialogRepository dialogRepository;
    private ResourceProvider resourceProvider;
    private SharedPreferencesProvider sharedPreferencesProvider;
    private ChatSocket chatSocket;

    private Picasso picasso;

    private CompositeDisposable disposables = new CompositeDisposable();


    private MutableLiveData<Integer> isMessagesVisible = new MutableLiveData<>();
    private MutableLiveData<Boolean> isMessagesLoading = new MutableLiveData<>();
    private MutableLiveData<String> lvSendPanelText = new MutableLiveData<>();
    private MutableLiveData<String> lvEditMessageOriginalText = new MutableLiveData<>();

    private MutableLiveData<Integer> lvEditMessageVisibility = new MutableLiveData<>();



    private PublishSubject<RxObject> psRxMessage = PublishSubject.create();
    private PublishSubject<List<RxMessage>> psRxMessagesList = PublishSubject.create();
    private PublishSubject<String> psPerformRollbackMessageSwipe = PublishSubject.create();
    private PublishSubject<String> psUserImageClicked = PublishSubject.create();
    private PublishSubject<UserDetailsResponse> psProfileSelectedLoaded = PublishSubject.create();
    private PublishSubject<DialogProfileResponse> psDialogInfoLoadFinishedAction = PublishSubject.create();


    private String idDialog;
    private Map<String, String> headersMap = new HashMap<>();
    private HashSet<String> uniqueMessagesIdSet = new HashSet<>();

    private Boolean isEditingMessage = false;
    private RxMessage editableMessage;


    public ConversationViewModel(
              ChatRepository chatRepository
             , DialogRepository dialogRepository
            , ResourceProvider resourceProvider
            , SharedPreferencesProvider sharedPreferencesProvider
            , ChatSocket chatSocket
            , Picasso picasso) {

        this.chatRepository = chatRepository;
        this.dialogRepository = dialogRepository;
        this.resourceProvider = resourceProvider;
        this.sharedPreferencesProvider = sharedPreferencesProvider;
        this.chatSocket = chatSocket;
        this.picasso = picasso;

        this.isMessagesVisible.setValue(View.VISIBLE);
        this.isMessagesLoading.setValue(false);
        this.lvEditMessageVisibility.setValue(View.GONE);

        SharedPreferences pref = sharedPreferencesProvider.getSharedPreferences("localPref", 0);
        headersMap.put("Authorization", pref.getString("token", ""));

        subscribePublishers();

    }

    private void subscribePublishers() {
        //получаем реактивно сообщения
        disposables.add(
                chatSocket
                        .getPsRxMessageA()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(rxObject -> {
                            //Fixing time from Mongo if its message:
                            if (rxObject.getMessage() != null) {
                                Calendar cal = Calendar.getInstance();
                                cal.setTime(rxObject.getMessage().getDateSent());
                                cal.add(Calendar.HOUR_OF_DAY, Configuration.MONGO_TIMEZONE_CORRECTION_HRS);
                                rxObject.getMessage().setDateSent(cal.getTime());
                            }
                            psRxMessage.onNext(rxObject);
                        },
                                e->Log.e("AVX", "err on rx "+e.getMessage()+"; "+e.getLocalizedMessage(), e))
        );

        disposables.add(
            psUserImageClicked
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(this::sendProfileInfoRequest
                            , err->Log.e("AVX", "err", err))
        );
    }

    private void sendProfileInfoRequest(String profileId) {
        disposables.add(
                chatRepository
                        .getProfileInfo(Configuration.URL_GET_PROFILE_INFO+"/"+profileId, headersMap)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                profileInfo -> psProfileSelectedLoaded.onNext(profileInfo)
                                ,err->Log.e("AVX", "err", err)
                        )
        );
    }

    private void sendSettingDialogId() {
        RxObject rxObject = new RxObject(ObjectType.SETTING, SettingType.ID_DIALOG, idDialog, null);
        chatSocket.putRxObjectA(rxObject);
    }

    private void sendSettingUsername() {
        String username = sharedPreferencesProvider.getSharedPreferences("localPref", 0).getString("username", "");
        RxObject rxObject = new RxObject(ObjectType.SETTING, SettingType.USERNAME, username, null);
        chatSocket.putRxObjectA(rxObject);
    }


    public void sendRxEventMessageDelete(String messageId) {
        RxObject rxObject = new RxObject(ObjectType.EVENT, EventType.MESSAGE_DELETED, messageId, (RxMessage)null);
        chatSocket.putRxObjectA(rxObject);
    }

    public Picasso getPicasso() {
        return picasso;
    }

    public HashSet<String> getUniqueMessagesIdSet() {
        return uniqueMessagesIdSet;
    }

    public PublishSubject<RxObject> getPsRxMessage() {
        return psRxMessage;
    }

    public PublishSubject<String> getPsPerformRollbackMessageSwipe() {
        return psPerformRollbackMessageSwipe;
    }

    public PublishSubject<String> getPsUserImageClicked() {
        return psUserImageClicked;
    }

    public PublishSubject<UserDetailsResponse> getPsProfileSelectedLoaded() {
        return psProfileSelectedLoaded;
    }

    public MutableLiveData<String> getLvEditMessageOriginalText() {
        return lvEditMessageOriginalText;
    }

    public void setLvEditMessageOriginalText(MutableLiveData<String> lvEditMessageOriginalText) {
        this.lvEditMessageOriginalText = lvEditMessageOriginalText;
    }

    public String getIdDialog() {
        return idDialog;
    }

    public void setIdDialog(String idDialog) {
        this.idDialog = idDialog;
        sendSettingDialogId();



        //подгружаем список из статик бд
        disposables.add(
            chatRepository
                .getMessageList(headersMap, idDialog)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(rxMessageList -> psRxMessagesList.onNext(rxMessageList)
                    , e-> Log.e("AVX", "got error while fetching static messages ", e)
                )
        );

        //delayed with 1000ms, due to socket err:
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {

            @Override
            public void run() {
                sendSettingUsername();
            }

        }, 1000);
    }

    public PublishSubject<List<RxMessage>> getPsRxMessagesList() {
        return psRxMessagesList;
    }

    public void setPsRxMessagesList(PublishSubject<List<RxMessage>> psRxMessagesList) {
        this.psRxMessagesList = psRxMessagesList;
    }

    public MutableLiveData<String> getLvSendPanelText() {
        return lvSendPanelText;
    }

    public MutableLiveData<Integer> getIsMessagesVisible() {
        return isMessagesVisible;
    }

    public MutableLiveData<Boolean> getIsMessagesLoading() {
        return isMessagesLoading;
    }

    public ResourceProvider getResourceProvider() {
        return resourceProvider;
    }

    public PublishSubject<DialogProfileResponse> getPsDialogInfoLoadFinishedAction() {
        return psDialogInfoLoadFinishedAction;
    }

    public void onRefreshMessages() {

    }

    public MutableLiveData<Integer> getLvEditMessageVisibility() {
        return lvEditMessageVisibility;
    }

    public void setLvEditMessageVisibility(MutableLiveData<Integer> lvEditMessageVisibility) {
        this.lvEditMessageVisibility = lvEditMessageVisibility;
    }

    public void onClickDialogOptions() {
        disposables.add(
            dialogRepository
                .getDialogInfo(Configuration.URL_DIALOG_PROFILE + "/"+idDialog, headersMap)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(dialogInfo -> {
                    psDialogInfoLoadFinishedAction.onNext(dialogInfo);
                }, e-> Log.e("AVX", "err ", e))
        );
    }

    public void onBtnCloseEdit(View view) {

        //отправляем в активити действие на отмену свайпа сообщения
        psPerformRollbackMessageSwipe.onNext(editableMessage.getId());

        lvEditMessageVisibility.setValue(View.GONE);
        lvEditMessageOriginalText.setValue("");
        lvSendPanelText.setValue("");
        editableMessage = null;
        this.isEditingMessage = false;
    }

    public void onBtnSend(View view) {
        if (isEditingMessage) {

            editableMessage.setValue(lvSendPanelText.getValue());

            RxObject rxObject = new RxObject(ObjectType.EVENT, EventType.MESSAGE_EDIT, null, editableMessage);
            editableMessage = null;
            chatSocket.putRxObjectA(rxObject);

            //closing everything
            lvEditMessageVisibility.setValue(View.GONE);
            lvEditMessageOriginalText.setValue("");
            lvSendPanelText.setValue("");
            isEditingMessage = false;
        } else {
            String msgText = lvSendPanelText.getValue();
            lvSendPanelText.setValue("");

            RxMessage rxMessage = new RxMessage(null,
                    sharedPreferencesProvider.getSharedPreferences("localPref", 0).getString("username", "")
                    , sharedPreferencesProvider.getSharedPreferences("localPref", 0).getString("user_id", "")
                    , msgText
                    , new Date()
                    , idDialog
            );

            Log.w("AVX", "sending rx message:"+rxMessage.getValue());
            RxObject rxObject = new RxObject(ObjectType.EVENT, EventType.MESSAGE_NEW_FROM_CLIENT, null, rxMessage);
            chatSocket.putRxObjectA(rxObject);

        }
    }

    public void openMessageEditBox(RxMessage rxMsg) {
        lvEditMessageVisibility.setValue(View.VISIBLE);
        lvEditMessageOriginalText.setValue("Оригинальный текст: "+rxMsg.getValue());
        lvSendPanelText.setValue(rxMsg.getValue());
        editableMessage = rxMsg;
        this.isEditingMessage = true;
    }


}