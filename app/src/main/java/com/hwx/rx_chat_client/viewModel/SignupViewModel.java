package com.hwx.rx_chat_client.viewModel;

import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.view.View;

import com.hwx.rx_chat.common.request.SignupRequest;
import com.hwx.rx_chat_client.repository.ChatRepository;
import com.hwx.rx_chat_client.util.ResourceProvider;
import com.hwx.rx_chat_client.util.SharedPreferencesProvider;

import javax.inject.Inject;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;

public class SignupViewModel extends ViewModel {
    private MutableLiveData<String> lvUsername = new MutableLiveData<>();
    private MutableLiveData<String> lvEmail = new MutableLiveData<>();
    private MutableLiveData<String> lvPrimaryPassword = new MutableLiveData<>();
    private MutableLiveData<String> lvCheckPassword = new MutableLiveData<>();
    private MutableLiveData<String> lvSignupResponse = new MutableLiveData<>();

    private MutableLiveData<Integer> lvIsVisibleProgressBar = new MutableLiveData<>();
    private MutableLiveData<Integer> lvIsVisibleResponse = new MutableLiveData<>();

    private PublishSubject<String> psGotoLoginActivity = PublishSubject.create();

    private ChatRepository chatRepository;
    private ResourceProvider resourceProvider;
    private SharedPreferencesProvider sharedPreferencesProvider;

    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    @Inject
    public SignupViewModel(
              ChatRepository chatRepository
            , ResourceProvider resourceProvider
            , SharedPreferencesProvider sharedPreferencesProvider
    ) {
        this.chatRepository = chatRepository;
        this.sharedPreferencesProvider = sharedPreferencesProvider;
        this.resourceProvider = resourceProvider;

        lvIsVisibleProgressBar.setValue(View.GONE);
        lvIsVisibleResponse.setValue(View.GONE);

    }

    public MutableLiveData<String> getLvUsername() {
        return lvUsername;
    }

    public MutableLiveData<String> getLvEmail() {
        return lvEmail;
    }

    public MutableLiveData<String> getLvPrimaryPassword() {
        return lvPrimaryPassword;
    }

    public MutableLiveData<String> getLvCheckPassword() {
        return lvCheckPassword;
    }

    public MutableLiveData<String> getLvSignupResponse() {
        return lvSignupResponse;
    }

    public MutableLiveData<Integer> getLvIsVisibleProgressBar() {
        return lvIsVisibleProgressBar;
    }

    public MutableLiveData<Integer> getLvIsVisibleResponse() {
        return lvIsVisibleResponse;
    }

    public PublishSubject<String> getPsGotoLoginActivity() {
        return psGotoLoginActivity;
    }

    public void onBtnGotoLoginClick(View view) {
        psGotoLoginActivity.onNext("dummy");
    }

    public void onBtnSignUpClick(View view) {
        lvIsVisibleProgressBar.setValue(View.VISIBLE);

        SignupRequest signupRequest = new SignupRequest(lvUsername.getValue(), lvEmail.getValue(), lvPrimaryPassword.getValue(), lvCheckPassword.getValue());
        if (signupRequest.isValid()) {
            compositeDisposable.add(
                chatRepository
                    .signUpUser(signupRequest)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(resp -> {
                                if (resp.getCode().equals("ok"))
                                    psGotoLoginActivity.onNext(null);
                                else {
                                    lvIsVisibleProgressBar.setValue(View.GONE);
                                    lvSignupResponse.setValue(resp.getCode());
                                    lvIsVisibleResponse.setValue(View.VISIBLE);
                                }

                            }
                            ,err->{
                                lvIsVisibleProgressBar.setValue(View.GONE);
                                lvSignupResponse.setValue(err.getMessage());
                                lvIsVisibleResponse.setValue(View.VISIBLE);
                            }
                    )
            );
        } else {
            lvIsVisibleProgressBar.setValue(View.GONE);
            lvSignupResponse.setValue(signupRequest.getInvalidInfo());
            lvIsVisibleResponse.setValue(View.VISIBLE);
        }
    }
}
