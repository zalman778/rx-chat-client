package com.hwx.rx_chat_client.viewModel;

import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.databinding.BindingAdapter;
import android.net.wifi.aware.PublishConfig;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.hwx.rx_chat.common.response.FriendResponse;
import com.hwx.rx_chat_client.Configuration;
import com.hwx.rx_chat_client.service.ChatRepository;
import com.squareup.picasso.Picasso;

import java.util.Map;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.subjects.PublishSubject;

public class FriendElementViewModel extends ViewModel {
    private MutableLiveData<String> lvImageUrl = new MutableLiveData<>();
    private MutableLiveData<String> lvUsername = new MutableLiveData<>();
    private String userId;
    private PublishSubject<String> psProfileSelected = PublishSubject.create();

    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    private Map<String, String> headersMap;
    private ChatRepository chatRepository;
    //TODO fix memory leak!
    private static Picasso staticPicasso;

    public FriendElementViewModel(FriendResponse friendResponse, Map<String, String> headersMap, ChatRepository chatRepository, Picasso picasso) {
        setFriendResponse(friendResponse);
        this.headersMap = headersMap;
        this.chatRepository = chatRepository;
        staticPicasso = picasso;
    }

    public MutableLiveData<String> getLvImageUrl() {
        return lvImageUrl;
    }

    public void setLvImageUrl(MutableLiveData<String> lvImageUrl) {
        this.lvImageUrl = lvImageUrl;
    }

    public MutableLiveData<String> getLvUsername() {
        return lvUsername;
    }

    public void setLvUsername(MutableLiveData<String> lvUsername) {
        this.lvUsername = lvUsername;
    }

    public PublishSubject<String> getPsProfileSelected() {
        return psProfileSelected;
    }

    // Loading Image using Picasso
    @BindingAdapter("imageUrl")
    public static void setLvImageUrl(ImageView imageView, String url){
        if (url != null)
            staticPicasso
                    .load(url)
                    .into(imageView);
    }

    public void setFriendResponse(FriendResponse friendResponse) {
        Log.w("AVX", "setFrResp:"+friendResponse.getAccepted());
        if (friendResponse.getAccepted() != null && !friendResponse.getAccepted())
            lvUsername.setValue("(ЗАПРОС) "+friendResponse.getUsername());
        else
            lvUsername.setValue(friendResponse.getUsername());

        lvImageUrl.setValue(Configuration.HTTPS_SERVER_URL+friendResponse.getImageUrl());
        userId = friendResponse.getUserId();
    }


    public void onUserSelected(View view) {
        psProfileSelected.onNext(userId);
    }


}
