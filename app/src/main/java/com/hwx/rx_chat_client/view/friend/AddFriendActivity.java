package com.hwx.rx_chat_client.view.friend;

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
import com.hwx.rx_chat_client.databinding.ActivityAddFriendBinding;
import com.hwx.rx_chat_client.viewModel.friend.AddFriendViewModel;

import javax.inject.Inject;

import dagger.android.AndroidInjector;
import dagger.android.DispatchingAndroidInjector;
import dagger.android.HasActivityInjector;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class AddFriendActivity extends AppCompatActivity implements HasActivityInjector {

    @Inject
    public ViewModelProvider.Factory mFactory;

    @Inject
    DispatchingAndroidInjector<Activity> activityDispatchingAndroidInjector;

    private ActivityAddFriendBinding activityAddFriendBinding;
    private AddFriendViewModel addFriendViewModel;

    private CompositeDisposable compositeDisposable = new CompositeDisposable();
    private FriendElementAdapter friendElementAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initDataBinding();

        initRecyclerViewAdapter();

        addFriendViewModel
            .getLvSeachUsername()

            .observe(this, username ->{
                if (username != null && username.length() > 3) {
                    addFriendViewModel.sendSearchUsersRequest(username);
                }
            });


        subscribePublishers();

    }

    private void subscribePublishers() {
        compositeDisposable.add(
                addFriendViewModel.getPsRecievedFriendResponseList()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(friendList->{
                            friendElementAdapter.setFriendList(friendList);
                        })
        );

        compositeDisposable.add(
                addFriendViewModel
                        .getPsProfileSelected()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(profileId->{
                            startActivity(ProfileActivity.fillDetail(getApplicationContext(), profileId));
                        }, e-> Log.e("AVX", "error on req", e))
        );
    }

    private void initDataBinding() {
        addFriendViewModel = ViewModelProviders.of(this, mFactory).get(AddFriendViewModel.class);
        activityAddFriendBinding = DataBindingUtil.setContentView(this, R.layout.activity_add_friend);
        activityAddFriendBinding.setLifecycleOwner(this);
        activityAddFriendBinding.setAddFriendViewModel(addFriendViewModel);

    }

    private void initRecyclerViewAdapter() {
        friendElementAdapter = new FriendElementAdapter(
                  addFriendViewModel.getPsProfileSelected()
                , addFriendViewModel.getPicasso()
                , null
                , null
                , activityAddFriendBinding.listUsers
                , FriendElementAdapter.MODE_SIMPLE
        );
        activityAddFriendBinding.listUsers.setLayoutManager(new LinearLayoutManager(this));
        activityAddFriendBinding.listUsers.setAdapter(friendElementAdapter);
    }

    public static Intent getIntent(Context context) {
        return new Intent(context, AddFriendActivity.class);
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
