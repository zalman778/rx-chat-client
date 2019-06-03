package com.hwx.rx_chat_client.viewModel;

import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.View;

import com.hwx.rx_chat.common.response.DialogResponse;
import com.hwx.rx_chat_client.R;
import com.hwx.rx_chat_client.fragment.HomeFragment;
import com.hwx.rx_chat_client.fragment.MessagesFragment;
import com.hwx.rx_chat_client.fragment.ProfileFragment;
import com.hwx.rx_chat_client.service.ChatRepository;
import com.hwx.rx_chat_client.util.ResourceProvider;
import com.hwx.rx_chat_client.util.SharedPreferencesProvider;
import com.hwx.rx_chat_client.util.SingleLiveEvent;
import com.hwx.rx_chat_client.view.misc.HomeTab;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class HomeViewModel extends ViewModel {


    private ChatRepository chatRepository;
    private ResourceProvider resourceProvider;
    private SharedPreferencesProvider sharedPreferencesProvider;

    //dialogs fragment
    public MutableLiveData<Integer> isDialogsVisible = new MutableLiveData<>();
    public MutableLiveData<Boolean> isDialogsLoading = new MutableLiveData<>();
    private MutableLiveData<Fragment> ldTabSwitched = new MutableLiveData<>();
    private MutableLiveData<List<DialogResponse>> liveDialogList = new MutableLiveData<>();
    //мониторит выбор диалога в списке
    private SingleLiveEvent lvDialogPicked = new SingleLiveEvent();
    //private MutableLiveData<Dialog> lvConcreteDialogPicked = new MutableLiveData<>();


    //profile fragment
    public MutableLiveData<String> profileUsername = new MutableLiveData<>();
    private MutableLiveData<Integer> ldProfileLogout = new MutableLiveData<>();



    public MutableLiveData<Drawable> homeTabDrawable = new MutableLiveData<>();
    public MutableLiveData<Drawable> messagesTabDrawable = new MutableLiveData<>();
    public MutableLiveData<Drawable> profileTabDrawable = new MutableLiveData<>();

    public MutableLiveData<String> homeTabText = new MutableLiveData<>();
    public MutableLiveData<String> messagesTabText = new MutableLiveData<>();
    public MutableLiveData<String> profileTabText = new MutableLiveData<>();

    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    private Map<String, String> headersMap = new HashMap<>();

    public HomeViewModel(
              ChatRepository chatRepository
            , ResourceProvider resourceProvider
            , SharedPreferencesProvider sharedPreferencesProvider

    ) {
        this.chatRepository = chatRepository;
        this.resourceProvider = resourceProvider;
        this.sharedPreferencesProvider = sharedPreferencesProvider;

        isDialogsVisible.setValue(View.GONE);
        isDialogsLoading.setValue(false);
        onTabSelected(HomeTab.HOME);

        SharedPreferences pref = sharedPreferencesProvider.getSharedPreferences("localPref", 0);
        headersMap.put("Authorization", pref.getString("token", ""));
    }


    public MutableLiveData<List<DialogResponse>> getLiveDialogList() {
        return liveDialogList;
    }

    public MutableLiveData<Fragment> getLdTabSwitched() {
        return ldTabSwitched;
    }

    public MutableLiveData<Integer> getLdProfileLogout() {
        return ldProfileLogout;
    }

    public SingleLiveEvent getLvDialogPicked() {
        return lvDialogPicked;
    }

    public Map<String, String> getHeadersMap() {
        return headersMap;
    }

    //TODO - this is bad way, refactor!!
    public ChatRepository getChatRepository() {
        return chatRepository;
    }

    public void onTabSelected(HomeTab tab) {
        resetTabs();
        switch (tab) {
            case HOME:
                homeTabDrawable.setValue(resourceProvider.getDrawable(R.drawable.ic_home_dark));
                homeTabText.setValue(resourceProvider.getString(R.string.home));
                showHomeFragment();
                break;

            case MESSAGES:
                messagesTabDrawable.setValue(resourceProvider.getDrawable(R.drawable.ic_messages_dark));
                messagesTabText.setValue(resourceProvider.getString(R.string.messages));
                showMessagesFragment();
                break;
            case PROFILE:
                profileTabDrawable.setValue(resourceProvider.getDrawable(R.drawable.ic_user_dark));
                profileTabText.setValue(resourceProvider.getString(R.string.profile));
                showProfileFragment();
                break;
        }
    }

    private void resetTabs() {
        homeTabDrawable.setValue(resourceProvider.getDrawable(R.drawable.ic_home_light));
        messagesTabDrawable.setValue(resourceProvider.getDrawable(R.drawable.ic_messages_light));
        profileTabDrawable.setValue(resourceProvider.getDrawable(R.drawable.ic_user_light));

        homeTabText.setValue("");
        messagesTabText.setValue("");
        profileTabText.setValue("");
    }


    public void onRefreshDialogs() {
        isDialogsLoading.setValue(true);
        isDialogsVisible.setValue(View.GONE);



        Disposable disposable = chatRepository
                .getDialogList(
                  headersMap
                , sharedPreferencesProvider.getSharedPreferences("localPref", 0).getString("user_id", "")
                )
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(dialogList -> {
                    liveDialogList.setValue(dialogList);
                    isDialogsLoading.setValue(false);
                    isDialogsVisible.setValue(View.VISIBLE);

                }, throwable -> Log.e("AVX", "Error on api call:", throwable));
        compositeDisposable.add(disposable);
    }

    private void showHomeFragment() {
        ldTabSwitched.setValue(new HomeFragment());
    }

    private void showMessagesFragment() {
        ldTabSwitched.setValue(new MessagesFragment());
    }

    private void showProfileFragment() {
        ldTabSwitched.setValue(new ProfileFragment());
    }

    public void onProfileLogout(View view) {
        Log.w("avx", "hVM:onlogout");
        ldProfileLogout.setValue(1);

    }


}
