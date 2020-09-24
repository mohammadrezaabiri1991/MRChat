package com.mohammadreza.mrchat.model;

import io.realm.RealmResults;

public class ChatListModel {
    private String id = "";
    private String userName = "";
    private String originalImageUri = "";
    private String smallImageUri = "";
    private long status;

    private String lastMessage = "";
    private long lastMessageTime;

    public ChatListModel(ContactsModel contactsModel) {
        this.id = contactsModel.getId();
        this.userName = contactsModel.getUserName();
        this.originalImageUri = contactsModel.getOriginalImageUri();
        this.smallImageUri = contactsModel.getSmallImageUri();
        this.status = contactsModel.getStatus();
    }

    public ChatListModel(ChatListRealmModel model) {
        this.id = model.getId();
        this.userName = model.getUserName();
        this.originalImageUri = model.getOriginalImageUrl();
        this.smallImageUri = model.getSmallImageUrl();
        this.lastMessage = model.getLastMessage();
        this.lastMessageTime = model.getLatMessageTime();
        this.status = model.getStatus();
    }

    public ChatListModel() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getOriginalImageUri() {
        return originalImageUri;
    }

    public void setOriginalImageUri(String originalImageUri) {
        this.originalImageUri = originalImageUri;
    }

    public String getSmallImageUri() {
        return smallImageUri;
    }

    public void setSmallImageUri(String smallImageUri) {
        this.smallImageUri = smallImageUri;
    }

    public long getStatus() {
        return status;
    }

    public void setStatus(long status) {
        this.status = status;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public long getLastMessageTime() {
        return lastMessageTime;
    }

    public void setLastMessageTime(long lastMessageTime) {
        this.lastMessageTime = lastMessageTime;
    }

}
