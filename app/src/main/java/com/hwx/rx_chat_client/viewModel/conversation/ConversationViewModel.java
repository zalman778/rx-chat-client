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
import com.hwx.rx_chat_client.Configuration;
import com.hwx.rx_chat_client.rsocket.ChatSocket;
import com.hwx.rx_chat_client.service.ChatRepository;
import com.hwx.rx_chat_client.util.ResourceProvider;
import com.hwx.rx_chat_client.util.SharedPreferencesProvider;

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
    private ResourceProvider resourceProvider;
    private SharedPreferencesProvider sharedPreferencesProvider;
    private ChatSocket chatSocket;
    private CompositeDisposable disposables = new CompositeDisposable();


    private MutableLiveData<Integer> isMessagesVisible = new MutableLiveData<>();
    private MutableLiveData<Boolean> isMessagesLoading = new MutableLiveData<>();
    private MutableLiveData<String> lvSendPanelText = new MutableLiveData<>();
    private MutableLiveData<String> lvEditMessageOriginalText = new MutableLiveData<>();

    private MutableLiveData<Integer> lvEditMessageVisibility = new MutableLiveData<>();



    private PublishSubject<RxObject> psRxMessage = PublishSubject.create();
    private PublishSubject<List<RxMessage>> psRxMessagesList = PublishSubject.create();
    private PublishSubject<String> psPerformRollbackMessageSwipe = PublishSubject.create();


    private String idDialog;
    private Map<String, String> headersMap = new HashMap<>();
    private HashSet<String> uniqueMessagesIdSet = new HashSet<>();

    private Boolean isEditingMessage = false;
    private RxMessage editatbleMessage;


    public ConversationViewModel(
              ChatRepository chatRepository
            , ResourceProvider resourceProvider
            , SharedPreferencesProvider sharedPreferencesProvider
            , ChatSocket chatSocket
                                 ) {

        this.chatRepository = chatRepository;
        this.resourceProvider = resourceProvider;
        this.sharedPreferencesProvider = sharedPreferencesProvider;
        this.chatSocket = chatSocket;

        this.isMessagesVisible.setValue(View.VISIBLE);
        this.isMessagesLoading.setValue(false);
        this.lvEditMessageVisibility.setValue(View.GONE);

        SharedPreferences pref = sharedPreferencesProvider.getSharedPreferences("localPref", 0);
        headersMap.put("Authorization", pref.getString("token", ""));


        //получаем реактивно сообщения
        disposables.add(
            chatSocket
                .getPsRxMessage()
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
                }, e->Log.e("AVX", "err on rx", e))
        );
    }

    private void sendSettingDialogId() {
        RxObject rxObject = new RxObject(ObjectType.SETTING, SettingType.ID_DIALOG, idDialog, null);
        chatSocket.putRxObject(rxObject);
    }

    private void sendSettingUsername() {
        String username = sharedPreferencesProvider.getSharedPreferences("localPref", 0).getString("username", "");
        RxObject rxObject = new RxObject(ObjectType.SETTING, SettingType.USERNAME, username, null);
        chatSocket.putRxObject(rxObject);
    }


    public void sendRxEventMessageDelete(String messageId) {
        RxObject rxObject = new RxObject(ObjectType.EVENT, EventType.MESSAGE_DELETED, messageId, (RxMessage)null);
        chatSocket.putRxObject(rxObject);
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

    public void onRefreshMessages() {

    }

    public MutableLiveData<Integer> getLvEditMessageVisibility() {
        return lvEditMessageVisibility;
    }

    public void setLvEditMessageVisibility(MutableLiveData<Integer> lvEditMessageVisibility) {
        this.lvEditMessageVisibility = lvEditMessageVisibility;
    }

    public void onBtnCloseEdit(View view) {

        //отправляем в активити действие на отмену свайпа сообщения
        psPerformRollbackMessageSwipe.onNext(editatbleMessage.getId());

        lvEditMessageVisibility.setValue(View.GONE);
        lvEditMessageOriginalText.setValue("");
        lvSendPanelText.setValue("");
        editatbleMessage = null;
        this.isEditingMessage = false;
    }

    public void onBtnSend(View view) {
        if (!isEditingMessage) {
            String msgText = lvSendPanelText.getValue();
            lvSendPanelText.setValue("");

            RxMessage rxMessage = new RxMessage(null,
                    sharedPreferencesProvider.getSharedPreferences("localPref", 0).getString("username", "")
                    , sharedPreferencesProvider.getSharedPreferences("localPref", 0).getString("user_id", "")
                    , msgText
                    , new Date()
                    , idDialog
            );

            RxObject rxObject = new RxObject(ObjectType.EVENT, EventType.MESSAGE_NEW_FROM_CLIENT, null, rxMessage);
            chatSocket.putRxObject(rxObject);
        } else {

            editatbleMessage.setValue(lvSendPanelText.getValue());

            RxObject rxObject = new RxObject(ObjectType.EVENT, EventType.MESSAGE_EDIT, null, editatbleMessage);
            editatbleMessage = null;
            chatSocket.putRxObject(rxObject);

            //closing everything
            lvEditMessageVisibility.setValue(View.GONE);
            lvEditMessageOriginalText.setValue("");
            lvSendPanelText.setValue("");
            isEditingMessage = false;
        }
    }

    public void openMessageEditBox(RxMessage rxMsg) {
        lvEditMessageVisibility.setValue(View.VISIBLE);
        lvEditMessageOriginalText.setValue("Оригинальный текст: "+rxMsg.getValue());
        lvSendPanelText.setValue(rxMsg.getValue());
        editatbleMessage = rxMsg;
        this.isEditingMessage = true;
    }
}