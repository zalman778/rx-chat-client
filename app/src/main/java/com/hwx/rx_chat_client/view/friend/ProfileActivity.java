package com.hwx.rx_chat_client.view.friend;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.hwx.rx_chat.common.response.UserDetailsResponse;
import com.hwx.rx_chat_client.R;
import com.hwx.rx_chat_client.RxChatApplication;
import com.hwx.rx_chat_client.databinding.ActivityProfileBinding;
import com.hwx.rx_chat_client.util.ViewModelFactory;
import com.hwx.rx_chat_client.view.ConversationActivity;
import com.hwx.rx_chat_client.viewModel.friend.ProfileViewModel;

import javax.inject.Inject;

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

public class ProfileActivity extends AppCompatActivity {


    private static final String EXTRA_USER_PROFILE_DETAILS = "EXTRA_USER_PROFILE_DETAILS";

    @Inject
    ViewModelFactory viewModelFactory;

    private ActivityProfileBinding activityProfileBinding;
    private ProfileViewModel profileViewModel;

    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ((RxChatApplication) getApplication()).getAppComponent().doInjectProfileActivity(this);
        initDataBinding();

        UserDetailsResponse userDetailsResponse = (UserDetailsResponse) getIntent().getSerializableExtra(EXTRA_USER_PROFILE_DETAILS);
        profileViewModel.setUserDetailsResponse(userDetailsResponse);

        subscribePublishers();
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
        profileViewModel = ViewModelProviders.of(this, viewModelFactory).get(ProfileViewModel.class);
        activityProfileBinding = DataBindingUtil.setContentView(this, R.layout.activity_profile);
        activityProfileBinding.setLifecycleOwner(this);
        activityProfileBinding.setProfileViewModel(profileViewModel);
    }



    public static Intent fillDetail(Context context, UserDetailsResponse userDetailsResponse) {
        Intent intent = new Intent(context, ProfileActivity.class);
        intent.putExtra(EXTRA_USER_PROFILE_DETAILS, userDetailsResponse);
        return intent;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        compositeDisposable.dispose();
    }

}
