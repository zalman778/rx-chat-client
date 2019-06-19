package com.hwx.rx_chat_client.adapter;

import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.MutableLiveData;
import android.databinding.DataBindingUtil;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.hwx.rx_chat.common.entity.rx.RxMessage;
import com.hwx.rx_chat.common.response.DialogResponse;
import com.hwx.rx_chat_client.R;
import com.hwx.rx_chat_client.databinding.ActivityDialogElementBinding;
import com.hwx.rx_chat_client.repository.ChatRepository;
import com.hwx.rx_chat_client.util.SingleLiveEvent;
import com.hwx.rx_chat_client.viewModel.conversation.DialogElementViewModel;
import com.hwx.rx_chat_client.viewModel.misc.DialogListAndIdDialogHolder;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DialogElementAdapter extends RecyclerView.Adapter<DialogElementAdapter.DialogElementViewHolder> {


    private List<DialogResponse> dialogList = new ArrayList<>();
    private LifecycleOwner lifecycleOwner;
    private SingleLiveEvent lvDialogPicked;
    private Map<String, String> headersMap;
    private ChatRepository chatRepository;

    //key - dialogId
    private Map<String, DialogElementViewModel> viewModelsMap = new HashMap<>();

    public DialogElementAdapter(
              SingleLiveEvent lvDialogPicked
            , LifecycleOwner lifecycleOwner
            , Map<String, String> headersMap
            , ChatRepository chatRepository
    ) {
        this.lifecycleOwner = lifecycleOwner;
        this.lvDialogPicked = lvDialogPicked;
        this.headersMap = headersMap;
        this.chatRepository = chatRepository;
    }

    //смотрим есть ли такой диалог, если есть, то в нем обновляем инфу, если нет, то создаем новый
    public void createOrUpdateDialogByRxMessage(RxMessage rxMessage) {
        //dialogList.stream().filter(e->e.getDialogId().equals(rxMessage.getIdDialog())).findFirst().orElseGet(null);

        Integer dialogPosition = getAdapterPositionByDialogId(rxMessage.getIdDialog());
        if (dialogPosition != null) {
            DialogResponse tempResponse = dialogList.get(dialogPosition);
            tempResponse.setLastMessage(rxMessage.getValue());
            tempResponse.setLastDate(rxMessage.getDateSent());
            tempResponse.setLastUser(rxMessage.getUserFromName());
            dialogList.remove(dialogPosition);
            dialogList.add(0, tempResponse);
            notifyDataSetChanged();
        } else {
            DialogResponse dialogResponse = new DialogResponse();
            dialogResponse.setDialogName(rxMessage.getIdDialog());//todo? where to get dialog name?!
            dialogResponse.setLastUser(rxMessage.getUserFromName());
            dialogResponse.setLastMessage(rxMessage.getValue());
            dialogResponse.setLastDate(rxMessage.getDateSent());
            dialogList.add(0, dialogResponse);
            notifyItemInserted(this.dialogList.size());
        }
    }

    public Integer getAdapterPositionByDialogId(String dialogId) {
        for (int i = 0; i < dialogList.size(); i++) {
            if (dialogList.get(i).getDialogId().equals(dialogId))
                return i;
        }
        return null;
    }


    public void setDialogList(List<DialogResponse> dialogList) {
        this.dialogList = dialogList;
        notifyDataSetChanged();
    }

    //avx: test method:
    public void addDialog(DialogResponse dialogResponse) {
        this.dialogList.add(dialogResponse);
        //notifyDataSetChanged();
        notifyItemInserted(this.dialogList.size());
    }

    public Map<String, DialogElementViewModel> getViewModelsMap() {
        return viewModelsMap;
    }

    @NonNull
    @Override
    public DialogElementViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        ActivityDialogElementBinding activityDialogElementBinding = DataBindingUtil.inflate(
                LayoutInflater.from(viewGroup.getContext()), R.layout.activity_dialog_element,viewGroup, false
        );
        return new DialogElementViewHolder(activityDialogElementBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull DialogElementViewHolder dialogElementViewHolder, int i) {

        String dialogId = dialogList.get(i).getDialogId();

        if (viewModelsMap.get(dialogId) == null) {
            DialogElementViewModel dialogElementViewModel = new DialogElementViewModel(dialogList.get(i), headersMap, chatRepository, lvDialogPicked);
            viewModelsMap.put(dialogId, dialogElementViewModel);
        }
        dialogElementViewHolder.bindDialogElement(dialogList.get(i), viewModelsMap.get(dialogId));
    }

    @Override
    public int getItemCount() {
        return dialogList.size();
    }

    public static class DialogElementViewHolder extends RecyclerView.ViewHolder {

        ActivityDialogElementBinding activityDialogElementBinding;

        public DialogElementViewHolder(@NonNull ActivityDialogElementBinding activityDialogElementBinding) {
            super(activityDialogElementBinding.dialogSimpleObject);
            this.activityDialogElementBinding = activityDialogElementBinding;
        }

        void bindDialogElement(
                DialogResponse dialogResponse,
                DialogElementViewModel dialogElementViewModel) {
             activityDialogElementBinding.setDialogElementViewModel(dialogElementViewModel);
             activityDialogElementBinding.getDialogElementViewModel().setDialogResponse(dialogResponse);
        }
    }
}
