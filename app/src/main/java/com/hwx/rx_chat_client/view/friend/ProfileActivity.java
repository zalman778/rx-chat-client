package com.hwx.rx_chat_client.view.friend;

import android.app.Activity;
import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.hwx.rx_chat_client.R;
import com.hwx.rx_chat_client.background.service.RxService;
import com.hwx.rx_chat_client.databinding.ActivityProfileBinding;
import com.hwx.rx_chat_client.view.dialog.ConversationActivity;
import com.hwx.rx_chat_client.viewModel.friend.ProfileViewModel;

import javax.inject.Inject;

import dagger.android.AndroidInjector;
import dagger.android.DispatchingAndroidInjector;
import dagger.android.HasActivityInjector;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

/*
    активити профиля пользователя
    должны быть кнопки:
    * открыть(начать) чат
    * отправить запрос в друзья
    * заблокировать?
    *

 */

public class ProfileActivity extends AppCompatActivity implements HasActivityInjector {

    @Inject
    public ViewModelProvider.Factory mFactory;

    @Inject
    DispatchingAndroidInjector<Activity> activityDispatchingAndroidInjector;


    private static final String EXTRA_PROFILE_ID = "EXTRA_PROFILE_ID";


    private ActivityProfileBinding activityProfileBinding;
    private ProfileViewModel profileViewModel;

    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    private RxService rxService;
    private boolean isRxServiceBounded;

    private ServiceConnection rxServiceConnection = new ServiceConnection() {
        public void onServiceConnected(
                ComponentName className
                , IBinder service
        ) {
            rxService = ((RxService.RxServiceBinder) service).getService();
            isRxServiceBounded = true;
        }

        public void onServiceDisconnected(ComponentName arg0) {
            isRxServiceBounded = false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent rxServiceIntent = new Intent(this, RxService.class);
        bindService(rxServiceIntent, rxServiceConnection, 0);

        initDataBinding();

        String profileId = getIntent().getStringExtra(EXTRA_PROFILE_ID);
        profileViewModel.setProfileId(profileId);

        subscribePublishers();

        //setting up rxService in VM:
        new Handler(Looper.getMainLooper()).postDelayed(()-> profileViewModel.setRxService(rxService), 500);
    }

    private void subscribePublishers() {
        compositeDisposable.add(
            profileViewModel.getPsDialogOpenAction()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(dialogId -> {
                        startActivity(ConversationActivity.getIntent(getApplicationContext(), dialogId));
                    }, err-> Log.e("AVX", "err", err))
        );
    }


    private void initDataBinding() {
        profileViewModel = ViewModelProviders.of(this, mFactory).get(ProfileViewModel.class);
        activityProfileBinding = DataBindingUtil.setContentView(this, R.layout.activity_profile);
        activityProfileBinding.setLifecycleOwner(this);
        activityProfileBinding.setProfileViewModel(profileViewModel);
    }



    public static Intent fillDetail(Context context, String profileId) {
        Intent intent = new Intent(context, ProfileActivity.class);
        intent.putExtra(EXTRA_PROFILE_ID, profileId);
        return intent;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isRxServiceBounded)
            unbindService(rxServiceConnection);

        compositeDisposable.dispose();
    }

    @Override
    public AndroidInjector<Activity> activityInjector() {
        return activityDispatchingAndroidInjector;
    }
}
