package com.hwx.rx_chat_client.background.p2p.db.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import com.hwx.rx_chat_client.background.p2p.db.entity.P2pDialog;

import java.util.List;

import io.reactivex.Flowable;

@Dao
public abstract class P2pDialogDao {

    @Query("SELECT * FROM p2pdialog")
    public abstract Flowable<List<P2pDialog>> getAll();

    @Query("SELECT * FROM p2pdialog WHERE id = :id")
    public abstract P2pDialog getById(String id);

    @Insert
    public abstract void insert(P2pDialog p2pdialog);

    @Update
    public abstract void update(P2pDialog p2pdialog);

    @Delete
    public abstract void delete(P2pDialog p2pdialog);

//    @Transaction
//    public P2pDialog findOrCreateDialogByMessage(P2pMessage p2pMessage) {
//        try {
//            P2pDialog p2pDialog = getById(p2pMessage.getIdDialog());
//            return
//        }
//    }

}
