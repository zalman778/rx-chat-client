package com.hwx.rx_chat_client.view;

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

import com.hwx.rx_chat.common.entity.rx.RxMessage;
import com.hwx.rx_chat.common.object.rx.types.EventType;
import com.hwx.rx_chat.common.object.rx.types.ObjectType;
import com.hwx.rx_chat_client.R;
import com.hwx.rx_chat_client.RxChatApplication;
import com.hwx.rx_chat_client.adapter.ConversationElementAdapter;
import com.hwx.rx_chat_client.adapter.misc.ItemTouchHelperCallback;
import com.hwx.rx_chat_client.databinding.ActivityConversationBinding;
import com.hwx.rx_chat_client.util.ViewModelFactory;
import com.hwx.rx_chat_client.view.friend.ProfileActivity;
import com.hwx.rx_chat_client.viewModel.conversation.ConversationViewModel;
import com.hwx.rx_chat_client.viewModel.misc.DialogListAndIdDialogHolder;

import javax.inject.Inject;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class ConversationActivity extends AppCompatActivity {

    private static final String EXTRA_MESSAGES_LIST = "EXTRA_MESSAGES_LIST";
    private static final String EXTRA_DIALOG_ID = "EXTRA_DIALOG_ID";

    @Inject
    ViewModelFactory viewModelFactory;

    private ConversationViewModel conversationViewModel;
    private ActivityConversationBinding activityConversationBinding;

    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    private ConversationElementAdapter conversationElementAdapter;
    private LinearLayoutManager linearLayoutManager;
    private String currentUserName;
    private String currentUserId;
    private ItemTouchHelper mItemTouchHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ((RxChatApplication) getApplication()).getAppComponent().doInjectConversationActivity(this);
        initDataBinding();

        currentUserName = getApplicationContext().getSharedPreferences("localPref", 0).getString("username", "");
        currentUserId = getApplicationContext().getSharedPreferences("localPref", 0).getString("user_id", "");

        initRecyclerViewAdapter();
        subscribePublishers();
    }

    private void initRecyclerViewAdapter() {
        conversationElementAdapter = new ConversationElementAdapter(
                this
                , currentUserName
                , conversationViewModel.getResourceProvider()
                , activityConversationBinding
                , conversationViewModel.getPicasso()
                , conversationViewModel.getPsUserImageClicked()
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

                                        //right way, but rethink!
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
                                        .show())
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
                conversationViewModel.getPsProfileSelectedLoaded()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(userInfo->{
                            startActivity(ProfileActivity.fillDetail(getApplicationContext(), userInfo));
                            //adapter check for err...
                        }, e-> Log.e("AVX", "error on req", e))
        );
    }

    @Override
    protected void onStop() {
        super.onStop();
        compositeDisposable.clear();
    }

    private void initDataBinding() {
        conversationViewModel = ViewModelProviders.of(this, viewModelFactory).get(ConversationViewModel.class);

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
