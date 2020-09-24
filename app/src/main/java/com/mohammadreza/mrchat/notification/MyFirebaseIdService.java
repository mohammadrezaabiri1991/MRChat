package com.mohammadreza.mrchat.notification;

import androidx.annotation.NonNull;

import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.mohammadreza.mrchat.constant.ChatConstant;
import com.mohammadreza.mrchat.model.ContactsModel;
import com.mohammadreza.mrchat.repository.ChatRealmDatabase;

public class MyFirebaseIdService extends FirebaseMessagingService {

    @Override
    public void onNewToken(@NonNull String s) {
        super.onNewToken(s);
        String refreshToken = FirebaseInstanceId.getInstance().getToken();
        try {
            ContactsModel user = ChatRealmDatabase.checkUserExist(ChatConstant.IS_ONLINE_FIELD_NAME, ChatConstant.KEEP_ONLINE, this);
            if (user != null) {
                updateToken(refreshToken, user.getId());
            }
        } catch (Exception e) {
        }
    }
    private void updateToken(String refreshToken, String userID) {
        Token token = new Token(refreshToken);
        FirebaseDatabase.getInstance().getReference(ChatConstant.TOKENS).child(userID).setValue(token);
    }
}
