package com.mohammadreza.mrchat.model;

import androidx.annotation.Keep;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.RealmClass;

@Keep
@RealmClass
public class ContactsRealmModel extends RealmObject {
    @PrimaryKey
    private String id = "";
    private int isOnline;
    private String userName = "";
    private String originalImageUrl = "";
    private String smallImageUrl = "";
    private long status;

    public ContactsRealmModel() {
    }

    public ContactsRealmModel(ContactsModel user) {
        this.id = user.getId();
        this.userName = user.getUserName();
        this.originalImageUrl = user.getOriginalImageUri();
        this.smallImageUrl = user.getSmallImageUri();
        this.status = user.getStatus();
    }


    public int getIsOnline() {
        return isOnline;
    }

    public void setIsOnline(int isOnline) {
        this.isOnline = isOnline;
    }


    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public String getSmallImageUrl() {
        return smallImageUrl;
    }

    public void setSmallImageUrl(String smallImageUrl) {
        this.smallImageUrl = smallImageUrl;
    }
}
