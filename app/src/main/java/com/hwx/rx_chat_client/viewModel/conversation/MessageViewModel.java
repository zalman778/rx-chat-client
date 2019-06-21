package com.hwx.rx_chat_client.viewModel.conversation;

import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.databinding.BindingAdapter;
import android.graphics.drawable.ColorDrawable;
import android.util.Log;
import android.widget.ImageView;

import com.hwx.rx_chat.common.entity.rx.RxMessage;
import com.hwx.rx_chat_client.Configuration;
import com.hwx.rx_chat_client.R;
import com.hwx.rx_chat_client.util.ResourceProvider;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;

import io.reactivex.subjects.PublishSubject;

public class MessageViewModel extends ViewModel {
    private MutableLiveData<String> lvUserFrom = new MutableLiveData<>();
    private MutableLiveData<String> lvValue = new MutableLiveData<>();
    private MutableLiveData<String> lvMessageDate = new MutableLiveData<>();
    private MutableLiveData<ColorDrawable> lvMessageBackgroundColor = new MutableLiveData<>();
    private MutableLiveData<String> lvMessageUserImage = new MutableLiveData<>();
    private ResourceProvider resourceProvider;
    private String fromUserId;
    private PublishSubject<String> psUserImageClicked;

    //TODO fix memory leak!
    private static Picasso staticPicasso;

    public MessageViewModel(ResourceProvider resourceProvider, Picasso picasso,  PublishSubject<String> psUserImageClicked) {
        this.resourceProvider = resourceProvider;
        lvMessageBackgroundColor.setValue(new ColorDrawable(resourceProvider.getColorDrawable(R.color.transparent)));
        this.psUserImageClicked = psUserImageClicked;
        staticPicasso = picasso;
    }

    public void setRxMessage(RxMessage rxMessage) {
        lvUserFrom.setValue(rxMessage.getUserFromName());
        lvValue.setValue(rxMessage.getValue());
        if (rxMessage.getDateSent() != null)
            lvMessageDate.setValue(new SimpleDateFormat("HH:mm:ss MM.dd").format(rxMessage.getDateSent()));
        if (rxMessage.getImageUrl() != null)
            lvMessageUserImage.setValue(
                    Configuration.HTTPS_SERVER_URL + Configuration.IMAGE_PREFIX+rxMessage.getImageUrl()
            );
        fromUserId = rxMessage.getUserFromId();
    }

    public MutableLiveData<String> getLvUserFrom() {
        return lvUserFrom;
    }

    public MutableLiveData<String> getLvValue() {
        return lvValue;
    }

    public MutableLiveData<String> getLvMessageDate() {
        return lvMessageDate;
    }

    public MutableLiveData<ColorDrawable> getLvMessageBackgroundColor() {
        return lvMessageBackgroundColor;
    }

    public MutableLiveData<String> getLvMessageUserImage() {
        return lvMessageUserImage;
    }

    // Loading Image using Picasso
    @BindingAdapter("imageUrl")
    public static void setLvMessageUserImage(ImageView imageView, String url){
        staticPicasso
                .load(url)
                .into(imageView);
    }

    public void onUserImageClicked() {
        psUserImageClicked.onNext(fromUserId);
    }
}
