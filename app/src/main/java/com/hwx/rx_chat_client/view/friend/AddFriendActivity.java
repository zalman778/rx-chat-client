package com.hwx.rx_chat_client.view.friend;

import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;

import com.hwx.rx_chat_client.R;
import com.hwx.rx_chat_client.RxChatApplication;
import com.hwx.rx_chat_client.adapter.FriendElementAdapter;
import com.hwx.rx_chat_client.databinding.ActivityAddFriendBinding;
import com.hwx.rx_chat_client.util.ViewModelFactory;
import com.hwx.rx_chat_client.viewModel.friend.AddFriendViewModel;
import com.hwx.rx_chat_client.viewModel.misc.DialogListAndIdDialogHolder;

import javax.inject.Inject;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class AddFriendActivity extends AppCompatActivity {


    @Inject
    ViewModelFactory viewModelFactory;

    private ActivityAddFriendBinding activityAddFriendBinding;
    private AddFriendViewModel addFriendViewModel;

    private CompositeDisposable compositeDisposable = new CompositeDisposable();
    private FriendElementAdapter friendElementAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ((RxChatApplication) getApplication()).getAppComponent().doInjectAddFriendActivity(this);

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
                        .getPsProfileSelectedLoaded()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(userInfo->{
                            startActivity(ProfileActivity.fillDetail(getApplicationContext(), userInfo));
                        }, e-> Log.e("AVX", "error on req", e))
        );
    }

    private void initDataBinding() {
        addFriendViewModel = ViewModelProviders.of(this, viewModelFactory).get(AddFriendViewModel.class);
        activityAddFriendBinding = DataBindingUtil.setContentView(this, R.layout.activity_add_friend);
        activityAddFriendBinding.setLifecycleOwner(this);
        activityAddFriendBinding.setAddFriendViewModel(addFriendViewModel);

    }

    private void initRecyclerViewAdapter() {
        //TODO - change this to another logic!
        MutableLiveData<DialogListAndIdDialogHolder> lvFriendPicked =new MutableLiveData<>();

        friendElementAdapter = new FriendElementAdapter(
                //TODO - change lvDialog picked!!
                addFriendViewModel.getPsProfileSelected(), this, addFriendViewModel.getHeadersMap()
                , addFriendViewModel.getChatRepository(), addFriendViewModel.getPicasso(), activityAddFriendBinding.listUsers
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

}
