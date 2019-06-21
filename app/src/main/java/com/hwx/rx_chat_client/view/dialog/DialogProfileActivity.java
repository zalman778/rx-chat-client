package com.hwx.rx_chat_client.view.dialog;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;

import com.hwx.rx_chat.common.response.DialogProfileResponse;
import com.hwx.rx_chat_client.R;
import com.hwx.rx_chat_client.RxChatApplication;
import com.hwx.rx_chat_client.adapter.FriendElementAdapter;
import com.hwx.rx_chat_client.databinding.ActivityDialogProfileBinding;
import com.hwx.rx_chat_client.util.ViewModelFactory;
import com.hwx.rx_chat_client.view.friend.ProfileActivity;
import com.hwx.rx_chat_client.viewModel.conversation.DialogProfileViewModel;

import javax.inject.Inject;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;

public class DialogProfileActivity extends AppCompatActivity {

    private static final String EXTRA_DIALOG_INFO = "EXTRA_DIALOG_INFO";
    @Inject
    ViewModelFactory viewModelFactory;

    private ActivityDialogProfileBinding activityDialogProfileBinding;
    private DialogProfileViewModel dialogProfileViewModel;

    private CompositeDisposable compositeDisposable = new CompositeDisposable();
    private FriendElementAdapter friendElementAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);





        ((RxChatApplication) getApplication()).getAppComponent().doInjectDialogProfileActivity(this);

        initDataBinding();

        initRecyclerViewAdapter();

        subscribePublishers();

        //recieving extra data
        DialogProfileResponse dialogProfileResponse = (DialogProfileResponse) getIntent().getSerializableExtra(EXTRA_DIALOG_INFO);
        dialogProfileViewModel.setDialogResponse(dialogProfileResponse);
        friendElementAdapter.setFriendList(dialogProfileResponse.getFriendList());
    }





    private void initDataBinding() {
        dialogProfileViewModel = ViewModelProviders.of(this, viewModelFactory).get(DialogProfileViewModel.class);
        activityDialogProfileBinding = DataBindingUtil.setContentView(this, R.layout.activity_dialog_profile);
        activityDialogProfileBinding.setLifecycleOwner(this);
        activityDialogProfileBinding.setDialogProfileViewModel(dialogProfileViewModel);
    }

    private void initRecyclerViewAdapter() {
        friendElementAdapter = new FriendElementAdapter(

                dialogProfileViewModel.getPsProfileSelected(), this, null
                , null, dialogProfileViewModel.getPicasso()
                , activityDialogProfileBinding.listMembers, false
        );
        activityDialogProfileBinding.listMembers.setLayoutManager(new LinearLayoutManager(this));
        activityDialogProfileBinding.listMembers.setAdapter(friendElementAdapter);
    }

    private void subscribePublishers() {
        compositeDisposable.add(
                dialogProfileViewModel
                        .getPsProfileSelecedLoadedAction()
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(userInfo->{
                            startActivity(ProfileActivity.fillDetail(getApplicationContext(), userInfo));
                        }, e-> Log.e("AVX", "error on req", e))
        );
    }

    public static Intent getIntent(Context context, DialogProfileResponse dialogProfileResponse) {
        Intent intent = new Intent(context, DialogProfileActivity.class);
        intent.putExtra(EXTRA_DIALOG_INFO, dialogProfileResponse);
        return intent;
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        compositeDisposable.dispose();
    }
}
