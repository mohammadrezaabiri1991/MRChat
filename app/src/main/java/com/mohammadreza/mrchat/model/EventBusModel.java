package com.mohammadreza.mrchat.model;


import android.graphics.Bitmap;

import androidx.annotation.Keep;

@Keep
public class EventBusModel {

    private ChatListModel chatListModel;
    private Bitmap bitmap;

    private boolean isMessageActivityPaused;

    private String messageId = "";
    private String messageBody = "";
    private String myId = "";
    private String friendId = "";

    public EventBusModel() {
    }


    public EventBusModel(String messageId, String messageBody, String myId, String friendId) {
        this.messageId = messageId;
        this.messageBody = messageBody;
        this.myId = myId;
        this.friendId = friendId;
    }

    public EventBusModel(ChatListModel model) {
        this.chatListModel = model;

    }

    public EventBusModel(ChatListRealmModel realmModel) {
        this.chatListModel = new ChatListModel(realmModel);
    }


    public ChatListModel getChatListModel() {
        return chatListModel;
    }


    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    public String getMessageId() {
        return messageId;
    }


    public String getMessageBody() {
        return messageBody;
    }

    public String getMyId() {
        return myId;
    }

    public String getFriendId() {
        return friendId;
    }

    public boolean isMessageActivityPaused() {
        return isMessageActivityPaused;
    }

    public void setMessageActivityPaused(boolean messageActivityPaused) {
        isMessageActivityPaused = messageActivityPaused;
    }
}
