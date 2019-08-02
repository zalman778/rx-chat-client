package com.hwx.rx_chat_client.view.dialog;

import android.app.Activity;
import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;

import com.hwx.rx_chat_client.R;
import com.hwx.rx_chat_client.adapter.FriendElementAdapter;
import com.hwx.rx_chat_client.adapter.misc.ItemTouchHelperCallback;
import com.hwx.rx_chat_client.databinding.ActivityDialogProfileBinding;
import com.hwx.rx_chat_client.view.friend.ProfileActivity;
import com.hwx.rx_chat_client.viewModel.conversation.DialogProfileViewModel;

import javax.inject.Inject;

import dagger.android.AndroidInjector;
import dagger.android.DispatchingAndroidInjector;
import dagger.android.HasActivityInjector;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;

public class DialogProfileActivity extends AppCompatActivity implements HasActivityInjector {

    @Inject
    public ViewModelProvider.Factory mFactory;

    @Inject
    DispatchingAndroidInjector<Activity> activityDispatchingAndroidInjector;

    private static final String EXTRA_DIALOG_ID = "EXTRA_DIALOG_ID";

    private ActivityDialogProfileBinding activityDialogProfileBinding;
    private DialogProfileViewModel dialogProfileViewModel;

    private CompositeDisposable compositeDisposable = new CompositeDisposable();
    private FriendElementAdapter friendElementAdapter;
    private ItemTouchHelper mItemTouchHelper;

    private StringBuilder currentUserId = new StringBuilder();
    private String dialogId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initDataBinding();

        initRecyclerViewAdapter();

        subscribePublishers();

        dialogId = getIntent().getStringExtra(EXTRA_DIALOG_ID);
        dialogProfileViewModel.setDialogId(dialogId);

        currentUserId.append(getApplicationContext().getSharedPreferences("localPref", 0).getString("user_id", ""));
    }


    private void initDataBinding() {
        dialogProfileViewModel = ViewModelProviders.of(this, mFactory).get(DialogProfileViewModel.class);
        activityDialogProfileBinding = DataBindingUtil.setContentView(this, R.layout.activity_dialog_profile);
        activityDialogProfileBinding.setLifecycleOwner(this);
        activityDialogProfileBinding.setDialogProfileViewModel(dialogProfileViewModel);
    }

    private void initRecyclerViewAdapter() {
        friendElementAdapter = new FriendElementAdapter(
                  dialogProfileViewModel.getPsProfileSelected()
                , dialogProfileViewModel.getPicasso()
                , dialogProfileViewModel.getCreatorId()
                , currentUserId
                , activityDialogProfileBinding.listMembers
                , FriendElementAdapter.MODE_DIALOG_USERS
        );
        activityDialogProfileBinding.listMembers.setLayoutManager(new LinearLayoutManager(this));
        activityDialogProfileBinding.listMembers.setAdapter(friendElementAdapter);

        ItemTouchHelper.Callback callback = new ItemTouchHelperCallback(friendElementAdapter);
        mItemTouchHelper = new ItemTouchHelper(callback);
        mItemTouchHelper.attachToRecyclerView(activityDialogProfileBinding.listMembers);
    }

    private void subscribePublishers() {
        compositeDisposable.add(
                dialogProfileViewModel
                        .getPsProfileSelected()
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(profileId-> startActivity(ProfileActivity.fillDetail(getApplicationContext()
                                , profileId)), e-> Log.e("AVX", "error on req", e))
        );

        compositeDisposable.add(
                dialogProfileViewModel
                        .getPsDialogMembersLoadedAction()
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(membersList-> friendElementAdapter.setFriendList(membersList)
                                , e-> Log.e("AVX", "error on req", e))
        );

        compositeDisposable.add(
                friendElementAdapter
                    .getPsItemSwipeRightAction()
                    .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(adapterPosition->{
                            new AlertDialog.Builder(this)
                                    .setTitle("Dialog member deleting")
                                    .setMessage("Do you really want to delete  \""+
                                            friendElementAdapter.getFriendReponseByAdapterPosition(adapterPosition).getUsername()+"\" from this dialog ?")
                                    .setIcon(android.R.drawable.ic_dialog_alert)
                                    .setPositiveButton(android.R.string.yes,
                                            (dialog, whichButton) -> {
                                                dialogProfileViewModel.sendDialogMemberDeletion(
                                                          friendElementAdapter.getFriendReponseByAdapterPosition(adapterPosition).getUserId()
                                                        , dialogId
                                                );
                                            })
                                    .setNegativeButton(android.R.string.no,
                                            (dialog, whichButton) ->
                                                friendElementAdapter.performRollbackSwipeRight(adapterPosition)
                                    )
                                    .show();

                        }, e-> Log.e("AVX", "error on req", e))
        );
    }

    public static Intent getIntent(Context context, String dialogId) {
        Intent intent = new Intent(context, DialogProfileActivity.class);
        intent.putExtra(EXTRA_DIALOG_ID, dialogId);
        return intent;
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
