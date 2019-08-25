package com.hwx.rx_chat_client.view.dialer;

import android.Manifest;
import android.app.Activity;
import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;

import com.hwx.rx_chat_client.R;
import com.hwx.rx_chat_client.background.p2p.service.RxP2PService;
import com.hwx.rx_chat_client.databinding.ActivityDialAcceptorBinding;
import com.hwx.rx_chat_client.viewModel.dialer.DialAcceptorViewModel;

import javax.inject.Inject;

import dagger.android.AndroidInjector;
import dagger.android.DispatchingAndroidInjector;
import dagger.android.HasActivityInjector;

public class DialAcceptorActivity extends AppCompatActivity implements HasActivityInjector {

    @Inject
    public ViewModelProvider.Factory mFactory;

    @Inject
    DispatchingAndroidInjector<Activity> activityDispatchingAndroidInjector;

    @Override
    public AndroidInjector<Activity> activityInjector() {
        return activityDispatchingAndroidInjector;
    }

    public static final String EXTRA_DIALER_CAPTION = "EXTRA_DIALER_CAPTION";
    public static final String EXTRA_REMOTE_PROFILE_ID = "EXTRA_REMOTE_PROFILE_ID";



    private String remoteProfileId;
    private String dialogCaption;

    private RxP2PService rxP2PService;
    private boolean isRxP2PServiceBounded;
    private DialAcceptorViewModel dialAcceptorViewModel;
    private ActivityDialAcceptorBinding activityDialAcceptorBinding;

    private ServiceConnection rxP2PServiceConnection = new ServiceConnection() {
        public void onServiceConnected(
                ComponentName className
                , IBinder service
        ) {
            rxP2PService = ((RxP2PService.RxP2PServiceBinder) service).getService();
            isRxP2PServiceBounded = true;
            onP2pServiceConnected();
        }

        public void onServiceDisconnected(ComponentName arg0) {
            isRxP2PServiceBounded = false;
        }
    };

    private void onP2pServiceConnected() {
        dialAcceptorViewModel.setRxP2PService(rxP2PService);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityCompat.requestPermissions(DialAcceptorActivity.this, new String[]{Manifest.permission.RECORD_AUDIO}, 1);

        Intent rxP2pServiceIntent = new Intent(this, RxP2PService.class);
        bindService(rxP2pServiceIntent, rxP2PServiceConnection, 0);

        remoteProfileId = getIntent().getStringExtra(EXTRA_REMOTE_PROFILE_ID);
        dialogCaption = getIntent().getStringExtra(EXTRA_DIALER_CAPTION);


        initDataBinding();
        subscribePublishers();
    }

    private void subscribePublishers() {

    }

    private void initDataBinding() {
        dialAcceptorViewModel = ViewModelProviders.of(this, mFactory).get(DialAcceptorViewModel.class);
        dialAcceptorViewModel.setRemoteProfileId(remoteProfileId);
        dialAcceptorViewModel.setDialogCaption(dialogCaption);
        activityDialAcceptorBinding = DataBindingUtil.setContentView(this, R.layout.activity_dial_acceptor);
        activityDialAcceptorBinding.setLifecycleOwner(this);
        activityDialAcceptorBinding.setDialAcceptorViewModel(dialAcceptorViewModel);
    }


    @Override
    protected void onStop() {
        super.onStop();
        unbindService(rxP2PServiceConnection);
    }
}
