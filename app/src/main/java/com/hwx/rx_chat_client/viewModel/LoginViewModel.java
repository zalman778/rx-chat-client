package com.hwx.rx_chat_client.viewModel;

import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import com.hwx.rx_chat.common.response.LoginResponse;
import com.hwx.rx_chat_client.model.LoginUser;
import com.hwx.rx_chat_client.service.ChatRepository;

import java.util.Objects;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;
import okhttp3.Headers;

public class LoginViewModel extends ViewModel {

    public MutableLiveData<String> emailAddress = new MutableLiveData<>();
    public MutableLiveData<String> password = new MutableLiveData<>();
    public MutableLiveData<String> result = new MutableLiveData<>();

    private MutableLiveData<LoginResponse> responseLiveData = new MutableLiveData<>();
    private PublishSubject<String> psGotoLoginActivity = PublishSubject.create();
    private final CompositeDisposable disposables = new CompositeDisposable();

    private ChatRepository chatRepository;

    public LoginViewModel(ChatRepository chatRepository) {

        this.chatRepository = chatRepository;
        //TODO: remove
        emailAddress.setValue("a@a.a");
        password.setValue("12345678");
    }

    public MutableLiveData<LoginResponse> getResponseLiveData() {
        return responseLiveData;
    }

    public MutableLiveData<String> getResult() {
        return result;
    }

    public PublishSubject<String> getPsGotoLoginActivity() {
        return psGotoLoginActivity;
    }

    public void onBtnGotoSigninClick(View view) {
        psGotoLoginActivity.onNext("dummy");
    }

    public void onClick(View view) {

        LoginUser loginUser = new LoginUser(emailAddress.getValue(), password.getValue());

        if (TextUtils.isEmpty(Objects.requireNonNull(loginUser).getStrEmailAddress())) {
            result.setValue("Enter an E-Mail Address");
        }
        else if (!loginUser.isEmailValid()) {
             result.setValue("Enter a Valid E-mail Address");
        }
        else if (TextUtils.isEmpty(Objects.requireNonNull(loginUser).getStrPassword())) {
            result.setValue("Enter a password");
        }
        else if (!loginUser.isPasswordLengthGreaterThan6()) {
            result.setValue("Enter at least 7 Digit password");
        }
        else {
            String encryptedPass = password.getValue();
            disposables.add(chatRepository.authorizeWithResponse(emailAddress.getValue(), encryptedPass)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnSubscribe((d) -> responseLiveData.setValue(new LoginResponse("loading", null)))
                    .subscribe(
                            result -> {
                                Headers headers = result.headers();
                                try {
                                    responseLiveData.setValue(result.body());
                                } catch (Exception e) {
                                    responseLiveData.setValue(new LoginResponse("error", e.getMessage()));
                                }
                               },
                            throwable -> {
                                Log.e("TAG", "error on request:", throwable);
                                responseLiveData.setValue(new LoginResponse("error", throwable.getMessage()));

                            }
                    ));
        }

    }
}
