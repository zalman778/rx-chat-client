package com.hwx.rx_chat_client.viewModel.conversation;

import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.View;

import com.hwx.rx_chat.common.response.FriendResponse;
import com.hwx.rx_chat.common.response.UserDetailsResponse;
import com.hwx.rx_chat_client.Configuration;
import com.hwx.rx_chat_client.repository.ChatRepository;
import com.hwx.rx_chat_client.repository.DialogRepository;
import com.hwx.rx_chat_client.repository.FriendRepository;
import com.hwx.rx_chat_client.util.SharedPreferencesProvider;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;

public class CreateDialogViewModel extends ViewModel {
    private ChatRepository chatRepository;
    private DialogRepository dialogRepository;
    private SharedPreferencesProvider sharedPreferencesProvider;

    private String userId;

    private static Picasso staticPicasso;

    private MutableLiveData<String> lvDialogCaption = new MutableLiveData<>();
    private PublishSubject<List<FriendResponse>> psRecievedFriendResponseList = PublishSubject.create();
    private PublishSubject<String> psProfileSelected = PublishSubject.create();
    private PublishSubject<UserDetailsResponse> psProfileSelectedLoaded = PublishSubject.create();
    private MutableLiveData<Integer> lvUserListVisibility = new MutableLiveData<>();

    private PublishSubject<String> psCreateDialogAction = PublishSubject.create();
    private PublishSubject<String> psCreateDialogCompletedAction = PublishSubject.create();

    private CompositeDisposable compositeDisposable = new CompositeDisposable();
    private Map<String, String> headersMap = new HashMap<>();

    @Inject
    public CreateDialogViewModel(DialogRepository dialogRepository, ChatRepository chatRepository, SharedPreferencesProvider sharedPreferencesProvider, Picasso picasso) {
        this.chatRepository = chatRepository;
        this.dialogRepository = dialogRepository;
        this.sharedPreferencesProvider = sharedPreferencesProvider;

        staticPicasso = picasso;

        lvUserListVisibility.setValue(View.GONE);

        SharedPreferences pref = sharedPreferencesProvider.getSharedPreferences("localPref", 0);
        headersMap.put("Authorization", pref.getString("token", ""));
        userId = sharedPreferencesProvider.getSharedPreferences("localPref", 0).getString("user_id", "");


    }


    public PublishSubject<UserDetailsResponse> getPsProfileSelectedLoaded() {
        return psProfileSelectedLoaded;
    }

    public MutableLiveData<String> getLvDialogCaption() {
        return lvDialogCaption;
    }

    public void setLvDialogCaption(MutableLiveData<String> lvDialogCaption) {
        this.lvDialogCaption = lvDialogCaption;
    }

    public ChatRepository getChatRepository() {
        return chatRepository;
    }

    public SharedPreferencesProvider getSharedPreferencesProvider() {
        return sharedPreferencesProvider;
    }

    public Map<String, String> getHeadersMap() {
        return headersMap;
    }

    public static Picasso getPicasso() {
        return staticPicasso;
    }

    public PublishSubject<List<FriendResponse>> getPsRecievedFriendResponseList() {
        return psRecievedFriendResponseList;
    }

    public MutableLiveData<Integer> getLvUserListVisibility() {
        return lvUserListVisibility;
    }

    public PublishSubject<String> getPsProfileSelected() {
        return psProfileSelected;
    }

    public PublishSubject<String> getPsCreateDialogAction() {
        return psCreateDialogAction;
    }

    public PublishSubject<String> getPsCreateDialogCompletedAction() {
        return psCreateDialogCompletedAction;
    }

    public void onClickCreateDialog() {
        String dialogCaption = lvDialogCaption.getValue();
        if (dialogCaption != null)
            psCreateDialogAction.onNext(lvDialogCaption.getValue());
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        compositeDisposable.dispose();
    }

    public void onRefreshFriendsList() {
        lvUserListVisibility.setValue(View.GONE);
        Disposable disposable = chatRepository
                .getFriendList(
                        Configuration.URL_FRIENDS_LIST+"/"+userId, headersMap
                )
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(friendsList -> {
                    Log.w("AVX", "got dialog list with size = "+friendsList.size());
                    psRecievedFriendResponseList.onNext(friendsList);
                    lvUserListVisibility.setValue(View.VISIBLE);

                }, throwable -> Log.e("AVX", "Error on api call:", throwable));
        compositeDisposable.add(disposable);
    }

    public void sendCreateDialogRequest(List<String> pickedProfiles, String dialogCaption) {
        compositeDisposable.add(
                dialogRepository
                .createDialog(headersMap, dialogCaption, pickedProfiles)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(response -> {
                        if (response.getCode().equals("ok"))
                            psCreateDialogCompletedAction.onNext(response.getValue());
                    }, throwable -> Log.e("AVX", "Error on api call:", throwable)
                )
        );
    }
}
