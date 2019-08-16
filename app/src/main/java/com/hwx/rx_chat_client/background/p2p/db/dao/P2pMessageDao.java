package com.hwx.rx_chat_client.background.p2p.db.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import com.hwx.rx_chat_client.background.p2p.db.entity.P2pMessage;

import java.util.List;

import io.reactivex.Flowable;

@Dao
public abstract class P2pMessageDao {

    @Query("SELECT * FROM p2pmessage")
    public abstract Flowable<List<P2pMessage>> getAll();

    @Query("SELECT * FROM p2pmessage WHERE id = :id")
    public abstract P2pMessage getById(long id);

    @Insert
    public abstract void insert(P2pMessage p2pmessage);

    @Update
    public abstract void update(P2pMessage p2pmessage);

    @Delete
    public abstract void delete(P2pMessage p2pmessage);

}