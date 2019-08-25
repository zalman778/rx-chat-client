package com.hwx.rx_chat_client.viewModel.dialer;

import android.arch.lifecycle.MutableLiveData;
import android.util.Log;

import com.hwx.rx_chat_client.background.p2p.service.RxP2PService;
import com.hwx.rx_chat_client.util.ResourceProvider;

import javax.inject.Inject;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class DialCallerViewModel extends DialerBaseViewModel {

    @Inject
    public DialCallerViewModel(
            ResourceProvider resourceProvider
    ) {
        this.resourceProvider = resourceProvider;
    }

    public void setRxP2PService(RxP2PService rxP2PService) {
        this.rxP2PService = rxP2PService;
        subscribeP2pPublishers();
    }

    private void subscribeP2pPublishers() {
        compositeDisposable.add(
            rxP2PService
                    .getPsVoiceCallResponseAction()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(rx->{
                        Log.i("AVX", "##started streaming from call resp...");
                        startVoiceStreaming();
                        startVoicePlayback();
                    })
        );
    }

    public MutableLiveData<String> getLvVoiceCallCaptionTitle() {
        return lvVoiceCallCaptionTitle;
    }

    public MutableLiveData<String> getLvVoiceCallStatus() {
        return lvVoiceCallStatus;
    }

    public void onClickVoiceCallDrop() {

    }

}
