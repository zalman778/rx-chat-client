package com.hwx.rx_chat_client.view.dialog;

import android.app.Activity;
import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;

import com.hwx.rx_chat_client.R;
import com.hwx.rx_chat_client.adapter.FriendElementAdapter;
import com.hwx.rx_chat_client.databinding.ActivityCreateDialogBinding;
import com.hwx.rx_chat_client.viewModel.conversation.CreateDialogViewModel;

import java.util.List;

import javax.inject.Inject;

import dagger.android.AndroidInjector;
import dagger.android.DispatchingAndroidInjector;
import dagger.android.HasActivityInjector;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class CreateDialogActivity extends AppCompatActivity implements HasActivityInjector {

    @Inject
    public ViewModelProvider.Factory mFactory;

    @Inject
    DispatchingAndroidInjector<Activity> activityDispatchingAndroidInjector;

    private ActivityCreateDialogBinding activityCreateDialogBinding;
    private CreateDialogViewModel createDialogViewModel;

    private CompositeDisposable compositeDisposable = new CompositeDisposable();
    private FriendElementAdapter friendElementAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initDataBinding();

        initRecyclerViewAdapter();

        subscribePublishers();

        createDialogViewModel.onRefreshFriendsList();

    }

    private void subscribePublishers() {
        compositeDisposable.add(
                createDialogViewModel
                        .getPsRecievedFriendResponseList()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(friendList->{
                            friendElementAdapter.setFriendList(friendList);
                        })
        );

        compositeDisposable.add(
                createDialogViewModel
                        .getPsCreateDialogAction()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(dialogCaption -> {
                            List<String> pickedUsers = friendElementAdapter.getListOfSelectedProfiles();
                            if (pickedUsers.size() > 0) {
                                createDialogViewModel.sendCreateDialogRequest(pickedUsers, dialogCaption);
                            }
                        }, e->Log.e("AVX", "err", e))
        );

        compositeDisposable.add(
                createDialogViewModel
                        .getPsCreateDialogCompletedAction()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                .subscribe(dialogId-> {
                    startActivity(ConversationActivity.getIntent(this, dialogId));
                }, e->Log.e("AVX", "err", e))
        );

    }

    private void initDataBinding() {
        createDialogViewModel = ViewModelProviders.of(this, mFactory).get(CreateDialogViewModel.class);
        activityCreateDialogBinding = DataBindingUtil.setContentView(this, R.layout.activity_create_dialog);
        activityCreateDialogBinding.setLifecycleOwner(this);
        activityCreateDialogBinding.setCreateDialogViewModel(createDialogViewModel);

    }

    private void initRecyclerViewAdapter() {


        friendElementAdapter = new FriendElementAdapter(
                  createDialogViewModel.getPsProfileSelected()
                , createDialogViewModel.getPicasso()
                , null
                , null
                , activityCreateDialogBinding.listUsers
                , FriendElementAdapter.MODE_USER_PICK
        );
        activityCreateDialogBinding.listUsers.setLayoutManager(new LinearLayoutManager(this));
        activityCreateDialogBinding.listUsers.setAdapter(friendElementAdapter);
    }

    public static Intent getIntent(Context context) {
        return new Intent(context, CreateDialogActivity.class);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        compositeDisposable.dispose();
    }

    @Override
    public AndroidInjector<Activity> activityInjector() {
        return activityDispatchingAndroidInjector;
    }
}
