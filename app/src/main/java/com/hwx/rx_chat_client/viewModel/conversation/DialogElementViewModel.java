package com.hwx.rx_chat_client.viewModel.conversation;

import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.databinding.BindingAdapter;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.hwx.rx_chat.common.response.DialogResponse;
import com.hwx.rx_chat_client.Configuration;
import com.hwx.rx_chat_client.repository.ChatRepository;
import com.hwx.rx_chat_client.util.SingleLiveEvent;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Map;

import io.reactivex.disposables.CompositeDisposable;

public class DialogElementViewModel extends ViewModel {

    private MutableLiveData<String> dialogCaption = new MutableLiveData<>();
    private MutableLiveData<String> dialogLastUser = new MutableLiveData<>();
    private MutableLiveData<String> dialogLastMessage = new MutableLiveData<>();
    private MutableLiveData<String> dialogImageUrl = new MutableLiveData<>();
    private MutableLiveData<String> dialogLastTime = new MutableLiveData<>();
    private String dialogId;

    private SingleLiveEvent lvDialogPicked;

    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    private Map<String, String> headersMap;
    private ChatRepository chatRepository;
    //TODO fix memory leak!
    private static Picasso staticPicasso;

    public DialogElementViewModel(DialogResponse dialogResponse
            , Map<String, String> headersMap
            , ChatRepository chatRepository
            , SingleLiveEvent lvDialogPicked
            , Picasso picasso
    ) {
        setDialogResponse(dialogResponse);
        this.headersMap = headersMap;
        this.chatRepository = chatRepository;
        this.lvDialogPicked = lvDialogPicked;
        staticPicasso = picasso;
    }

    public void setDialogResponse(DialogResponse dialogResponse) {
        dialogCaption.setValue(dialogResponse.getDialogName());
        dialogLastUser.setValue(dialogResponse.getLastUser());
        dialogLastMessage.setValue(dialogResponse.getLastMessage());
        if (dialogResponse.getLastDate() != null) {
            dialogLastTime.setValue(new SimpleDateFormat("HH:mm:ss MM.dd").format(dialogResponse.getLastDate()));
        }
        dialogId = dialogResponse.getDialogId();
        //Log.w("AVX", "got image = "+Configuration.IMAGE_PREFIX+dialogResponse.getChatImage());
        if (dialogResponse.getChatImage() != null)
            dialogImageUrl.setValue(
                    Configuration.HTTPS_SERVER_URL + Configuration.IMAGE_PREFIX+dialogResponse.getChatImage()
            );
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

    public void onDialogSelected(View view) {
        lvDialogPicked.setValue(dialogId);
    }

    // Loading Image using Picasso
    @BindingAdapter("imageUrl")
    public static void loadImageUrl(ImageView imageView, String url){
        staticPicasso
                .load(url)
                .into(imageView);
    }


}
