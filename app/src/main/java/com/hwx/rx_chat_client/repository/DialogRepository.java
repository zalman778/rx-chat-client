package com.hwx.rx_chat_client.repository;

import com.hwx.rx_chat.common.response.DefaultResponse;
import com.hwx.rx_chat_client.service.DialogService;

import java.util.ArrayList;
import java.util.List;
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

    public Observable<DefaultResponse> createDialog(Map<String, String> headersMap, String dialogCaption, List<String> pickedProfiles) {
        return dialogService.createDialog(headersMap, dialogCaption, pickedProfiles);
    }
}
