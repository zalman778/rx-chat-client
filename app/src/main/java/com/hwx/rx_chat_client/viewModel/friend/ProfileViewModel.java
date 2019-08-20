package com.hwx.rx_chat_client.viewModel.friend;

import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.content.SharedPreferences;
import android.databinding.BindingAdapter;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.hwx.rx_chat.common.response.UserDetailsResponse;
import com.hwx.rx_chat_client.Configuration;
import com.hwx.rx_chat_client.repository.ChatRepository;
import com.hwx.rx_chat_client.repository.DialogRepository;
import com.hwx.rx_chat_client.repository.FriendRepository;
import com.hwx.rx_chat_client.util.SharedPreferencesProvider;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;

public class ProfileViewModel extends ViewModel {

    private SharedPreferencesProvider sharedPreferencesProvider;
    //TODO memory lead, fix it...
    private static Picasso staticPicasso;
    private ChatRepository chatRepository;
    private DialogRepository dialogRepository;
    private FriendRepository friendRepository;
    private String profileId;
    private String selfUserId;

    private CompositeDisposable compositeDisposable = new CompositeDisposable();
    private Map<String, String> headersMap = new HashMap<>();

    private PublishSubject<String> psDialogOpenAction = PublishSubject.create();
    private PublishSubject<String> psP2pDialogOpenAction = PublishSubject.create();


    //form objects
    private MutableLiveData<String> lvProfileAvatarUrl = new MutableLiveData<>();
    private MutableLiveData<String> lvProfileFirstname = new MutableLiveData<>();
    private MutableLiveData<String> lvProfileLastname = new MutableLiveData<>();
    private MutableLiveData<String> lvProfileUsername = new MutableLiveData<>();
    private MutableLiveData<String> lvProfileBio = new MutableLiveData<>();

    private MutableLiveData<Integer> lvVisibilitySendFriendRequest = new MutableLiveData<>();
    private MutableLiveData<Integer> lvVisibilityProfileProgress = new MutableLiveData<>();
    private MutableLiveData<Integer> lvVisibilityOpenDialog = new MutableLiveData<>();
    private MutableLiveData<Integer> lvVisibilityOpenP2PChat = new MutableLiveData<>();

    private MutableLiveData<String> lvProfileRequestResult = new MutableLiveData<>();

    @Inject
    public ProfileViewModel(
              SharedPreferencesProvider sharedPreferencesProvider
            , FriendRepository friendRepository
            , DialogRepository dialogRepository
            , ChatRepository chatRepository
            , Picasso picasso
    ) {
        this.sharedPreferencesProvider = sharedPreferencesProvider;
        this.friendRepository = friendRepository;
        this.dialogRepository = dialogRepository;
        this.chatRepository = chatRepository;
        staticPicasso = picasso;

        SharedPreferences pref = sharedPreferencesProvider.getSharedPreferences("localPref", 0);
        headersMap.put("Authorization", pref.getString("token", ""));
        selfUserId = pref.getString("user_id", "");

        lvVisibilitySendFriendRequest.setValue(View.GONE);
        lvVisibilityOpenDialog.setValue(View.GONE);
        lvVisibilityOpenP2PChat.setValue(View.GONE);
        lvVisibilityProfileProgress.setValue(View.GONE);

    }

    public PublishSubject<String> getPsP2pDialogOpenAction() {
        return psP2pDialogOpenAction;
    }

    public void setProfileId(String profileId) {

        if (!selfUserId.equals(profileId)) {
            lvVisibilityOpenDialog.setValue(View.VISIBLE);
            lvVisibilityOpenP2PChat.setValue(View.VISIBLE);
        }

        lvVisibilityProfileProgress.setValue(View.VISIBLE);
        Log.w("AVX", "connecting to "+Configuration.URL_GET_PROFILE_INFO+"/"+profileId);
        compositeDisposable.add(
                chatRepository
                        .getProfileInfo(Configuration.URL_GET_PROFILE_INFO+"/"+profileId, headersMap)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                 profileInfo->{
                                     setUserDetailsResponse(profileInfo);
                                     lvVisibilityProfileProgress.setValue(View.GONE);
                                 }
                                ,err->Log.e("AVX", "err", err)
                        )
        );
    }

    public void setUserDetailsResponse(UserDetailsResponse userDetailsResponse) {
        profileId = userDetailsResponse.getId();
        if (userDetailsResponse.getImageUrl() != null)
            lvProfileAvatarUrl.setValue(
                    Configuration.HTTPS_SERVER_URL + Configuration.IMAGE_PREFIX + userDetailsResponse.getImageUrl()
            );
        lvProfileFirstname.setValue(userDetailsResponse.getFirstname());
        lvProfileLastname.setValue(userDetailsResponse.getLastname());
        lvProfileUsername.setValue(userDetailsResponse.getUsername());
        lvProfileBio.setValue(userDetailsResponse.getBio());

        if (userDetailsResponse.getAvailableSendFriendRequest())
            lvVisibilitySendFriendRequest.setValue(View.VISIBLE);


    }

    public MutableLiveData<String> getLvProfileAvatarUrl() {
        return lvProfileAvatarUrl;
    }

    public MutableLiveData<String> getLvProfileFirstname() {
        return lvProfileFirstname;
    }


    public MutableLiveData<String> getLvProfileLastname() {
        return lvProfileLastname;
    }


    public MutableLiveData<String> getLvProfileUsername() {
        return lvProfileUsername;
    }


    public MutableLiveData<String> getLvProfileBio() {
        return lvProfileBio;
    }



    public MutableLiveData<Integer> getLvVisibilitySendFriendRequest() {
        return lvVisibilitySendFriendRequest;
    }

    public MutableLiveData<Integer> getLvVisibilityProfileProgress() {
        return lvVisibilityProfileProgress;
    }

    public PublishSubject<String> getPsDialogOpenAction() {
        return psDialogOpenAction;
    }

    public MutableLiveData<String> getLvProfileRequestResult() {
        return lvProfileRequestResult;
    }

    public MutableLiveData<Integer> getLvVisibilityOpenDialog() {
        return lvVisibilityOpenDialog;
    }

    public MutableLiveData<Integer> getLvVisibilityOpenP2PChat() {
        return lvVisibilityOpenP2PChat;
    }

    // Loading Image using Picasso
    @BindingAdapter("imageUrl")
    public static void setLvProfileAvatarUrl(ImageView imageView, String url){
        if (url != null)
            staticPicasso
                    .load(url)
                    .into(imageView);
    }

    public void onClickOpenP2PChat() {
        psP2pDialogOpenAction.onNext(profileId);
    }

    public void onClickOpenChat() {
        compositeDisposable.add(
            dialogRepository
                .findOrCreateDialog(Configuration.URL_DIALOGS_FIND_OR_CREATE + "/"+ selfUserId +"/"+profileId, headersMap)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                          resp-> psDialogOpenAction.onNext(resp.getValue())
                        , err-> Log.e("AVX", "err", err))

        );
    }

    public void onClickSendFriendRequest() {
        compositeDisposable.add(
            friendRepository.createFriendRequest(Configuration.URL_FRIENDS_REQUEST_CREATE+"/"+profileId, headersMap)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                          resp-> {
                              if (!resp.getCode().equals("err"))
                                lvVisibilitySendFriendRequest.setValue(View.GONE);
                          }
                        , err-> Log.e("AVX", "err", err))
        );
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        compositeDisposable.dispose();
    }



}
