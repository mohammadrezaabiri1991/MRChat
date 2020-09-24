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
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.lifecycle.MutableLiveData;
import androidx.recyclerview.widget.RecyclerView;

import com.mohammadreza.mrchat.R;
import com.mohammadreza.mrchat.constant.ChatConstant;
import com.mohammadreza.mrchat.model.ChatListModel;
import com.mohammadreza.mrchat.model.ContactsModel;
import com.mohammadreza.mrchat.ui.MessageActivity;
import com.mohammadreza.mrchat.utils.MyAlertDialog;
import com.mohammadreza.mrchat.utils.Utils;

import java.util.Calendar;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.mohammadreza.mrchat.viewmodel.ChatMainViewModel.comFromChatMain;

public class ChatUserListAdapter extends RecyclerView.Adapter<ChatUserListAdapter.FeedModelViewHolder> {

    private final AppCompatActivity appCompatActivity;
    private final int isMoonClick;
    private String status = "";
    private LayoutInflater inflater;
    private List<ChatListModel> friendsList;
    private MutableLiveData<ChatListModel> chatLisLive;
    private String currentUserId = "";

    public ChatUserListAdapter(List<ChatListModel> items, AppCompatActivity appCompatActivity, String currentUserId, String status, int isMoonClick, MutableLiveData<ChatListModel> chatListLive) {
        friendsList = items;
        this.appCompatActivity = appCompatActivity;
        this.currentUserId = currentUserId;
        this.status = status;
        this.isMoonClick = isMoonClick;
        this.chatLisLive = chatListLive;
    }

    @NonNull
    @Override
    public FeedModelViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int type) {
        if (inflater == null)
            inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.chat_contact_item, parent, false);
        return new FeedModelViewHolder(view);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onBindViewHolder(@NonNull FeedModelViewHolder holder, int position) {
        final ChatListModel item = friendsList.get(position);
        try {
            Utils.loadImageFromStorage((item.getId() + appCompatActivity.getString(R.string.small)), appCompatActivity, holder.userProfile, null);
            if (!item.getSmallImageUri().isEmpty()) {
                Utils.actionDownloadAndSaveImage(holder.userProfile, item.getId(), item.getSmallImageUri(), appCompatActivity);
            } else {
                holder.userProfile.setImageDrawable(null);
                Utils.removeFromStorage(item.getId(), appCompatActivity);
            }


            holder.parentChatUserItem.setOnClickListener(v -> Utils.callIntent(appCompatActivity, MessageActivity.class, new ContactsModel(item), currentUserId));

            holder.txtUserFamilyChatUser.setText(item.getUserName());

            holder.txtUserImageNameChatUser.setText((String.valueOf(item.getUserName().charAt(0))));

            if (!item.getLastMessage().isEmpty()) {
                holder.txtLastMessage.setText(item.getLastMessage());
                holder.txtLastMsgDate.setText(Utils.compareOnTowTime(Calendar.getInstance().getTimeInMillis(), item.getLastMessageTime(), appCompatActivity, ""));
            } else {
                holder.txtLastMessage.setText(appCompatActivity.getResources().getString(R.string.noMessages));
            }


            holder.userProfile.setOnClickListener(v -> {
                if (holder.userProfile.getDrawable() != null) {
                    MyAlertDialog.createFullScreenImageDialog(appCompatActivity, holder.userProfile.getDrawable(), item.getOriginalImageUri(), item.getId());
                }
            });

            if (status.contentEquals(appCompatActivity.getString(R.string.mr_chat))) {
                holder.imgStatusOnlineChatUser.setVisibility(View.VISIBLE);
            } else {
                holder.imgStatusOnlineChatUser.setVisibility(View.INVISIBLE);
            }

            calculateUSerState(item.getStatus(), holder.imgStatusOnlineChatUser);
        } catch (Exception e) {
        }
        setLiveValue(item, holder);
    }

    private void setLiveValue(ChatListModel item, FeedModelViewHolder holder) {
        chatLisLive.observe(appCompatActivity, chatListModel -> {
            if (chatListModel != null && item.getId().equals(chatListModel.getId())) {
                if (!chatListModel.getSmallImageUri().isEmpty()) {
                    if (!item.getSmallImageUri().equals(chatListModel.getSmallImageUri())) {
                        Utils.actionDownloadAndSaveImage(holder.userProfile, item.getId(), item.getSmallImageUri(), appCompatActivity);
                    }
                } else {
                    holder.userProfile.setImageDrawable(null);
                    Utils.removeFromStorage(item.getId(), appCompatActivity);
                }
                item.setSmallImageUri(chatListModel.getSmallImageUri());
                item.setOriginalImageUri(chatListModel.getOriginalImageUri());

                if (comFromChatMain != 1) {
                    item.setLastMessageTime(chatListModel.getLastMessageTime());
                    item.setLastMessage(chatListModel.getLastMessage());
                    holder.txtLastMessage.setText(chatListModel.getLastMessage());
                    holder.txtLastMsgDate.setText(Utils.compareOnTowTime(Calendar.getInstance().getTimeInMillis(), chatListModel.getLastMessageTime(), appCompatActivity, ""));
                }

                item.setStatus(chatListModel.getStatus());
                calculateUSerState(item.getStatus(), holder.imgStatusOnlineChatUser);
            }
        });
    }


    private void calculateUSerState(long status, ImageView imgStatusOnlineChatUser) {
        if ((Calendar.getInstance().getTime().getTime() - ChatConstant.TIME_INTERVAL_TO_GET_STATES) <= status) {
            imgStatusOnlineChatUser.setImageResource(R.drawable.full_round_back);
        } else {
            imgStatusOnlineChatUser.setImageResource(R.drawable.round_back_dark);
        }
    }


    @Override
    public int getItemCount() {
        return friendsList.size();
    }

    public class FeedModelViewHolder extends RecyclerView.ViewHolder {
        View viewLineContact;
        ConstraintLayout parentChatUserItem;
        TextView txtUserImageNameChatUser;
        CircleImageView userProfile;
        ImageView imgStatusOnlineChatUser;
        TextView txtUserFamilyChatUser;
        TextView txtLastMessage;
        TextView txtLastMsgDate;

        private FeedModelViewHolder(View view) {
            super(view);
            parentChatUserItem = view.findViewById(R.id.parentContactItem);
            txtUserImageNameChatUser = view.findViewById(R.id.txtUserImageNameContactItem);
            userProfile = view.findViewById(R.id.imgProfileContactItem);
            imgStatusOnlineChatUser = view.findViewById(R.id.imgStatusOnlineContactItem);
            txtUserFamilyChatUser = view.findViewById(R.id.txtUserFamilyContactItem);
            txtLastMessage = view.findViewById(R.id.txtLast_mOrPhone_nContactItem);
            txtLastMsgDate = view.findViewById(R.id.txtLastMsgDate);
            viewLineContact = view.findViewById(R.id.viewLineContact);

            if (isMoonClick == 1) {
                setDummyThemeToItems(appCompatActivity.getResources().getColor(R.color.colorBackground), appCompatActivity.getResources().getColor(R.color.colorText), appCompatActivity.getResources().getColor(R.color.colorLineLight));
            } else if (isMoonClick == 2) {
                setDummyThemeToItems(appCompatActivity.getResources().getColor(R.color.darkColorAccent), appCompatActivity.getResources().getColor(R.color.drawerIconTextColor), appCompatActivity.getResources().getColor(R.color.colorLineDark));

            }

        }

        private void setDummyThemeToItems(int colorParent, int colorText, int colorLine) {
            parentChatUserItem.setBackgroundColor(colorParent);
            txtUserFamilyChatUser.setTextColor(colorText);
            txtLastMessage.setTextColor(colorText);
            txtLastMsgDate.setTextColor(colorText);
            viewLineContact.setBackgroundColor(colorLine);
        }
    }


}