package com.hwx.rx_chat.common.object.rx.types;

import java.io.Serializable;

public enum EventType implements Serializable {
     MESSAGE_NEW_FROM_CLIENT
    ,MESSAGE_NEW_FROM_SERVER
    ,MESSAGE_DELETED
    ,MESSAGE_EDIT
    ,FRIEND_SOCKET_INFO
}

