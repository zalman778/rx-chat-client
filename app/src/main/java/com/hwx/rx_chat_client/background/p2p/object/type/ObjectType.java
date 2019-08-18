package com.hwx.rx_chat_client.background.p2p.object.type;

public enum ObjectType {
     MESSAGE
    /*
        Req/res for request channel operation
     */
    ,PROFILE_ID_REQUEST
    ,PROFILE_ID_RESPONSE
    /*
        Req/res for message deletion:
        msgId in value
     */
    ,ACTION_REMOVE_MESSAGE_REQUEST
    ,ACTION_REMOVE_MESSAGE_REPONSE

    //message edit
    ,ACTION_EDIT_MESSAGE_REQUEST
    ,ACTION_EDIT_MESSAGE_RESPONSE
}
