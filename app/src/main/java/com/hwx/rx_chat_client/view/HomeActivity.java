package com.hwx.rx_chat_client.view;

import android.app.Activity;
import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.hwx.rx_chat.common.object.rx.RxObject;
import com.hwx.rx_chat.common.object.rx.types.ObjectType;
import com.hwx.rx_chat.common.object.rx.types.SettingType;
import com.hwx.rx_chat_client.R;
import com.hwx.rx_chat_client.background.p2p.service.RxP2PService;
import com.hwx.rx_chat_client.background.service.RxService;
import com.hwx.rx_chat_client.databinding.ActivityHomeBinding;
import com.hwx.rx_chat_client.viewModel.HomeViewModel;

import javax.inject.Inject;

import dagger.android.AndroidInjector;
import dagger.android.DispatchingAndroidInjector;
import dagger.android.support.HasSupportFragmentInjector;
import in.mayanknagwanshi.imagepicker.ImageSelectActivity;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class HomeActivity extends AppCompatActivity implements HasSupportFragmentInjector {

    @Inject
    DispatchingAndroidInjector<Fragment> fragmentDispatchingAndroidInjector;

    @Override
    public AndroidInjector<Fragment> supportFragmentInjector() {
        return fragmentDispatchingAndroidInjector;
    }

    @Inject
    public ViewModelProvider.Factory mFactory;

    private HomeViewModel homeViewModel;
    private ActivityHomeBinding activityHomeBinding;
    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    private RxService rxService;
    private boolean isRxServiceBounded;

    private RxP2PService rxP2PService;
    private boolean isRxP2PServiceBounded;

    String userId;

    private SharedPreferences preferences;

    private static final Integer REQUEST_CODE_IMAGE_SELECT = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_home);

        preferences = getApplicationContext().getSharedPreferences("localPref", 0); // 0 - for private mode

        boolean loggedIn = preferences.getBoolean("logged_in", false);

        //если авторизации не было, то прыгаем на активити авторизации
        if (!loggedIn) {
            finish();
            startActivity(LoginActivity.getIntent(HomeActivity.this));
        }

        //иначе грузим меню и все остальное
        initDataBinding();
        subscribePublishers();

        //initializing client-server rxService
        Intent rxServiceIntent = new Intent(this, RxService.class);
        startService(rxServiceIntent);
        bindService(rxServiceIntent, rxServiceConnection, 0);

        Intent rxP2PServiceIntent = new Intent(this, RxP2PService.class);
        startService(rxP2PServiceIntent);
        bindService(rxP2PServiceIntent, rxP2PServiceConnection, 0);

        //binding service
        userId = preferences.getString("user_id", "");

    }
    private void onServiceBoundedAction() {
        sendRxUserIdForBackground();
    }
    private void onP2pServiceBoundedAction() {

        rxP2PService.setProfileId(userId);
        homeViewModel.setRxP2PService(rxP2PService);
    }

    private void sendRxUserIdForBackground() {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(() -> {
            if (userId != null && !userId.isEmpty()) {
                RxObject rxObject = new RxObject(ObjectType.SETTING, SettingType.ID_USER_FOR_BACKGROUND, userId, null);
                rxService.sendRxObject(rxObject);
            }
        }, 1000);
    }

    private ServiceConnection rxServiceConnection = new ServiceConnection() {
        public void onServiceConnected(
                  ComponentName className
                , IBinder service
        ) {
            rxService = ((RxService.RxServiceBinder) service).getService();
            isRxServiceBounded = true;
            onServiceBoundedAction();
        }

        public void onServiceDisconnected(ComponentName arg0) {
            isRxServiceBounded = false;
        }
    };

    private ServiceConnection rxP2PServiceConnection = new ServiceConnection() {
        public void onServiceConnected(
                ComponentName className
                , IBinder service
        ) {
            rxP2PService = ((RxP2PService.RxP2PServiceBinder) service).getService();
            isRxP2PServiceBounded = true;
            onP2pServiceBoundedAction();
        }

        public void onServiceDisconnected(ComponentName arg0) {
            isRxP2PServiceBounded = false;
        }
    };




    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_IMAGE_SELECT && resultCode == Activity.RESULT_OK) {
            String filePath = data.getStringExtra(ImageSelectActivity.RESULT_FILE_PATH);

            homeViewModel.sendNewProfileAvatar(filePath);
        }
    }



    public static Intent getIntent(Context context) {
        return new Intent(context, HomeActivity.class);
    }

    private void replaceFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.container, fragment);
        transaction.commit();
    }

    private void initDataBinding() {
        homeViewModel = ViewModelProviders.of(this, mFactory).get(HomeViewModel.class);

        activityHomeBinding = DataBindingUtil.setContentView(this, R.layout.activity_home);
        activityHomeBinding.setLifecycleOwner(this);
        activityHomeBinding.setVm(homeViewModel);
    }

    private void subscribePublishers() {
        //обработка нажатия logout
        compositeDisposable.add(
                homeViewModel
                        .getPsProfileLogout()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(integer -> {
                            Log.d("avx", "onChanged:logoutListenner");
                            SharedPreferences.Editor editor = preferences.edit();
                            editor.putString("user_id", null);
                            editor.putString("username", null);
                            editor.putString("token", null);
                            editor.putBoolean("logged_in", false);

                            editor.putString("profileAvatarUrl", null);
                            editor.putString("profileFirstName", null);
                            editor.putString("profileLastName", null);
                            editor.putString("profileBio", null);

                            editor.apply();
                            //recreate();
                            finish();
                            startActivity(LoginActivity.getIntent(HomeActivity.this));
                        })
        );

        //обработка нажатия save profile
        compositeDisposable.add(
                homeViewModel
                        .getPsProfileSave()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(e->{
                            homeViewModel.getLvProfileProgressVisibility().setValue(View.VISIBLE);

                        })
        );

        //обработка нажатия image pick
        compositeDisposable.add(
                homeViewModel
                        .getPsProfileImage()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(e->{
                            Intent intent = new Intent(getBaseContext(), ImageSelectActivity.class);
                            intent.putExtra(ImageSelectActivity.FLAG_COMPRESS, false);//default is true
                            intent.putExtra(ImageSelectActivity.FLAG_CAMERA, true);//default is true
                            intent.putExtra(ImageSelectActivity.FLAG_GALLERY, true);//default is true
                            startActivityForResult(intent, REQUEST_CODE_IMAGE_SELECT);

                        })
        );


        compositeDisposable.add(
                homeViewModel
                        .getPsTabSwitched()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(this::replaceFragment)
        );
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isRxServiceBounded)
            unbindService(rxServiceConnection);

        if (isRxP2PServiceBounded)
            unbindService(rxP2PServiceConnection);

        compositeDisposable.dispose();
    }


}
