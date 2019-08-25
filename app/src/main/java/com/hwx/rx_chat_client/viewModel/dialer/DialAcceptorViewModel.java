package com.hwx.rx_chat_client.viewModel.dialer;

import android.arch.lifecycle.MutableLiveData;
import android.util.Log;
import android.view.View;

import com.hwx.rx_chat_client.background.p2p.object.RxP2PObject;
import com.hwx.rx_chat_client.background.p2p.object.type.ObjectType;
import com.hwx.rx_chat_client.util.ResourceProvider;

import javax.inject.Inject;

public class DialAcceptorViewModel extends DialerBaseViewModel {

    private MutableLiveData<Integer> lvVisibilityBtnVoiceCallAccept = new MutableLiveData<>();
    private MutableLiveData<Integer> lvVisibilityBtnVoiceCallDecline = new MutableLiveData<>();
    private MutableLiveData<Integer> lvVisibilityBtnVoiceCallDrop = new MutableLiveData<>();

    @Inject
    public DialAcceptorViewModel(
            ResourceProvider resourceProvider
    ) {
        super();
        this.resourceProvider = resourceProvider;

        lvVisibilityBtnVoiceCallAccept.setValue(View.VISIBLE);
        lvVisibilityBtnVoiceCallDecline.setValue(View.VISIBLE);
        lvVisibilityBtnVoiceCallDrop.setValue(View.GONE);

    }


    public MutableLiveData<Integer> getLvVisibilityBtnVoiceCallAccept() {
        return lvVisibilityBtnVoiceCallAccept;
    }

    public MutableLiveData<Integer> getLvVisibilityBtnVoiceCallDecline() {
        return lvVisibilityBtnVoiceCallDecline;
    }

    public MutableLiveData<Integer> getLvVisibilityBtnVoiceCallDrop() {
        return lvVisibilityBtnVoiceCallDrop;
    }

    public void onClickBtnVoiceCallAccept() {
        voiceCallRequestHandle(true);

        lvVisibilityBtnVoiceCallAccept.setValue(View.GONE);
        lvVisibilityBtnVoiceCallDecline.setValue(View.GONE);
        lvVisibilityBtnVoiceCallDrop.setValue(View.VISIBLE);

        //тут можно начить стримить
        Log.i("AVX", "##started streaming from call req...");
        startVoiceStreaming();
        startVoicePlayback();
    }

    public void onClickBtnVoiceCallDecline() {
        voiceCallRequestHandle(false);
        //TODO: close activity:
    }

    public void onClickBtnVoiceCallDrop() {
        status = false;
        recorder.release();
        //TODO: close activity:
    }



    //dialer command
    private void voiceCallRequestHandle(boolean accepted) {
        if (accepted) {
            RxP2PObject rxP2PObject = new RxP2PObject();
            rxP2PObject.setObjectType(ObjectType.ACTION_VOICE_CALL_START_RESPONSE);
            rxP2PObject.setaBoolean(accepted);
            rxP2PService.sendRxP2PObject(remoteProfileId, rxP2PObject);

            Log.w("AVX", "## sent ACTION_VOICE_CALL_START_RESPONSE");
        }
    }

    public void setDialogCaption(String dialogCaption) {
        this.dialogCaption = dialogCaption;
        lvVoiceCallCaptionTitle.setValue(dialogCaption);
    }

    public String getDialogCaption() {
        return dialogCaption;
    }
}
