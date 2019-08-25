package com.hwx.rx_chat_client.background.p2p.object.type;

public enum ObjectType {
     MESSAGE
    /*
        Req/res for request channel operation, and starts DH key exchange:
        value - caption
        valueId - avatarUrl
     */
    , WELCOME_HANDSHAKE_REQEST
    , WELCOME_HANDSHAKE_RESPONSE
    /*
        Req/res for message deletion:
        msgId in value
     */
    ,ACTION_REMOVE_MESSAGE_REQUEST
    ,ACTION_REMOVE_MESSAGE_RESPONSE

    //message edit
    ,ACTION_EDIT_MESSAGE_REQUEST
    ,ACTION_EDIT_MESSAGE_RESPONSE

    //voice calls:
    , ACTION_VOICE_CALL_START_REQUEST
    , ACTION_VOICE_CALL_START_RESPONSE
    , VOICE_CALL_PAYLOAD

}
