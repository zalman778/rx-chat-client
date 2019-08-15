package com.hwx.rx_chat_client.background.p2p.object;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.annotations.SerializedName;
import com.hwx.rx_chat.common.entity.rx.RxMessage;
import com.hwx.rx_chat_client.background.p2p.object.type.ObjectType;


import java.io.Serializable;
import java.util.Objects;

public class RxP2PObject implements Serializable {

    @SerializedName("object_type")
    @JsonProperty("object_type")
    private ObjectType objectType;

    private RxMessage message;

    private String value;

    public RxP2PObject() {
    }

    public RxP2PObject(ObjectType objectType, RxMessage message) {
        this.objectType = objectType;
        this.message = message;
    }

    public RxP2PObject(ObjectType objectType, String value) {
        this.objectType = objectType;
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RxP2PObject that = (RxP2PObject) o;
        return objectType == that.objectType &&
                Objects.equals(message, that.message);
    }

    @Override
    public int hashCode() {
        return Objects.hash(objectType, message);
    }

    public ObjectType getObjectType() {
        return objectType;
    }

    public void setObjectType(ObjectType objectType) {
        this.objectType = objectType;
    }

    public RxMessage getMessage() {
        return message;
    }

    public void setMessage(RxMessage message) {
        this.message = message;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "RxP2PObject{" +
                "objectType=" + objectType +
                ", message=" + message +
                ", value='" + value + '\'' +
                '}';
    }
}
