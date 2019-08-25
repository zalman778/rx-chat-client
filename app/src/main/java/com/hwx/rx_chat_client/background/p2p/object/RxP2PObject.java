package com.hwx.rx_chat_client.background.p2p.object;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.annotations.SerializedName;
import com.hwx.rx_chat.common.entity.rx.RxMessage;
import com.hwx.rx_chat_client.background.p2p.object.type.ObjectType;


import java.io.Serializable;
import java.util.Arrays;
import java.util.Objects;

public class RxP2PObject implements Serializable {

    @SerializedName("object_type")
    @JsonProperty("object_type")
    private ObjectType objectType;

    private RxMessage message;

    private String value;

    private boolean bool;

    @SerializedName("bytes_payload")
    @JsonProperty("bytes_payload")
    private byte[] bytesPayload;

    @SerializedName("value_id")
    @JsonProperty("value_id")
    private String valueId;

    @SerializedName("value_additional")
    @JsonProperty("value_additional")
    private String valueAdditional;

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

    public String getValueId() {
        return valueId;
    }

    public void setValueId(String valueId) {
        this.valueId = valueId;
    }

    public String getValueAdditional() {
        return valueAdditional;
    }

    public void setValueAdditional(String valueAdditional) {
        this.valueAdditional = valueAdditional;
    }

    public boolean isaBoolean() {
        return bool;
    }

    public void setaBoolean(boolean aBoolean) {
        this.bool = aBoolean;
    }

    public byte[] getBytesPayload() {
        return bytesPayload;
    }

    public void setBytesPayload(byte[] bytesPayload) {
        this.bytesPayload = bytesPayload;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RxP2PObject that = (RxP2PObject) o;
        return bool == that.bool &&
                objectType == that.objectType &&
                Objects.equals(message, that.message) &&
                Objects.equals(value, that.value) &&
                Arrays.equals(bytesPayload, that.bytesPayload) &&
                Objects.equals(valueId, that.valueId) &&
                Objects.equals(valueAdditional, that.valueAdditional);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(objectType, message, value, bool, valueId, valueAdditional);
        result = 31 * result + Arrays.hashCode(bytesPayload);
        return result;
    }

    @Override
    public String toString() {
        return "RxP2PObject{" +
                "objectType=" + objectType +
                ", message=" + message +
                ", value='" + value + '\'' +
                ", bool=" + bool +
                ", valueId='" + valueId + '\'' +
                ", valueAdditional='" + valueAdditional + '\'' +
                '}';
    }
}
