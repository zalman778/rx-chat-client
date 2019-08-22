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
import android.os.IBinder;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;

import com.hwx.rx_chat_client.R;
import com.hwx.rx_chat_client.adapter.ConversationElementAdapter;
import com.hwx.rx_chat_client.adapter.misc.ItemTouchHelperCallback;
import com.hwx.rx_chat_client.background.p2p.object.type.ObjectType;
import com.hwx.rx_chat_client.background.p2p.service.RxP2PService;
import com.hwx.rx_chat_client.background.service.RxService;
import com.hwx.rx_chat_client.databinding.ActivityP2pConversationBinding;
import com.hwx.rx_chat_client.view.friend.ProfileActivity;
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

    private RxService rxService;
    private boolean isRxServiceBounded;

    private P2pConversationViewModel p2pConversationViewModel;
    private ActivityP2pConversationBinding activityP2pConversationBinding;
    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    private ConversationElementAdapter conversationElementAdapter;
    private LinearLayoutManager linearLayoutManager;
    private String currentUserName;
    private String currentUserId;
    private ItemTouchHelper mItemTouchHelper;

    private String remoteProfileId;


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
            onP2pServiceConnected();
        }

        public void onServiceDisconnected(ComponentName arg0) {
            isRxP2PServiceBounded = false;
        }
    };

    private ServiceConnection rxServiceConnection = new ServiceConnection() {
        public void onServiceConnected(
                  ComponentName className
                , IBinder service
        ) {
            rxService = ((RxService.RxServiceBinder) service).getService();
            isRxServiceBounded = true;
            onRxServiceConnected();
        }

        public void onServiceDisconnected(ComponentName arg0) {
            isRxServiceBounded = false;
        }
    };

    private void onRxServiceConnected() {
        p2pConversationViewModel.setRxService(rxService);
    }

    private void onP2pServiceConnected() {
        p2pConversationViewModel.setRemoteProfileId(remoteProfileId);
        p2pConversationViewModel.setRxP2PService(rxP2PService);

        subscribeP2pServicePublishers();
    }

    private void subscribeP2pServicePublishers() {
        //удаление сообщения - ответ на запрос удаления от собеседника
        compositeDisposable.add(
                rxP2PService
                        .getPsRemoveMessageAction()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(messageId->{
                            Log.w("AVX", "caught message remove action with id = "+messageId);
                            conversationElementAdapter.performConfirmedMessageDeletion(messageId);
                        }, e-> Log.e("AVX", "error on req", e))
        );

        //редактирование сообщений
        compositeDisposable.add(
            rxP2PService
                .getPsEditMessageAction()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(rxMessage->{

                    String messageId = rxMessage.getId();

                    Log.w("AVX", "caught message edition with id = "+messageId +"; and new text = "+rxMessage.getValue());


                    Integer messagePosition = conversationElementAdapter.getMessagePositionByMessageId(messageId);
                    if (messagePosition != null) {
                        conversationElementAdapter.getMessagesList().set(messagePosition, rxMessage);
                        conversationElementAdapter.notifyItemChanged(messagePosition);

                        conversationElementAdapter.performRollbackMessageViewSwipe(messageId); //todo: costyl?
                    }
                }, e-> Log.e("AVX", "error on req", e))
        );



    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //recieving extra data
        remoteProfileId = getIntent().getStringExtra(EXTRA_REMOTE_PROFILE_ID);




        Intent rxServiceIntent = new Intent(this, RxP2PService.class);
        bindService(rxServiceIntent, rxP2PServiceConnection, 0);

        Intent rxP2pServiceIntent = new Intent(this, RxService.class);
        bindService(rxP2pServiceIntent, rxServiceConnection, 0);

        initDataBinding();

        SharedPreferences preferences = getApplicationContext().getSharedPreferences("localPref", 0);
        currentUserName = preferences.getString("username", "");
        currentUserId = preferences.getString("user_id", "");

        initRecyclerViewAdapter();
        subscribePublishers();

    }

    @Override
    protected void onStop() {
        super.onStop();

//        if (isRxP2PServiceBounded)
//            unbindService(rxP2PServiceConnection);

        compositeDisposable.clear();
    }

    private void initDataBinding() {
        p2pConversationViewModel = ViewModelProviders.of(this, mFactory).get(P2pConversationViewModel.class);
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

        conversationElementAdapter.setDeletingAnyMessage(true);

        linearLayoutManager = new LinearLayoutManager(this);
        activityP2pConversationBinding.listMessages.setLayoutManager(linearLayoutManager);
        activityP2pConversationBinding.listMessages.setAdapter(conversationElementAdapter);

        ItemTouchHelper.Callback callback = new ItemTouchHelperCallback(conversationElementAdapter);
        mItemTouchHelper = new ItemTouchHelper(callback);
        mItemTouchHelper.attachToRecyclerView(activityP2pConversationBinding.listMessages);
    }

    private void subscribePublishers() {

        //получение названия диалога из vm
        compositeDisposable.add(
                p2pConversationViewModel
                        .getPsDialogCaptionRefreshAction()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(this::setTitle, err-> Log.e("AVX", "error on sub:", err))
        );

        //событие получения сообщения от другого пользователя
        compositeDisposable.add(
            p2pConversationViewModel
                .getPsRxMessageRecievedAction()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(rxObject -> {
                    Log.w("AVX", "p2pConvAct: got rxObj = "+rxObject);
                    if (rxObject.getObjectType().equals(ObjectType.MESSAGE)) {
                        Log.w("AVX", "p2pActConv: got sent rxObj");
                        //подставляем свой url
//                        String imageUrl = Configuration.HTTPS_SERVER_URL+Configuration.IMAGE_PREFIX+rxObject.getMessage().getImageUrl();
//                        rxObject.getMessage().setImageUrl(imageUrl);

                        conversationElementAdapter.getMessagesList().add(rxObject.getMessage());
                        conversationElementAdapter.notifyItemInserted(conversationElementAdapter.getMessagesList().size()-1); //-1 del
                        linearLayoutManager.scrollToPosition(conversationElementAdapter.getMessagesList().size()-1);
                        Log.w("AVX", "p2pConvAct: "+ "added one message:");
                    }
                }, err-> Log.e("AVX", "error on sub:", err))
        );

        //событие получения списка сообщений из локальной базы
        compositeDisposable.add(
                p2pConversationViewModel
                        .getPsRxMessagesListLoadedFromLocalDbAction()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(rxMsgList->{
                            conversationElementAdapter.getMessagesList().addAll(rxMsgList);
                            conversationElementAdapter.notifyDataSetChanged();

                            Log.w("AVX", "p2pConvAct: added to adapter from "
                                    +(conversationElementAdapter.getMessagesList().size() - rxMsgList.size())
                                    +  " with size = " + rxMsgList.size() );
                            linearLayoutManager.scrollToPosition(conversationElementAdapter.getMessagesList().size()-1);
                        }, err-> Log.e("AVX", "error on sub:", err))
        );

        //событие нажатия на фото пользователя в чате
        compositeDisposable.add(
                p2pConversationViewModel
                        .getPsProfileSelectedAction()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(profileId->{
                            startActivity(ProfileActivity.fillDetail(getApplicationContext(), profileId));
                        }, e-> Log.e("AVX", "error on req", e))
        );

        //подписываемся на отмену удаления сообщений
        compositeDisposable.add(
                p2pConversationViewModel
                        .getPsPerformRollbackMessageSwipe()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                msgId->conversationElementAdapter.performRollbackMessageViewSwipe(msgId),
                                err->Log.e("AVX", "err", err))
        );

        //отслеживаем запросы на удаление сообщений
        compositeDisposable.add(
                conversationElementAdapter
                        .getPsMessageDeleteRequest()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(msgId->
                                        new AlertDialog.Builder(this)
                                                .setTitle("Message deleting")
                                                .setMessage("Do you really want to delete this message? \'"+conversationElementAdapter.getMessageById(msgId).getValue()+"\"")
                                                .setIcon(android.R.drawable.ic_dialog_alert)
                                                .setPositiveButton(android.R.string.yes,
                                                        (dialog, whichButton) -> {
                                                            p2pConversationViewModel.sendRxEventMessageDelete(msgId);
//                                                            conversationElementAdapter.performConfirmedMessageDeletion(msgId);
                                                        })
                                                .setNegativeButton(android.R.string.no,
                                                        (dialog, whichButton) ->
                                                                conversationElementAdapter.performRollbackMessageViewSwipe(msgId)
                                                )
                                                .show()
                                , e-> Log.e("AVX", "error on req", e))
        );

        //отслеживаем запросы на редактирование сообщений
        compositeDisposable.add(
                conversationElementAdapter
                        .getPsMessageEditRequest()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(rxMsg -> {
                            p2pConversationViewModel.openMessageEditBox(rxMsg);
                        }, e-> Log.e("AVX", "error on req", e))
        );

    }

    public static Intent getIntent(Context context, String remoteProfileId) {
        Intent intent = new Intent(context, P2pConversationActivity.class);
        intent.putExtra(EXTRA_REMOTE_PROFILE_ID, remoteProfileId);
        return intent;
    }

}
