package com.hwx.rx_chat_client.background.p2p.db;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.TypeConverters;

import com.hwx.rx_chat_client.background.p2p.db.converter.DateConverter;
import com.hwx.rx_chat_client.background.p2p.db.dao.P2pDialogDao;
import com.hwx.rx_chat_client.background.p2p.db.dao.P2pMessageDao;
import com.hwx.rx_chat_client.background.p2p.db.entity.P2pDialog;
import com.hwx.rx_chat_client.background.p2p.db.entity.P2pMessage;

@Database(entities = {P2pMessage.class, P2pDialog.class}, version = 1)
@TypeConverters({DateConverter.class})
public abstract class P2pDatabase extends RoomDatabase {
    public abstract P2pDialogDao p2pDialogDao();
    public abstract P2pMessageDao p2pMessageDao();
}
