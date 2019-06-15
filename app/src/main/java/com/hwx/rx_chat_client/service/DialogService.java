package com.hwx.rx_chat_client.service;

import com.hwx.rx_chat.common.response.DefaultResponse;

import java.util.Map;

import io.reactivex.Observable;
import retrofit2.http.GET;
import retrofit2.http.HeaderMap;
import retrofit2.http.Url;

public interface DialogService {
    @GET
    Observable<DefaultResponse> findOrCreateDialog(
              @Url String url
            , @HeaderMap Map<String, String> headers
    );
}
