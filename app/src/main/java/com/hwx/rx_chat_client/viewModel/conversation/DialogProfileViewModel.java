package com.hwx.rx_chat_client.viewModel.conversation;

import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.content.SharedPreferences;
import android.databinding.BindingAdapter;
import android.util.Log;
import android.widget.ImageView;

import com.hwx.rx_chat.common.response.DialogProfileResponse;
import com.hwx.rx_chat.common.response.UserDetailsResponse;
import com.hwx.rx_chat_client.Configuration;
import com.hwx.rx_chat_client.repository.ChatRepository;
import com.hwx.rx_chat_client.repository.DialogRepository;
import com.hwx.rx_chat_client.util.SharedPreferencesProvider;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;

public class DialogProfileViewModel extends ViewModel {

    private DialogRepository dialogRepository;
    private ChatRepository chatRepository;
    private SharedPreferencesProvider sharedPreferencesProvider;
    private static Picasso staticPicasso;

    private CompositeDisposable compositeDisposable = new CompositeDisposable();
    private Map<String, String> headersMap = new HashMap<>();

    private MutableLiveData<String> lvImageUrl = new MutableLiveData<>();
    private MutableLiveData<String> lvCaption = new MutableLiveData<>();
    private MutableLiveData<Boolean> lvDialogMembersRefreshing = new MutableLiveData<>();
    private MutableLiveData<Integer> lvDialogMembersVisible = new MutableLiveData<>();

    private PublishSubject<String> psProfileSelected = PublishSubject.create();
    private PublishSubject<UserDetailsResponse> psProfileSelecedLoadedAction = PublishSubject.create();

    public DialogProfileViewModel(
              DialogRepository dialogRepository
            , ChatRepository chatRepository
            , SharedPreferencesProvider sharedPreferencesProvider
            , Picasso picasso
    ) {
        staticPicasso = picasso;
        this.dialogRepository = dialogRepository;
        this.chatRepository = chatRepository;
        this.sharedPreferencesProvider = sharedPreferencesProvider;

        SharedPreferences pref = sharedPreferencesProvider.getSharedPreferences("localPref", 0);
        headersMap.put("Authorization", pref.getString("token", ""));

        subscribePublishers();
    }

    private void subscribePublishers() {
        //profile was picked, requesting profile info and sending it to new activity:
        compositeDisposable.add(
                psProfileSelected
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(this::sendProfileInfoRequest
                                , err-> Log.e("AVX", "err", err))
        );
    }

    private void sendProfileInfoRequest(String profileId) {
        compositeDisposable.add(
                chatRepository
                        .getProfileInfo(Configuration.URL_GET_PROFILE_INFO+"/"+profileId, headersMap)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                profileInfo -> psProfileSelecedLoadedAction.onNext(profileInfo)
                                ,err->Log.e("AVX", "err", err)
                        )
        );
    }

    public void setDialogResponse(DialogProfileResponse dialogProfileResponse) {
        lvImageUrl.setValue(Configuration.HTTPS_SERVER_URL+Configuration.IMAGE_PREFIX+dialogProfileResponse.getChatImage());
        lvCaption.setValue(dialogProfileResponse.getDialogName());

    }

    public DialogRepository getDialogRepository() {
        return dialogRepository;
    }

    public SharedPreferencesProvider getSharedPreferencesProvider() {
        return sharedPreferencesProvider;
    }

    public static Picasso getStaticPicasso() {
        return staticPicasso;
    }

    public MutableLiveData<String> getLvImageUrl() {
        return lvImageUrl;
    }

    public MutableLiveData<String> getLvCaption() {
        return lvCaption;
    }

    public MutableLiveData<Boolean> getLvDialogMembersRefreshing() {
        return lvDialogMembersRefreshing;
    }

    public MutableLiveData<Integer> getLvDialogMembersVisible() {
        return lvDialogMembersVisible;
    }

    public Picasso getPicasso() {
        return staticPicasso;
    }

    public PublishSubject<String> getPsProfileSelected() {
        return psProfileSelected;
    }

    public PublishSubject<UserDetailsResponse> getPsProfileSelecedLoadedAction() {
        return psProfileSelecedLoadedAction;
    }

    // Loading Image using Picasso
    @BindingAdapter("imageUrl")
    public static void setLvProfileAvatarUrl(ImageView imageView, String url){
        if (url != null)
            staticPicasso
                    .load(url)
                    .into(imageView);
    }

    public void onRefreshDialogMembers() {

    }



}
