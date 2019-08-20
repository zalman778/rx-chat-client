package com.hwx.rx_chat_client.viewModel.conversation;

import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.databinding.BindingAdapter;
import android.graphics.drawable.ColorDrawable;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.hwx.rx_chat.common.response.DialogResponse;
import com.hwx.rx_chat_client.Configuration;
import com.hwx.rx_chat_client.R;
import com.hwx.rx_chat_client.repository.ChatRepository;
import com.hwx.rx_chat_client.util.ResourceProvider;
import com.hwx.rx_chat_client.util.SingleLiveEvent;
import com.hwx.rx_chat_client.viewModel.misc.DialogInfoHolder;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Map;

import javax.inject.Inject;

import io.reactivex.disposables.CompositeDisposable;


public class DialogElementViewModel extends ViewModel {

    private MutableLiveData<String> dialogCaption = new MutableLiveData<>();
    private MutableLiveData<String> dialogLastUser = new MutableLiveData<>();
    private MutableLiveData<String> dialogLastMessage = new MutableLiveData<>();
    private MutableLiveData<String> dialogImageUrl = new MutableLiveData<>();
    private MutableLiveData<String> dialogLastTime = new MutableLiveData<>();
    private MutableLiveData<ColorDrawable> lvDialogCardBackground = new MutableLiveData<>();

    private String remoteProfileId;
    private String dialogId;
    private boolean isPrivateDialog = false;

    private SingleLiveEvent lvDialogPicked;

    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    private Map<String, String> headersMap;
    private ChatRepository chatRepository;
    private ResourceProvider resourceProvider;

    //TODO fix memory leak!
    private static Picasso staticPicasso;

//    @Inject
    public DialogElementViewModel(DialogResponse dialogResponse
            , Map<String, String> headersMap
            , ChatRepository chatRepository
            , SingleLiveEvent lvDialogPicked
            , Picasso picasso
            , ResourceProvider resourceProvider
    ) {
        this.headersMap = headersMap;
        this.chatRepository = chatRepository;
        this.resourceProvider = resourceProvider;
        this.lvDialogPicked = lvDialogPicked;

        staticPicasso = picasso;

        setDialogResponse(dialogResponse);
    }

    public void setDialogResponse(DialogResponse dialogResponse) {
        dialogCaption.setValue(dialogResponse.getDialogName());
        dialogLastUser.setValue(dialogResponse.getLastUser());
        dialogLastMessage.setValue(dialogResponse.getLastMessage());
        remoteProfileId = dialogResponse.getRemoteProfileId();

        if (dialogResponse.getLastDate() != null) {
            dialogLastTime.setValue(new SimpleDateFormat("HH:mm:ss MM.dd").format(dialogResponse.getLastDate()));
        }

        dialogId = dialogResponse.getDialogId();
        isPrivateDialog = dialogResponse.isPrivate();

        if (dialogResponse.getChatImage() != null)
            dialogImageUrl.setValue(
                Configuration.HTTPS_SERVER_URL + Configuration.IMAGE_PREFIX+dialogResponse.getChatImage()
            );

        if (dialogResponse.isPrivate())
            lvDialogCardBackground.setValue(new ColorDrawable(resourceProvider.getColorDrawable(R.color.light_green)));
        else
            lvDialogCardBackground.setValue(new ColorDrawable(resourceProvider.getColorDrawable(R.color.grey_200)));
    }

    public MutableLiveData<String> getDialogCaption() {
        return dialogCaption;
    }


    public MutableLiveData<String> getDialogLastUser() {
        return dialogLastUser;
    }


    public MutableLiveData<String> getDialogLastMessage() {
        return dialogLastMessage;
    }


    public MutableLiveData<String> getDialogImageUrl() {
        return dialogImageUrl;
    }

    public MutableLiveData<String> getDialogLastTime() {
        return dialogLastTime;
    }

    public MutableLiveData<ColorDrawable> getLvDialogCardBackground() {
        return lvDialogCardBackground;
    }

    public void onDialogSelected(View view) {
        lvDialogPicked.setValue(new DialogInfoHolder(dialogId, isPrivateDialog, remoteProfileId));
    }

    // Loading Image using Picasso
    @BindingAdapter("imageUrl")
    public static void loadImageUrl(ImageView imageView, String url){
        staticPicasso
                .load(url)
                .into(imageView);
    }


}
