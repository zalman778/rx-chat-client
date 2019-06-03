package com.hwx.rx_chat_client.view;

import android.app.ProgressDialog;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.hwx.rx_chat_client.Configuration;
import com.hwx.rx_chat_client.R;
import com.hwx.rx_chat_client.RxChatApplication;
import com.hwx.rx_chat_client.databinding.ActivityLoginBinding;
import com.hwx.rx_chat.common.response.LoginResponse;
import com.hwx.rx_chat_client.util.ViewModelFactory;
import com.hwx.rx_chat_client.viewModel.LoginViewModel;

import javax.inject.Inject;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class LoginActivity extends AppCompatActivity {

    @Inject
    ViewModelFactory viewModelFactory;

    private ActivityLoginBinding activityLoginBinding;
    private LoginViewModel loginViewModel;
    private ProgressDialog progressDialog;

    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ((RxChatApplication) getApplication()).getAppComponent().doInjectLoginActivity(this);
        initDataBinding();

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("please wait...");
        progressDialog.setCancelable(true);

        loginViewModel.getResponseLiveData().observe(this, this::consumeResponse);

        compositeDisposable.add(
                loginViewModel
                        .getPsGotoLoginActivity()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(e->{
                            startActivity(SignupActivity.getIntent(LoginActivity.this));
                        })
        );
    }

    private void consumeResponse(LoginResponse loginResponse) {
        switch (loginResponse.getStatus()) {
            case "loading":
                progressDialog.show();
                break;
            case "error":
                progressDialog.dismiss();
                loginViewModel.getResult().setValue(loginResponse.getText());
                break;
            case "ok":
                progressDialog.dismiss();
                Log.w("AVX", "got login response:"+loginResponse.toString());
                SharedPreferences pref = getApplicationContext().getSharedPreferences("localPref", 0); // 0 - for private mode
                SharedPreferences.Editor editor = pref.edit();
                editor.putString("user_id", loginResponse.getUserId());
                editor.putString("name", loginResponse.getName());
                editor.putString("token", loginResponse.getToken());
                editor.putBoolean("logged_in", true);
                editor.apply();

                startActivity(HomeActivity.getIntent(LoginActivity.this));
                break;
        }
    }

    private void initDataBinding() {
        loginViewModel = ViewModelProviders.of(this, viewModelFactory).get(LoginViewModel.class);
        activityLoginBinding = DataBindingUtil.setContentView(this, R.layout.activity_login);
        activityLoginBinding.setLifecycleOwner(this);
        activityLoginBinding.setLoginViewModel(loginViewModel);
    }

    public static Intent getIntent(Context context) {
        return new Intent(context, LoginActivity.class);
    }
}