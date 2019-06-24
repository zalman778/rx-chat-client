package com.hwx.rx_chat_client.adapter;

import android.arch.lifecycle.LifecycleOwner;
import android.databinding.DataBindingUtil;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.hwx.rx_chat.common.response.FriendResponse;
import com.hwx.rx_chat_client.R;
import com.hwx.rx_chat_client.adapter.misc.ItemTouchHelperAdapter;
import com.hwx.rx_chat_client.databinding.ActivityFriendElementBinding;
import com.hwx.rx_chat_client.repository.ChatRepository;
import com.hwx.rx_chat_client.viewModel.friend.FriendElementViewModel;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;

/*
    Адаптер списка пользователей.

 */
public class FriendElementAdapter
        extends RecyclerView.Adapter<FriendElementAdapter.FriendElementViewHolder>
        implements ItemTouchHelperAdapter {

    public static final int MODE_SIMPLE = 0;    //обычный список юзеров
    public static final int MODE_FRIEND_REQUESTS = 1; //режим списка запросов в друзья
    public static final int MODE_DIALOG_USERS = 2; //режим списка пользователей диалога
    public static final int MODE_USER_PICK = 3; //режим выбора пользователя из списка

    private int currentMode;

    //2:
    private StringBuilder currentUserId;
    private StringBuilder dialogCreatorId;



    private List<FriendResponse> friendList = new ArrayList<>();


    private PublishSubject<String> psProfileSelected;
    private Picasso picasso;
    private RecyclerView recyclerView;

    private PublishSubject<Integer> psItemSwipeLeftAction = PublishSubject.create();
    private PublishSubject<Integer> psItemSwipeRightAction = PublishSubject.create();
    private PublishSubject<String> psProfilePicked = PublishSubject.create();

    private CompositeDisposable compositeDisposable = new CompositeDisposable();





    //key - dialogId
    private Map<String, FriendElementViewModel> viewModelsMap = new HashMap<>();

    public FriendElementAdapter(
              PublishSubject<String> psProfileSelected
            , Picasso picasso
            , StringBuilder dialogCreatorId
            , StringBuilder currentUserId
            , RecyclerView recyclerView
            , int currentMode
    ) {
        this.psProfileSelected = psProfileSelected;
        this.picasso = picasso;
        this.recyclerView = recyclerView;
        this.currentMode = currentMode;
        if (currentMode == MODE_DIALOG_USERS) {
            this.currentUserId = currentUserId;
            this.dialogCreatorId = dialogCreatorId;
            Log.w("AVX", "current userID="+currentUserId);
        }
        subscribePublishers();
    }

    private Integer getAdapterPositionByKey(String key) {
        for (int i = 0; i < friendList.size(); i++) {
            if (friendList.get(i).getUserId().equals(key))
                return i;
        }
        return null;
    }

    public List<String> getListOfSelectedProfiles() {
        return viewModelsMap.keySet().stream().filter(e->viewModelsMap.get(e).isPicked()).collect(Collectors.toList());
    }

    private void subscribePublishers() {

        if (currentMode == MODE_USER_PICK) {
            compositeDisposable.add(
                    psProfilePicked
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(profileId -> {
                                        Integer pos = getAdapterPositionByKey(profileId);
                                        if (pos != null) {
                                            if (viewModelsMap.get(profileId).isPicked())
                                                recyclerView.findViewHolderForAdapterPosition(pos).itemView.setBackgroundResource(R.color.light_gray);
                                            else
                                                recyclerView.findViewHolderForAdapterPosition(pos).itemView.setBackgroundResource(R.color.white);
                                        }
                                    }, err -> Log.e("AVX", "err", err)
                            )
            );
        }
    }

    public void setFriendList(List<FriendResponse> friendList) {
        this.friendList = friendList;
        notifyDataSetChanged();
    }

    public FriendResponse getFriendReponseByAdapterPosition(Integer adapterPosition) {
        return friendList.get(adapterPosition);
    }


    public PublishSubject<Integer> getPsItemSwipeLeftAction() {
        return psItemSwipeLeftAction;
    }

    public PublishSubject<Integer> getPsItemSwipeRightAction() {
        return psItemSwipeRightAction;
    }

    @NonNull
    @Override
    public FriendElementViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        ActivityFriendElementBinding activityFriendElementBinding = DataBindingUtil.inflate(
                LayoutInflater.from(viewGroup.getContext()), R.layout.activity_friend_element,viewGroup, false
        );
        return new FriendElementViewHolder(activityFriendElementBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull FriendElementViewHolder friendElementViewHolder, int i) {
        String userId = friendList.get(i).getUserId();

        if (viewModelsMap.get(userId) == null) {
            FriendElementViewModel friendElementViewModel =
                    new FriendElementViewModel(friendList.get(i), psProfileSelected
                            , psProfilePicked, picasso
                            , currentMode == FriendElementAdapter.MODE_USER_PICK
                    );
            viewModelsMap.put(userId, friendElementViewModel);
        }
        friendElementViewHolder.bindFriendElement(friendList.get(i), viewModelsMap.get(userId));
    }

    @Override
    public int getItemCount() {
        return friendList.size();
    }

    public void performRejectFriendRequest(int adapterPosition) {
        viewModelsMap.remove(friendList.get(adapterPosition).getUserId());
        friendList.remove(adapterPosition);
        notifyItemRemoved(adapterPosition);
    }

    public void performAcceptFriendRequest(String reqId) {
        //? wait for rx?
    }

    public void performRollbackSwipeRight(int adapterPosition) {
        RecyclerView.ViewHolder viewHolder = recyclerView.findViewHolderForAdapterPosition(adapterPosition);

        viewHolder.itemView.setTranslationX(0);
        viewHolder.itemView.setAlpha(1f);
        notifyItemChanged(adapterPosition);
    }


    public static class FriendElementViewHolder extends RecyclerView.ViewHolder {

        ActivityFriendElementBinding activityFriendElementBinding;

        public FriendElementViewHolder(@NonNull ActivityFriendElementBinding activityFriendElementBinding) {
            super(activityFriendElementBinding.dialogSimpleObject);
            this.activityFriendElementBinding = activityFriendElementBinding;
        }

        void bindFriendElement(
                FriendResponse friendResponse,
                FriendElementViewModel friendElementViewModel) {
             activityFriendElementBinding.setFriendElementViewModel(friendElementViewModel);

             activityFriendElementBinding.getFriendElementViewModel().setFriendResponse(friendResponse);
        }
    }

    //swiping
    @Override
    public boolean onItemMove(int fromPosition, int toPosition) {
        return false;
    }


    //определение цвета
    @Override
    public void onItemSwipping(int adapterPosition, Boolean direction) {
        if (currentMode == MODE_DIALOG_USERS) {

            RecyclerView.ViewHolder viewHolder =
                    recyclerView.findViewHolderForAdapterPosition(adapterPosition);
            if (!direction)
                viewHolder.itemView.setBackgroundResource(R.color.red);

        }

        if (currentMode == MODE_FRIEND_REQUESTS) {
            RecyclerView.ViewHolder viewHolder =
                    recyclerView.findViewHolderForAdapterPosition(adapterPosition);
            if (direction)
                viewHolder.itemView.setBackgroundResource(R.color.green);
            else
                viewHolder.itemView.setBackgroundResource(R.color.red);
        }
    }

    //dismiss friend request
    @Override
    public void onItemSwipeLeft(int adapterPosition) {
        psItemSwipeLeftAction.onNext(adapterPosition);

    }

    //accept friend request
    @Override
    public void onItemSwapRight(int adapterPosition) {
        psItemSwipeRightAction.onNext(adapterPosition);
    }

     /*
        Проверяем на возможность свайпа:
        Свайп может быть в 2ух случаях:
        1) в списке друзей - свайп на принятие/отказ от заявки в друщья
        2) в списке участников диалога - свайп на удаление
     */

    @Override
    public int getSwipeFlags(int adapterPosition) {
        if (currentMode == MODE_FRIEND_REQUESTS)
            if (friendList.get(adapterPosition).getAccepted() != null && !friendList.get(adapterPosition).getAccepted())
                return ItemTouchHelper.START | ItemTouchHelper.END;
        if (currentMode == MODE_DIALOG_USERS)
            if (!currentUserId.toString().equals(friendList.get(adapterPosition).getUserId())
                    && currentUserId.toString().equals(dialogCreatorId.toString())
            )
                return ItemTouchHelper.END;
        return 0;
    }
}
