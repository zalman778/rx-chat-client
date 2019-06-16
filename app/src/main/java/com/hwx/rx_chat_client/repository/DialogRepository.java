package com.hwx.rx_chat_client.repository;

import com.hwx.rx_chat.common.response.DefaultResponse;
import com.hwx.rx_chat_client.service.DialogService;

import java.util.Map;

import io.reactivex.Observable;

public class DialogRepository {
    private DialogService dialogService;

    public DialogRepository(DialogService dialogService) {
        this.dialogService = dialogService;
    }

    public Observable<DefaultResponse> findOrCreateDialog(String url,  Map<String, String> headersMap) {
        return dialogService.findOrCreateDialog(url, headersMap);
    }
}
