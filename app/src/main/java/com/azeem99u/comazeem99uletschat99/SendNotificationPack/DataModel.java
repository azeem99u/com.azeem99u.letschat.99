package com.azeem99u.comazeem99uletschat99.SendNotificationPack;

public class DataModel {

    private String title;
    private String body;
    private String click_action;
    private String channel_id;
    private String ticker;
    private String image;
    private String message_key;
    private String sender_id;
    private String receiver_id;


    public DataModel(String title, String body, String click_action, String channel_id, String image, String message_key, String sender_id, String receiver_id) {
        this.title = title;
        this.body = body;
        this.click_action = click_action;
        this.channel_id = channel_id;
        this.image = image;
        this.message_key = message_key;
        this.sender_id = sender_id;
        this.receiver_id = receiver_id;
    }

    public DataModel() {
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

    public String getMessage_key() {
        return message_key;
    }

    public void setMessage_key(String message_key) {
        this.message_key = message_key;
    }

    public String getSender_id() {
        return sender_id;
    }

    public void setSender_id(String sender_id) {
        this.sender_id = sender_id;
    }

    public String getReceiver_id() {
        return receiver_id;
    }

    public void setReceiver_id(String receiver_id) {
        this.receiver_id = receiver_id;
    }
}