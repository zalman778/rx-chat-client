package com.hwx.rx_chat_client.viewModel;

import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.view.View;

import com.hwx.rx_chat.common.entity.rx.RxMessage;
import com.hwx.rx_chat.common.response.DialogResponse;
import com.hwx.rx_chat_client.service.ChatRepository;
import com.hwx.rx_chat_client.util.SingleLiveEvent;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Map;

import io.reactivex.disposables.CompositeDisposable;

public class DialogElementViewModel extends ViewModel {

    private MutableLiveData<String> dialogCaption = new MutableLiveData<>();
    private MutableLiveData<String> dialogLastUser = new MutableLiveData<>();
    private MutableLiveData<String> dialogLastMessage = new MutableLiveData<>();
    private MutableLiveData<String> dialogImageUrl = new MutableLiveData<>();
    private MutableLiveData<String> dialogLastTime = new MutableLiveData<>();
    private String dialogId;

    private SingleLiveEvent lvDialogPicked = new SingleLiveEvent();

    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    private Map<String, String> headersMap;
    private ChatRepository chatRepository;

    public DialogElementViewModel(DialogResponse dialogResponse, Map<String, String> headersMap, ChatRepository chatRepository) {
        setDialogResponse(dialogResponse);
        this.headersMap = headersMap;
        this.chatRepository = chatRepository;
    }

    public void setDialogResponse(DialogResponse dialogResponse) {
        dialogCaption.setValue(dialogResponse.getDialogName());
        dialogLastUser.setValue(dialogResponse.getLastUser());
        dialogLastMessage.setValue(dialogResponse.getLastMessage());
        if (dialogResponse.getLastDate() != null) {
            dialogLastTime.setValue(new SimpleDateFormat("HH:mm:ss MM.dd").format(dialogResponse.getLastDate()));
        }
        dialogId = dialogResponse.getDialogId();
    }

    public MutableLiveData<String> getDialogCaption() {
        return dialogCaption;
    }

    public void setDialogCaption(MutableLiveData<String> dialogCaption) {
        this.dialogCaption = dialogCaption;
    }

    public MutableLiveData<String> getDialogLastUser() {
        return dialogLastUser;
    }

    public void setDialogLastUser(MutableLiveData<String> dialogLastUser) {
        this.dialogLastUser = dialogLastUser;
    }

    public MutableLiveData<String> getDialogLastMessage() {
        return dialogLastMessage;
    }

    public void setDialogLastMessage(MutableLiveData<String> dialogLastMessage) {
        this.dialogLastMessage = dialogLastMessage;
    }

    public MutableLiveData<String> getDialogImageUrl() {
        return dialogImageUrl;
    }

    public void setDialogImageUrl(MutableLiveData<String> dialogImageUrl) {
        this.dialogImageUrl = dialogImageUrl;
    }

    public SingleLiveEvent getLvDialogPicked() {
        return lvDialogPicked;
    }

    public MutableLiveData<String> getDialogLastTime() {
        return dialogLastTime;
    }

    public void onDialogSelected(View view) {

        lvDialogPicked.setValue(new ArrayList<RxMessage>());

//        Disposable disposable = chatRepository
//                .getMessageList(headersMap, dialogId)
//                .subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(messageList -> {
//                    Log.w("AVX", "got list of messages:"+messageList.size());
//                    lvDialogPicked.setValue(messageList);

//                }, throwable -> Log.e("AVX", "Error on api call:", throwable));
//        compositeDisposable.add(disposable);
    }


}
