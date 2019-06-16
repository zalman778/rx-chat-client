package com.hwx.rx_chat_client.adapter;

import android.arch.lifecycle.LifecycleOwner;
import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.hwx.rx_chat.common.entity.rx.RxMessage;
import com.hwx.rx_chat_client.R;
import com.hwx.rx_chat_client.adapter.misc.ItemTouchHelperAdapter;
import com.hwx.rx_chat_client.databinding.ActivityConversationBinding;
import com.hwx.rx_chat_client.databinding.ConvItemMessageOtherBinding;
import com.hwx.rx_chat_client.databinding.ConvItemMessageSelfBinding;
import com.hwx.rx_chat_client.util.ResourceProvider;
import com.hwx.rx_chat_client.viewModel.conversation.MessageViewModel;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.subjects.PublishSubject;

public class ConversationElementAdapter
        extends RecyclerView.Adapter<ConversationElementAdapter.ConversationElementViewHolder>
        implements ItemTouchHelperAdapter {

    public static final int VIEW_TYPE_SELF_MESSAGE = 0;
    public static final int VIEW_TYPE_OTHER_MESSAGE = 1;
    private ResourceProvider resourceProvider;
    private Picasso picasso;


    private List<RxMessage> messagesList = new ArrayList<>();
    private LifecycleOwner lifecycleOwner;


    private Map<String, MessageViewModel> viewModelsMap = new HashMap<>();

    private String currentUsername;

    private PublishSubject<String> psMessageDeleteRequest = PublishSubject.create();
    private PublishSubject<RxMessage> psMessageEditRequest = PublishSubject.create();
    private PublishSubject<String> psUserImageClicked;

    private ActivityConversationBinding activityConversationBinding;

    public ConversationElementAdapter(
              LifecycleOwner lifecycleOwner
            , String currentUserName
            , ResourceProvider resourceProvider
            , ActivityConversationBinding activityConversationBinding
            , Picasso picasso
            , PublishSubject<String> psUserImageClicked
    ) {
        this.lifecycleOwner = lifecycleOwner;
        this.currentUsername = currentUserName;
        this.resourceProvider = resourceProvider;
        this.activityConversationBinding = activityConversationBinding;
        this.picasso = picasso;
        this.psUserImageClicked = psUserImageClicked;
    }

    public PublishSubject<RxMessage> getPsMessageEditRequest() {
        return psMessageEditRequest;
    }

    public void setPsMessageEditRequest(PublishSubject<RxMessage> psMessageEditRequest) {
        this.psMessageEditRequest = psMessageEditRequest;
    }

    public List<RxMessage> getMessagesList() {
        return messagesList;
    }

    public PublishSubject<String> getPsMessageDeleteRequest() {
        return psMessageDeleteRequest;
    }

    public Map<String, MessageViewModel> getViewModelsMap() {
        return viewModelsMap;
    }

    public RxMessage getMessageById(String messageId) {
        for (RxMessage rxMessage : getMessagesList())
        {
            if (rxMessage.getId().equals(messageId))
                return rxMessage;
        }
        return null;
    }

    public Integer getMessagePositionByMessageId(String messageId) {
        for (int i = 0; i < messagesList.size(); i++) {
            if (messagesList.get(i).getId().equals(messageId))
                return i;
        }
        return null;
    }


    @NonNull
    @Override
    public ConversationElementViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int itemViewType) {
        ViewDataBinding viewDataBinding = DataBindingUtil.inflate(
                  LayoutInflater.from(viewGroup.getContext())
                , itemViewType == ConversationElementAdapter.VIEW_TYPE_OTHER_MESSAGE ? R.layout.conv_item_message_other : R.layout.conv_item_message_self
                , viewGroup
                , false
        );
        return new ConversationElementViewHolder(viewDataBinding);
    }

    @Override
    public int getItemViewType(int position) {
        //return super.getItemViewType(position);
        return messagesList.get(position).getUserFromName().equals(currentUsername) ?
                ConversationElementAdapter.VIEW_TYPE_SELF_MESSAGE : ConversationElementAdapter.VIEW_TYPE_OTHER_MESSAGE;
    }

    @Override
    public void onBindViewHolder(@NonNull ConversationElementViewHolder conversationElementViewHolder, int i) {
        String messageId = messagesList.get(i).getId();
        if (viewModelsMap.get(messageId) == null) {
            MessageViewModel messageViewModel = new MessageViewModel(resourceProvider, picasso, psUserImageClicked);
            viewModelsMap.put(messageId, messageViewModel);
        }
        conversationElementViewHolder.bindDialogElement(messagesList.get(i), viewModelsMap.get(messageId));
    }
    @Override
    public int getItemCount() {
        return messagesList.size();
    }

    @Override
    public boolean onItemMove(int fromPosition, int toPosition) {
        return false;

    }

    @Override
    public void onItemSwipeLeft(int position) {
        psMessageDeleteRequest.onNext(getMessagesList().get(position).getId());
    }

    public void performConfirmedMessageDeletion(String messageId) {
        if (getMessagePositionByMessageId(messageId) != null) {
            int msgPos = getMessagePositionByMessageId(messageId);
            viewModelsMap.remove(getMessagesList().get(msgPos).getId());
            getMessagesList().remove(msgPos);
            notifyItemRemoved(msgPos);
        }
    }

    public void performRollbackMessageViewSwipe(String messageId) {

        if (getMessagePositionByMessageId(messageId) != null) {
            int msgPos = getMessagePositionByMessageId(messageId);
            RecyclerView.ViewHolder viewHolder = activityConversationBinding.listMessages.findViewHolderForAdapterPosition(msgPos);

            viewHolder.itemView.setTranslationX(0);
            viewHolder.itemView.setAlpha(1f);
            //notifyItemChanged(msgPos);
        }
    }

    @Override
    public void onItemSwipping(int adapterPosition, Boolean direction) {
        //в этом методе только меняем фон... TODO... нельзя тут вызывать notifyItemChanged

        RecyclerView.ViewHolder viewHolder = activityConversationBinding.listMessages.findViewHolderForAdapterPosition(adapterPosition);
        if (direction)
            viewHolder.itemView.setBackgroundResource(R.color.green);
        else
            viewHolder.itemView.setBackgroundResource(R.color.red);

    }

    @Override
    public void onItemSwapRight(int adapterPosition) {
        psMessageEditRequest.onNext(getMessagesList().get(adapterPosition));
    }

    @Override
    public boolean checkHasRightToSwipe(int adapterPosition) {
        return currentUsername.equals(getMessagesList().get(adapterPosition).getUserFromName());
    }

    public static class ConversationElementViewHolder extends RecyclerView.ViewHolder /*implements View.OnCreateContextMenuListener */{

        ViewDataBinding viewDataBinding;

        public ConversationElementViewHolder(@NonNull ViewDataBinding viewDataBinding) {
            super(viewDataBinding instanceof ConvItemMessageOtherBinding
                    ? ((ConvItemMessageOtherBinding)viewDataBinding).convItemObject
                    : ((ConvItemMessageSelfBinding)viewDataBinding).convItemObject
            );
            this.viewDataBinding = viewDataBinding;
        }

        void bindDialogElement(
                RxMessage rxMessage,
                MessageViewModel messageViewModel) {
            if (viewDataBinding instanceof ConvItemMessageOtherBinding) {
                ((ConvItemMessageOtherBinding)viewDataBinding).setMessageViewModel(messageViewModel);
                ((ConvItemMessageOtherBinding)viewDataBinding).getMessageViewModel().setRxMessage(rxMessage);
            } else {
                ((ConvItemMessageSelfBinding)viewDataBinding).setMessageViewModel(messageViewModel);
                ((ConvItemMessageSelfBinding)viewDataBinding).getMessageViewModel().setRxMessage(rxMessage);
            }
        }
    }
}
