package com.hwx.rx_chat.common.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.Objects;

public class ProfileInfoUpdateRequest implements Serializable {

    @SerializedName("first_name")
    @JsonProperty("first_name")
    private String firstName;

    @SerializedName("last_name")
    @JsonProperty("last_name")
    private String lastName;

    private String username;

    private String bio;

    public ProfileInfoUpdateRequest(String firstName, String lastName, String username, String bio) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.username = username;
        this.bio = bio;
    }

    public ProfileInfoUpdateRequest() {
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProfileInfoUpdateRequest that = (ProfileInfoUpdateRequest) o;
        return Objects.equals(firstName, that.firstName) &&
                Objects.equals(lastName, that.lastName) &&
                Objects.equals(username, that.username) &&
                Objects.equals(bio, that.bio);
    }

    @Override
    public int hashCode() {
        return Objects.hash(firstName, lastName, username, bio);
    }

    @Override
    public String toString() {
        return "ProfileInfoUpdateRequest{" +
                "firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", username='" + username + '\'' +
                ", bio='" + bio + '\'' +
                '}';
    }
}
