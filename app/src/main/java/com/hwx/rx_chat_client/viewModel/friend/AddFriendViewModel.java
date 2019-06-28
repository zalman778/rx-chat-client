package com.hwx.rx_chat_client.viewModel.friend;

import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.View;

import com.hwx.rx_chat.common.response.FriendResponse;
import com.hwx.rx_chat.common.response.UserDetailsResponse;
import com.hwx.rx_chat_client.Configuration;
import com.hwx.rx_chat_client.repository.ChatRepository;
import com.hwx.rx_chat_client.repository.FriendRepository;
import com.hwx.rx_chat_client.util.SharedPreferencesProvider;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;

public class AddFriendViewModel extends ViewModel {
    private ChatRepository chatRepository;
    private FriendRepository friendRepository;
    private SharedPreferencesProvider sharedPreferencesProvider;

    private static Picasso staticPicasso;

    private MutableLiveData<String> lvSeachUsername = new MutableLiveData<>();
    private PublishSubject<List<FriendResponse>> psRecievedFriendResponseList = PublishSubject.create();
    private PublishSubject<String> psProfileSelected = PublishSubject.create();
    private MutableLiveData<Integer> lvUserListVisibility = new MutableLiveData<>();

    private CompositeDisposable compositeDisposable = new CompositeDisposable();
    private Map<String, String> headersMap = new HashMap<>();

    public AddFriendViewModel(FriendRepository friendRepository, ChatRepository chatRepository, SharedPreferencesProvider sharedPreferencesProvider, Picasso picasso) {
        this.chatRepository = chatRepository;
        this.friendRepository = friendRepository;
        this.sharedPreferencesProvider = sharedPreferencesProvider;

        staticPicasso = picasso;

        lvUserListVisibility.setValue(View.GONE);

        SharedPreferences pref = sharedPreferencesProvider.getSharedPreferences("localPref", 0);
        headersMap.put("Authorization", pref.getString("token", ""));
    }



    public MutableLiveData<String> getLvSeachUsername() {
        return lvSeachUsername;
    }

    public void setLvSeachUsername(MutableLiveData<String> lvSeachUsername) {
        this.lvSeachUsername = lvSeachUsername;
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

    public void sendSearchUsersRequest(String username) {
        compositeDisposable.add(
            chatRepository
                .searchUsers(Configuration.URL_USERS_SEARCH+"/"+ username, headersMap)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(friendList -> {
                    psRecievedFriendResponseList.onNext(friendList);
                    lvUserListVisibility.setValue(View.VISIBLE);
                    Log.w("AVX", "got list of friend with size = "+friendList.size());
                }
                , err->Log.e("AVX", "err", err))
        );
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        compositeDisposable.dispose();
    }
}
