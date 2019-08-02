package com.hwx.rx_chat_client.fragment;

import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.hwx.rx_chat_client.R;
import com.hwx.rx_chat_client.databinding.FragmentProfileBinding;
import com.hwx.rx_chat_client.viewModel.HomeViewModel;

import javax.inject.Inject;

public class ProfileFragment extends Fragment {



    @Inject
    public ViewModelProvider.Factory mFactory;

    private FragmentProfileBinding fragmentProfileBinding;
    private HomeViewModel homeViewModel;


    public ProfileFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        fragmentProfileBinding = DataBindingUtil.inflate(
                inflater, R.layout.fragment_profile, container, false);



        View view = fragmentProfileBinding.getRoot();
        homeViewModel = ViewModelProviders.of(getActivity(), mFactory).get(HomeViewModel.class);

        fragmentProfileBinding.setHomeViewModel(homeViewModel);
        fragmentProfileBinding.setLifecycleOwner(this);

        getActivity().setTitle("Profile");

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);


    }
}