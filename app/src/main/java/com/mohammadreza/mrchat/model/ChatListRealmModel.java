package com.mohammadreza.mrchat.model;

import androidx.annotation.Keep;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.RealmClass;

@Keep
@RealmClass
public class ChatListRealmModel extends RealmObject {

    @PrimaryKey
    private String id = "";
    private String userName = "";
    private String originalImageUrl = "";
    private String smallImageUrl = "";
    private long status;
    private String lastMessage = "";
    private long latMessageTime;


    public ChatListRealmModel(ChatListModel model) {
        this.id = model.getId();
        this.userName = model.getUserName();
        this.lastMessage = model.getLastMessage();
        this.latMessageTime = model.getLastMessageTime();
        this.status = model.getStatus();
        this.originalImageUrl = model.getOriginalImageUri();
        this.smallImageUrl = model.getSmallImageUri();
    }

    public ChatListRealmModel() {
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


    public String getOriginalImageUrl() {
        return originalImageUrl;
    }

    public void setOriginalImageUrl(String originalImageUrl) {
        this.originalImageUrl = originalImageUrl;
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

    public String getSmallImageUrl() {
        return smallImageUrl;
    }

    public void setSmallImageUrl(String smallImageUrl) {
        this.smallImageUrl = smallImageUrl;
    }

    public long getLatMessageTime() {
        return latMessageTime;
    }

    public void setLatMessageTime(long latMessageTime) {
        this.latMessageTime = latMessageTime;
    }
}

