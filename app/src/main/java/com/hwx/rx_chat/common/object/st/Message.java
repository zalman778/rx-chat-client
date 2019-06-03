//package com.hwx.rx_chat.common.entity.st;
//
//import com.google.gson.annotations.SerializedName;
//
//
//import java.io.Serializable;
//import java.util.Date;
//import java.util.Objects;
//
//
//public class Message implements Serializable {
//
//    @SerializedName("id")
//    private String id;
//
//    @SerializedName("user_from")
//    private String userFrom;
//
//    @SerializedName("value")
//    private String value;
//
//    @SerializedName("date_sent")
//    private Date dateSent;
//
//    @SerializedName("date_exp")
//    private Date dateExp;
//
//    @SerializedName("is_expirable")
//    private boolean isExpirable;
//
//    private Dialog msgDialog;
//
//    public String getId() {
//        return id;
//    }
//
//    public void setId(String id) {
//        this.id = id;
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
//    public Date getDateSent() {
//        return dateSent;
//    }
//
//    public void setDateSent(Date dateSent) {
//        this.dateSent = dateSent;
//    }
//
//    public Date getDateExp() {
//        return dateExp;
//    }
//
//    public void setDateExp(Date dateExp) {
//        this.dateExp = dateExp;
//    }
//
//    public boolean isExpirable() {
//        return isExpirable;
//    }
//
//    public void setExpirable(boolean expirable) {
//        isExpirable = expirable;
//    }
//
//    @Override
//    public boolean equals(Object o) {
//        if (this == o) return true;
//        if (o == null || getClass() != o.getClass()) return false;
//        Message message = (Message) o;
//        return isExpirable == message.isExpirable &&
//                Objects.equals(id, message.id) &&
//                Objects.equals(userFrom, message.userFrom) &&
//                Objects.equals(value, message.value) &&
//                Objects.equals(dateSent, message.dateSent) &&
//                Objects.equals(dateExp, message.dateExp) &&
//                Objects.equals(msgDialog, message.msgDialog);
//    }
//
//    @Override
//    public int hashCode() {
//        return Objects.hash(id, userFrom, value, dateSent, dateExp, isExpirable, msgDialog);
//    }
//
//    @Override
//    public String toString() {
//        return "Message{" +
//                "id='" + id + '\'' +
//                ", userFrom='" + userFrom + '\'' +
//                ", value='" + value + '\'' +
//                ", dateSent=" + dateSent +
//                ", dateExp=" + dateExp +
//                ", isExpirable=" + isExpirable +
//                ", msgDialog=" + msgDialog +
//                '}';
//    }
//}
