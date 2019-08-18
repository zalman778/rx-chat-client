package com.hwx.rx_chat_client.viewModel.conversation;

import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.View;

import com.hwx.rx_chat.common.entity.rx.RxMessage;
import com.hwx.rx_chat.common.object.rx.RxObject;
import com.hwx.rx_chat.common.object.rx.types.EventType;
import com.hwx.rx_chat.common.object.rx.types.ObjectType;
import com.hwx.rx_chat.common.object.rx.types.SettingType;
import com.hwx.rx_chat_client.Configuration;
import com.hwx.rx_chat_client.background.service.RxService;
import com.hwx.rx_chat_client.repository.ChatRepository;
import com.hwx.rx_chat_client.repository.DialogRepository;
import com.hwx.rx_chat_client.util.ResourceProvider;
import com.hwx.rx_chat_client.util.SharedPreferencesProvider;
import com.squareup.picasso.Picasso;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;

public class ConversationViewModel extends ViewModel {

    private ChatRepository chatRepository;
    private DialogRepository dialogRepository;
    private ResourceProvider resourceProvider;
    private SharedPreferencesProvider sharedPreferencesProvider;
    private Picasso picasso;

    private String userId;

    private CompositeDisposable disposables = new CompositeDisposable();


    private MutableLiveData<Integer> isMessagesVisible = new MutableLiveData<>();
    private MutableLiveData<Boolean> isMessagesLoading = new MutableLiveData<>();
    private MutableLiveData<String> lvSendPanelText = new MutableLiveData<>();
    private MutableLiveData<String> lvEditMessageOriginalText = new MutableLiveData<>();

    private MutableLiveData<Integer> lvEditMessageVisibility = new MutableLiveData<>();



    private PublishSubject<RxObject> psRxMessage = PublishSubject.create();
    private PublishSubject<List<RxMessage>> psRxMessagesList = PublishSubject.create();
    private PublishSubject<String> psPerformRollbackMessageSwipe = PublishSubject.create();
    private PublishSubject<String> psProfileSelectedAction = PublishSubject.create();
    private PublishSubject<String> psDialogInfoAction = PublishSubject.create();


    private String idDialog;
    private Map<String, String> headersMap = new HashMap<>();
    private HashSet<String> uniqueMessagesIdSet = new HashSet<>();

    private Boolean isEditingMessage = false;
    private RxMessage editableMessage;


    private RxService rxService;

    @Inject
    public ConversationViewModel(
              ChatRepository chatRepository
            , DialogRepository dialogRepository
            , ResourceProvider resourceProvider
            , SharedPreferencesProvider sharedPreferencesProvider
            , Picasso picasso
    ) {

        this.chatRepository = chatRepository;
        this.dialogRepository = dialogRepository;
        this.resourceProvider = resourceProvider;
        this.sharedPreferencesProvider = sharedPreferencesProvider;
        this.picasso = picasso;

        this.isMessagesVisible.setValue(View.VISIBLE);
        this.isMessagesLoading.setValue(false);
        this.lvEditMessageVisibility.setValue(View.GONE);

        SharedPreferences pref = sharedPreferencesProvider.getSharedPreferences("localPref", 0);
        headersMap.put("Authorization", pref.getString("token", ""));
        userId = sharedPreferencesProvider.getSharedPreferences("localPref", 0).getString("user_id", "");
        //subscribePublishers();
    }



    private void subscribePublishers() {
        //получаем реактивно сообщения
        disposables.add(
            rxService.getPpRxProcessor()
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

    }

    private void sendSettingDialogId() {
        RxObject rxObject = new RxObject(ObjectType.SETTING, SettingType.ID_DIALOG_FOR_CONVERSATION, idDialog, null);
        rxService.sendRxObject(rxObject);
    }

    private void sendSettingUserId() {
        RxObject rxObject = new RxObject(ObjectType.SETTING, SettingType.ID_USER_FOR_BACKGROUND, userId, null);
        rxService.sendRxObject(rxObject);
    }

    private void sendSettingUsername() {
        RxObject rxObject = new RxObject(ObjectType.SETTING, SettingType.ID_USER_FOR_CONVERSATION, userId, null);
        rxService.sendRxObject(rxObject);
    }


    public void sendRxEventMessageDelete(String messageId) {
        RxObject rxObject = new RxObject(ObjectType.EVENT, EventType.MESSAGE_DELETED, messageId, (RxMessage)null);
        rxService.sendRxObject(rxObject);
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

    public PublishSubject<String> getPsProfileSelectedAction() {
        return psProfileSelectedAction;
    }

    public MutableLiveData<String> getLvEditMessageOriginalText() {
        return lvEditMessageOriginalText;
    }

    public String getIdDialog() {
        return idDialog;
    }

    public void setIdDialog(String idDialog) {
        this.idDialog = idDialog;

        //подгружаем список из статик бд
        disposables.add(
            chatRepository
                .getMessageList(
                        Configuration.URL_MESSAGES_LIST+"/"+idDialog
                        , headersMap
                )
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(rxMessageList -> psRxMessagesList.onNext(rxMessageList)
                    , e-> Log.e("AVX", "got error while fetching static messages ", e)
                )
        );
    }

    public PublishSubject<List<RxMessage>> getPsRxMessagesList() {
        return psRxMessagesList;
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

    public PublishSubject<String> getPsDialogInfoAction() {
        return psDialogInfoAction;
    }


    public void setRxService(RxService rxService) {
        this.rxService = rxService;
        sendSettingUserId();
        sendSettingDialogId();
        sendSettingUsername();
        subscribePublishers();
    }

    public void onRefreshMessages() {

    }

    public MutableLiveData<Integer> getLvEditMessageVisibility() {
        return lvEditMessageVisibility;
    }


    public void onClickDialogOptions() {
        psDialogInfoAction.onNext(idDialog);
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
            rxService.sendRxObject(rxObject);

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
            rxService.sendRxObject(rxObject);

        }
    }

    public void openMessageEditBox(RxMessage rxMsg) {
        lvEditMessageVisibility.setValue(View.VISIBLE);
        lvEditMessageOriginalText.setValue("Оригинальный текст: "+rxMsg.getValue());
        lvSendPanelText.setValue(rxMsg.getValue());
        editableMessage = rxMsg;
        isEditingMessage = true;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        disposables.dispose();
    }


}