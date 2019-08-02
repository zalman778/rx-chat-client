package com.hwx.rx_chat_client.view;

import android.app.Activity;
import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.hwx.rx_chat_client.R;
import com.hwx.rx_chat_client.databinding.ActivitySignupBinding;
import com.hwx.rx_chat_client.viewModel.SignupViewModel;

import javax.inject.Inject;

import dagger.android.AndroidInjector;
import dagger.android.DispatchingAndroidInjector;
import dagger.android.HasActivityInjector;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class SignupActivity extends AppCompatActivity implements HasActivityInjector {

    @Inject
    public ViewModelProvider.Factory mFactory;

    @Inject
    DispatchingAndroidInjector<Activity> activityDispatchingAndroidInjector;

    private SignupViewModel signupViewModel;
    private ActivitySignupBinding activitySignupBinding;

    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initDataBinding();

        compositeDisposable.add(
            signupViewModel
                .getPsGotoLoginActivity()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(e->{
                        finish();
                        startActivity(LoginActivity.getIntent(SignupActivity.this));
                    })
        );
    }

    private void initDataBinding() {
        signupViewModel = ViewModelProviders.of(this, mFactory).get(SignupViewModel.class);
        activitySignupBinding = DataBindingUtil.setContentView(this, R.layout.activity_signup);
        activitySignupBinding.setLifecycleOwner(this);
        activitySignupBinding.setSignupViewModel(signupViewModel);
    }

    public static Intent getIntent(Context context) {
        return new Intent(context, SignupActivity.class);
    }

    @Override
    public AndroidInjector<Activity> activityInjector() {
        return activityDispatchingAndroidInjector;
    }
}
