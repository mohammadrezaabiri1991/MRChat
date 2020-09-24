package com.mohammadreza.mrchat.repository;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.mohammadreza.mrchat.constant.ChatConstant;
import com.mohammadreza.mrchat.model.ChatListModel;
import com.mohammadreza.mrchat.model.ChatListRealmModel;
import com.mohammadreza.mrchat.model.ContactsModel;
import com.mohammadreza.mrchat.model.ContactsRealmModel;
import com.mohammadreza.mrchat.model.EventBusModel;
import com.mohammadreza.mrchat.model.MessageRealmModel;
import com.mohammadreza.mrchat.ui.PhoneActivity;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;


public class ChatRealmDatabase {

    private static RealmConfiguration configContactsDatabase(Context context) {
        Realm.init(context);
        return new RealmConfiguration.Builder()
                .name(ChatConstant.CHAT_CONTACTS_DATABASE_NAME)
                .schemaVersion(1)
                .deleteRealmIfMigrationNeeded()
                .build();
    }

//    private static RealmConfiguration configMessageDatabase(Context context) {
//        Realm.init(context);
//        return new RealmConfiguration.Builder()
//                .name(ChatConstant.MESSAGE_DATABASE_NAME)
//                .schemaVersion(1)
//                .deleteRealmIfMigrationNeeded()
//                .build();
//    }

    private static RealmConfiguration configHowSendMessageDatabase(Context context) {
        Realm.init(context);
        return new RealmConfiguration.Builder()
                .name(ChatConstant.USERS_SEND_MESSAGE_DATABASE_NAME)
                .schemaVersion(1)
                .deleteRealmIfMigrationNeeded()
                .build();
    }


    public static void addNewUserToChatDatabase(ContactsRealmModel user, Context context) {
        Realm mRealm = Realm.getInstance(configContactsDatabase(context));
        mRealm.beginTransaction();
        mRealm.copyToRealmOrUpdate(user);
        mRealm.commitTransaction();
        mRealm.close();

    }

    public static void addUsersSendMessageToChatDatabase(final ChatListModel user, Context context, String comeFrom) {
//        new Thread(() -> {
        Realm mRealm = Realm.getInstance(configHowSendMessageDatabase(context));
        if (!mRealm.isInTransaction()) {
            mRealm.beginTransaction();
        }
        ChatListRealmModel realmModel = mRealm.where(ChatListRealmModel.class).equalTo(ChatConstant.TABLE_ID_FIELD_NAME, user.getId()).findFirst();

        if (comeFrom.equals(ChatConstant.COME_FROM_MESSAGE)) {
            actionComFromMessage(realmModel, user);
        } else {
            if (realmModel != null) {
                if (user.getStatus() < realmModel.getStatus()) {
                    user.setStatus(realmModel.getStatus());
                }
                if (user.getLastMessageTime() < realmModel.getLatMessageTime()) {
                    user.setLastMessageTime(realmModel.getLatMessageTime());
                    user.setLastMessage(realmModel.getLastMessage());
                }
            }
        }
        mRealm.copyToRealmOrUpdate(new ChatListRealmModel(user));
        mRealm.commitTransaction();
        mRealm.close();
//        }).start();
    }


    private static void actionComFromMessage(ChatListRealmModel realmModel, ChatListModel user) {
        if (realmModel != null) {
            if (user.getStatus() > realmModel.getStatus() || user.getLastMessageTime() > realmModel.getLatMessageTime() || !user.getLastMessage().equals(realmModel.getLastMessage())) {
                EventBus.getDefault().post(new EventBusModel(user));
            }
        } else {
            EventBus.getDefault().post(new EventBusModel(user));
        }
    }


//    public static void saveMessages(MessageRealmModel model, Context context) {
//        Realm mRealm = Realm.getInstance(configMessageDatabase(context));
//        mRealm.beginTransaction();
//        mRealm.copyToRealmOrUpdate(model);
//        mRealm.commitTransaction();
//        mRealm.close();
//    }

//    public static void deleteDeliverMessage(String fieldName, long messageId, Context context) {
//        Realm mRealm = Realm.getInstance(configMessageDatabase(context));
//        mRealm.beginTransaction();
//        MessageRealmModel messageRealmModel = mRealm.where(MessageRealmModel.class).equalTo(fieldName, messageId).findFirst();
//        if (messageRealmModel != null) {
//            messageRealmModel.deleteFromRealm();
//        }
//        mRealm.commitTransaction();
//        mRealm.close();
//    }


    public static List<ContactsRealmModel> getUsers(Context context) {
        Realm mRealm = Realm.getInstance(configContactsDatabase(context));
        mRealm.beginTransaction();
        RealmResults<ContactsRealmModel> contacts = mRealm.where(ContactsRealmModel.class).findAll();
        List<ContactsRealmModel> contactsRealmModels = mRealm.copyFromRealm(contacts);
        mRealm.commitTransaction();
        mRealm.close();
        return contactsRealmModels;
    }

    public static List<ChatListRealmModel> getUsersSendMessage(Context context) {
        Realm mRealm = Realm.getInstance(configHowSendMessageDatabase(context));
        if (!mRealm.isInTransaction()) {
            mRealm.beginTransaction();
        }
        RealmResults<ChatListRealmModel> realmUser = mRealm.where(ChatListRealmModel.class).findAll();
        List<ChatListRealmModel> chatListModels = mRealm.copyFromRealm(realmUser);
        mRealm.commitTransaction();
        mRealm.close();
        return chatListModels;
    }


//    public static RealmResults<MessageRealmModel> getMessages(Context context) {
//        Realm mRealm = Realm.getInstance(configMessageDatabase(context));
//        mRealm.beginTransaction();
//        RealmResults<MessageRealmModel> realmUser = mRealm.where(MessageRealmModel.class).findAll();
//        mRealm.commitTransaction();
//        return realmUser;
//
//    }


    public static void removeAllDatabases(AppCompatActivity appCompatActivity) {
        try {

//            Realm mRealms = Realm.getInstance(configMessageDatabase(appCompatActivity));
//            mRealms.beginTransaction();
//            mRealms.delete(ChatListRealmModel.class);
//            mRealms.deleteAll();
//            mRealms.commitTransaction();
//            mRealms.close();

            Realm cRealm = Realm.getInstance(configContactsDatabase(appCompatActivity));
            cRealm.beginTransaction();
//            cRealm.delete(ContactsRealmModel.class);
            cRealm.deleteAll();
            cRealm.commitTransaction();
            cRealm.close();

//            Realm hRealms = Realm.getInstance(configHowSendMessageDatabase(appCompatActivity));
//            hRealms.beginTransaction();
//            hRealms.delete(ChatListRealmModel.class);
//            hRealms.deleteAll();
//            hRealms.commitTransaction();
//            hRealms.close();

            FirebaseAuth.getInstance().signOut();
            appCompatActivity.startActivity(new Intent(appCompatActivity, PhoneActivity.class));
            appCompatActivity.finish();


        } catch (Exception ignore) {
        }
    }


    public static ContactsModel checkUserExist(String fieldName, int requestValue, Context context) {
        Realm mRealm = Realm.getInstance(configContactsDatabase(context));
        try {
            mRealm.beginTransaction();
            ContactsRealmModel realmUser = mRealm.where(ContactsRealmModel.class).equalTo(fieldName, requestValue).findFirst();
            mRealm.commitTransaction();
            if (realmUser != null) {
                return new ContactsModel(realmUser);
            } else {
                return null;
            }
        } finally {
            mRealm.close();
        }
    }

    public static ContactsModel checkUserExist(String fieldName, String requestValue, Context context) {
        Realm mRealm = Realm.getInstance(configContactsDatabase(context));
        try {
            mRealm.beginTransaction();
            ContactsRealmModel realmUser = mRealm.where(ContactsRealmModel.class).equalTo(fieldName, requestValue).findFirst();
            mRealm.commitTransaction();
            if (realmUser != null) {
                return new ContactsModel(realmUser);
            } else {
                return null;
            }
        } finally {
            mRealm.close();

        }
    }


    public static void changeImageUrl(String filedName, String requestValue, String original, String small, Context context) {
        Realm mRealm = Realm.getInstance(configContactsDatabase(context));
        mRealm.beginTransaction();
        ContactsRealmModel toEdit = mRealm.where(ContactsRealmModel.class).equalTo(filedName, requestValue).findFirst();
        if (toEdit != null) {
            toEdit.setOriginalImageUrl(original);
            toEdit.setSmallImageUrl(small);
        }
        mRealm.commitTransaction();
        mRealm.close();
    }

}
