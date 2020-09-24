package com.mohammadreza.mrchat.adapter;

import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.mohammadreza.mrchat.R;
import com.mohammadreza.mrchat.constant.ChatConstant;
import com.mohammadreza.mrchat.model.MessagesModel;
import com.mohammadreza.mrchat.service.MyService;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MessagesListAdapter extends RecyclerView.Adapter<MessagesListAdapter.MessagesModelViewHolder> {

    private static final int MSG_TYPE_RECEIVER = 0;
    private static final int MSG_TYPE_SENDER = 1;
    private final String senderId;
    private AppCompatActivity appCompatActivity;
    private List<MessagesModel> messageList;
    private LayoutInflater inflater;
    private MyService mService;

    public MessagesListAdapter(List<MessagesModel> messageList, String senderId, AppCompatActivity appCompatActivity, MyService mService) {
        this.messageList = messageList;
        this.senderId = senderId;
        this.appCompatActivity = appCompatActivity;
        this.mService = mService;
//        if (mService == null) {
//            this.mService = new MyService();
//        }
    }


    @NonNull
    @Override
    public MessagesModelViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int type) {
        View view;

        if (inflater == null)
            inflater = LayoutInflater.from(parent.getContext());

        if (type == MSG_TYPE_SENDER) {
            view = inflater.inflate(R.layout.message_items_sender, parent, false);
        } else {
            view = inflater.inflate(R.layout.message_items_receiver, parent, false);
        }
        return new MessagesModelViewHolder(view);

    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onBindViewHolder(@NonNull MessagesModelViewHolder holder, int position) {
        MessagesModel messagesModel = messageList.get(position);
        setMessageState(holder.imgMessageState, messagesModel, holder);

        holder.txtMessageContent.setText(messagesModel.getMessage());
        holder.txtMessageTime.setText(getTimeHour(messagesModel.getId()));
        try {
            MessagesModel nextModel = messageList.get(position - 1);

            new Thread(() -> {
                if (compareOnTowTime(messagesModel.getId(), nextModel.getId()) && position != 0) {
                    appCompatActivity.runOnUiThread(() -> holder.txtMessageDayTime.setVisibility(View.GONE));
                } else {
                    appCompatActivity.runOnUiThread(() -> {
                        holder.txtMessageDayTime.setVisibility(View.VISIBLE);
                        holder.txtMessageDayTime.setText(getTimeDay(messagesModel.getId()));
                    });
                }

            }).start();
        } catch (Exception e) {
            if (position == 0) {
                holder.txtMessageDayTime.setVisibility(View.VISIBLE);
                holder.txtMessageDayTime.setText(getTimeDay(messagesModel.getId()));
            }
            e.getMessage();
        }

    }


    @Override
    public int getItemCount() {
        return messageList.size();
    }


    @Override
    public int getItemViewType(int position) {
        if (messageList.get(position).getSender().equals(senderId)) {
            return MSG_TYPE_SENDER;
        } else {
            return MSG_TYPE_RECEIVER;
        }
    }

    private void setMessageState(ImageView imgMessageState, MessagesModel messagesModel, MessagesModelViewHolder holder) {
        if (imgMessageState != null) {
            if (messagesModel.getDeliveryState() == 0) {
                imgMessageState.setImageResource(R.drawable.ic_clock);
                if (mService != null) {
                    mService.deliverMessages(messagesModel, imgMessageState, appCompatActivity);
                }
            } else if (messagesModel.getDeliveryState() == 1) {
                imgMessageState.setImageResource(R.drawable.ic_deliver_message);
            } else if (messagesModel.getDeliveryState() == 2) {
                imgMessageState.setImageResource(R.drawable.ic_seen_message);
            }
            seenMyMessage(holder.imgMessageState, messagesModel);
        }

    }

    private void seenMyMessage(ImageView imgMessageState, MessagesModel messagesModel) {
        if (mService != null && mService.myMessageSeen != null) {
            mService.myMessageSeen.observe(appCompatActivity, isReadMessage -> {
                if (isReadMessage != null && isReadMessage == 2 && messagesModel.getDeliveryState() != 0) {
                    messagesModel.setDeliveryState(isReadMessage);
                    imgMessageState.setImageResource(R.drawable.ic_seen_message);
                }
            });
        }
    }


    public class MessagesModelViewHolder extends RecyclerView.ViewHolder {
        private TextView txtMessageDayTime;
        private TextView txtMessageContent;
        private ImageView imgMessageState;
        private TextView txtMessageTime;

        private MessagesModelViewHolder(View view) {
            super(view);
            txtMessageContent = view.findViewById(R.id.txtMessageContent);
            imgMessageState = view.findViewById(R.id.imgMessageState);
            txtMessageTime = view.findViewById(R.id.txtMessageTime);
            txtMessageDayTime = view.findViewById(R.id.txtMessageDayTime);
        }


    }

    private boolean compareOnTowTime(long timeCurrent, long nextTime) {
        Calendar current = Calendar.getInstance();
        current.setTime(new Date(timeCurrent));

        int currentDay = current.get(Calendar.DAY_OF_MONTH);
        int currentMonth = current.get(Calendar.MONTH);
        int currentYear = current.get(Calendar.YEAR);

        current.setTime(new Date(nextTime));

        int nextDay = current.get(Calendar.DAY_OF_MONTH);
        int nextMonth = current.get(Calendar.MONTH);
        int nextYear = current.get(Calendar.YEAR);

        return nextYear == currentYear && nextMonth == currentMonth && nextDay == currentDay;
    }


    private String getTimeDay(long date) {
        Calendar now = Calendar.getInstance();
        now.setTime(new Date(date));
        int day = now.get(Calendar.DAY_OF_MONTH);
        String month = String.format(Locale.US, ChatConstant.TIME_FORMAT, now);
        return day + "-" + month;
    }

    private String getTimeHour(long date) {
        Calendar now = Calendar.getInstance();
        now.setTime(new Date(date));
        int hour = now.get(Calendar.HOUR_OF_DAY);
        String minute = "";
        if (now.get(Calendar.MINUTE) < 10) {
            minute = "0" + now.get(Calendar.MINUTE);
        } else {
            minute = String.valueOf(now.get(Calendar.MINUTE));
        }
        return hour + ":" + minute;
    }

//    @Override
//    public long getItemId(int position) {
//        return messageList.get(position).hashCode();
//    }
}