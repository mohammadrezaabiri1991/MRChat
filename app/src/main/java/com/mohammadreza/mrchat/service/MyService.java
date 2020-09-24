package com.mohammadreza.mrchat.service;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mohammadreza.mrchat.R;
import com.mohammadreza.mrchat.constant.ChatConstant;
import com.mohammadreza.mrchat.model.MessagesModel;
import com.mohammadreza.mrchat.notification.APIService;
import com.mohammadreza.mrchat.notification.Client;
import com.mohammadreza.mrchat.notification.Data;
import com.mohammadreza.mrchat.notification.MyResponse;
import com.mohammadreza.mrchat.notification.NotificationSender;
import com.mohammadreza.mrchat.repository.ChatRealmDatabase;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class MyService extends Service {
    private DatabaseReference chatListReference;
    private MediaPlayer deliver;

    private final IBinder binder = new LocalBinder();
    private APIService apiService;


    public MutableLiveData<Integer> myMessageSeen;
    public MutableLiveData<Boolean> friendIsTyping;
    private DatabaseReference chats;
    private FirebaseDatabase baseReference;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    public boolean isBound;
    private DatabaseReference tokenReference;


    public class LocalBinder extends Binder {
        public MyService getService() {
            return MyService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        isBound = true;
        return binder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        isBound = false;
        return super.onUnbind(intent);
    }

    public MyService() {
    }

    @Override
    public void onCreate() {
        baseReference = FirebaseDatabase.getInstance();
        chats = baseReference.getReference(ChatConstant.CHATS);
        chatListReference = baseReference.getReference(ChatConstant.CHAT_LIST);
        tokenReference = baseReference.getReference().child(ChatConstant.TOKENS);
        chats.keepSynced(true);
//        chatListReference.keepSynced(true);

        super.onCreate();
    }

    public void deliverMessages(MessagesModel model, ImageView imgMessageState, AppCompatActivity appCompatActivity) {
        if (deliver == null && appCompatActivity != null) {
            deliver = MediaPlayer.create(appCompatActivity, R.raw.deliver_m);
        }

        if (baseReference == null) {
            baseReference = FirebaseDatabase.getInstance();
        }
        if (chats == null) {
            chats = baseReference.getReference(ChatConstant.CHATS);
//            chats.keepSynced(true);
        }
        if (chatListReference == null) {
            chatListReference = baseReference.getReference(ChatConstant.CHAT_LIST);
        }
        if (tokenReference == null) {
            tokenReference = baseReference.getReference().child(ChatConstant.TOKENS);
        }


        assert appCompatActivity != null;
        sharedPreferences = appCompatActivity.getSharedPreferences(appCompatActivity.getString(R.string.mr_chat), MODE_PRIVATE);

//TODO
        sendMessageToMyMessages(model, imgMessageState, appCompatActivity);


    }

    private void sendMessageToMyMessages(MessagesModel model, ImageView imgMessageState, AppCompatActivity appCompatActivity) {
        chats.child(model.getSender()).child(model.getReceiver()).child(String.valueOf(model.getId())).setValue(model).addOnSuccessListener(task -> {
//            ChatRealmDatabase.deleteDeliverMessage(ChatConstant.TABLE_ID_FIELD_NAME, model.getId(), appCompatActivity);
            if (model.getDeliveryState() == 0) {
                sendToChatList(model);
            }
            chats.child(model.getSender()).child(model.getReceiver()).child(String.valueOf(model.getId())).child(ChatConstant.DELIVERY_STATE_FIELD_NAME).setValue(1);
            sendNotificationToFriend(model);
            model.setDeliveryState(1);
            try {
                if (!appCompatActivity.isFinishing()) {
                    imgMessageState.setImageResource(R.drawable.ic_deliver_message);
                    deliver.start();
                }
            } catch (Exception e) {

            }

        });
    }

    private void sendNotificationToFriend(MessagesModel model) {
        tokenReference.child(model.getReceiver()).child(ChatConstant.TOKEN).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String userToken = dataSnapshot.getValue(String.class);
                sendNotifications(userToken, model);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void sendNotifications(String userToken, MessagesModel messagesModel) {
        if (apiService == null) {
            apiService = Client.getClient(ChatConstant.FIREBASE_URL).create(APIService.class);
        }
        Data data = new Data(messagesModel.getSender().trim(), messagesModel.getMessage().trim(), messagesModel.getId());
        NotificationSender sender = new NotificationSender(data, userToken);
        apiService.sendNotifcation(sender).enqueue(new Callback<MyResponse>() {
            @Override
            public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {
            }

            @Override
            public void onFailure(Call<MyResponse> call, Throwable t) {

            }
        });
    }


    private void sendToChatList(MessagesModel model) {
        chatListReference.child(model.getReceiver()).child(model.getSender()).setValue(model);
        String friendId = sharedPreferences.getString(model.getReceiver(), "");
        editor = sharedPreferences.edit();
        if (friendId == null || friendId.isEmpty())
            chatListReference.child(model.getSender()).child(model.getReceiver()).setValue(model).addOnSuccessListener(aVoid -> {
                editor.putString(model.getReceiver(), model.getReceiver());
                editor.apply();
            });
    }


    public void watchForReadMyLastMessage(String myId, String friendId, DatabaseReference chatListReference) {
        myMessageSeen = new MutableLiveData<>();
        chatListReference.child(friendId).child(myId).child(ChatConstant.DELIVERY_STATE_FIELD_NAME).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                try {
                    int isRead = dataSnapshot.getValue(Integer.class);
                    if (isRead == 2) {
                        myMessageSeen.setValue(isRead);
                        myMessageSeen.setValue(null);
                    }
                } catch (Exception e) {
                }

                if (!isBound) {
                    chatListReference.child(friendId).child(myId).child(ChatConstant.DELIVERY_STATE_FIELD_NAME).removeEventListener(this);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    public void changeFriendTypingState(String friendId, String currentUserId, DatabaseReference chatListReference) {
        chatListReference.child(friendId).child(currentUserId).child(ChatConstant.IS_TYPING).setValue(false);
    }

    public void getFriendIsTyping(String friendId, String currentUserId, DatabaseReference chatListReference) {
        friendIsTyping = new MutableLiveData<>();
        chatListReference.child(friendId).child(currentUserId).child(ChatConstant.IS_TYPING).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                try {
                    boolean isTyping = snapshot.getValue(Boolean.class);
                    friendIsTyping.setValue(isTyping);
                } catch (Exception e) {
                }

                if (!isBound) {
                    chatListReference.child(friendId).child(currentUserId).child(ChatConstant.IS_TYPING).removeEventListener(this);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }



    public void changeMyTypingState(boolean isTyping, String currentId, String friendId, DatabaseReference chatListReference) {
        chatListReference.child(currentId).child(friendId).child(ChatConstant.IS_TYPING).setValue(isTyping);
    }
}

