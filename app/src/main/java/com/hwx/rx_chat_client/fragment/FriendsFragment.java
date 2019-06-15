package com.hwx.rx_chat_client.fragment;

import android.arch.lifecycle.ViewModelProviders;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.hwx.rx_chat_client.R;
import com.hwx.rx_chat_client.adapter.FriendElementAdapter;
import com.hwx.rx_chat_client.adapter.misc.ItemTouchHelperCallback;
import com.hwx.rx_chat_client.databinding.FragmentFriendsBinding;
import com.hwx.rx_chat_client.util.ViewModelFactory;
import com.hwx.rx_chat_client.view.friend.AddFriendActivity;
import com.hwx.rx_chat_client.view.friend.ProfileActivity;
import com.hwx.rx_chat_client.viewModel.HomeViewModel;

import javax.inject.Inject;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class FriendsFragment extends Fragment {

    @Inject
    ViewModelFactory viewModelFactory;

    private FragmentFriendsBinding fragmentFriendsBinding;
    private HomeViewModel homeViewModel;

    private CompositeDisposable compositeDisposable = new CompositeDisposable();
    private FriendElementAdapter friendElementAdapter;

    public FriendsFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        fragmentFriendsBinding = DataBindingUtil.inflate(
                inflater, R.layout.fragment_friends, container, false);



        View view = fragmentFriendsBinding.getRoot();
        homeViewModel = ViewModelProviders.of(getActivity(), viewModelFactory).get(HomeViewModel.class);
        fragmentFriendsBinding.setHomeViewModel(homeViewModel);
        fragmentFriendsBinding.setLifecycleOwner(this);


        friendElementAdapter = new FriendElementAdapter(
             homeViewModel.getPsProfileSelected(), getActivity(), homeViewModel.getHeadersMap(), homeViewModel.getChatRepository(), homeViewModel.getPicasso()
        );
        fragmentFriendsBinding.listFriends.setLayoutManager(new LinearLayoutManager(getActivity()));
        fragmentFriendsBinding.listFriends.setAdapter(friendElementAdapter);

        //swipe support
        ItemTouchHelper.Callback callback = new ItemTouchHelperCallback(friendElementAdapter);
        ItemTouchHelper mItemTouchHelper = new ItemTouchHelper(callback);
        mItemTouchHelper.attachToRecyclerView(fragmentFriendsBinding.listFriends);

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_friends_fragment_btn_add, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id == R.id.btnAddFriend){
            startActivity(AddFriendActivity.getIntent(getContext()));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        setHasOptionsMenu(true);

        homeViewModel.getLvFriendsList().observe(getViewLifecycleOwner(), friendList -> {
            FriendElementAdapter friendElementAdapter = (FriendElementAdapter) fragmentFriendsBinding.listFriends.getAdapter();
            friendElementAdapter.setFriendList(friendList);
        });

        subscribePublishers();
        homeViewModel.onRefreshFriendsList();
    }


    private void subscribePublishers() {
        compositeDisposable.add(
                friendElementAdapter
                        .getPsFriendRequestAccept()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(e->{
                            homeViewModel.sendFriendRequestAccept(e);
                            //adapter check for err...
                        }, e-> Log.e("AVX", "error on req", e))
        );

        compositeDisposable.add(
                friendElementAdapter
                        .getPsFriendRequestReject()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(e->{
                            homeViewModel.sendFriendRequestReject(e);
                            //adapter check for err...
                        }, e-> Log.e("AVX", "error on req", e))
        );

        compositeDisposable.add(
                homeViewModel.getPsProfileSelectedLoaded()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(userInfo->{
                            startActivity(ProfileActivity.fillDetail(getContext(), userInfo));
                            //adapter check for err...
                        }, e-> Log.e("AVX", "error on req", e))
        );
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        compositeDisposable.dispose();
    }
}