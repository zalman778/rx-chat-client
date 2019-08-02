package com.hwx.rx_chat_client.util;

import android.content.Context;
import android.graphics.drawable.Drawable;

/**
 * Represents a single point of access to the resources
 */

public class ResourceProvider {

    private Context mContext;

    public ResourceProvider(Context mContext) {
        this.mContext = mContext;
    }

    public String getString(int resId) {
        return mContext.getString(resId);
    }

    public String getString(int resId, String value) {
        return mContext.getString(resId, value);
    }

    public Drawable getDrawable(int resId) { return mContext.getDrawable(resId); }

    public int getColorDrawable (int resId) {return mContext.getColor(resId); }
}
