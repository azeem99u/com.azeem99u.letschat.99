package com.azeem99u.comazeem99uletschat99.models;

public class ContactsModel {

    private String username = "";
    private String userAbout = "";
    private String userImage = "";

    public ContactsModel(String username, String userAbout, String userImage) {
        this.username = username;
        this.userAbout = userAbout;
        this.userImage = userImage;
    }

    public ContactsModel() {}

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

    public void setUserImage(String userImage) {
        this.userImage = userImage;
    }
}
