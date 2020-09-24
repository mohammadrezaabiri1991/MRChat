package com.mohammadreza.mrchat.viewmodel;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.os.IBinder;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;
import androidx.databinding.BindingAdapter;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.OnLifecycleEvent;
import androidx.lifecycle.ProcessLifecycleOwner;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mohammadreza.mrchat.BR;
import com.mohammadreza.mrchat.R;
import com.mohammadreza.mrchat.adapter.MessagesListAdapter;
import com.mohammadreza.mrchat.application.MyApp;
import com.mohammadreza.mrchat.constant.ChatConstant;
import com.mohammadreza.mrchat.databinding.ActivityMessageBinding;
import com.mohammadreza.mrchat.model.ChatListModel;
import com.mohammadreza.mrchat.model.ContactsModel;
import com.mohammadreza.mrchat.model.EventBusModel;
import com.mohammadreza.mrchat.model.MessageRealmModel;
import com.mohammadreza.mrchat.model.MessagesModel;
import com.mohammadreza.mrchat.receiver.NetworkStateReceiver;
import com.mohammadreza.mrchat.repository.ChatRealmDatabase;
import com.mohammadreza.mrchat.service.MyService;
import com.mohammadreza.mrchat.utils.MyAlertDialog;
import com.mohammadreza.mrchat.utils.Utils;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import de.hdodenhof.circleimageview.CircleImageView;
import io.realm.RealmResults;

import static android.content.Context.MODE_PRIVATE;
import static com.mohammadreza.mrchat.receiver.NetworkStateReceiver.CONNECTED_TO_FIREBASE;
import static com.mohammadreza.mrchat.receiver.NetworkStateReceiver.CONNECTING;
import static com.mohammadreza.mrchat.receiver.NetworkStateReceiver.NO_NET;


public class MessageViewModel extends BaseObservable implements NetworkStateReceiver.NetworkStateReceiverListener, LifecycleObserver {

    public static String friendId = "";
    private final AppCompatImageView imgSendMessage;
    private SharedPreferences sharedPreferences;
    public NetworkStateReceiver networkStateReceiverMessage;
    private AppCompatActivity appCompatActivity;
    private String currentUserId = "";
    private String friendImageOriginalUrl = "";
    private String friendImageSmallUrl = "";
    private String txtMessage = "";
    private String setNetStateMessage = "";
    private long userStatus;
    private String friendUserName = "";
    private boolean isProgressLoadMessage;
    private boolean isTxtNotingMainChatVisibleMessage;
    private MutableLiveData<List<MessagesModel>> messageLiveData;
    private List<MessagesModel> messageViewModels;
    private List<MessagesModel> myMessageList;
    //    private List<MessagesModel> FriendMessage;
    private List<MessagesModel> myNotDeliverMessage;
    private boolean typingState;
    private boolean newState;

    private String netConnectionState = "";
    private int connectionState;
    private DatabaseReference chats;
    private DatabaseReference chatListReference;
    private long friendLastMessageId;

    private MyService myService;


    public ServiceConnection connection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            MyService.LocalBinder binder = (MyService.LocalBinder) service;
            myService = binder.getService();
            setMyService(binder.getService());
            myService.watchForReadMyLastMessage(getCurrentUserId(), getFriendId(), chatListReference);
            myService.getFriendIsTyping(getFriendId(), getCurrentUserId(), chatListReference);
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {

        }
    };
    private ScheduledExecutorService scheduleIsTyping;


    public MessageViewModel(AppCompatActivity appCompatActivity, ActivityMessageBinding messageBinding) {
        this.appCompatActivity = appCompatActivity;
        FirebaseDatabase baseReference = FirebaseDatabase.getInstance();
        chats = baseReference.getReference(ChatConstant.CHATS);
        chats.keepSynced(true);
        chatListReference = baseReference.getReference(ChatConstant.CHAT_LIST);
        chatListReference.keepSynced(true);
        messageLiveData = new MutableLiveData<>();
        messageViewModels = new ArrayList<>();
        myNotDeliverMessage = new ArrayList<>();
        myMessageList = new ArrayList<>();
        imgSendMessage = messageBinding.activityMessageFooterId.imgSendMessage;
        getIntentData();
        registerBroadCast();
        getDraftsMessage();
//        readNotDeliverMessage();
        checkFriendOnlineState();

        setProgressLoadMessage(true);
        readMyMessages(getCurrentUserId(), getFriendId());
        readFriendMessages(getCurrentUserId(), getFriendId());
        readFriendLastMessage(getCurrentUserId(), getFriendId());

        ProcessLifecycleOwner.get().getLifecycle().addObserver(this);
        checkEdtValueTypingState(messageBinding.activityMessageFooterId.edtEnterMessage);

    }

    public void readMyMessages(String currentId, String friendId) {
        chats.child(currentId).child(friendId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                myMessageList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    MessagesModel model = snapshot.getValue(MessagesModel.class);
                    assert model != null;
                    myMessageList.add(model);
                }

                if (dataSnapshot.getChildrenCount() > 0) {
                    if (myMessageList.size() == dataSnapshot.getChildrenCount()) {
                        chats.child(currentId).child(friendId).removeEventListener(this);
                        Log.d("REMOVE_LISTENER", "1");
                    }
                } else {
                    chats.child(currentId).child(friendId).removeEventListener(this);
                    Log.d("REMOVE_LISTENER", "2");
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }


    public void readFriendMessages(String currentId, String friendId) {
        chats.child(friendId).child(currentId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshots) {
                messageViewModels.clear();
                for (DataSnapshot snapshot : snapshots.getChildren()) {
                    MessagesModel model = snapshot.getValue(MessagesModel.class);
                    assert model != null;
                    messageViewModels.add(model);
                    if (model.getDeliveryState() != 2) {
                        snapshot.child(ChatConstant.DELIVERY_STATE_FIELD_NAME).getRef().setValue(2);
                    }
                }

                if (snapshots.getChildrenCount() > 0) {
                    if (messageViewModels.size() == snapshots.getChildrenCount()) {
                        chats.child(friendId).child(currentId).removeEventListener(this);
                        Log.d("REMOVE_LISTENER", "3");
                        messageViewModels.remove(messageViewModels.size() - 1);
                    }
                    setProgressLoadMessage(false);
                    setTxtNotingMessage(false);
                } else {
                    chats.child(friendId).child(currentId).removeEventListener(this);
                    Log.d("REMOVE_LISTENER", "4");
                    setProgressLoadMessage(false);
                    setTxtNotingMessage(true);
                }

                messageViewModels.addAll(myMessageList);
                messageLiveData.setValue(messageViewModels);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    // TODO
    private void readFriendLastMessage(String currentId, String friendId) {
        chatListReference.child(currentId).child(friendId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                try {
//                    Long id = dataSnapshot.child("id").getValue(Long.class);
                    MessagesModel messagesModel = dataSnapshot.getValue(MessagesModel.class);
                    if (messagesModel != null && !messagesModel.getSender().equals(currentId)) {
                        if (messagesModel.getId() != friendLastMessageId) {
                            friendLastMessageId = messagesModel.getId();
                            messageViewModels.add(messagesModel);
                            messageLiveData.setValue(messageViewModels);
                        }
                        dataSnapshot.child(ChatConstant.DELIVERY_STATE_FIELD_NAME).getRef().setValue(2);
                        changeLasMessageState(messagesModel, currentId, friendId);

                        if (messagesModel.getId() > getUserStatus()) {
                            setUserStatus(messagesModel.getId());
                            Log.d("USER_STATUs", "status user   " + getUserStatus());
                            Log.d("USER_STATUs", "status time   " + Calendar.getInstance().getTimeInMillis());
                        }

                        setProgressLoadMessage(false);
                        setTxtNotingMessage(false);
                    } else {
                        setProgressLoadMessage(false);
                        setTxtNotingMessage(true);
                    }
                } catch (Exception e) {
                }

                if (appCompatActivity.isFinishing()) {
                    chatListReference.child(currentId).child(friendId).removeEventListener(this);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }

    public void changeLasMessageState(MessagesModel model, String currentUserId, String friendId) {
        chats.child(friendId).child(currentUserId).child(String.valueOf(model.getId())).child(ChatConstant.DELIVERY_STATE_FIELD_NAME).setValue(2);
    }

    @BindingAdapter(value = {"app:friendUrl", "app:friendId", "appCompatActivity"})
    public static void setMessageProfileImage(final CircleImageView imageView,
                                              final String url, final String id, AppCompatActivity appCompatActivity) {
        Utils.loadImageFromStorage(id + imageView.getContext().getString(R.string.small), appCompatActivity, imageView, null);

        Glide.with(MyApp.appContext)
                .asBitmap()
                .load(url)
                .into(new CustomTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resources, @Nullable Transition<? super Bitmap> transition) {
                        imageView.setImageBitmap(resources);
                        Utils.saveToInternalStorage(id + imageView.getContext().getString(R.string.small), resources, imageView.getContext());
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {
                    }
                });
    }

    @BindingAdapter(value = {"app:recycler_message", "sender_id", "appCompatActivity", "my_service"})
    public static void setRecyclerMessageData(final RecyclerView recyclerView, final MutableLiveData<List<MessagesModel>> liveData, String senderId, AppCompatActivity appCompatActivity, MyService myService) {
        liveData.observe((LifecycleOwner) recyclerView.getContext(), messagesModels -> {
            if (messagesModels != null) {
                try {
                    Collections.sort(messagesModels, new MessageTimeCompare());
                } catch (Exception ignore) {
                }
                MessagesListAdapter adapter = new MessagesListAdapter(messagesModels, senderId, appCompatActivity, myService);
                recyclerView.setAdapter(adapter);
            }
        });


    }

    private void getIntentData() {
        Intent intent = appCompatActivity.getIntent();
        if (intent != null) {
            currentUserId = intent.getStringExtra(ChatConstant.CHAT_CURRENT_ID_INTENT_KEY);
            ContactsModel friendModel = (ContactsModel) intent.getSerializableExtra(ChatConstant.CHAT_USER_CLASS_INTENT_KEY);
            if (friendModel != null) {
                friendId = friendModel.getId();
                friendUserName = friendModel.getUserName();
                friendImageOriginalUrl = friendModel.getOriginalImageUri();
                friendImageSmallUrl = friendModel.getSmallImageUri();
                setUserStatus(friendModel.getStatus());

            }
        }
    }


    private void checkEdtValueTypingState(EditText edtEnterMessage) {
        edtEnterMessage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (!editable.toString().trim().isEmpty()) {
                    myTyping(false);
                } else {
                    myTyping(true);
                }
            }
        });
    }

    private void myTyping(boolean typeState) {
        if (newState != typeState && myService != null) {
//            if (Calendar.getInstance().getTimeInMillis() <= (getUserStatus() + ChatConstant.FIVE_M_TIME_SPAM)) {
            if (myMessageList.size() > 0) {
                myService.changeMyTypingState(newState, getCurrentUserId(), getFriendId(), chatListReference);
            }
//            }
        }
        newState = typeState;
    }


    public void getFriendIsTyping(String friendId, String currentUserId, DatabaseReference chatListReference) {
        chatListReference.child(friendId).child(currentUserId).child(ChatConstant.IS_TYPING).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                try {
                    boolean isTyping = snapshot.getValue(Boolean.class);
                    if (isTyping) {
                        setSetNetStateMessage(appCompatActivity.getString(R.string.is_typing));
                        setTypingState(true);
                        checkFriendTypingState();
                    } else {
                        setTypingState(false);
                        if (scheduleIsTyping != null) {
                            scheduleIsTyping.shutdown();
                        }
                        setSetNetStateMessage(netConnectionState);
                    }
                } catch (Exception e) {
                }
                if (appCompatActivity.isFinishing()) {
                    chatListReference.child(friendId).child(currentUserId).child(ChatConstant.IS_TYPING).removeEventListener(this);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void checkFriendTypingState() {
        scheduleIsTyping = Executors.newScheduledThreadPool(5);
        scheduleIsTyping.scheduleAtFixedRate(() -> {
            setSetNetStateMessage(netConnectionState);
            if (myService != null) {
                myService.changeFriendTypingState(getFriendId(), getCurrentUserId(), chatListReference);
            }
        }, 20, 20, TimeUnit.SECONDS);

    }

    private void checkFriendOnlineState() {
        ScheduledExecutorService scheduleIsOnline = Executors.newScheduledThreadPool(5);
        scheduleIsOnline.scheduleAtFixedRate(() -> {
            if (connectionState == 0) {
                setSetNetStateMessage(appCompatActivity.getResources().getString(R.string.waiting_for_network));
            } else if (connectionState == 1) {
                setSetNetStateMessage(appCompatActivity.getResources().getString(R.string.connecting));
            } else if (connectionState == 2) {
                setSetNetStateMessage(Utils.compareOnTowTime(Calendar.getInstance().getTimeInMillis(), getUserStatus(), appCompatActivity, ChatConstant.COME_FROM_MESSAGE));
            }
        }, 30, 40, TimeUnit.SECONDS);

    }


    public void OnFriendImgClick(View imageView) {
        CircleImageView circleImageView = (CircleImageView) imageView;
        if (circleImageView.getDrawable() != null) {
            MyAlertDialog.createFullScreenImageDialog(appCompatActivity, circleImageView.getDrawable(), getFriendImageOriginalUrl(), getFriendId());
        }

    }

    public void onClickHandlerMessage(View v) {
        if (v.getTag().equals(appCompatActivity.getResources().getString(R.string.imgBackMessage))) {
            Utils.hideKeyboard(appCompatActivity);
            appCompatActivity.finish();
        } else {
            if (!getTxtMessage().trim().isEmpty()) {
                if (!getCurrentUserId().isEmpty() && !getFriendId().isEmpty()) {
                    sendMessage(getCurrentUserId(), getFriendId(), getTxtMessage());
                }
            }

            setTxtMessage("");
        }
    }


    private void sendMessage(String sender, String receiver, String message) {
        MessagesModel messageModel = new MessagesModel();
        long messageId = System.currentTimeMillis();

        messageModel.setId(messageId);
        messageModel.setSender(sender);
        messageModel.setReceiver(receiver);
        messageModel.setMessage(message);

        messageModel.setDeliveryState(0);
        invisibleDetailToShow(false);

        if (receiver.equals(sender)) {
            return;
        }

        messageViewModels.add(messageModel);
        messageLiveData.setValue(messageViewModels);
//        ChatRealmDatabase.saveMessages(new MessageRealmModel(messageModel), appCompatActivity);

    }

    private void addFriendToChatList(String lastMessage, long messageTime) {
        ChatListModel model = new ChatListModel();
        model.setId(getFriendId());
        model.setUserName(getFriendUserName());
        model.setLastMessage(lastMessage);
        model.setLastMessageTime(messageTime);
        model.setStatus(getUserStatus());
        model.setOriginalImageUri(getFriendImageOriginalUrl());
        model.setSmallImageUri(getFriendImageSmallUrl());
        ChatRealmDatabase.addUsersSendMessageToChatDatabase(model, appCompatActivity, ChatConstant.COME_FROM_MESSAGE);

    }

//    private void readNotDeliverMessage() {
//        RealmResults<MessageRealmModel> messageRealmModels = ChatRealmDatabase.getMessages(appCompatActivity);
//        if (messageRealmModels != null && messageRealmModels.size() > 0) {
//            myNotDeliverMessage.clear();
//            for (MessageRealmModel realmModel : messageRealmModels) {
//                MessagesModel model = new MessagesModel(realmModel);
//                if ((model.getSender().equals(getCurrentUserId()) && model.getReceiver().equals(getFriendId()) && model.getDeliveryState() == 0)) {
//                    myNotDeliverMessage.add(model);
//                }
//            }
//            messageViewModels.addAll(myNotDeliverMessage);
//            messageLiveData.setValue(messageViewModels);
//
//            if (myNotDeliverMessage.size() > 0) {
//                invisibleDetailToShow(false);
//                setProgressLoadMessage(false);
//            } else {
//                invisibleDetailToShow(true);
//            }
//        }
//    }

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    public void onMessageCreate() {
        appCompatActivity.bindService(new Intent(appCompatActivity, MyService.class), connection, Context.BIND_AUTO_CREATE);
        getFriendIsTyping(getFriendId(), getCurrentUserId(), chatListReference);
    }


    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true, priority = 1)
    public void onMessageEvent(EventBusModel model) {
        if (model.isMessageActivityPaused()) {
            try {
                if (!getTxtMessage().trim().isEmpty()) {
                    addFriendToChatList(getTxtMessage(), Calendar.getInstance().getTimeInMillis());
                    saveOnDrafts(getTxtMessage());
                    Toast.makeText(appCompatActivity, appCompatActivity.getResources().getString(R.string.drats), Toast.LENGTH_SHORT).show();
                    chatListReference.child(currentUserId).child(friendId).child(ChatConstant.IS_TYPING).setValue(false);
                } else {
                    saveOnDrafts("");
                    addFriendToChatList(messageViewModels.get(messageViewModels.size() - 1).getMessage(), messageViewModels.get(messageViewModels.size() - 1).getId());
                }

                if (isTypingState()) {
                    chatListReference.child(friendId).child(currentUserId).child(ChatConstant.IS_TYPING).setValue(false);
                }
            } catch (Exception e) {
            }
        }
    }


    private void saveOnDrafts(String value) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString((ChatConstant.PREFER_FRIEND_ID + getFriendId()), value);
        editor.apply();
    }

    private void getDraftsMessage() {
        sharedPreferences = appCompatActivity.getSharedPreferences(appCompatActivity.getString(R.string.mr_chat), MODE_PRIVATE);
        String draftsMessage = sharedPreferences.getString((ChatConstant.PREFER_FRIEND_ID + getFriendId()), null);
        if (draftsMessage != null) {
            setTxtMessage(draftsMessage);

        }
    }

    private void invisibleDetailToShow(boolean txtNothing) {
        setTxtNotingMessage(txtNothing);
    }

    private void registerBroadCast() {
        networkStateReceiverMessage = new NetworkStateReceiver(appCompatActivity);
        networkStateReceiverMessage.addListener(this);
        appCompatActivity.registerReceiver(networkStateReceiverMessage, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
    }

    @Override
    public void firebaseConnectionState(int state) {
        switch (state) {
            case NO_NET:
                setSetNetStateMessage(appCompatActivity.getString(R.string.waiting_for_network));
                netConnectionState = appCompatActivity.getString(R.string.waiting_for_network);
                connectionState = 0;
                break;
            case CONNECTING:
                setSetNetStateMessage(appCompatActivity.getString(R.string.connecting));
                netConnectionState = (appCompatActivity.getString(R.string.connecting));
                connectionState = 1;
                break;

            case CONNECTED_TO_FIREBASE:
                getUserOnlineStatus();
                connectionState = 2;
                break;

        }
    }

    public void onFabMessageClick(RecyclerView recyclerView) {
        if (messageViewModels.size() < 1)
            return;
        recyclerView.scrollToPosition(messageViewModels.size() - 1);
    }

    public void getUserOnlineStatus() {
        try {
            setSetNetStateMessage(Utils.compareOnTowTime(Calendar.getInstance().getTimeInMillis(), getUserStatus(), appCompatActivity, ChatConstant.COME_FROM_MESSAGE));
            netConnectionState = getSetNetStateMessage();
        } catch (Exception ignore) {
            setSetNetStateMessage(appCompatActivity.getResources().getString(R.string.last_seen_mr));
            netConnectionState = appCompatActivity.getResources().getString(R.string.last_seen_mr);

        }
    }


    public MutableLiveData<List<MessagesModel>> getMessageLiveData() {
        return messageLiveData;
    }


    @Bindable
    public AppCompatActivity getAppCompatActivity() {
        return appCompatActivity;
    }

    public void setAppCompatActivity(AppCompatActivity appCompatActivity) {
        this.appCompatActivity = appCompatActivity;
    }

    public String getFriendImageOriginalUrl() {
        return friendImageOriginalUrl;
    }

    @Bindable
    public String getSetNetStateMessage() {
        return setNetStateMessage;
    }

    public void setSetNetStateMessage(String setNetStateMessage) {
        this.setNetStateMessage = setNetStateMessage;
        notifyPropertyChanged(BR.setNetStateMessage);
    }

    @Bindable
    public String getFriendImageSmallUrl() {
        return friendImageSmallUrl;
    }


    @Bindable
    public String getFriendUserName() {
        return friendUserName;
    }

    @Bindable
    public String getCurrentUserId() {
        return currentUserId;
    }

    @Bindable
    public String getFriendId() {
        return friendId;
    }

    @Bindable
    public String getTxtMessage() {
        if (txtMessage.trim().isEmpty()) {
            imgSendMessage.setImageResource(R.drawable.ic_send_message_off);
        } else {
            imgSendMessage.setImageResource(R.drawable.ic_send_message_on);
        }
        return txtMessage;
    }

    public void setTxtMessage(String txtMessage) {
        this.txtMessage = txtMessage;
        notifyPropertyChanged(BR.txtMessage);
    }

    @Bindable
    public MyService getMyService() {
        return myService;
    }

    public void setMyService(MyService myService) {
        this.myService = myService;
        notifyPropertyChanged(BR.myService);
    }

    @Bindable
    public long getUserStatus() {
        return userStatus;
    }

    public void setUserStatus(long userStatus) {
        this.userStatus = userStatus;
    }

    @Bindable
    public boolean isProgressLoadMessage() {
        return isProgressLoadMessage;
    }

    public void setProgressLoadMessage(boolean progressLoadMessage) {
        isProgressLoadMessage = progressLoadMessage;
        notifyPropertyChanged(BR.progressLoadMessage);
    }

    @Bindable
    public boolean isTxtNotingMainChatVisibleMessage() {
        return isTxtNotingMainChatVisibleMessage;
    }


    public void setTxtNotingMessage(boolean txtNotingMainChatVisibleMessage) {
        isTxtNotingMainChatVisibleMessage = txtNotingMainChatVisibleMessage;
        notifyPropertyChanged(BR.txtNotingMainChatVisibleMessage);
    }

    public static class MessageTimeCompare implements Comparator<MessagesModel> {
        public int compare(@NonNull MessagesModel left, @NonNull MessagesModel right) {
            return left.getId() > right.getId() ? 1 : -1;
        }

    }

    @Bindable
    public boolean isTypingState() {
        return typingState;
    }

    public void setTypingState(boolean typingState) {
        this.typingState = typingState;
        notifyPropertyChanged(BR.typingState);
    }

}
