package com.hwx.rx_chat.common.object.st;


import com.hwx.rx_chat.common.object.UserEntity;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Objects;


public class Dialog implements Serializable {


    private String id;

    private String name;


    private UserEntity userCreated;


    private List<UserEntity> members;

    private Date createDate;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public UserEntity getUserCreated() {
        return userCreated;
    }

    public void setUserCreated(UserEntity userCreated) {
        this.userCreated = userCreated;
    }

    public List<UserEntity> getMembers() {
        return members;
    }

    public void setMembers(List<UserEntity> members) {
        this.members = members;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Dialog dialog = (Dialog) o;
        return Objects.equals(id, dialog.id) &&
                Objects.equals(name, dialog.name) &&
                Objects.equals(userCreated, dialog.userCreated) &&
                Objects.equals(members, dialog.members) &&
                Objects.equals(createDate, dialog.createDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, userCreated, members, createDate);
    }

    @Override
    public String toString() {
        return "Dialog{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", userCreated=" + userCreated +
                ", members=" + members +
                ", createDate=" + createDate +
                '}';
    }
}
