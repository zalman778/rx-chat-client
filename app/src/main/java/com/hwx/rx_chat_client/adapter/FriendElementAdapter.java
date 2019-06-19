package com.hwx.rx_chat_client.adapter;

import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.MutableLiveData;
import android.databinding.DataBindingUtil;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.hwx.rx_chat.common.response.FriendResponse;
import com.hwx.rx_chat_client.R;
import com.hwx.rx_chat_client.adapter.misc.ItemTouchHelperAdapter;
import com.hwx.rx_chat_client.databinding.ActivityFriendElementBinding;
import com.hwx.rx_chat_client.repository.ChatRepository;
import com.hwx.rx_chat_client.viewModel.friend.FriendElementViewModel;
import com.hwx.rx_chat_client.viewModel.misc.DialogListAndIdDialogHolder;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.OptionalInt;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;

public class FriendElementAdapter
        extends RecyclerView.Adapter<FriendElementAdapter.FriendElementViewHolder>
        implements ItemTouchHelperAdapter {


    private List<FriendResponse> friendList = new ArrayList<>();
    private LifecycleOwner lifecycleOwner;
    private MutableLiveData<DialogListAndIdDialogHolder> lvDialogPicked;
    private PublishSubject<String> psProfileSelected;
    private Map<String, String> headersMap;
    private ChatRepository chatRepository;
    private Picasso picasso;
    private boolean isMultiselectMode;

    private RecyclerView recyclerViewlistUsers;

    private PublishSubject<Integer> psFriendRequestReject = PublishSubject.create();
    private PublishSubject<Integer> psFriendRequestAccept = PublishSubject.create();
    private PublishSubject<String> psProfilePicked = PublishSubject.create();

    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    //key - dialogId
    private Map<String, FriendElementViewModel> viewModelsMap = new HashMap<>();

    public FriendElementAdapter(
              PublishSubject<String> psProfileSelected
            , LifecycleOwner lifecycleOwner
            , Map<String, String> headersMap
            , ChatRepository chatRepository
            , Picasso picasso
            , RecyclerView recyclerViewlistUsers
            , boolean isMultiselectMode
    ) {
        this.lifecycleOwner = lifecycleOwner;
        this.psProfileSelected = psProfileSelected;
        this.headersMap = headersMap;
        this.chatRepository = chatRepository;
        this.picasso = picasso;
        this.recyclerViewlistUsers = recyclerViewlistUsers;
        this.isMultiselectMode = isMultiselectMode;

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
        Log.w("AVX", "isMultiselectMode = "+isMultiselectMode);
        if (isMultiselectMode) {
            compositeDisposable.add(
                    psProfilePicked
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(profileId -> {
                                        Integer pos = getAdapterPositionByKey(profileId);
                                        if (pos != null) {
                                            if (viewModelsMap.get(profileId).isPicked())
                                                recyclerViewlistUsers.findViewHolderForAdapterPosition(pos).itemView.setBackgroundResource(R.color.light_gray);
                                            else
                                                recyclerViewlistUsers.findViewHolderForAdapterPosition(pos).itemView.setBackgroundResource(R.color.white);
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

    //avx: test method:
    public void addFriend(FriendResponse friendResponse) {
        this.friendList.add(friendResponse);
        //notifyDataSetChanged();
        notifyItemInserted(this.friendList.size());
    }

    public FriendResponse getFriendReponseByAdapterPosition(Integer adapterPosition) {
        return friendList.get(adapterPosition);
    }

    public Map<String, FriendElementViewModel> getViewModelsMap() {
        return viewModelsMap;
    }

    public PublishSubject<Integer> getPsFriendRequestReject() {
        return psFriendRequestReject;
    }

    public PublishSubject<Integer> getPsFriendRequestAccept() {
        return psFriendRequestAccept;
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
            FriendElementViewModel friendElementViewModel = new FriendElementViewModel(friendList.get(i), psProfileSelected, psProfilePicked, picasso, isMultiselectMode);
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

    public void performRollbackFriendRequest(int adapterPosition) {
        RecyclerView.ViewHolder viewHolder = recyclerViewlistUsers.findViewHolderForAdapterPosition(adapterPosition);

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



    @Override
    public void onItemSwipping(int adapterPosition, Boolean direction) {
        //TODO change background color...
    }

    //dismiss friend request
    @Override
    public void onItemSwipeLeft(int position) {
        psFriendRequestReject.onNext(position/*friendList.get(position).getRequestId()*/);

    }

    //accept friend request
    @Override
    public void onItemSwapRight(int adapterPosition) {
        psFriendRequestAccept.onNext(adapterPosition);
    }

    @Override
    public boolean checkHasRightToSwipe(int adapterPosition) {
        Log.w("AVX", "checking for acceptance of swipe:"+friendList.get(adapterPosition).getAccepted());
        return !friendList.get(adapterPosition).getAccepted();
    }
}
