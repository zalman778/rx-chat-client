package com.hwx.rx_chat_client.util;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPreferencesProvider {

    private Context mContext;

    public SharedPreferencesProvider(Context mContext) {
        this.mContext = mContext;
    }

    public SharedPreferences getSharedPreferences(String name, int mode) {
        return  mContext.getSharedPreferences(name, mode);
    }
}
