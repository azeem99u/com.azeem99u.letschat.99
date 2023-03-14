package com.azeem99u.comazeem99uletschat99.SendNotificationPack;

import com.google.gson.annotations.SerializedName;

public class RootModel {




    @SerializedName("to") //  "to" changed to token
    private String token;

    @SerializedName("priority")
    private String notificationPriority;


    @SerializedName("direct_boot_ok")
    private Boolean isBoot;


//    @SerializedName("notification")
//    private NotificationModel notification;

    @SerializedName("data")
    private DataModel data;

    public RootModel(String token, String notificationPriority, DataModel data) {
        this.token = token;
        this.notificationPriority = notificationPriority;
        //this.notification = notification;
        this.data = data;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

//    public NotificationModel getNotification() {
//        return notification;
//    }

//    public void setNotification(NotificationModel notification) {
//        this.notification = notification;
//    }

    public DataModel getData() {
        return data;
    }

    public void setData(DataModel data) {
        this.data = data;
    }
}
