package com.hwx.rx_chat_client.view;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.hwx.rx_chat_client.R;
import com.hwx.rx_chat_client.RxChatApplication;
import com.hwx.rx_chat_client.databinding.ActivitySignupBinding;
import com.hwx.rx_chat_client.util.ViewModelFactory;
import com.hwx.rx_chat_client.viewModel.SignupViewModel;

import javax.inject.Inject;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class SignupActivity extends AppCompatActivity {

    @Inject
    ViewModelFactory viewModelFactory;

    private SignupViewModel signupViewModel;
    private ActivitySignupBinding activitySignupBinding;

    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        ((RxChatApplication) getApplication()).getAppComponent().doInjectSignupRepository(this);
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
        signupViewModel = ViewModelProviders.of(this, viewModelFactory).get(SignupViewModel.class);
        activitySignupBinding = DataBindingUtil.setContentView(this, R.layout.activity_signup);
        activitySignupBinding.setLifecycleOwner(this);
        activitySignupBinding.setSignupViewModel(signupViewModel);
    }

    public static Intent getIntent(Context context) {
        return new Intent(context, SignupActivity.class);
    }

}
