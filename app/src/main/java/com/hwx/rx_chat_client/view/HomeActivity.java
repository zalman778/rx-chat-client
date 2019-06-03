package com.hwx.rx_chat_client.view;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.hwx.rx_chat_client.R;
import com.hwx.rx_chat_client.RxChatApplication;
import com.hwx.rx_chat_client.databinding.ActivityHomeBinding;
import com.hwx.rx_chat_client.util.ViewModelFactory;
import com.hwx.rx_chat_client.viewModel.HomeViewModel;

import javax.inject.Inject;

public class HomeActivity extends AppCompatActivity {

    @Inject
    ViewModelFactory viewModelFactory;

    private HomeViewModel homeViewModel;
    private ActivityHomeBinding activityHomeBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        SharedPreferences pref = getApplicationContext().getSharedPreferences("localPref", 0); // 0 - for private mode

        boolean loggedIn = pref.getBoolean("logged_in", false);

        //если авторизации не было, то прыгаем на активити авторизации
        if (!loggedIn) {
            startActivity(LoginActivity.getIntent(HomeActivity.this));
        }

        //иначе грузим меню и все остальное
        ((RxChatApplication) getApplication()).getAppComponent().doInjectHomeActivity(this);
        initDataBinding();

        //обработка нажатия logout
        homeViewModel.getLdProfileLogout().observe(this, integer -> {
            Log.d("avx", "onChanged:logoutListenner");
            SharedPreferences pref1 = getApplicationContext().getSharedPreferences("localPref", 0);
            SharedPreferences.Editor editor = pref1.edit();
            editor.putString("user_id", null);
            editor.putString("name", null);
            editor.putString("token", null);
            editor.putBoolean("logged_in", false);
            editor.apply();
            recreate();
        });

        //обработка выбора фрагмента
        homeViewModel.getLdTabSwitched().observe(this, this::replaceFragment);

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

}
