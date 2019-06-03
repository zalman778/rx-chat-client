package com.hwx.rx_chat_client.fragment;

import android.arch.lifecycle.ViewModelProviders;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.hwx.rx_chat_client.R;
import com.hwx.rx_chat_client.adapter.DialogElementAdapter;
import com.hwx.rx_chat_client.databinding.FragmentMessagesBinding;
import com.hwx.rx_chat_client.util.ViewModelFactory;
import com.hwx.rx_chat_client.view.ConversationActivity;
import com.hwx.rx_chat_client.viewModel.HomeViewModel;
import com.hwx.rx_chat_client.viewModel.misc.DialogListAndIdDialogHolder;

import javax.inject.Inject;

public class MessagesFragment extends Fragment {

    @Inject
    ViewModelFactory viewModelFactory;

    private FragmentMessagesBinding fragmentMessagesBinding;
    private HomeViewModel homeViewModel;

    public MessagesFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        fragmentMessagesBinding = DataBindingUtil.inflate(
                inflater, R.layout.fragment_messages, container, false);



        View view = fragmentMessagesBinding.getRoot();
        homeViewModel = ViewModelProviders.of(getActivity(), viewModelFactory).get(HomeViewModel.class);
        fragmentMessagesBinding.setHomeViewModel(homeViewModel);
        fragmentMessagesBinding.setLifecycleOwner(this);


        DialogElementAdapter dialogElementAdapter = new DialogElementAdapter(
                homeViewModel.getLvDialogPicked(), getActivity(), homeViewModel.getHeadersMap(), homeViewModel.getChatRepository()
        );
        fragmentMessagesBinding.listMessages.setLayoutManager(new LinearLayoutManager(getActivity()));
        fragmentMessagesBinding.listMessages.setAdapter(dialogElementAdapter);
        //avx test........

//        CompositeDisposable compositeDisposable = new CompositeDisposable();
//        Disposable disposable = Observable
//                .just("1", "2", "3")
//                .delay(7000, TimeUnit.MILLISECONDS)
//                .subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
//
//                .subscribe(new Consumer<String>() {
//                    @Override
//                    public void accept(String s) throws Exception {
//                        DialogResponse dr = new DialogResponse();
//                        dr.setDialogId("xx"+s);
//                        dr.setDialogName("name_"+s);
//                        dr.setLastDate(new Date());
//                        dr.setLastMessage("msg"+s);
//                        dr.setLastUser("user"+s);
//                        dialogElementAdapter.addDialog(dr);
//                    }
//                });
//        compositeDisposable.add(disposable);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        //for case when fragment destroyed and subscribtions should no longer come to closed fragment..
        //when recieving list of dialogs..
        homeViewModel.getLiveDialogList().observe(getViewLifecycleOwner(), dialogList -> {
            DialogElementAdapter dialogElementAdapter = (DialogElementAdapter) fragmentMessagesBinding.listMessages.getAdapter();
            dialogElementAdapter.setDialogList(dialogList);
        });

        //диалог выбран и его данные уже загружены
        homeViewModel.getLvDialogPicked().observe(
                getViewLifecycleOwner(), o -> {
                    //Log.w("AVX", "got list of messages : "+((ArrayList<MessageResponse>) o).size());
                    startActivity(ConversationActivity.getIntent(getActivity(), (DialogListAndIdDialogHolder) o));
                }
        );
    }
}