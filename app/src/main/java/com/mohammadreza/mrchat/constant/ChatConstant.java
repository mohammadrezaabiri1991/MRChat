package com.mohammadreza.mrchat.constant;

public class ChatConstant {
    public static final int GALLERY_REQUEST = 103;
    public static final int CAMERA_PER_REQUEST = 210;
    public static final int SMS_REQ_CODE = 300;
    public static final int EX_STORAGE_REQ_CODE = 400;
    public static final long SCALE_ANIM_DURATION = 500;
    public static final int PICK_CONTACT_CODE = 600;
    public static final int CONTACT_PER_CODE = 660;


    //    ------------------------------ crop Constants
    public static final int DEFAULT_ASPECT_RATIO_VALUES = 10;
    public static final String ASPECT_RATIO_X = "ASPECT_RATIO_X";
    public static final String ASPECT_RATIO_Y = "ASPECT_RATIO_Y";
    public static final int OFF = 0;


    public static final String CODE_AND_PHONE_NUMBER = "CODE_AND_PHONE_NUMBER";
    public static final int KEEP_ONLINE = 1;
    public static final int KEEP_OFFLINE = 0;
    public static final String CHAT_USER_CLASS_INTENT_KEY = "CHAT_USER_CLASS_INTENT_KEY";
    public static final String CHAT_USER_FAMILY_INTENT_KEY = "CHAT_USER_FAMILY_INTENT_KEY";
    public static final String CHAT_CURRENT_ID_INTENT_KEY = "CHAT_CURRENT_ID_INTENT_KEY";
    public static final String CHAT_IMAGE_PRO_INTENT_KEY = "CHAT_IMAGE_PRO_INTENT_KEY";
    public static final String CHAT_FRIEND_ID_INTENT_KEY = "CHAT_ID_INTENT_KEY";

    //    database
    public static final String CHAT_CONTACTS_DATABASE_NAME = "chatContactDatabase";
    public static final String MESSAGE_DATABASE_NAME = "messageDatabase";
    public static final String USERS_SEND_MESSAGE_DATABASE_NAME = "usersSendMessageDatabase";
    public static final String IS_ONLINE_FIELD_NAME = "isOnline";
    public static final String TABLE_ID_FIELD_NAME = "id";

    public static final String SENDER_ID_FIELD_NAME = "sender";
    public static final String RECEIVER_ID_FIELD_NAME = "receiver";
    public static final String LAST_MESSAGE_FILED_NAME = "lastMessage";
    public static final String IS_NOTIFICATION_SEND = "isNotificationSend";

    //    firebase storage
    public static final String STORAGE_ORIGINAL_IMAGE_PATH = "/images/originalImages/";
    public static final String STORAGE_SMALL_IMAGE_PATH = "/images/smallImages/";
    public static final String JPG = ".jpg";

    //    RealTime Databases
    public static final String CHATS = "Chats";
    public static final String USERS = "Users";
    public static final String CHAT_LIST = "ChatList";
    public static final String TOKENS = "Tokens";


    public static final String ORIGINAL_FIELD_NAME = "originalImageUri";
    public static final String STR_SMALL_FIELD_NAME = "smallImageUri";
    public static final String STATS_FIELD_NAME = "status";
    public static final String DELIVERY_STATE_FIELD_NAME = "deliveryState";
    public static final String STATUS_OFFLINE = "offline";
    public static final String STATUS_ONLINE = "Online";

    public static final String SENDER = "sender";
    public static final String RECEIVER = "receiver";

    public static final String COME_FROM_STOP_PROGRESS = "COME_FROM_STOP_PROGRESS";
    public static final String COME_FROM_EXIT_APP = "COME_FROM_EXIT_APP";
    public static final String COME_FROM_LOG_OUT_ACC = "COME_FROM_LOG_OUT_ACC";
    public static final String COME_FROM_DELETE_ACC = "COME_FROM_DELETE_ACC";
    public static final String PM = "PM";
    public static final String AM = "AM";
    public static final String TIME_FORMAT = "%tB";
    public static final String LAST_MESSAGE = "LAST_MESSAGE";
    public static final String LAST_MESSAGE_TIME = "LAST_MESSAGE_TIME";


    public static final int MESSAGE_SEEN = 2;
    public static final int MESSAGE_DELIVER = 1;
    public static final int MESSAGE_NOT_DELIVER = 0;
    public static final String FILE_PATH = "FilePath";
    public static final String PLEASE_WAIT = "please wait...";
    public static final String IMAGE_DIR = "mrChat";
    public static final String JPG_FORMAT = ".jpg";
    public static final String SERVER_INFO = ".info/connected";
    public static final String ID_EDT_SEARCH_VIEW = "android:id/search_src_text";
    public static final long THIRTY_SECOND_TIME_SPAM = 35000;
    public static final long F_S_TIME_SPAM = 50000;
    public static final long FIVE_M_TIME_SPAM = 300000;
    public static final long TIME_INTERVAL = 20000;
    public static final long TIME_INTERVAL_TO_GET_STATES = 70000;
    public static final long T_I_TO_GET_TYPING_STATE = 15000;
    public static final String COME_FROM_PHONE = "COME_FROM_PHONE";
    public static final String COME_FROM_VERIFY = "COME_FROM_VERIFY";
    public static final String CHAT_MAIN_ACTIVITY_NAME = "com.mohammadreza.chat.mrchat.ui.ChatMainActivity";
    public static final String MESSAGE_ACTIVITY_NAME = "com.mohammadreza.chat.mrchat.ui.ChatMainActivity";
    public static final String MR_CHAT_ORG_URL = "https://firebasestorage.googleapis.com/v0/b/fir-auth-c1399.appspot.com/o/images%2Fusers%2FMRchat.png?alt=media&token=48b9a78a-b8c6-4783-9cda-17c8cb862940";
    public static final String MR_CHAT_SM_URL = "https://firebasestorage.googleapis.com/v0/b/fir-auth-c1399.appspot.com/o/images%2Fusers%2FMRchat.png?alt=media&token=48b9a78a-b8c6-4783-9cda-17c8cb862940";
    public static final String MR_TAG = "MR_TAG";
    public static final String COME_FROM_CAMERA = "COME_FROM_CAMERA";
    public static final String COME_FROM_GALLERY = "COME_FROM_GALLERY";
    public static final String PREFER_DRAFTS = "Drafts";
    public static final String IS_SEND_FOR_ONCE = "massage_send";
    public static final String PREFER_FRIEND_ID = "FriendID";
    public static final String PREFER_FRIEND_ID_SM = "mFriend";
    public static final String FIREBASE_URL = "https://fcm.googleapis.com/";
    public static final String TOKEN = "token";
    public static final String IS_TYPING = "isTyping";
    public static final String ALL_FRIEND = "AllFriend";
    public static final String SELECTED_FRIEND = "SelectedFriend";
    public static final String COME_FROM_MESSAGE = "comFromMessage";


    //    must be delete
//    public static final String CURRENT_USER_IDF = "+989357764384";
}
