package com.hwx.rx_chat_client.background.p2p.db.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import com.hwx.rx_chat_client.background.p2p.db.entity.P2pDialog;

import java.util.List;

import io.reactivex.Flowable;
import io.reactivex.Single;

@Dao
public abstract class P2pDialogDao {

    @Query("SELECT * FROM p2pdialog")
    public abstract Single<List<P2pDialog>> getAll();

    @Query("SELECT * FROM p2pdialog WHERE id = :id")
    public abstract P2pDialog getById(String id);

    @Query("SELECT dd.* FROM p2pdialog dd " +
            "where profile_id = :profileId " +
            "order by " +
            "(select max(mm.date_sent) from p2pmessage mm where mm.id_dialog = dd.id)")
    public abstract Single<List<P2pDialog>> getAllSortedByLastMessageDate(String profileId);


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
