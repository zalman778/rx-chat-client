//package com.hwx.rx_chat.common.response;
//
//import com.fasterxml.jackson.annotation.JsonProperty;
//
//import java.io.Serializable;
//import java.util.Date;
//import java.util.Objects;
//
//public class MessageResponse implements Serializable {
//
//    private String uid;
//
//    @JsonProperty("user_from")
//    private String userFrom;
//
//    private String value;
//
//    @JsonProperty("sent_at")
//    private Date sentAt;
//
//    public MessageResponse(String uid, String userFrom, String value, Date sentAt) {
//        this.uid = uid;
//        this.userFrom = userFrom;
//        this.value = value;
//        this.sentAt = sentAt;
//    }
//
//    public String getUid() {
//        return uid;
//    }
//
//    public void setUid(String uid) {
//        this.uid = uid;
//    }
//
//    public String getUserFrom() {
//        return userFrom;
//    }
//
//    public void setUserFrom(String userFrom) {
//        this.userFrom = userFrom;
//    }
//
//    public String getValue() {
//        return value;
//    }
//
//    public void setValue(String value) {
//        this.value = value;
//    }
//
//    public Date getSentAt() {
//        return sentAt;
//    }
//
//    public void setSentAt(Date sentAt) {
//        this.sentAt = sentAt;
//    }
//
//    @Override
//    public boolean equals(Object o) {
//        if (this == o) return true;
//        if (o == null || getClass() != o.getClass()) return false;
//        MessageResponse that = (MessageResponse) o;
//        return Objects.equals(uid, that.uid) &&
//                Objects.equals(userFrom, that.userFrom) &&
//                Objects.equals(value, that.value) &&
//                Objects.equals(sentAt, that.sentAt);
//    }
//
//    @Override
//    public int hashCode() {
//        return Objects.hash(uid, userFrom, value, sentAt);
//    }
//
//    @Override
//    public String toString() {
//        return "MessageResponse{" +
//                "uid='" + uid + '\'' +
//                ", userFrom='" + userFrom + '\'' +
//                ", value='" + value + '\'' +
//                ", sentAt=" + sentAt +
//                '}';
//    }
//}
