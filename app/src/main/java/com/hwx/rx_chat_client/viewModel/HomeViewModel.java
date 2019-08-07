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

import com.hwx.rx_chat.common.entity.rx.RxMessage;
import com.hwx.rx_chat.common.object.rx.RxObject;
import com.hwx.rx_chat.common.object.rx.types.ObjectType;
import com.hwx.rx_chat.common.object.rx.types.SettingType;
import com.hwx.rx_chat.common.request.ProfileInfoUpdateRequest;
import com.hwx.rx_chat.common.response.DialogResponse;
import com.hwx.rx_chat.common.response.FriendResponse;
import com.hwx.rx_chat_client.Configuration;
import com.hwx.rx_chat_client.R;
import com.hwx.rx_chat_client.background.p2p.service.RxP2PService;
import com.hwx.rx_chat_client.fragment.DialogsFragment;
import com.hwx.rx_chat_client.fragment.FriendsFragment;
import com.hwx.rx_chat_client.fragment.HomeFragment;
import com.hwx.rx_chat_client.fragment.ProfileFragment;
import com.hwx.rx_chat_client.repository.ChatRepository;
import com.hwx.rx_chat_client.repository.FriendRepository;
import com.hwx.rx_chat_client.util.ResourceProvider;
import com.hwx.rx_chat_client.util.SharedPreferencesProvider;
import com.hwx.rx_chat_client.util.SingleLiveEvent;
import com.hwx.rx_chat_client.view.misc.HomeTab;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.processors.PublishProcessor;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

public class HomeViewModel extends ViewModel {

    private ChatRepository chatRepository;

    private FriendRepository friendRepository;

    private ResourceProvider resourceProvider;

    private SharedPreferencesProvider sharedPreferencesProvider;

    //TODO memory lead, fix it...
    private Picasso picasso;

    private RxP2PService rxP2PService;

    private static Picasso staticPicasso;

    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    private Map<String, String> headersMap = new HashMap<>();
    private String userId;

    //dialogs fragment
    public MutableLiveData<Integer> isDialogsVisible = new MutableLiveData<>();
    public MutableLiveData<Boolean> isDialogsLoading = new MutableLiveData<>();
    private PublishSubject<Fragment> psTabSwitched = PublishSubject.create();
    private MutableLiveData<List<DialogResponse>> liveDialogList = new MutableLiveData<>();

    //мониторит выбор диалога в списке
    private SingleLiveEvent lvDialogPicked = new SingleLiveEvent();

    //получает события в сокет - пока не используется
    private PublishProcessor<RxObject> ppDialogFragment = PublishProcessor.create();

    private PublishSubject<RxMessage> psRecievedRxMessageAction = PublishSubject.create();


    //profile fragment
    private PublishSubject<Integer> psProfileLogout = PublishSubject.create();
    private PublishSubject<String> psProfileSelected = PublishSubject.create();

    public MutableLiveData<Drawable> homeTabDrawable = new MutableLiveData<>();
    public MutableLiveData<Drawable> messagesTabDrawable = new MutableLiveData<>();
    public MutableLiveData<Drawable> profileTabDrawable = new MutableLiveData<>();
    public MutableLiveData<Drawable> friendsTabDrawable = new MutableLiveData<>();

    public MutableLiveData<String> homeTabText = new MutableLiveData<>();



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


    //friends objects:
    private MutableLiveData<Integer> isFriendsListVisible = new MutableLiveData<>();
    private MutableLiveData<Boolean> isFriendsListLoading = new MutableLiveData<>();
    private MutableLiveData<List<FriendResponse>> lvFriendsList = new MutableLiveData<>();

    @Inject
    public HomeViewModel(
              ChatRepository chatRepository
            , FriendRepository friendRepository
            , ResourceProvider resourceProvider
            , SharedPreferencesProvider sharedPreferencesProvider
            , Picasso picasso

    ) {
        this.chatRepository = chatRepository;
        //Log.w("AVX", "inside homeViewModel 2 contructor: chatRepo is"+(chatRepository == null));
        this.friendRepository = friendRepository;
        this.resourceProvider = resourceProvider;
        this.sharedPreferencesProvider = sharedPreferencesProvider;
        staticPicasso = picasso;

        lvProfileProgressVisibility.setValue(View.GONE);

        isDialogsVisible.setValue(View.GONE);
        isDialogsLoading.setValue(false);
        onTabSelected(HomeTab.HOME);


        isFriendsListVisible.setValue(View.GONE);
        isFriendsListLoading.setValue(false);

        SharedPreferences pref = sharedPreferencesProvider.getSharedPreferences("localPref", 0);
        headersMap.put("Authorization", pref.getString("token", ""));
        userId =  pref.getString("user_id", "");

        //loading bio from prefs:
        String imageUrl = pref.getString("profileAvatarUrl", "");
        if (imageUrl != null && !imageUrl.isEmpty())
            lvProfileAvatarUrl.setValue(pref.getString("profileAvatarUrl", ""));

        lvProfileFirstname.setValue(pref.getString("profileFirstName", ""));
        lvProfileLastname.setValue(pref.getString("profileLastName", ""));
        lvProfileBio.setValue(pref.getString("profileBio", ""));
        lvProfileUsername.setValue(pref.getString("username", ""));

        subscribePublishers();

        //sending userId
        RxObject rxObject = new RxObject(ObjectType.SETTING, SettingType.ID_USER_FOR_BACKGROUND, userId, null);
        ppDialogFragment.onNext(rxObject);
    }

    public MutableLiveData<List<DialogResponse>> getLiveDialogList() {
        return liveDialogList;
    }

    public PublishSubject<Fragment> getPsTabSwitched() {
        return psTabSwitched;
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

    public MutableLiveData<String> getLvProfileLastname() {
        return lvProfileLastname;
    }

    public PublishSubject<Integer> getPsProfileSave() {
        return psProfileSave;
    }

    public PublishSubject<Integer> getPsProfileImage() {
        return psProfileImage;
    }

    public MutableLiveData<Integer> getIsFriendsListVisible() {
        return isFriendsListVisible;
    }

    public MutableLiveData<Boolean> getIsFriendsListLoading() {
        return isFriendsListLoading;
    }

    public MutableLiveData<List<FriendResponse>> getLvFriendsList() {
        return lvFriendsList;
    }

    public PublishSubject<String> getPsProfileSelected() {
        return psProfileSelected;
    }

    public PublishSubject<RxMessage> getPsRecievedRxMessageAction() {
        return psRecievedRxMessageAction;
    }

    public void setRxP2PService(RxP2PService rxP2PService) {
        this.rxP2PService = rxP2PService;
        subscribeRxP2pPublishers();
    }

    private void subscribeRxP2pPublishers() {
        compositeDisposable.add(
            rxP2PService
                .getRxObj()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(rxP2PObject -> {
                    if (rxP2PObject.getObjectType().equals(com.hwx.rx_chat_client.background.p2p.object.type.ObjectType.MESSAGE)) {
                        RxMessage rxMessage = rxP2PObject.getMessage();
                        Log.w("AVX", "got message from user "+rxMessage.toString());
                    }
                })
        );
    }

    public Picasso getPicasso() {
        return staticPicasso;
    }

    // Loading Image using Picasso
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
                messagesTabDrawable.setValue(resourceProvider.getDrawable(R.drawable.ic_message_dark));
                showDialogsFragment();
                break;
            case PROFILE:
                profileTabDrawable.setValue(resourceProvider.getDrawable(R.drawable.ic_user_dark));
                showProfileFragment();
                break;
            case FRIENDS:
                friendsTabDrawable.setValue(resourceProvider.getDrawable(R.drawable.ic_friends_dark));
                showFriendsFragment();
                break;
        }
    }

    private void showFriendsFragment() {
        psTabSwitched.onNext(new FriendsFragment());
    }

    private void resetTabs() {
        homeTabDrawable.setValue(resourceProvider.getDrawable(R.drawable.ic_home_light));
        messagesTabDrawable.setValue(resourceProvider.getDrawable(R.drawable.ic_message_light));
        profileTabDrawable.setValue(resourceProvider.getDrawable(R.drawable.ic_user_light));
        friendsTabDrawable.setValue(resourceProvider.getDrawable(R.drawable.ic_friends_light));

        homeTabText.setValue("");

    }

    private void subscribePublishers() {
        //TODO: chatSocket
//        compositeDisposable.add(
//            chatSocket
//                .getEventChannel(ppDialogFragment)
//                .subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(rxObject -> {
//                    //Fixing time from Mongo if its message:
//                    if (rxObject.getMessage() != null) {
//                        Calendar cal = Calendar.getInstance();
//                        cal.setTime(rxObject.getMessage().getDateSent());
//                        cal.add(Calendar.HOUR_OF_DAY, Configuration.MONGO_TIMEZONE_CORRECTION_HRS);
//                        rxObject.getMessage().setDateSent(cal.getTime());
//
//                        //updating message viewmodel:
//                        psRecievedRxMessageAction.onNext(rxObject.getMessage());
//                    }
//                    Log.w("AVX", "GOT in hvm : rxObj = "+rxObject.toString());
//                    //psRxMessage.onNext(rxObject);
//                }, e->Log.e("AVX", "err on rx "+e.getMessage()+"; "+e.getLocalizedMessage(), e))
//        );

    }


    public void onRefreshDialogs() {
        isDialogsLoading.setValue(true);
        isDialogsVisible.setValue(View.GONE);



        Disposable disposable =
            chatRepository
                .getDialogList(
                    Configuration.URL_DIALOGS_LIST+"/"+userId
                      , headersMap
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

    public void onRefreshFriendsList() {
        isFriendsListLoading.setValue(true);
        isFriendsListVisible.setValue(View.GONE);

        Disposable disposable = chatRepository
                .getFriendList(
                        Configuration.URL_FRIENDS_LIST+"/"+userId
                         , headersMap
                )
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(friendsList -> {
                    lvFriendsList.setValue(friendsList);
                    isFriendsListLoading.setValue(false);
                    isFriendsListVisible.setValue(View.VISIBLE);

                }, throwable -> Log.e("AVX", "Error on api call:", throwable));
        compositeDisposable.add(disposable);
    }



    private void showHomeFragment() {
        psTabSwitched.onNext(new HomeFragment());
    }

    private void showDialogsFragment() {
        Log.w("AVX", "rx setted userId = "+userId);
        RxObject rxObject = new RxObject(ObjectType.SETTING, SettingType.ID_USER_FOR_CONVERSATION, userId, null);
        ppDialogFragment.onNext(rxObject);
        psTabSwitched.onNext(new DialogsFragment());
    }

    private void showProfileFragment() {
        psTabSwitched.onNext(new ProfileFragment());
    }

    public void onProfileAvatarClick(View view) {
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
                    if (e.getCode().equals("err")) {
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
                    lvProfileAvatarUrl.setValue(Configuration.HTTPS_SERVER_URL + Configuration.IMAGE_PREFIX +e.getValue());

                }, e->Log.e("AVX", "err", e))
        );

    }

    public void sendFriendRequestAccept(String requestID) {
        compositeDisposable.add(
            friendRepository
                .acceptFriendRequest(headersMap, requestID)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(e->{
                        Log.i("AVX", "got answer from server" + e.getCode()+" "+e.getMessage());
                    }, e->Log.e("AVX", "err", e))
        );

    }

    public void sendFriendRequestReject(String requestID) {
        compositeDisposable.add(
                friendRepository
                        .rejectFriendRequest(headersMap, requestID)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(e->{
                            Log.i("AVX", "got answer from server" + e.getCode()+" "+e.getMessage());
                        }, e->Log.e("AVX", "err", e))
        );
    }
}
