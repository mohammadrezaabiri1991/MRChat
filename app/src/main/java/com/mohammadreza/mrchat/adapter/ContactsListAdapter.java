package com.mohammadreza.mrchat.adapter;

import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.mohammadreza.mrchat.R;
import com.mohammadreza.mrchat.constant.ChatConstant;
import com.mohammadreza.mrchat.model.ContactsModel;
import com.mohammadreza.mrchat.ui.MessageActivity;
import com.mohammadreza.mrchat.utils.MyAlertDialog;
import com.mohammadreza.mrchat.utils.Utils;

import java.util.Calendar;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ContactsListAdapter extends RecyclerView.Adapter<ContactsListAdapter.FeedModelViewHolder> {

    private final AppCompatActivity appCompatActivity;
    private String currentUserId = "";
    private LayoutInflater inflater;
    private List<ContactsModel> contactList;
    private String netState = "";

    public ContactsListAdapter(List<ContactsModel> contactList, AppCompatActivity appCompatActivity, String currentUserId, String netState) {
        this.appCompatActivity = appCompatActivity;
        this.currentUserId = currentUserId;
        this.contactList = contactList;
        this.netState = netState;
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
    public void onBindViewHolder(FeedModelViewHolder holder, int position) {
        final ContactsModel item = contactList.get(position);

        Utils.loadImageFromStorage((item.getId() + appCompatActivity.getString(R.string.small)), appCompatActivity, holder.imgFriendProfile, null);

        if (!item.getSmallImageUri().isEmpty()) {
            Utils.actionDownloadAndSaveImage(holder.imgFriendProfile, item.getId(), item.getSmallImageUri(), appCompatActivity);
        } else {
            holder.imgFriendProfile.setImageDrawable(null);
            Utils.removeFromStorage(item.getId(), appCompatActivity);
        }


        try {
            holder.txtUserFamily.setText(item.getUserName());

            holder.txtNumberContact.setText(separateNumber(item.getId()));

            holder.txtUserImageNameContact.setText((String.valueOf(item.getUserName().charAt(0))));
        } catch (Exception ignore) {
            holder.txtNumberContact.setText(item.getId());
        }

        holder.itemParentContacts.setOnClickListener(v -> {
            Utils.callIntent(appCompatActivity, MessageActivity.class, item, currentUserId);
            appCompatActivity.finish();
        });


        if (netState.contentEquals(appCompatActivity.getResources().getString(R.string.new_message))) {
            holder.imgStatusOnlineContactItem.setVisibility(View.VISIBLE);
        } else {
            holder.imgStatusOnlineContactItem.setVisibility(View.INVISIBLE);
        }
        calculateUSerState(item.getStatus(), holder.imgStatusOnlineContactItem);

        holder.imgFriendProfile.setOnClickListener(v -> {
            if (holder.imgFriendProfile.getDrawable() != null) {
                MyAlertDialog.createFullScreenImageDialog(appCompatActivity, holder.imgFriendProfile.getDrawable(), item.getOriginalImageUri(), item.getId());
            }
        });
    }

    private String separateNumber(String id) {
        String s1 = id.substring(0, 3);
        String s2 = id.substring(3, 6);
        String s3 = id.substring(6, 9);
        String s4 = id.substring(9, 13);
        return s1 + " " + s2 + " " + s3 + " " + s4;
    }

    private void calculateUSerState(long status, ImageView imgStatusOnlineChatUser) {
        if ((Calendar.getInstance().getTime().getTime() - ChatConstant.THIRTY_SECOND_TIME_SPAM) < status) {
            imgStatusOnlineChatUser.setImageResource(R.drawable.full_round_back);
        } else {
            imgStatusOnlineChatUser.setImageResource(R.drawable.round_back_dark);
        }
    }

    @Override
    public int getItemCount() {
        return contactList.size();
    }

    public static class FeedModelViewHolder extends RecyclerView.ViewHolder {
        private ConstraintLayout itemParentContacts;
        private TextView txtUserImageNameContact;
        private CircleImageView imgFriendProfile;
        private ImageView imgStatusOnlineContactItem;
        private TextView txtUserFamily;
        private TextView txtNumberContact;

        private FeedModelViewHolder(View view) {
            super(view);
            itemParentContacts = view.findViewById(R.id.parentContactItem);
            txtUserImageNameContact = view.findViewById(R.id.txtUserImageNameContactItem);
            imgFriendProfile = view.findViewById(R.id.imgProfileContactItem);
            imgStatusOnlineContactItem = view.findViewById(R.id.imgStatusOnlineContactItem);
            txtUserFamily = view.findViewById(R.id.txtUserFamilyContactItem);
            txtNumberContact = view.findViewById(R.id.txtLast_mOrPhone_nContactItem);

        }
    }

}