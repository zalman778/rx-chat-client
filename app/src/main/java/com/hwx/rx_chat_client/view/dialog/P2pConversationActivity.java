package com.hwx.rx_chat_client.view.dialog;

import android.app.Activity;
import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;

import com.hwx.rx_chat_client.Configuration;
import com.hwx.rx_chat_client.R;
import com.hwx.rx_chat_client.adapter.ConversationElementAdapter;
import com.hwx.rx_chat_client.adapter.misc.ItemTouchHelperCallback;
import com.hwx.rx_chat_client.background.p2p.object.type.ObjectType;
import com.hwx.rx_chat_client.background.p2p.service.RxP2PService;
import com.hwx.rx_chat_client.databinding.ActivityP2pConversationBinding;
import com.hwx.rx_chat_client.viewModel.conversation.P2pConversationViewModel;

import javax.inject.Inject;

import dagger.android.AndroidInjector;
import dagger.android.DispatchingAndroidInjector;
import dagger.android.HasActivityInjector;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class P2pConversationActivity extends AppCompatActivity implements HasActivityInjector {

    private static final String EXTRA_REMOTE_PROFILE_ID = "EXTRA_REMOTE_PROFILE_ID";

    @Inject
    public ViewModelProvider.Factory mFactory;

    @Inject
    DispatchingAndroidInjector<Activity> activityDispatchingAndroidInjector;

    private RxP2PService rxP2PService;
    private boolean isRxP2PServiceBounded;

    private P2pConversationViewModel p2pConversationViewModel;
    private ActivityP2pConversationBinding activityP2pConversationBinding;
    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    private ConversationElementAdapter conversationElementAdapter;
    private LinearLayoutManager linearLayoutManager;
    private String currentUserName;
    private String currentUserId;
    private ItemTouchHelper mItemTouchHelper;


    @Override
    public AndroidInjector<Activity> activityInjector() {
        return activityDispatchingAndroidInjector;
    }

    private ServiceConnection rxP2PServiceConnection = new ServiceConnection() {
        public void onServiceConnected(
                ComponentName className
                , IBinder service
        ) {
            rxP2PService = ((RxP2PService.RxP2PServiceBinder) service).getService();
            isRxP2PServiceBounded = true;
        }

        public void onServiceDisconnected(ComponentName arg0) {
            isRxP2PServiceBounded = false;
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent rxServiceIntent = new Intent(this, RxP2PService.class);
        bindService(rxServiceIntent, rxP2PServiceConnection, 0);

        initDataBinding();


        //delayed with 1000ms, due to need some time to bind service
        Handler handler = new Handler();
        handler.postDelayed(() -> {
            p2pConversationViewModel.setRxP2PService(rxP2PService);

        }, 1000);

        SharedPreferences preferences = getApplicationContext().getSharedPreferences("localPref", 0);
        currentUserName = preferences.getString("username", "");
        currentUserId = preferences.getString("user_id", "");

        initRecyclerViewAdapter();
        subscribePublishers();
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (isRxP2PServiceBounded)
            unbindService(rxP2PServiceConnection);

        compositeDisposable.clear();
    }

    private void initDataBinding() {
        p2pConversationViewModel = ViewModelProviders.of(this, mFactory).get(P2pConversationViewModel.class);

        //recieving extra data
        String remoteProfileId = getIntent().getStringExtra(EXTRA_REMOTE_PROFILE_ID);

        p2pConversationViewModel.setRemoteProfileId(remoteProfileId);

        activityP2pConversationBinding = DataBindingUtil.setContentView(this, R.layout.activity_p2p_conversation);
        activityP2pConversationBinding.setLifecycleOwner(this);
        activityP2pConversationBinding.setP2pConversationViewModel(p2pConversationViewModel);
    }

    private void initRecyclerViewAdapter() {
        conversationElementAdapter = new ConversationElementAdapter(
                currentUserName
                , p2pConversationViewModel.getResourceProvider()
                , activityP2pConversationBinding.listMessages
                , p2pConversationViewModel.getPicasso()
                , p2pConversationViewModel.getPsProfileSelectedAction()
        );

        linearLayoutManager = new LinearLayoutManager(this);
        activityP2pConversationBinding.listMessages.setLayoutManager(linearLayoutManager);
        activityP2pConversationBinding.listMessages.setAdapter(conversationElementAdapter);

        ItemTouchHelper.Callback callback = new ItemTouchHelperCallback(conversationElementAdapter);
        mItemTouchHelper = new ItemTouchHelper(callback);
        mItemTouchHelper.attachToRecyclerView(activityP2pConversationBinding.listMessages);
    }

    private void subscribePublishers() {
        compositeDisposable.add(
            p2pConversationViewModel
                .getPsRxMessageRecievedAction()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(rxObject -> {
                    if (rxObject.getObjectType().equals(ObjectType.MESSAGE)) {
                        //подставляем свой url
                        rxObject.getMessage().setImageUrl(Configuration.HTTPS_SERVER_URL+rxObject.getMessage().getImageUrl());

                        conversationElementAdapter.getMessagesList().add(rxObject.getMessage());

                        conversationElementAdapter.notifyItemInserted(conversationElementAdapter.getMessagesList().size()-1);
                        linearLayoutManager.scrollToPosition(conversationElementAdapter.getMessagesList().size()-1);
                    }
                }, err-> Log.e("AVX", "error on sub:", err))
        );
    }

    public static Intent getIntent(Context context, String remoteProfileId) {
        Intent intent = new Intent(context, P2pConversationActivity.class);
        intent.putExtra(EXTRA_REMOTE_PROFILE_ID, remoteProfileId);
        return intent;
    }

}
