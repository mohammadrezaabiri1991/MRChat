package com.mohammadreza.mrchat.model;

import androidx.annotation.Keep;
@Keep
public class ChatBusModel {
    private boolean isSuccess;

    public ChatBusModel(boolean isSuccess) {
        this.isSuccess = isSuccess;
    }

    public boolean isSuccess() {
        return isSuccess;
    }


}
