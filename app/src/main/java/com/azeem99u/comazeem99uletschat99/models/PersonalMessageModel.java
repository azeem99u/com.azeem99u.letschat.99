package com.azeem99u.comazeem99uletschat99.models;

import java.util.Objects;

public class PersonalMessageModel {
    private String messageKey;
    private String type;
    private String message;
    private String fileName;
    private String phoneStoragePath;
    private String myStoragePath;
    private String downloadUrl;
    private String thumbDownloadUrl;
    private String fileSize;
    private String time;
    private String from;
    private String delivery;

    public PersonalMessageModel() {}

    //Simple
    public PersonalMessageModel(String messageKey, String type, String message, String time, String from, String delivery) {
        this.messageKey = messageKey;
        this.type = type;
        this.from = from;
        this.message = message;
        this.time = time;
        this.delivery = delivery;
    }
    //message with image


    public PersonalMessageModel(String messageKey, String type, String message, String fileName, String phoneStoragePath, String myStoragePath,String fileSize, String downloadUrl, String thumbDownloadUrl, String time, String from,String delivery) {
        this.messageKey = messageKey;
        this.type = type;
        this.message = message;
        this.fileName = fileName;
        this.phoneStoragePath = phoneStoragePath;
        this.myStoragePath = myStoragePath;
        this.fileSize = fileSize;
        this.downloadUrl = downloadUrl;
        this.thumbDownloadUrl = thumbDownloadUrl;
        this.time = time;
        this.from = from;
        this.delivery = delivery;
    }

    public PersonalMessageModel(String messageKey, String type, String fileName, String phoneStoragePath, String myStoragePath,String fileSize, String downloadUrl, String time, String from,String delivery) {
        this.messageKey = messageKey;
        this.type = type;
        this.fileName = fileName;
        this.phoneStoragePath = phoneStoragePath;
        this.myStoragePath = myStoragePath;
        this.fileSize = fileSize;
        this.downloadUrl = downloadUrl;
        this.time = time;
        this.from = from;
        this.delivery = delivery;
    }

    public String getMessageKey() {
        return messageKey;
    }

    public void setMessageKey(String messageKey) {
        this.messageKey = messageKey;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getPhoneStoragePath() {
        return phoneStoragePath;
    }

    public void setPhoneStoragePath(String phoneStoragePath) {
        this.phoneStoragePath = phoneStoragePath;
    }

    public String getMyStoragePath() {
        return myStoragePath;
    }

    public void setMyStoragePath(String myStoragePath) {
        this.myStoragePath = myStoragePath;
    }


    public String getFileSize() {
        return fileSize;
    }

    public void setFileSize(String fileSize) {
        this.fileSize = fileSize;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public String getThumbDownloadUrl() {
        return thumbDownloadUrl;
    }

    public void setThumbDownloadUrl(String thumbDownloadUrl) {
        this.thumbDownloadUrl = thumbDownloadUrl;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }



    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PersonalMessageModel that = (PersonalMessageModel) o;
        return Objects.equals(messageKey, that.messageKey) && Objects.equals(type, that.type) && Objects.equals(from, that.from);
    }

    @Override
    public int hashCode() {
        return Objects.hash(messageKey, type, message, from);
    }

    @Override
    public String toString() {
        return "PersonalMessageModel{" +
                "messageKey='" + messageKey + '\'' +
                ", type='" + type + '\'' +
                ", message='" + message + '\'' +
                ", fileName='" + fileName + '\'' +
                ", phoneStoragePath='" + phoneStoragePath + '\'' +
                ", myStoragePath='" + myStoragePath + '\'' +
                ", downloadUrl='" + downloadUrl + '\'' +
                ", thumbDownloadUrl='" + thumbDownloadUrl + '\'' +
                ", fileSize='" + fileSize + '\'' +
                ", time='" + time + '\'' +
                ", from='" + from + '\'' +
                '}';
    }

    public String getDelivery() {
        return delivery;
    }

    public void setDelivery(String delivery) {
        this.delivery = delivery;
    }

}

