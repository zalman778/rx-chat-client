package com.hwx.rx_chat_client.view.dialer;

import android.Manifest;
import android.app.Activity;
import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;

import com.hwx.rx_chat_client.R;
import com.hwx.rx_chat_client.background.p2p.service.RxP2PService;
import com.hwx.rx_chat_client.databinding.ActivityDialCallerBinding;
import com.hwx.rx_chat_client.viewModel.dialer.DialCallerViewModel;

import javax.inject.Inject;

import dagger.android.AndroidInjector;
import dagger.android.DispatchingAndroidInjector;
import dagger.android.HasActivityInjector;



public class DialCallerActivity extends AppCompatActivity implements HasActivityInjector {

    @Inject
    public ViewModelProvider.Factory mFactory;

    @Inject
    DispatchingAndroidInjector<Activity> activityDispatchingAndroidInjector;

    @Override
    public AndroidInjector<Activity> activityInjector() {
        return activityDispatchingAndroidInjector;
    }

    public static final String EXTRA_REMOTE_PROFILE_ID = "EXTRA_REMOTE_PROFILE_ID";

    private String remoteProfileId;

    private RxP2PService rxP2PService;
    private boolean isRxP2PServiceBounded;
    private DialCallerViewModel dialCallerViewModel;
    private ActivityDialCallerBinding activityDialCallerBinding;

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
        dialCallerViewModel.setRxP2PService(rxP2PService);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityCompat.requestPermissions(DialCallerActivity.this, new String[]{Manifest.permission.RECORD_AUDIO}, 1);


        Intent rxP2pServiceIntent = new Intent(this, RxP2PService.class);
        bindService(rxP2pServiceIntent, rxP2PServiceConnection, 0);

        remoteProfileId = getIntent().getStringExtra(EXTRA_REMOTE_PROFILE_ID);

        initDataBinding();
    }

    private void initDataBinding() {
        dialCallerViewModel = ViewModelProviders.of(this, mFactory).get(DialCallerViewModel.class);
        dialCallerViewModel.setRemoteProfileId(remoteProfileId);
        activityDialCallerBinding = DataBindingUtil.setContentView(this, R.layout.activity_dial_caller);
        activityDialCallerBinding.setLifecycleOwner(this);
        activityDialCallerBinding.setDialCallerViewModel(dialCallerViewModel);
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (isRxP2PServiceBounded)
            unbindService(rxP2PServiceConnection);
    }

    public static Intent getIntent(Context context, String remoteProfileId) {
        Intent intent = new Intent(context, DialCallerActivity.class);
        intent.putExtra(EXTRA_REMOTE_PROFILE_ID, remoteProfileId);
        return intent;
    }
}
