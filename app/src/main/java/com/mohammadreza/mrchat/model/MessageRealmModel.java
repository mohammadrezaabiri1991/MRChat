package com.mohammadreza.mrchat.model;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.RealmClass;
import androidx.annotation.Keep;
@Keep
@RealmClass
public class MessageRealmModel extends RealmObject {
    @PrimaryKey
    private long id;
    private String senderId = "";
    private String receiverId = "";
    private String Message = "";
    private int deliveryState;

    public MessageRealmModel(MessagesModel messagesModel) {
        this.id = messagesModel.getId();
        this.senderId = messagesModel.getSender();
        this.receiverId = messagesModel.getReceiver();
        this.Message = messagesModel.getMessage();
        this.deliveryState = messagesModel.getDeliveryState();
    }


    public int getDeliveryState() {
        return deliveryState;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public MessageRealmModel() {
    }

    public void setDeliveryState(int deliveryState) {
        this.deliveryState = deliveryState;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(String receiverId) {
        this.receiverId = receiverId;
    }

    public String getMessage() {
        return Message;
    }

    public void setMessage(String message) {
        Message = message;
    }
}
