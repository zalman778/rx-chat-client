package com.hwx.rx_chat_client.background.p2p.db.service;

import android.database.sqlite.SQLiteConstraintException;
import android.os.AsyncTask;
import android.util.Log;

import com.hwx.rx_chat_client.background.p2p.db.dao.P2pDialogDao;
import com.hwx.rx_chat_client.background.p2p.db.dao.P2pMessageDao;
import com.hwx.rx_chat_client.background.p2p.db.entity.P2pDialog;
import com.hwx.rx_chat_client.background.p2p.db.entity.P2pMessage;

import java.util.Date;

public class P2pDbService {

    private P2pDialogDao p2pDialogDao;
    private P2pMessageDao p2pMessageDao;

    public P2pDbService(P2pDialogDao p2pDialogDao, P2pMessageDao p2pMessageDao) {
        this.p2pDialogDao = p2pDialogDao;
        this.p2pMessageDao = p2pMessageDao;
    }

    public void asyncInsertMessage(P2pMessage p2pMessage, String profileId, String remoteProfileId) {
        AsyncTask.execute(()-> {
            Log.w("AVX", "trying to save mag = "+p2pMessage.toString());
            try {
                p2pMessageDao.insert(p2pMessage);
            } catch (SQLiteConstraintException ex) {
                createDialog(
                          p2pMessage.getIdDialog()
                        , p2pMessage.getUserFromName()
                        , remoteProfileId
                        , profileId
                );
                p2pMessageDao.insert(p2pMessage);
            }
        });
    }

    public void asyncDeleteMessage(String messageId) {
        AsyncTask.execute(()->{
            p2pMessageDao.deleteById(messageId);
        });
    }

    public void asyncUpdateMessage(P2pMessage p2pMessage) {
        AsyncTask.execute(()->{
           p2pMessageDao.update(p2pMessage);
        });
    }

    private void createDialog(String idDialog, String userFromName, String userFromId, String profileId) {
        P2pDialog p2pDialog = new P2pDialog();
        p2pDialog.setCaption(userFromName);
        p2pDialog.setDateCreated(new Date());
        p2pDialog.setRemoteProfileId(userFromId);
        p2pDialog.setId(idDialog);
        p2pDialog.setProfileId(profileId);
        p2pDialogDao.insert(p2pDialog);
    }

}
