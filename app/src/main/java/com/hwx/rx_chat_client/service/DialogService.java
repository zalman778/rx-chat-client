package com.hwx.rx_chat_client.service;

import com.hwx.rx_chat.common.response.DefaultResponse;
import com.hwx.rx_chat_client.Configuration;

import java.util.List;
import java.util.Map;

import io.reactivex.Observable;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.HeaderMap;
import retrofit2.http.POST;
import retrofit2.http.Url;

public interface DialogService {
    @GET
    Observable<DefaultResponse> findOrCreateDialog(
              @Url String url
            , @HeaderMap Map<String, String> headers
    );

    @FormUrlEncoded
    @POST(Configuration.URL_DIALOGS_CREATE)
    Observable<DefaultResponse> createDialog(
              @HeaderMap Map<String, String> headers
            , @Field("dialogCaption") String dialogCaption
            , @Field("pickedProfiles") List<String> pickedProfiles
    );
}
