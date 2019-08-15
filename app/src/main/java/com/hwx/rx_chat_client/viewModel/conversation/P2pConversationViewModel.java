package com.hwx.rx_chat_client.viewModel.conversation;

import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;

import com.hwx.rx_chat.common.entity.rx.RxMessage;
import com.hwx.rx_chat_client.background.p2p.StringUtils;
import com.hwx.rx_chat_client.background.p2p.db.P2pDatabase;
import com.hwx.rx_chat_client.background.p2p.db.entity.P2pMessage;
import com.hwx.rx_chat_client.background.p2p.object.RxP2PObject;
import com.hwx.rx_chat_client.background.p2p.object.type.ObjectType;
import com.hwx.rx_chat_client.background.p2p.service.RxP2PService;
import com.hwx.rx_chat_client.util.ResourceProvider;
import com.hwx.rx_chat_client.util.SharedPreferencesProvider;
import com.squareup.picasso.Picasso;

import java.util.Date;
import java.util.HashSet;
import java.util.List;

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
    private PublishSubject<List<RxMessage>> psRxMessagesList = PublishSubject.create();
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
    ) {

        this.resourceProvider = resourceProvider;
        this.sharedPreferencesProvider = sharedPreferencesProvider;
        this.picasso = picasso;
        this.p2pDatabase = p2pDatabase;

        isMessagesVisible.setValue(View.VISIBLE);
        isMessagesLoading.setValue(false);
        lvEditMessageVisibility.setValue(View.GONE);

        userId = sharedPreferencesProvider.getSharedPreferences("localPref", 0).getString("user_id", "");
        profileAvatarUrl = sharedPreferencesProvider.getSharedPreferences("localPref", 0).getString("profileAvatarUrl", "");
        Log.w("AVX", "got profileAvatarUrl = "+profileAvatarUrl);
        idDialog = StringUtils.stringXOR(userId, remoteProfileId);

    }

    public void setRxP2PService(RxP2PService rxP2PService) {
        this.rxP2PService = rxP2PService;
        subscribeP2pPublishers();
    }

    private void subscribeP2pPublishers() {
        compositeDisposable.add(
            rxP2PService
                .getPipeHolder(remoteProfileId)
                .getRxPipe()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(rx -> {
                            psRxMessageRecievedAction.onNext(rx);

                            AsyncTask.execute(() ->
                                p2pDatabase.p2pMessageDao().insert(new P2pMessage(rx.getMessage()))
                            );

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

    public String getIdDialog() {
        return idDialog;
    }

    //vm clicks:
    public void onBtnSend(View view) {
        String msgText = lvSendPanelText.getValue();
        lvSendPanelText.setValue("");


        RxMessage rxMessage = new RxMessage(null,
                  sharedPreferencesProvider.getSharedPreferences("localPref", 0).getString("username", "")
                , sharedPreferencesProvider.getSharedPreferences("localPref", 0).getString("user_id", "")
                , msgText
                , new Date()
                , idDialog
        );
        rxMessage.setImageUrl(profileAvatarUrl);

        RxP2PObject rxObj = new RxP2PObject(ObjectType.MESSAGE, rxMessage);

        Log.w("AVX", "sending rx message:"+rxMessage.getValue());

        rxP2PService.sendRxP2PObject(remoteProfileId, rxObj);
    }

    public void onBtnCloseEdit(View view) {

    }

    public void onRefreshMessages() {

    }



}
