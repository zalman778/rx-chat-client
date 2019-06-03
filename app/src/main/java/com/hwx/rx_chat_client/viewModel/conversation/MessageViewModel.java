package com.hwx.rx_chat_client.viewModel.conversation;

import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.graphics.drawable.ColorDrawable;

import com.hwx.rx_chat.common.entity.rx.RxMessage;
import com.hwx.rx_chat_client.R;
import com.hwx.rx_chat_client.util.ResourceProvider;

import java.text.SimpleDateFormat;

public class MessageViewModel extends ViewModel {
    private MutableLiveData<String> lvUserFrom = new MutableLiveData<>();
    private MutableLiveData<String> lvValue = new MutableLiveData<>();
    private MutableLiveData<String> lvMessageDate = new MutableLiveData<>();
    private MutableLiveData<ColorDrawable> lvMessageBackgroundColor = new MutableLiveData<>();
    private ResourceProvider resourceProvider;

    public MessageViewModel(ResourceProvider resourceProvider) {
        this.resourceProvider = resourceProvider;
        lvMessageBackgroundColor.setValue(new ColorDrawable(resourceProvider.getColorDrawable(R.color.transparent)));
    }

    public void setRxMessage(RxMessage rxMessage) {
        lvUserFrom.setValue(rxMessage.getUserFromName());
        lvValue.setValue(rxMessage.getValue());
        if (rxMessage.getDateSent() != null)
            lvMessageDate.setValue(new SimpleDateFormat("HH:mm:ss MM.dd").format(rxMessage.getDateSent()));
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
}
