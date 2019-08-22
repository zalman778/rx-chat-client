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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RxP2PObject that = (RxP2PObject) o;
        return objectType == that.objectType &&
                Objects.equals(message, that.message) &&
                Objects.equals(value, that.value) &&
                Objects.equals(valueId, that.valueId) &&
                Objects.equals(valueAdditional, that.valueAdditional);
    }

    @Override
    public int hashCode() {
        return Objects.hash(objectType, message, value, valueId, valueAdditional);
    }

    @Override
    public String toString() {
        return "RxP2PObject{" +
                "objectType=" + objectType +
                ", message=" + message +
                ", value='" + value + '\'' +
                ", valueId='" + valueId + '\'' +
                ", valueAdditional='" + valueAdditional + '\'' +
                '}';
    }
}
