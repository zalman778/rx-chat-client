package com.hwx.rx_chat_client.fragment;

import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.hwx.rx_chat_client.R;
import com.hwx.rx_chat_client.adapter.DialogElementAdapter;
import com.hwx.rx_chat_client.databinding.FragmentMessagesBinding;
import com.hwx.rx_chat_client.view.dialog.ConversationActivity;
import com.hwx.rx_chat_client.view.dialog.CreateDialogActivity;
import com.hwx.rx_chat_client.view.dialog.P2pConversationActivity;
import com.hwx.rx_chat_client.viewModel.HomeViewModel;
import com.hwx.rx_chat_client.viewModel.misc.DialogInfoHolder;

import javax.inject.Inject;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

/*
    Фрагмент списка диалогов - самое левое окно в homeActivity
 */
public class DialogsFragment extends Fragment {

    @Inject
    public ViewModelProvider.Factory mFactory;

    private FragmentMessagesBinding fragmentMessagesBinding;
    private HomeViewModel homeViewModel;
    private DialogElementAdapter dialogElementAdapter;

    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    public DialogsFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        fragmentMessagesBinding = DataBindingUtil.inflate(
                inflater, R.layout.fragment_messages, container, false);



        View view = fragmentMessagesBinding.getRoot();
        homeViewModel = ViewModelProviders.of(getActivity(), mFactory).get(HomeViewModel.class);
        fragmentMessagesBinding.setHomeViewModel(homeViewModel);
        fragmentMessagesBinding.setLifecycleOwner(this);


        dialogElementAdapter = new DialogElementAdapter(
                  homeViewModel.getLvDialogPicked()
                , getActivity()
                , homeViewModel.getHeadersMap()
                , homeViewModel.getChatRepository()
                , homeViewModel.getPicasso()
                , homeViewModel.getResourceProvider()
        );
        fragmentMessagesBinding.listMessages.setLayoutManager(new LinearLayoutManager(getActivity()));
        fragmentMessagesBinding.listMessages.setAdapter(dialogElementAdapter);

        subscribePublishers();
        return view;
    }

    private void subscribePublishers() {
        compositeDisposable.add(
            homeViewModel
                .getPsRecievedRxMessageAction()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(rxMessage -> {
                    dialogElementAdapter.createOrUpdateDialogByRxMessage(rxMessage);
                    //dialogElementAdapter.addDialog();
                }, e-> Log.e("AVX", "err", e))
        );
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_dialog_fragment_btn_add, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id == R.id.btnAddDialog){
            startActivity(CreateDialogActivity.getIntent(getContext()));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        setHasOptionsMenu(true);


        //for case when fragment destroyed and subscribtions should no longer come to closed fragment..
        //when recieving list of dialogs..
        homeViewModel.getLiveDialogList().observe(getViewLifecycleOwner()
                , dialogList -> {
            DialogElementAdapter dialogElementAdapter = (DialogElementAdapter) fragmentMessagesBinding.listMessages.getAdapter();
            dialogElementAdapter.setDialogList(dialogList);
        });

        //диалог выбран и его данные уже загружены
        homeViewModel.getLvDialogPicked().observe(
                 getViewLifecycleOwner()
                , holder -> {
                     DialogInfoHolder dialogInfoHolder = (DialogInfoHolder) holder;
                     if (dialogInfoHolder.isPrivate()) {
                         startActivity(P2pConversationActivity.getIntent(getActivity(), dialogInfoHolder.getRemoteProfileId()));
                     } else {
                         startActivity(ConversationActivity.getIntent(getActivity(), dialogInfoHolder.getId()));
                     }
                }
        );

        getActivity().setTitle("Dialogs");

        homeViewModel.onRefreshDialogs();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        compositeDisposable.dispose();
    }
}