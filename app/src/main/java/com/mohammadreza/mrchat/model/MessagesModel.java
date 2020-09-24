package com.mohammadreza.mrchat.model;

import androidx.annotation.Keep;

import java.io.Serializable;

@Keep
public class MessagesModel implements Serializable {
    private long id;
    private int deliveryState;
    private String message = "";
    private String receiver = "";
    private String sender = "";
    private boolean isTyping;

    public MessagesModel(MessageRealmModel model) {
        this.id = model.getId();
        this.deliveryState = model.getDeliveryState();
        this.message = model.getMessage();
        this.receiver = model.getReceiverId();
        this.sender = model.getSenderId();
    }

    public MessagesModel(EventBusModel busModel) {
        this.id = Long.parseLong(busModel.getMessageId());
        this.deliveryState = 2;
        this.message = busModel.getMessageBody();
        this.receiver = busModel.getMyId();
        this.sender = busModel.getFriendId();
    }

    public MessagesModel() {
    }


    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }


    public int getDeliveryState() {
        return deliveryState;
    }

    public void setDeliveryState(int deliveryState) {
        this.deliveryState = deliveryState;
    }


}
