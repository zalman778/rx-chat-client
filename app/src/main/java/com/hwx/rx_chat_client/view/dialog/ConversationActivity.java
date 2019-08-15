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
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.hwx.rx_chat.common.entity.rx.RxMessage;
import com.hwx.rx_chat.common.object.rx.types.EventType;
import com.hwx.rx_chat.common.object.rx.types.ObjectType;
import com.hwx.rx_chat_client.R;
import com.hwx.rx_chat_client.adapter.ConversationElementAdapter;
import com.hwx.rx_chat_client.adapter.misc.ItemTouchHelperCallback;
import com.hwx.rx_chat_client.background.service.RxService;
import com.hwx.rx_chat_client.databinding.ActivityConversationBinding;
import com.hwx.rx_chat_client.view.friend.ProfileActivity;
import com.hwx.rx_chat_client.viewModel.conversation.ConversationViewModel;

import javax.inject.Inject;

import dagger.android.AndroidInjector;
import dagger.android.DispatchingAndroidInjector;
import dagger.android.HasActivityInjector;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class ConversationActivity extends AppCompatActivity implements HasActivityInjector {

    @Inject
    public ViewModelProvider.Factory mFactory;

    @Inject
    DispatchingAndroidInjector<Activity> activityDispatchingAndroidInjector;

    private static final String EXTRA_DIALOG_ID = "EXTRA_DIALOG_ID";

    private ConversationViewModel conversationViewModel;
    private ActivityConversationBinding activityConversationBinding;

    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    private ConversationElementAdapter conversationElementAdapter;
    private LinearLayoutManager linearLayoutManager;
    private String currentUserName;
    private String currentUserId;
    private ItemTouchHelper mItemTouchHelper;

    private RxService rxService;
    private boolean isRxServiceBounded;


    @Override
    public AndroidInjector<Activity> activityInjector() {
        return activityDispatchingAndroidInjector;
    }

    private ServiceConnection rxServiceConnection = new ServiceConnection() {
        public void onServiceConnected(
                ComponentName className
                , IBinder service
        ) {
            rxService = ((RxService.RxServiceBinder) service).getService();
            isRxServiceBounded = true;
        }

        public void onServiceDisconnected(ComponentName arg0) {
            isRxServiceBounded = false;
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent rxServiceIntent = new Intent(this, RxService.class);
        bindService(rxServiceIntent, rxServiceConnection, 0);

        initDataBinding();


        //delayed with 1000ms, due to need some time to bind service
        Handler handler = new Handler();
        handler.postDelayed(() -> {
            conversationViewModel.setRxService(rxService);

        }, 1000);




        SharedPreferences preferences = getApplicationContext().getSharedPreferences("localPref", 0);
        currentUserName = preferences.getString("username", "");
        currentUserId = preferences.getString("user_id", "");

        initRecyclerViewAdapter();
        subscribePublishers();
    }

    

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_dialog_profile_btn_options, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.btnDialogOptions:
                conversationViewModel.onClickDialogOptions();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    private void initRecyclerViewAdapter() {
        conversationElementAdapter = new ConversationElementAdapter(
                  currentUserName
                , conversationViewModel.getResourceProvider()
                , activityConversationBinding.listMessages
                , conversationViewModel.getPicasso()
                , conversationViewModel.getPsProfileSelectedAction()
        );

        linearLayoutManager = new LinearLayoutManager(this);
        activityConversationBinding.listMessages.setLayoutManager(linearLayoutManager);
        activityConversationBinding.listMessages.setAdapter(conversationElementAdapter);

        ItemTouchHelper.Callback callback = new ItemTouchHelperCallback(conversationElementAdapter);
        mItemTouchHelper = new ItemTouchHelper(callback);
        mItemTouchHelper.attachToRecyclerView(activityConversationBinding.listMessages);
    }

    private void subscribePublishers() {
        //подписываемся на ивенты
        compositeDisposable.add(
                conversationViewModel
                        .getPsRxMessage()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(rxObject -> {

                                    //удаление сообщений
                                    if (rxObject.getObjectType().equals(ObjectType.EVENT)
                                            && rxObject.getEventType().equals(EventType.MESSAGE_DELETED)
                                    ) {
                                        String messageId = rxObject.getObjectId();
                                        Integer messagePosition = conversationElementAdapter.getMessagePositionByMessageId(messageId);
//                        Log.i("AVX", "got message del event:"+messageId+"; "+(messagePosition != null ? messagePosition.toString() : "null"));
                                        if (messagePosition != null) {
                                            conversationElementAdapter.getMessagesList().remove((int) messagePosition);
                                            conversationElementAdapter.notifyItemRemoved(messagePosition);
                                        }
                                    }

                                    //редактирование
                                    if (rxObject.getObjectType().equals(ObjectType.EVENT)
                                            && rxObject.getEventType().equals(EventType.MESSAGE_EDIT)
                                    ) {
                                        String messageId = rxObject.getObjectId();
                                        Integer messagePosition = conversationElementAdapter.getMessagePositionByMessageId(messageId);
                                        if (messagePosition != null) {
                                            RxMessage rxMessage = conversationElementAdapter.getMessagesList().get(messagePosition);
                                            rxMessage.setValue((String)rxObject.getValue());
                                            rxMessage.setEdited(true);

                                            conversationElementAdapter.notifyItemChanged(messagePosition);
                                        }
                                    }

                                    //новое сообщений
                                    if (rxObject.getObjectType().equals(ObjectType.EVENT)
                                            && rxObject.getEventType().equals(EventType.MESSAGE_NEW_FROM_SERVER)
                                            && !conversationViewModel.getUniqueMessagesIdSet().contains(rxObject.getMessage().getId())
                                    ) {
                                        Log.i("AVX","got rx event with mesage:"+rxObject.getMessage().getId()+" "+rxObject.getMessage().getValue());
                                        conversationViewModel.getUniqueMessagesIdSet().add(rxObject.getMessage().getId());

                                        //right way, but not working
//                        conversationElementAdapter.getMessagesList().add(rxObject.getMessage());
//                        conversationElementAdapter.notifyItemInserted(conversationElementAdapter.getMessagesList().size());
//                        linearLayoutManager.scrollToPosition(conversationElementAdapter.getMessagesList().size()-1);

                                        //not the best way, but whatever...
                                        conversationElementAdapter.getMessagesList().add(rxObject.getMessage());
                                        //sorting
                                        conversationElementAdapter.getMessagesList().sort((a, b) -> a.getDateSent().compareTo(b.getDateSent()));
                                        conversationElementAdapter.notifyDataSetChanged();
                                        linearLayoutManager.scrollToPosition(conversationElementAdapter.getMessagesList().size()-1);

                                    }
                                }, err-> Log.e("AVX", "error on sub:", err)
                        ));

        //подписывамся на получение списка сообщений со статик бд
        compositeDisposable.add(
                conversationViewModel
                        .getPsRxMessagesList()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(rxMessages -> {
                            conversationElementAdapter.getMessagesList().addAll(rxMessages);

                            //unique msg map
                            conversationElementAdapter.getMessagesList().removeIf(e->!conversationViewModel.getUniqueMessagesIdSet().add(e.getId()));

                            //sorting
                            conversationElementAdapter.getMessagesList().sort((a, b) -> a.getDateSent().compareTo(b.getDateSent()));
                            conversationElementAdapter.notifyDataSetChanged();
                            linearLayoutManager.scrollToPosition(conversationElementAdapter.getMessagesList().size()-1);

                        })
        );

        //подписываемся на отмену удаления сообщений
        compositeDisposable.add(
                conversationViewModel
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
                                                    conversationViewModel.sendRxEventMessageDelete(msgId);
                                                    conversationElementAdapter.performConfirmedMessageDeletion(msgId);
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
                            conversationViewModel.openMessageEditBox(rxMsg);
                        })
        );



        //событие нажатия на фото пользователя в чате
        compositeDisposable.add(
                conversationViewModel
                        .getPsProfileSelectedAction()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(profileId->{
                            startActivity(ProfileActivity.fillDetail(getApplicationContext(), profileId));
                        }, e-> Log.e("AVX", "error on req", e))
        );

        //событие нажатия открытия страницы информации о диалоге
        compositeDisposable.add(
            conversationViewModel
                    .getPsDialogInfoAction()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                .subscribe(dialogId->{
                    startActivity(DialogProfileActivity.getIntent(this, dialogId));
                }, e-> Log.e("AVX", "error ", e))
        );

    }

    @Override
    protected void onStop() {
        super.onStop();

        if (isRxServiceBounded)
            unbindService(rxServiceConnection);

        compositeDisposable.clear();
    }

    private void initDataBinding() {
        conversationViewModel = ViewModelProviders.of(this, mFactory).get(ConversationViewModel.class);

        //recieving extra data
        String idDialog = getIntent().getStringExtra(EXTRA_DIALOG_ID);

        conversationViewModel.setIdDialog(idDialog);

        activityConversationBinding = DataBindingUtil.setContentView(this, R.layout.activity_conversation);
        activityConversationBinding.setLifecycleOwner(this);
        activityConversationBinding.setConversationViewModel(conversationViewModel);
    }

    public static Intent getIntent(Context context, String dialogId) {
        Intent intent = new Intent(context, ConversationActivity.class);
        intent.putExtra(EXTRA_DIALOG_ID, dialogId);
        return intent;
    }


}
