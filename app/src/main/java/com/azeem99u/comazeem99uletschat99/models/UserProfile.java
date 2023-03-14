package com.azeem99u.comazeem99uletschat99.models;

import java.util.Objects;

public class UserProfile {

    private String userId;
    private String username;
    private String userAbout;
    private String userImage;


    public UserProfile(String userId, String username, String userAbout, String userImage) {
        this.userId = userId;
        this.username = username;
        this.userAbout = userAbout;
        this.userImage = userImage;
    }

    public UserProfile(String userId, String username, String userAbout) {
        this.userId = userId;
        this.username = username;
        this.userAbout = userAbout;
    }

    UserProfile() {
    }


    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUserAbout() {
        return userAbout;
    }

    public void setUserAbout(String userAbout) {
        this.userAbout = userAbout;
    }

    public String getUserImage() {
        return userImage;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserProfile that = (UserProfile) o;
        return Objects.equals(userId, that.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId);
    }
}
