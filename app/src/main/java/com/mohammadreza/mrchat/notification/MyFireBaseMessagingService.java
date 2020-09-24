package com.mohammadreza.mrchat.notification;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.mohammadreza.mrchat.R;
import com.mohammadreza.mrchat.constant.ChatConstant;
import com.mohammadreza.mrchat.model.ContactsModel;
import com.mohammadreza.mrchat.model.EventBusModel;
import com.mohammadreza.mrchat.repository.ChatRealmDatabase;
import com.mohammadreza.mrchat.ui.ChatMainActivity;

import org.greenrobot.eventbus.EventBus;

import java.util.Calendar;

import static com.mohammadreza.mrchat.ui.MessageActivity.isMessageActivityRunning;
import static com.mohammadreza.mrchat.viewmodel.MessageViewModel.friendId;

public class MyFireBaseMessagingService extends FirebaseMessagingService {


    private static long lastNotifyTime;

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        try {
            ContactsModel user = ChatRealmDatabase.checkUserExist(ChatConstant.IS_ONLINE_FIELD_NAME, ChatConstant.KEEP_ONLINE, this);
            if (user != null) {
                String friendId = remoteMessage.getData().get(this.getString(R.string.not_sender));
                String messageId = remoteMessage.getData().get(this.getString(R.string.not_messageId));
                String messageBody = remoteMessage.getData().get(this.getString(R.string.not_message));
                assert messageId != null;
                EventBus.getDefault().post(new EventBusModel(messageId, messageBody, user.getId(), friendId));

                if (Calendar.getInstance().getTimeInMillis() > lastNotifyTime + 4000) {
                    lastNotifyTime = Calendar.getInstance().getTimeInMillis();
                    showNotification(friendId, messageBody, user, MyFireBaseMessagingService.this);
                }

            }
        } catch (
                Exception e) {
        }

    }


    private void showNotification(String title, String message, ContactsModel user, Context context) {
        if (isMessageActivityRunning && friendId != null && !friendId.isEmpty() && friendId.equals(title)) {
            return;
        }
        Intent intent = new Intent(context, ChatMainActivity.class);
        intent.putExtra(ChatConstant.CHAT_USER_CLASS_INTENT_KEY, user);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        String CHANNEL_ID = "channel_name";// The id of the channel.
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(message)
                .setAutoCancel(true)
                .setPriority(Notification.PRIORITY_HIGH)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setContentIntent(pendingIntent);


        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Channel Name";// The user-visible name of the channel.
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel mChannel = new NotificationChannel(CHANNEL_ID, name, importance);
            assert notificationManager != null;
            notificationManager.createNotificationChannel(mChannel);
        }
        assert notificationManager != null;
        notificationManager.notify(0, notificationBuilder.build()); // 0 is the request code, it should be unique id
    }

}
