package com.hwx.rx_chat.common.object.rx;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.annotations.SerializedName;
import com.hwx.rx_chat.common.entity.rx.RxMessage;
import com.hwx.rx_chat.common.object.rx.types.EventType;
import com.hwx.rx_chat.common.object.rx.types.ObjectType;
import com.hwx.rx_chat.common.object.rx.types.SettingType;

import java.io.Serializable;
import java.util.Objects;

/**
 * Основной rx класс для общения в реактивных сокетах.
 */
public class RxObject implements Serializable {

    //event/setting
    @SerializedName("object_type")
    @JsonProperty("object_type")
    private ObjectType objectType;

    //MESSAGE_NEW_FROM_CLIENT
    @SerializedName("event_type")
    @JsonProperty("event_type")
    private EventType eventType;

    //ID_DIALOG
    @SerializedName("setting_type")
    @JsonProperty("setting_type")
    private SettingType settingType;

    private Object value;

    private RxMessage message;

    @SerializedName("object_id")
    @JsonProperty("object_id")
    private String objectId;


    public RxObject() {
    }

    public RxObject(ObjectType objectType, EventType eventType, Object value, RxMessage rxMessage) {
        this.objectType = objectType;
        this.eventType = eventType;
        this.value = value;
        this.message = rxMessage;
    }

    public RxObject(ObjectType objectType, SettingType settingType, Object value, RxMessage rxMessage) {
        this.objectType = objectType;
        this.settingType = settingType;
        this.value = value;
        this.message = rxMessage;
    }

    public RxObject(ObjectType objectType, EventType eventType, Object value, String objectId) {
        this.objectType = objectType;
        this.eventType = eventType;
        this.value = value;
        this.objectId = objectId;
    }

    public ObjectType getObjectType() {
        return objectType;
    }

    public void setObjectType(ObjectType objectType) {
        this.objectType = objectType;
    }

    public EventType getEventType() {
        return eventType;
    }

    public void setEventType(EventType eventType) {
        this.eventType = eventType;
    }

    public SettingType getSettingType() {
        return settingType;
    }

    public void setSettingType(SettingType settingType) {
        this.settingType = settingType;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public String getObjectId() {
        return objectId;
    }

    public void setObjectId(String objectId) {
        this.objectId = objectId;
    }

    public RxMessage getMessage() {
        return message;
    }

    public void setMessage(RxMessage message) {
        this.message = message;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RxObject rxObject = (RxObject) o;
        return objectType == rxObject.objectType &&
                eventType == rxObject.eventType &&
                settingType == rxObject.settingType &&
                Objects.equals(value, rxObject.value) &&
                Objects.equals(message, rxObject.message) &&
                Objects.equals(objectId, rxObject.objectId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(objectType, eventType, settingType, value, message, objectId);
    }

    @Override
    public String toString() {
        return "RxObject{" +
                "objectType=" + objectType +
                ", eventType=" + eventType +
                ", settingType=" + settingType +
                ", value=" + value +
                ", message=" + message +
                ", objectId='" + objectId + '\'' +
                '}';
    }
}
