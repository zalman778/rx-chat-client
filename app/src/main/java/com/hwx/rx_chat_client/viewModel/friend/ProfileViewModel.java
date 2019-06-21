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
    private String userId;

    private CompositeDisposable compositeDisposable = new CompositeDisposable();
    private Map<String, String> headersMap = new HashMap<>();

    private PublishSubject<String> psDialogOpenAction = PublishSubject.create();


    //form objects
    private MutableLiveData<String> lvProfileAvatarUrl = new MutableLiveData<>();
    private MutableLiveData<String> lvProfileFirstname = new MutableLiveData<>();
    private MutableLiveData<String> lvProfileLastname = new MutableLiveData<>();
    private MutableLiveData<String> lvProfileUsername = new MutableLiveData<>();
    private MutableLiveData<String> lvProfileBio = new MutableLiveData<>();

    private MutableLiveData<Integer> lvVisibilitySendFriendRequest = new MutableLiveData<>();
    private MutableLiveData<Integer> lvVisibilityProfileProgress = new MutableLiveData<>();
    private MutableLiveData<String> lvProfileRequestResult = new MutableLiveData<>();


    public ProfileViewModel(
              SharedPreferencesProvider sharedPreferencesProvider
            , FriendRepository friendRepository
            , DialogRepository dialogRepository
            , Picasso picasso
    ) {
        this.sharedPreferencesProvider = sharedPreferencesProvider;
        this.friendRepository = friendRepository;
        this.dialogRepository = dialogRepository;
        staticPicasso = picasso;

        SharedPreferences pref = sharedPreferencesProvider.getSharedPreferences("localPref", 0);
        headersMap.put("Authorization", pref.getString("token", ""));
        userId = pref.getString("user_id", "");

        lvVisibilitySendFriendRequest.setValue(View.VISIBLE);
        lvVisibilityProfileProgress.setValue(View.GONE);
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
    }

    public MutableLiveData<String> getLvProfileAvatarUrl() {
        return lvProfileAvatarUrl;
    }

    public MutableLiveData<String> getLvProfileFirstname() {
        return lvProfileFirstname;
    }

    public void setLvProfileFirstname(MutableLiveData<String> lvProfileFirstname) {
        this.lvProfileFirstname = lvProfileFirstname;
    }

    public MutableLiveData<String> getLvProfileLastname() {
        return lvProfileLastname;
    }

    public void setLvProfileLastname(MutableLiveData<String> lvProfileLastname) {
        this.lvProfileLastname = lvProfileLastname;
    }

    public MutableLiveData<String> getLvProfileUsername() {
        return lvProfileUsername;
    }

    public void setLvProfileUsername(MutableLiveData<String> lvProfileUsername) {
        this.lvProfileUsername = lvProfileUsername;
    }

    public MutableLiveData<String> getLvProfileBio() {
        return lvProfileBio;
    }

    public void setLvProfileBio(MutableLiveData<String> lvProfileBio) {
        this.lvProfileBio = lvProfileBio;
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

    // Loading Image using Picasso
    @BindingAdapter("imageUrl")
    public static void setLvProfileAvatarUrl(ImageView imageView, String url){
        if (url != null)
            staticPicasso
                    .load(url)
                    .into(imageView);
    }

    public void onClickOpenChat() {
        compositeDisposable.add(
            dialogRepository
                .findOrCreateDialog(Configuration.URL_DIALOGS_FIND_OR_CREATE + "/"+userId+"/"+profileId, headersMap)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                          resp-> {
                              psDialogOpenAction.onNext(resp.getValue());

                          }
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
