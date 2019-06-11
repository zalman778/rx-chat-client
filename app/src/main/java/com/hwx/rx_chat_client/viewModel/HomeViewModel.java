package com.hwx.rx_chat_client.viewModel;

import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.content.SharedPreferences;
import android.databinding.BindingAdapter;
import android.graphics.drawable.Drawable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.hwx.rx_chat.common.request.ProfileInfoUpdateRequest;
import com.hwx.rx_chat.common.response.DialogResponse;
import com.hwx.rx_chat_client.Configuration;
import com.hwx.rx_chat_client.R;
import com.hwx.rx_chat_client.fragment.HomeFragment;
import com.hwx.rx_chat_client.fragment.MessagesFragment;
import com.hwx.rx_chat_client.fragment.ProfileFragment;
import com.hwx.rx_chat_client.service.ChatRepository;
import com.hwx.rx_chat_client.util.ResourceProvider;
import com.hwx.rx_chat_client.util.SharedPreferencesProvider;
import com.hwx.rx_chat_client.util.SingleLiveEvent;
import com.hwx.rx_chat_client.view.misc.HomeTab;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

public class HomeViewModel extends ViewModel {


    private ChatRepository chatRepository;
    private ResourceProvider resourceProvider;
    private SharedPreferencesProvider sharedPreferencesProvider;
    private static Picasso staticPicasso;

    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    private Map<String, String> headersMap = new HashMap<>();

    //dialogs fragment
    public MutableLiveData<Integer> isDialogsVisible = new MutableLiveData<>();
    public MutableLiveData<Boolean> isDialogsLoading = new MutableLiveData<>();
    private MutableLiveData<Fragment> ldTabSwitched = new MutableLiveData<>();
    private MutableLiveData<List<DialogResponse>> liveDialogList = new MutableLiveData<>();
    //мониторит выбор диалога в списке
    private SingleLiveEvent lvDialogPicked = new SingleLiveEvent();


    //profile fragment
    public MutableLiveData<String> profileUsername = new MutableLiveData<>();
    private PublishSubject<Integer> psProfileLogout = PublishSubject.create();



    public MutableLiveData<Drawable> homeTabDrawable = new MutableLiveData<>();
    public MutableLiveData<Drawable> messagesTabDrawable = new MutableLiveData<>();
    public MutableLiveData<Drawable> profileTabDrawable = new MutableLiveData<>();

    public MutableLiveData<String> homeTabText = new MutableLiveData<>();
    public MutableLiveData<String> messagesTabText = new MutableLiveData<>();
    public MutableLiveData<String> profileTabText = new MutableLiveData<>();

    //profile objects
    private MutableLiveData<String> lvProfileAvatarUrl = new MutableLiveData<>();
    private MutableLiveData<String> lvProfileFirstname = new MutableLiveData<>();
    private MutableLiveData<String> lvProfileLastname = new MutableLiveData<>();
    private MutableLiveData<String> lvProfileUsername = new MutableLiveData<>();
    private MutableLiveData<String> lvProfileBio = new MutableLiveData<>();

    private MutableLiveData<String> lvProfileRequestResult = new MutableLiveData<>();
    private MutableLiveData<Integer> lvProfileProgressVisibility = new MutableLiveData<>();

    private PublishSubject<Integer> psProfileSave = PublishSubject.create();
    private PublishSubject<Integer> psProfileImage = PublishSubject.create();


    public HomeViewModel(
              ChatRepository chatRepository
            , ResourceProvider resourceProvider
            , SharedPreferencesProvider sharedPreferencesProvider
              , Picasso picasso

    ) {
        this.chatRepository = chatRepository;
        this.resourceProvider = resourceProvider;
        this.sharedPreferencesProvider = sharedPreferencesProvider;
        staticPicasso = picasso;

        lvProfileProgressVisibility.setValue(View.GONE);

        isDialogsVisible.setValue(View.GONE);
        isDialogsLoading.setValue(false);
        onTabSelected(HomeTab.HOME);

        SharedPreferences pref = sharedPreferencesProvider.getSharedPreferences("localPref", 0);
        headersMap.put("Authorization", pref.getString("token", ""));

        //loading bio from prefs:
        lvProfileAvatarUrl.setValue(pref.getString("profileAvatarUrl", ""));
        lvProfileFirstname.setValue(pref.getString("profileFirstName", ""));
        lvProfileLastname.setValue(pref.getString("profileLastName", ""));
        lvProfileBio.setValue(pref.getString("profileBio", ""));
        lvProfileUsername.setValue(pref.getString("username", ""));


    }


    public MutableLiveData<List<DialogResponse>> getLiveDialogList() {
        return liveDialogList;
    }

    public MutableLiveData<Fragment> getLdTabSwitched() {
        return ldTabSwitched;
    }

    public PublishSubject<Integer> getPsProfileLogout() {
        return psProfileLogout;
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

    public MutableLiveData<String> getLvProfileAvatarUrl() {
        return lvProfileAvatarUrl;
    }

    public MutableLiveData<String> getLvProfileFirstname() {
        return lvProfileFirstname;
    }

    public MutableLiveData<String> getLvProfileUsername() {
        return lvProfileUsername;
    }

    public MutableLiveData<String> getLvProfileBio() {
        return lvProfileBio;
    }

    public MutableLiveData<String> getLvProfileRequestResult() {
        return lvProfileRequestResult;
    }

    public MutableLiveData<Integer> getLvProfileProgressVisibility() {
        return lvProfileProgressVisibility;
    }



    public void setLvProfileUsername(MutableLiveData<String> lvProfileUsername) {
        this.lvProfileUsername = lvProfileUsername;
    }

    public void setLvProfileFirstname(MutableLiveData<String> lvProfileFirstname) {
        this.lvProfileFirstname = lvProfileFirstname;
    }

    public void setLvProfileBio(MutableLiveData<String> lvProfileBio) {
        this.lvProfileBio = lvProfileBio;
    }

    public void setLvProfileRequestResult(MutableLiveData<String> lvProfileRequestResult) {
        this.lvProfileRequestResult = lvProfileRequestResult;
    }

    public void setLvProfileProgressVisibility(MutableLiveData<Integer> lvProfileProgressVisibility) {
        this.lvProfileProgressVisibility = lvProfileProgressVisibility;
    }

    public MutableLiveData<String> getLvProfileLastname() {
        return lvProfileLastname;
    }

    public void setLvProfileLastname(MutableLiveData<String> lvProfileLastname) {
        this.lvProfileLastname = lvProfileLastname;
    }

    public PublishSubject<Integer> getPsProfileSave() {
        return psProfileSave;
    }

    public void setPsProfileSave(PublishSubject<Integer> psProfileSave) {
        this.psProfileSave = psProfileSave;
    }

    public PublishSubject<Integer> getPsProfileImage() {
        return psProfileImage;
    }

    public void setPsProfileImage(PublishSubject<Integer> psProfileImage) {
        this.psProfileImage = psProfileImage;
    }

    // Loading Image using Glide Library.
    @BindingAdapter("imageUrl")
    public static void setLvProfileAvatarUrl(ImageView imageView, String url){
        staticPicasso
                .load(url)
                .into(imageView);
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

    public void onProfileAvatarClick(View view) {

        Log.i("AVX", "avatar");
        psProfileImage.onNext(1);
    }

    public void onProfileLogOut (View view) {
        psProfileLogout.onNext(1);
    }

    public void onProfileSaveChanges (View view) {
        lvProfileProgressVisibility.setValue(View.VISIBLE);

        ProfileInfoUpdateRequest profileInfoUpdateRequest = new ProfileInfoUpdateRequest(
                lvProfileFirstname.getValue()
                ,lvProfileLastname.getValue()
                ,lvProfileUsername.getValue()
                ,lvProfileBio.getValue()
        );

        compositeDisposable.add(
            chatRepository
                .updateProfileBio(headersMap, profileInfoUpdateRequest)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(e->{
                    lvProfileProgressVisibility.setValue(View.GONE);
                    //Log.i("AVX", "got answer from server" + e.getCode()+" "+e.getMessage());
                    if (!e.getCode().equals("err")) {
                        lvProfileRequestResult.setValue("Error: "+e.getMessage());
                        //TODO - special activity for changing username...
                    }

                }, e->Log.e("AVX", "err", e))
        );
        psProfileSave.onNext(1);
    }


    //sending avatar to server
    public void sendNewProfileAvatar(String filePath) {
        File file = new File(filePath);
        RequestBody requestBody = RequestBody.create(MediaType.parse("*/*"), file);
        MultipartBody.Part fileToUpload = MultipartBody.Part.createFormData("img", file.getName(), requestBody);

        Log.i("AVX", "sending avatar to the server... ");

        compositeDisposable.add(
            chatRepository
                .updateProfilePic(headersMap, fileToUpload)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(e->{
                    Log.i("AVX", "got answer from server" + e.getCode()+" "+e.getMessage());
                    lvProfileAvatarUrl.setValue(Configuration.HTTPS_SERVER_URL +e.getValue());

                }, e->Log.e("AVX", "err", e))
        );

    }
}
