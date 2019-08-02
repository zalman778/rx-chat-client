package com.hwx.rx_chat_client.viewModel.friend;

import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.databinding.BindingAdapter;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.hwx.rx_chat.common.response.FriendResponse;
import com.hwx.rx_chat_client.Configuration;
import com.squareup.picasso.Picasso;

import javax.inject.Inject;

import io.reactivex.subjects.PublishSubject;

public class FriendElementViewModel extends ViewModel {
    private MutableLiveData<String> lvImageUrl = new MutableLiveData<>();
    private MutableLiveData<String> lvUsername = new MutableLiveData<>();

    private String userId;
    private boolean canBePicked;
    private boolean isPicked = false;

    private PublishSubject<String> psProfileSelected;
    private PublishSubject<String> psProfilePicked;



    //TODO fix memory leak!
    private static Picasso staticPicasso;

    @Inject
    public FriendElementViewModel(FriendResponse friendResponse, PublishSubject<String> psProfileSelected,  PublishSubject<String> psProfilePicked, Picasso picasso, boolean canBePicked) {
        setFriendResponse(friendResponse);
        this.psProfileSelected = psProfileSelected;
        this.psProfilePicked = psProfilePicked;
        this.canBePicked = canBePicked;
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

    public boolean isPicked() {
        return isPicked;
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

        if (friendResponse.getImageUrl() != null)
            lvImageUrl.setValue(
                    Configuration.HTTPS_SERVER_URL+Configuration.IMAGE_PREFIX+friendResponse.getImageUrl()
            );
        userId = friendResponse.getUserId();
    }


    public void onUserSelected(View view) {
        if (canBePicked) {
            isPicked = !isPicked;
            psProfilePicked.onNext(userId);
        }
        else
            psProfileSelected.onNext(userId);
    }


}
