package com.mohammadreza.mrchat.utils;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.mohammadreza.mrchat.constant.ChatConstant;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import static com.mohammadreza.mrchat.constant.ChatConstant.TIME_INTERVAL;

public class UserOnlineState {
    private final Timer timer;
    private final DatabaseReference reference;
    private String id = "";


    public UserOnlineState(String id) {
        this.id = id;
        reference = FirebaseDatabase.getInstance().getReference(ChatConstant.USERS).child(id);
        timer = new Timer();
        sendUserStateEveryThirtySecond();
    }

    private void sendUserStateEveryThirtySecond() {
        TimerTask hourlyTask = new TimerTask() {
            @Override
            public void run() {
                Calendar utcCurrentTime = Calendar.getInstance();
                new Thread(() -> setStatus(utcCurrentTime.getTimeInMillis())).start();
            }
        };
        timer.schedule(hourlyTask, 1, TIME_INTERVAL);
    }

    public void setStatus(long status) {
        if (!id.isEmpty()) {
            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put(ChatConstant.STATS_FIELD_NAME, (status + TIME_INTERVAL));
            reference.updateChildren(hashMap);
        }
    }


}
