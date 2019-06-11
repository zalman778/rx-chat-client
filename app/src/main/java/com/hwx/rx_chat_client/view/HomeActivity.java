package com.hwx.rx_chat_client.view;

import android.app.Activity;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.hwx.rx_chat_client.R;
import com.hwx.rx_chat_client.RxChatApplication;
import com.hwx.rx_chat_client.databinding.ActivityHomeBinding;
import com.hwx.rx_chat_client.util.ViewModelFactory;
import com.hwx.rx_chat_client.viewModel.HomeViewModel;

import javax.inject.Inject;

import in.mayanknagwanshi.imagepicker.ImageSelectActivity;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class HomeActivity extends AppCompatActivity {

    @Inject
    ViewModelFactory viewModelFactory;

    private HomeViewModel homeViewModel;
    private ActivityHomeBinding activityHomeBinding;
    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        SharedPreferences pref = getApplicationContext().getSharedPreferences("localPref", 0); // 0 - for private mode

        boolean loggedIn = pref.getBoolean("logged_in", false);

        //если авторизации не было, то прыгаем на активити авторизации
        if (!loggedIn) {
            finish();
            startActivity(LoginActivity.getIntent(HomeActivity.this));
        }

        //иначе грузим меню и все остальное
        ((RxChatApplication) getApplication()).getAppComponent().doInjectHomeActivity(this);
        initDataBinding();

        //обработка нажатия logout
        compositeDisposable.add(
            homeViewModel
                .getPsProfileLogout()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(integer -> {
                    Log.d("avx", "onChanged:logoutListenner");
                    SharedPreferences pref1 = getApplicationContext().getSharedPreferences("localPref", 0);
                    SharedPreferences.Editor editor = pref1.edit();
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
                    startActivityForResult(intent, 1213);

                })
        );

        //обработка выбора фрагмента
        homeViewModel.getLdTabSwitched().observe(this, this::replaceFragment);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.i("AVX", "got result "+resultCode+"; "+ data == null ? "emp" : "non");
        if (requestCode == 1213 && resultCode == Activity.RESULT_OK) {
            String filePath = data.getStringExtra(ImageSelectActivity.RESULT_FILE_PATH);

            homeViewModel.sendNewProfileAvatar(filePath);
        }
    }

    private void initDataBinding() {
        homeViewModel = ViewModelProviders.of(this, viewModelFactory).get(HomeViewModel.class);
        activityHomeBinding = DataBindingUtil.setContentView(this, R.layout.activity_home);
        activityHomeBinding.setLifecycleOwner(this);
        activityHomeBinding.setVm(homeViewModel);
    }

    public static Intent getIntent(Context context) {
        return new Intent(context, HomeActivity.class);
    }

    private void replaceFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.container, fragment);
        transaction.commit();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        compositeDisposable.dispose();
    }
}
