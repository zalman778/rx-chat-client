package com.hwx.rx_chat_client.viewModel.misc;

import com.hwx.rx_chat.common.response.DialogResponse;

import java.util.ArrayList;

public class DialogListAndIdDialogHolder {

    private String idDialog;
    private ArrayList<DialogResponse> dialogList;

    public DialogListAndIdDialogHolder(String idDialog, ArrayList<DialogResponse> dialogList) {
        this.idDialog = idDialog;
        this.dialogList = dialogList;
    }

    public String getIdDialog() {
        return idDialog;
    }

    public void setIdDialog(String idDialog) {
        this.idDialog = idDialog;
    }

    public ArrayList<DialogResponse> getDialogList() {
        return dialogList;
    }

    public void setDialogList(ArrayList<DialogResponse> dialogList) {
        this.dialogList = dialogList;
    }
}
