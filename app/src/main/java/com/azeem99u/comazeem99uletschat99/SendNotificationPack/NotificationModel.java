package com.azeem99u.comazeem99uletschat99.SendNotificationPack;

public class NotificationModel {

    private String title;
    private String body;
    private String click_action;
    private String channel_id;
    private String ticker;
    private String image;

    public NotificationModel() {
    }

    public NotificationModel(String title, String body, String click_action, String channel_id, String ticker, String image) {
        this.title = title;
        this.body = body;
        this.click_action = click_action;
        this.channel_id = channel_id;
        this.ticker = ticker;
        this.image = image;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getClick_action() {
        return click_action;
    }

    public void setClick_action(String click_action) {
        this.click_action = click_action;
    }

    public String getChannel_id() {
        return channel_id;
    }

    public void setChannel_id(String channel_id) {
        this.channel_id = channel_id;
    }

    public String getTicker() {
        return ticker;
    }

    public void setTicker(String ticker) {
        this.ticker = ticker;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }
}
