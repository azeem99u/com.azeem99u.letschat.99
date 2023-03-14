package com.azeem99u.comazeem99uletschat99.models;

public class GroupMessageModel {
    private String userKey;
    private String name;
    private String message;
    private String date;
    private String userImage;
    private String time;

    public GroupMessageModel(String userKey, String userName, String message, String userImage, String time) {
        this.userKey = userKey;
        this.name = userName;
        this.message = message;
        this.userImage = userImage;
        this.time = time;
    }

    public GroupMessageModel() {}


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getUserKey() {
        return userKey;
    }

    public void setUserKey(String userKey) {
        this.userKey = userKey;
    }

    public String getUserImage() {
        return userImage;
    }

    public void setUserImage(String userImage) {
        this.userImage = userImage;
    }
}
