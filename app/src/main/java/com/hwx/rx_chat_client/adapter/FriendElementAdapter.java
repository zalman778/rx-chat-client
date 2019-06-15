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
import com.hwx.rx_chat_client.service.ChatRepository;
import com.hwx.rx_chat_client.viewModel.FriendElementViewModel;
import com.hwx.rx_chat_client.viewModel.misc.DialogListAndIdDialogHolder;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.android.schedulers.AndroidSchedulers;
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

    private PublishSubject<String> psFriendRequestReject = PublishSubject.create();
    private PublishSubject<String> psFriendRequestAccept = PublishSubject.create();

    //key - dialogId
    private Map<String, FriendElementViewModel> viewModelsMap = new HashMap<>();

    public FriendElementAdapter(
              PublishSubject<String> psProfileSelected
            , LifecycleOwner lifecycleOwner
            , Map<String, String> headersMap
            , ChatRepository chatRepository
            , Picasso picasso
    ) {
        this.lifecycleOwner = lifecycleOwner;
        this.psProfileSelected = psProfileSelected;
        this.headersMap = headersMap;
        this.chatRepository = chatRepository;
        this.picasso = picasso;
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

    public Map<String, FriendElementViewModel> getViewModelsMap() {
        return viewModelsMap;
    }

    public PublishSubject<String> getPsFriendRequestReject() {
        return psFriendRequestReject;
    }

    public PublishSubject<String> getPsFriendRequestAccept() {
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
            FriendElementViewModel friendElementViewModel = new FriendElementViewModel(friendList.get(i), headersMap, chatRepository, picasso);
            friendElementViewModel.getPsProfileSelected()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(profileId-> {
                        psProfileSelected.onNext(profileId);
            });

            viewModelsMap.put(userId, friendElementViewModel);
        }
        friendElementViewHolder.bindFriendElement(friendList.get(i), viewModelsMap.get(userId));
    }

    @Override
    public int getItemCount() {
        return friendList.size();
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

    //dismiss friend request
    @Override
    public void onItemSwipeLeft(int position) {
        psFriendRequestReject.onNext(friendList.get(position).getUserId());
    }

    @Override
    public void onItemSwipping(int adapterPosition, Boolean direction) {
        //TODO change background color...
    }

    //accept friend request
    @Override
    public void onItemSwapRight(int adapterPosition) {
        psFriendRequestAccept.onNext(friendList.get(adapterPosition).getRequestId());
    }

    @Override
    public boolean checkHasRightToSwipe(int adapterPosition) {
        Log.w("AVX", "checking for acceptance of swipe:"+friendList.get(adapterPosition).getAccepted());
        return !friendList.get(adapterPosition).getAccepted();
    }
}
