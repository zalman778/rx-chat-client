package com.hwx.rx_chat_client.view;

import android.app.Activity;
import android.app.ProgressDialog;
import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.hwx.rx_chat.common.response.LoginResponse;
import com.hwx.rx_chat_client.Configuration;
import com.hwx.rx_chat_client.R;
import com.hwx.rx_chat_client.databinding.ActivityLoginBinding;
import com.hwx.rx_chat_client.viewModel.LoginViewModel;

import javax.inject.Inject;

import dagger.android.AndroidInjector;
import dagger.android.DispatchingAndroidInjector;
import dagger.android.HasActivityInjector;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class LoginActivity extends AppCompatActivity implements HasActivityInjector {


    @Inject
    public ViewModelProvider.Factory mFactory;

    @Inject
    DispatchingAndroidInjector<Activity> activityDispatchingAndroidInjector;

    private ActivityLoginBinding activityLoginBinding;
    private LoginViewModel loginViewModel;
    private ProgressDialog progressDialog;

    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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
                            finish();
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
                editor.putString("username", loginResponse.getUsername());
                editor.putString("token", loginResponse.getToken());
                editor.putBoolean("logged_in", true);

                //user profile data
                if (loginResponse.getAvatarUrl() != null)
                    editor.putString("profileAvatarUrl",
                            Configuration.HTTPS_SERVER_URL +Configuration.IMAGE_PREFIX+ loginResponse.getAvatarUrl()
                    );
                editor.putString("profileFirstName", loginResponse.getFirstName());
                editor.putString("profileLastName", loginResponse.getLastName());
                editor.putString("profileBio", loginResponse.getBio());
                editor.apply();

                startActivity(HomeActivity.getIntent(LoginActivity.this));
                break;
        }
    }

    private void initDataBinding() {
        loginViewModel = ViewModelProviders.of(this, mFactory).get(LoginViewModel.class);
        activityLoginBinding = DataBindingUtil.setContentView(this, R.layout.activity_login);
        activityLoginBinding.setLifecycleOwner(this);
        activityLoginBinding.setLoginViewModel(loginViewModel);
    }

    public static Intent getIntent(Context context) {
        return new Intent(context, LoginActivity.class);
    }

    @Override
    public AndroidInjector<Activity> activityInjector() {
        return activityDispatchingAndroidInjector;
    }
}
