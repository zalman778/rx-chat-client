package com.hwx.rx_chat.common.object;


import com.hwx.rx_chat.common.object.st.Dialog;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;


public class UserEntity implements Serializable {


    private String id;

    private String name;

    private String passwordHash;

    private Date regDate;

    private boolean isActive;

    private String mail;


    private List<Dialog> dialogs = new ArrayList<Dialog>();

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

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public Date getRegDate() {
        return regDate;
    }

    public void setRegDate(Date regDate) {
        this.regDate = regDate;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public String getMail() {
        return mail;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }

    public List<Dialog> getDialogs() {
        return dialogs;
    }

    public void setDialogs(List<Dialog> dialogs) {
        this.dialogs = dialogs;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserEntity that = (UserEntity) o;
        return isActive == that.isActive &&
                Objects.equals(id, that.id) &&
                Objects.equals(name, that.name) &&
                Objects.equals(passwordHash, that.passwordHash) &&
                Objects.equals(regDate, that.regDate) &&
                Objects.equals(mail, that.mail) &&
                Objects.equals(dialogs, that.dialogs);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, passwordHash, regDate, isActive, mail, dialogs);
    }

    @Override
    public String toString() {
        return "UserEntity{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", passwordHash='" + passwordHash + '\'' +
                ", regDate=" + regDate +
                ", isActive=" + isActive +
                ", mail='" + mail + '\'' +
                ", dialogs=" + dialogs +
                '}';
    }
}
