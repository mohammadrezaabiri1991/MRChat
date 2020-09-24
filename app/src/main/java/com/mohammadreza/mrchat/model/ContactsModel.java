package com.mohammadreza.mrchat.model;

import androidx.annotation.Keep;

import java.io.Serializable;

@Keep
public class ContactsModel implements Serializable {
    private String id = "";
    private String userName = "";
    private String originalImageUri = "";
    private String smallImageUri = "";
    private long status;

    public ContactsModel(ContactsRealmModel realmUser) {
        this.id = realmUser.getId();
        this.userName = realmUser.getUserName();
        this.originalImageUri = realmUser.getOriginalImageUrl();
        this.smallImageUri = realmUser.getSmallImageUrl();
        this.status = realmUser.getStatus();
    }


    public ContactsModel() {

    }

    public ContactsModel(ChatListRealmModel users) {
        this.id = users.getId();
        this.userName = users.getUserName();
        this.originalImageUri = users.getOriginalImageUrl();
        this.smallImageUri = users.getSmallImageUrl();
        this.status = users.getStatus();

    }

    public ContactsModel(ChatListModel model) {
        this.id = model.getId();
        this.userName = model.getUserName();
        this.originalImageUri = model.getOriginalImageUri();
        this.smallImageUri = model.getSmallImageUri();
        this.status = model.getStatus();

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


    public String getOriginalImageUri() {
        return originalImageUri;
    }

    public void setOriginalImageUri(String originalImageUri) {
        this.originalImageUri = originalImageUri;
    }

//    public String getFamily() {
//        return family;
//    }
//
//    public void setFamily(String family) {
//        this.family = family;
//    }
}
