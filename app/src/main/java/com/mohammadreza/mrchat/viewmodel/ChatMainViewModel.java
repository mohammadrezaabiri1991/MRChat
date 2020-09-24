package com.mohammadreza.mrchat.viewmodel;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Build;
import android.provider.ContactsContract;
import android.util.TypedValue;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.WindowManager;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.core.view.GravityCompat;
import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;
import androidx.databinding.BindingAdapter;
import androidx.databinding.ObservableInt;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.OnLifecycleEvent;
import androidx.lifecycle.ProcessLifecycleOwner;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.mohammadreza.mrchat.BR;
import com.mohammadreza.mrchat.R;
import com.mohammadreza.mrchat.adapter.ChatUserListAdapter;
import com.mohammadreza.mrchat.application.MyApp;
import com.mohammadreza.mrchat.constant.ChatConstant;
import com.mohammadreza.mrchat.databinding.BottomSheetDrawerBinding;
import com.mohammadreza.mrchat.databinding.ChatDrawerBinding;
import com.mohammadreza.mrchat.databinding.NavHeaderChatBinding;
import com.mohammadreza.mrchat.firebase.ChatFirebase;
import com.mohammadreza.mrchat.model.ChatListModel;
import com.mohammadreza.mrchat.model.ChatListRealmModel;
import com.mohammadreza.mrchat.model.ContactsModel;
import com.mohammadreza.mrchat.model.EventBusModel;
import com.mohammadreza.mrchat.model.MessagesModel;
import com.mohammadreza.mrchat.notification.Token;
import com.mohammadreza.mrchat.receiver.NetworkStateReceiver;
import com.mohammadreza.mrchat.repository.ChatRealmDatabase;
import com.mohammadreza.mrchat.ui.ContactsActivity;
import com.mohammadreza.mrchat.ui.MessageActivity;
import com.mohammadreza.mrchat.utils.MyAlertDialog;
import com.mohammadreza.mrchat.utils.UserOnlineState;
import com.mohammadreza.mrchat.utils.Utils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.mohammadreza.mrchat.constant.ChatConstant.ORIGINAL_FIELD_NAME;
import static com.mohammadreza.mrchat.constant.ChatConstant.STR_SMALL_FIELD_NAME;
import static com.mohammadreza.mrchat.receiver.NetworkStateReceiver.CONNECTED_TO_FIREBASE;
import static com.mohammadreza.mrchat.receiver.NetworkStateReceiver.CONNECTING;
import static com.mohammadreza.mrchat.receiver.NetworkStateReceiver.NO_NET;
import static com.mohammadreza.mrchat.utils.GetPermissions.checkCameraPermission;
import static com.mohammadreza.mrchat.utils.GetPermissions.checkStoragePermission;
import static com.mohammadreza.mrchat.utils.GetPermissions.requestContactsPermission;

public class ChatMainViewModel extends BaseObservable implements NavigationView.OnNavigationItemSelectedListener, NetworkStateReceiver.NetworkStateReceiverListener, LifecycleObserver {

    private final DatabaseReference userReference;
    private final DatabaseReference chatListReference;
    private final BottomSheetDrawerBinding drawerSheetBinding;
    private AppCompatActivity appCompatActivity;
    private final ChatDrawerBinding drawerBinding;
    private final BottomSheetDialog bottom_sheet_drawer;
    public String connectedToServerState = "";
    private boolean isProgressLoadMain;
    private boolean isImgNotingMainChatVisible;
    public NetworkStateReceiver networkStateReceiverMain;
    private NavHeaderChatBinding headerBinding;
    private String id = "";
    private String userName = " ";
    private String smallImageUrl = "";
    private boolean isNavProgressVisible;
    private ObservableInt isBottomSheetRemoveVisible;
    private MutableLiveData<ChatListModel> chatListLiveData;
    private MutableLiveData<List<ChatListModel>> parentLiveData;
    private List<ChatListModel> chatListModels;
    private String originalImageUrl = "";
    private List<String> idChatList;
    private int onMoonClick;
    private Drawable backgroundDrawable;
    private TimerTask getFriendTimer;
    public boolean chatActivityIsRunning;
    //    private boolean isChatActivityRunning;
    private Timer timer;
    public static int comFromChatMain;


    public ChatMainViewModel(AppCompatActivity appCompat, ChatDrawerBinding drawerBinding, NavHeaderChatBinding headerBinding,
                             BottomSheetDrawerBinding drawerSheetBinding, BottomSheetDialog bottomSheerDialogDrawer) {
        appCompatActivity = appCompat;
        this.drawerBinding = drawerBinding;
        this.bottom_sheet_drawer = bottomSheerDialogDrawer;
        this.drawerSheetBinding = drawerSheetBinding;
        this.headerBinding = headerBinding;
        drawerBinding.navViewChat.setNavigationItemSelectedListener(this);
        FirebaseDatabase baseReference = FirebaseDatabase.getInstance();
        userReference = baseReference.getReference(ChatConstant.USERS);
        chatListReference = baseReference.getReference(ChatConstant.CHAT_LIST);
        chatListLiveData = new MutableLiveData<>();
        parentLiveData = new MutableLiveData<>();
        chatListModels = new ArrayList<>();
        idChatList = new ArrayList<>();
        backgroundDrawable = DrawableCompat.wrap(headerBinding.relativeHeader.getBackground()).mutate();
        isBottomSheetRemoveVisible = new ObservableInt(View.VISIBLE);

        getIntent();
        registerBroadCast();
        setSettingsToRecyclerView(drawerBinding.activityChatContent.recyclerChatUser);
        ProcessLifecycleOwner.get().getLifecycle().addObserver(this);

        timer = new Timer();
        getUserInfo();


        setProgressLoadMain(true);
        if (!getId().isEmpty()) {
            new UserOnlineState(getId());
        }
        UpdateToken();
        setFabBehavior(drawerBinding.activityChatContent.recyclerChatUser, drawerBinding.activityChatContent.floatingChat);
    }

    private void getIntent() {
        Intent intent = appCompatActivity.getIntent();
        if (intent != null) {
            ContactsModel chatUser = (ContactsModel) intent.getSerializableExtra(ChatConstant.CHAT_USER_CLASS_INTENT_KEY);
            if (chatUser != null) {
                id = chatUser.getId();
                userName = chatUser.getUserName();
                originalImageUrl = chatUser.getOriginalImageUri();
                smallImageUrl = chatUser.getSmallImageUri();
            }
        }
    }


    private void setSettingsToRecyclerView(RecyclerView mRecyclerView) {
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setNestedScrollingEnabled(false);
        mRecyclerView.setItemViewCacheSize(20);
        mRecyclerView.setDrawingCacheEnabled(true);
        mRecyclerView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(appCompatActivity));
    }

    private void getUserInfo() {
        getFriendTimer = new TimerTask() {
            @Override
            public void run() {
//                if (chatActivityIsRunning) {
                getFriendHowSendMessageToMe();
//                }
            }
        };
        timer.schedule(getFriendTimer, 0, ChatConstant.F_S_TIME_SPAM);
    }

    @BindingAdapter(value = {"recycler_chat", "current_id", "user_status", "app:onMoonClick", "app:chat_list_live"})
    public static void setChatUserRecyclerviewData(final RecyclerView recyclerView, final MutableLiveData<List<ChatListModel>> chatListModels, String currentId, String status, int onMoonClick, MutableLiveData<ChatListModel> chatListLive) {

        chatListModels.observe((LifecycleOwner) recyclerView.getContext(), chatListModels1 -> {
            try {
                Collections.sort(chatListModels1, new ChatListCompare());
            } catch (Exception e) {
            }
            ChatUserListAdapter adapter = new ChatUserListAdapter(chatListModels1, (AppCompatActivity) recyclerView.getContext(), currentId, status, onMoonClick, chatListLive);
            recyclerView.setAdapter(adapter);
        });

    }

    @BindingAdapter(value = {"src_chat_profile", "src_chat_profile_id", "appCompat"})
    public static void setImageProfile(CircleImageView imageView, String url, String id, AppCompatActivity appCompatActivity) {
        try {
            Utils.loadImageFromStorage(id + appCompatActivity.getString(R.string.small), appCompatActivity, imageView, null);
            Glide.with(MyApp.appContext)
                    .asBitmap()
                    .load(url)
                    .into(new CustomTarget<Bitmap>() {

                        @Override
                        public void onResourceReady(@NonNull Bitmap bitmap, @Nullable Transition<? super Bitmap> transition) {
                            imageView.setImageBitmap(bitmap);
                            Utils.saveToInternalStorage((id + MyApp.appContext.getString(R.string.small)), bitmap, MyApp.appContext);
                        }

                        @Override
                        public void onLoadCleared(@Nullable Drawable placeholder) {
                        }
                    });

        } catch (Exception e) {
        }
    }

    private void actionExitFromApp() {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        appCompatActivity.startActivity(intent);
        appCompatActivity.finish();
    }

    @BindingAdapter("app:bottomSheetBehaviorStateDrawer")
    public static void setStateDrawer(View v, int peekHeight) {
        BottomSheetBehavior<View> viewBottomSheetBehavior = BottomSheetBehavior.from(v);
        viewBottomSheetBehavior.setPeekHeight(peekHeight);
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, peekHeight, v.getContext().getResources().getDisplayMetrics());

    }

    private void UpdateToken() {
        String refreshToken = FirebaseInstanceId.getInstance().getToken();
        Token token = new Token(refreshToken);
        FirebaseDatabase.getInstance().getReference(ChatConstant.TOKENS).child(getId()).setValue(token);
    }

    //    TODO
    private void getUsersFromOfflineDatabase() {
        chatListModels.clear();
        idChatList.clear();
        List<ChatListRealmModel> realmResults = ChatRealmDatabase.getUsersSendMessage(appCompatActivity);
        if (realmResults != null && realmResults.size() > 0) {
            comFromChatMain = 0;
//            new Thread(() -> {
            for (ChatListRealmModel chatListRealmModel : realmResults) {
                chatListModels.add(new ChatListModel(chatListRealmModel));
                idChatList.add(chatListRealmModel.getId());
            }
            parentLiveData.postValue(chatListModels);
//            }).start();

            setProgressLoadMain(false);
            setImgNotingMainChatVisible(false);
        }

    }

    public void onMoonClick(View view) {
        try {
            view.setClickable(false);
            changeTheme((ImageView) view);
        } catch (Exception e) {
        }
    }


    public void getFriendHowSendMessageToMe() {
        chatListReference.child(getId()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.getChildrenCount() == 0) {
                    if (chatListModels.size() <= 0) {
                        setImgNotingMainChatVisible(true);
                    }
                    setProgressLoadMain(false);
                } else {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        String friendId = snapshot.getKey();
                        MessagesModel messagesModel = snapshot.getValue(MessagesModel.class);
                        if (friendId != null && !getId().equals(friendId) && messagesModel != null) {
                            getFriendsSendMessageDetail(messagesModel, friendId, ChatConstant.ALL_FRIEND);
                        }

                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void getFriendsSendMessageDetail(MessagesModel messagesModel, String friendId, String comFrom) {
        userReference.child(friendId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                try {
                    ChatListModel chatListModel = new ChatListModel(Objects.requireNonNull(dataSnapshot.getValue(ContactsModel.class)));
                    if (comFrom.equals(ChatConstant.ALL_FRIEND)) {
                        actionGetAllFriend(chatListModel, messagesModel, friendId);
                    } else if (comFrom.equals(ChatConstant.SELECTED_FRIEND)) {
                        actionAddFriendFromNotification(chatListModel, messagesModel);
                    }

                } catch (Exception e) {
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                if (chatListModels.size() > 0) {
                    setImgNotingMainChatVisible(false);
                } else {
                    setImgNotingMainChatVisible(true);
                }
                setProgressLoadMain(false);
            }
        });

    }


    private void actionGetAllFriend(ChatListModel chatListModel, MessagesModel messagesModel, String friendId) {
        if (!getId().equals(chatListModel.getId()) || !getId().equals(messagesModel.getSender())) {
            comFromChatMain = 1;
            if (!messagesModel.getSender().equals(getId()) && messagesModel.getId() > chatListModel.getStatus()) {
                chatListModel.setStatus(messagesModel.getId());
                chatListModel.setLastMessageTime(messagesModel.getId());

            } else {
                chatListModel.setLastMessageTime(chatListModel.getStatus());
            }
            chatListModel.setLastMessage(messagesModel.getMessage());

            if (idChatList.contains(friendId)) {
                chatListLiveData.setValue(chatListModel);
            } else {
                idChatList.add(friendId);
                chatListModels.add(chatListModel);
                parentLiveData.setValue(chatListModels);
            }

            ChatRealmDatabase.addUsersSendMessageToChatDatabase(chatListModel, appCompatActivity, "");

            setImgNotingMainChatVisible(false);
            setProgressLoadMain(false);
        }
    }


    private void actionAddFriendFromNotification(ChatListModel chatListModel, MessagesModel messagesModel) {
//        ArrayList<ChatListModel> listModels = new ArrayList<>();

        if (messagesModel.getSender().equals(chatListModel.getId())) {

            chatListModel.setLastMessage(messagesModel.getMessage());
            chatListModel.setLastMessageTime(messagesModel.getId());

            if (!messagesModel.getSender().equals(getId()) && messagesModel.getId() > chatListModel.getStatus()) {
                chatListModel.setStatus(messagesModel.getId());
            }
            actionAddFriendToList(chatListModel);

            ChatRealmDatabase.addUsersSendMessageToChatDatabase(chatListModel, appCompatActivity, "");
//            timer.schedule(getFriendTimer, 0, ChatConstant.TIME_INTERVAL_TO_GET_STATES);
//            timer = new Timer();
        }
    }


    private void changeTheme(ImageView themeChanger) {
        SharedPreferences.Editor editor = appCompatActivity.getSharedPreferences(appCompatActivity.getString(R.string.mr_chat), Context.MODE_PRIVATE).edit();
        SharedPreferences prefs = appCompatActivity.getSharedPreferences(appCompatActivity.getString(R.string.mr_chat), Context.MODE_PRIVATE);

        if (!prefs.getBoolean(appCompatActivity.getString(R.string.theme), false)) {
            appCompatActivity.setTheme(R.style.DarkTheme);
            setOnThemeClick(2);
            createDummyTheme(appCompatActivity.getResources().getColor(R.color.darkColorAccent), appCompatActivity.getResources().getColor(R.color.darkColorH_F), appCompatActivity.getResources().getColor(R.color.darkColorAccent), appCompatActivity.getResources().getColor(R.color.darkColorPrimaryDark),
                    appCompatActivity.getResources().getColor(R.color.drawerIconTextColor), appCompatActivity.getResources().getColor(R.color.darkColorAccent), 2, appCompatActivity.getResources().getColor(R.color.drawerIconTextColor));
            editor.putBoolean(appCompatActivity.getString(R.string.theme), true);

        } else {
            setOnThemeClick(1);
            appCompatActivity.setTheme(R.style.AppTheme);
            createDummyTheme(appCompatActivity.getResources().getColor(R.color.colorBackground), appCompatActivity.getResources().getColor(R.color.colorPrimary), appCompatActivity.getResources().getColor(R.color.colorWhite), appCompatActivity.getResources().getColor(R.color.colorPrimaryDark),
                    appCompatActivity.getResources().getColor(R.color.colorText), appCompatActivity.getResources().getColor(R.color.colorWhite), 1, appCompatActivity.getResources().getColor(R.color.colorTextDark));
            editor.putBoolean(appCompatActivity.getString(R.string.theme), false);
        }
        editor.putBoolean(appCompatActivity.getString(R.string.isThemeChange), true);
        editor.apply();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            createRevelAnim(themeChanger);
        } else {
            themeChanger.setClickable(false);
        }
    }

    private void createDummyTheme(int colorBackground, int colorHeader, int colorNav, int colorPrimerDark, int colorNavText, int colorNavTextSelect, int curvedState, int colorTextSheet) {
        drawerBinding.activityChatContent.coordinatorDrawer.setBackgroundColor(colorBackground);
        drawerBinding.activityChatContent.linearHeaderChat.setBackgroundColor(colorHeader);
        drawerBinding.navViewChat.setBackgroundColor(colorNav);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            appCompatActivity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            appCompatActivity.getWindow().setStatusBarColor(colorPrimerDark);
        }

        int[][] state = new int[][]{
                new int[]{-android.R.attr.state_enabled}, // disabled
                new int[]{android.R.attr.state_enabled}, // enabled
                new int[]{-android.R.attr.state_checked}, // unchecked
                new int[]{android.R.attr.state_pressed}  // pressed

        };
        int[] color = new int[]{
                colorNavText,
                colorNavText,
                colorNavText,
                colorNavTextSelect
        };

        ColorStateList ColorStateList1 = new ColorStateList(state, color);
        drawerBinding.navViewChat.setItemTextColor(ColorStateList1);
        drawerBinding.navViewChat.setItemIconTintList(ColorStateList1);


        if (backgroundDrawable != null) {
            DrawableCompat.setTint(backgroundDrawable, colorHeader);
        }

        if (curvedState == 1) {
            headerBinding.imgSelectPhotoDrawer.setBackgroundResource(R.drawable.nave_header_img);
            drawerSheetBinding.constraintDrawerSheet.setBackgroundResource(R.drawable.bottom_sheet_round);
        } else if (curvedState == 2) {
            drawerSheetBinding.constraintDrawerSheet.setBackgroundResource(R.drawable.bottom_sheet_round_dark);
            headerBinding.imgSelectPhotoDrawer.setBackgroundResource(R.drawable.nav_header_img_dark);
        }

        headerBinding.imgSelectPhotoDrawer.setColorFilter(colorNavText, PorterDuff.Mode.SRC_IN);
        drawerSheetBinding.imgCameraSheetDrawer.setColorFilter(colorNavText, PorterDuff.Mode.SRC_IN);
        drawerSheetBinding.imgGallerySheetDrawer.setColorFilter(colorNavText, PorterDuff.Mode.SRC_IN);

        drawerSheetBinding.txtSelectPhotoSheetDrawer.setTextColor(colorTextSheet);
        drawerSheetBinding.txtCameraSheetDrawer.setTextColor(colorTextSheet);
        drawerSheetBinding.txtGallerySheetDrawer.setTextColor(colorTextSheet);
        drawerSheetBinding.txtRemovePhotoSheetDrawer.setTextColor(colorTextSheet);

    }


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void createRevelAnim(ImageView themeChanger) {
        drawerBinding.drawerLayoutChat.post(() -> {
            int cx = (themeChanger.getLeft() + themeChanger.getRight()) / 2;
            int cy = (themeChanger.getTop() + themeChanger.getBottom()) / 2;
            int finalRadius = Math.max(drawerBinding.drawerLayoutChat.getWidth(), drawerBinding.drawerLayoutChat.getHeight());
            Animator circularReveal = ViewAnimationUtils.createCircularReveal(drawerBinding.drawerLayoutChat, cx, cy, 0, finalRadius);
            circularReveal.setDuration(400);
            circularReveal.setInterpolator(new LinearInterpolator());
            circularReveal.start();
            circularReveal.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    themeChanger.setClickable(true);
                    super.onAnimationEnd(animation);
                }
            });
        });
    }

    // TODO
    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true, priority = 1)
    public void onMessageEvent(EventBusModel model) {
        if (!model.getMessageBody().isEmpty() && !model.getFriendId().isEmpty() && chatActivityIsRunning) {
            comFromChatMain = 2;
            getFriendsSendMessageDetail(new MessagesModel(model), model.getFriendId(), ChatConstant.SELECTED_FRIEND);
//            timer.cancel();
        } else if (model.getChatListModel() != null) {
            comFromChatMain = 2;
            actionAddFriendToList(model.getChatListModel());
        }

        if (model.getBitmap() != null) {
            setNavProgressVisible(true);
            new Thread(() -> saveUserImageProfileInFirebaseStorage(headerBinding.imageViewHeaderNav, model.getBitmap())).start();
            if (EventBus.getDefault().isRegistered(ChatMainViewModel.this)) {
                EventBus.getDefault().unregister(ChatMainViewModel.this);
            }
        }
    }

    // TODO
    private void actionAddFriendToList(ChatListModel chatListModel) {
        try {
            if (idChatList.contains(chatListModel.getId())) {
                chatListLiveData.setValue(chatListModel);
            } else {
                idChatList.add(chatListModel.getId());
                chatListModels.add(chatListModel);
                parentLiveData.setValue(chatListModels);
                setImgNotingMainChatVisible(false);
                setProgressLoadMain(false);
            }
        } catch (Exception e) {
        }


    }


    public void clickHandlerFab() {
        Utils.callIntent(appCompatActivity, ContactsActivity.class, ChatConstant.CHAT_CURRENT_ID_INTENT_KEY, getId());
    }

    public void clickHandlerDrawer(View v) {
        drawerBinding.drawerLayoutChat.openDrawer(GravityCompat.START);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.nav_log_out:
                MyAlertDialog.createAlertDialog(appCompatActivity, appCompatActivity.getString(R.string.log_out_from_account_title), ChatConstant.COME_FROM_LOG_OUT_ACC).show();
                break;

            case R.id.nav_contacts:
                Utils.callIntent(appCompatActivity, ContactsActivity.class, ChatConstant.CHAT_CURRENT_ID_INTENT_KEY, getId());
                break;

            case R.id.nav_invite:
                if (requestContactsPermission(appCompatActivity)) {
                    Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
                    appCompatActivity.startActivityForResult(intent, ChatConstant.PICK_CONTACT_CODE);
                }
                break;

            case R.id.nav_support:
                actionNavSupport();
                break;
            case R.id.nav_info:
                MyAlertDialog.createInfoDialog(appCompatActivity);
                break;

            case R.id.nav_exit_app:
                actionExitFromApp();
                break;


        }

        return false;
    }

    private void actionNavSupport() {
        ContactsModel userModel = new ContactsModel();
        userModel.setId(appCompatActivity.getString(R.string.mrID));
        userModel.setUserName(appCompatActivity.getString(R.string.mr_chat));
//        userModel.setOriginalImageUri(ChatConstant.MR_CHAT_ORG_URL);
//        userModel.setSmallImageUri(ChatConstant.MR_CHAT_SM_URL);
        userModel.setStatus(100);

        Utils.callIntent(appCompatActivity, MessageActivity.class, userModel, id);
    }

    public void onClickNavHeaderSelectImage(View view) {
        if (view.getTag().equals(appCompatActivity.getResources().getString(R.string.imgSelectPhotoDrawer))) {
            if (headerBinding.imageViewHeaderNav.getDrawable() == null) {
                isBottomSheetRemoveVisible.set(View.GONE);
            } else {
                isBottomSheetRemoveVisible.set(View.VISIBLE);
            }
            bottom_sheet_drawer.show();
        } else if (view.getTag().equals(appCompatActivity.getResources().getString(R.string.tagCamera))) {
            checkCameraPermission(appCompatActivity, bottom_sheet_drawer);
        } else if (view.getTag().equals(appCompatActivity.getResources().getString(R.string.tagGallery))) {
            checkStoragePermission(appCompatActivity, bottom_sheet_drawer);
        } else if (view.getTag().equals(appCompatActivity.getResources().getString(R.string.tag_remove_photo))) {
            actionRemovePhoto();
        }
    }

    private void actionRemovePhoto() {
        headerBinding.imageViewHeaderNav.setImageDrawable(null);
        saveOrRemoveImageUrlFirebaseRealTime("", "");
        bottom_sheet_drawer.dismiss();
    }

    private void saveUserImageProfileInFirebaseStorage(CircleImageView circleImageView, Bitmap originalProfilePicture) {
        Bitmap smallProfilePicture = Utils.scaleDown(originalProfilePicture, appCompatActivity.getResources().getDimension(R.dimen.small_image_size), appCompatActivity.getResources().getDimension(R.dimen.small_image_size), true);
        StorageReference storage = FirebaseStorage.getInstance().getReference();
        Task<Uri> originalUriTask = ChatFirebase.createOriginalUriTask(storage, originalProfilePicture, getId());
        Task<Uri> smallUriTask = ChatFirebase.createSmallUriTask(storage, smallProfilePicture, getId());

        originalUriTask.addOnCompleteListener(original -> {
            if (original.isSuccessful()) {
                smallUriTask.addOnCompleteListener(small -> {
                    if (small.isSuccessful()) {

                        setNavProgressVisible(false);
                        saveOrRemoveImageUrlFirebaseRealTime(String.valueOf(original.getResult()), String.valueOf(small.getResult()));
                        Utils.saveToInternalStorage((getId() + appCompatActivity.getString(R.string.original)), originalProfilePicture, appCompatActivity);
                        Utils.saveToInternalStorage((getId() + appCompatActivity.getString(R.string.small)), smallProfilePicture, appCompatActivity);
                        circleImageView.setImageBitmap(smallProfilePicture);
                    }
                    setNavProgressVisible(false);
                });
            }
        });


    }

    private void saveOrRemoveImageUrlFirebaseRealTime(String originalUrl, String smallUrl) {
        if (getId() != null && !getId().isEmpty()) {
            userReference.child(getId()).child(ORIGINAL_FIELD_NAME).setValue(originalUrl).addOnSuccessListener(original ->
                    userReference.child(getId()).child(STR_SMALL_FIELD_NAME).setValue(smallUrl).addOnSuccessListener(small -> {
                        ChatRealmDatabase.changeImageUrl(ChatConstant.TABLE_ID_FIELD_NAME, getId(), originalUrl, smallUrl, appCompatActivity);
                        if (originalUrl.equals("")) {
                            Utils.removeFromStorage(getId(), appCompatActivity);
                        }
                    }));
        }
    }


    private void registerBroadCast() {
        if (networkStateReceiverMain == null) {
            networkStateReceiverMain = new NetworkStateReceiver(appCompatActivity);
        }
        networkStateReceiverMain.addListener(this);
        appCompatActivity.registerReceiver(networkStateReceiverMain, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
    }

    public void onImageProfileClick(View view) {
        CircleImageView circleImageView = (CircleImageView) view;
        if (circleImageView.getDrawable() != null) {
            MyAlertDialog.createFullScreenImageDialog(appCompatActivity, headerBinding.imageViewHeaderNav.getDrawable(), originalImageUrl, getId());
        }
    }

    private void setFabBehavior(RecyclerView recyclerView, FloatingActionButton fabGoToFirst) {
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                if (dy < 0 && !fabGoToFirst.isShown())
                    fabGoToFirst.show();
                else if (dy > 1 && fabGoToFirst.isShown())
                    fabGoToFirst.hide();
            }

            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                LinearLayoutManager manager = (LinearLayoutManager) recyclerView.getLayoutManager();
                if (manager != null) {
                    if (manager.findFirstVisibleItemPosition() == 1) {
                        fabGoToFirst.hide();
                    }
                }
                super.onScrollStateChanged(recyclerView, newState);
            }
        });
    }

    // TODO
//    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
//    public void onMainStop() {
//        isChatActivityRunning = false;
//    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    public void onMainStart() {
        getUsersFromOfflineDatabase();
//        isChatActivityRunning = true;
    }


    public static class ChatListCompare implements Comparator<ChatListModel> {
        public int compare(@NonNull ChatListModel left, @NonNull ChatListModel right) {
            return right.getLastMessageTime() > left.getLastMessageTime() ? 1 : -1;
        }

    }

    @Bindable
    public AppCompatActivity getAppCompatActivity() {
        return appCompatActivity;
    }

    public void setAppCompatActivity(AppCompatActivity appCompatActivity) {
        this.appCompatActivity = appCompatActivity;
    }

    @Bindable
    public boolean isProgressLoadMain() {
        return isProgressLoadMain;
    }

    public void setProgressLoadMain(boolean progressLoadMain) {
        isProgressLoadMain = progressLoadMain;
        notifyPropertyChanged(BR.progressLoadMain);
    }

    @Bindable
    public boolean isImgNotingMainChatVisible() {
        return isImgNotingMainChatVisible;
    }

    public void setImgNotingMainChatVisible(boolean imgNotingMainChatVisible) {
        this.isImgNotingMainChatVisible = imgNotingMainChatVisible;
        notifyPropertyChanged(BR.imgNotingMainChatVisible);
    }

    public MutableLiveData<ChatListModel> getChatsList() {
        return chatListLiveData;
    }

    @Bindable
    public List<ChatListModel> getChatListModels() {
        return chatListModels;
    }

    public void setChatListModels(List<ChatListModel> chatListModels) {
        this.chatListModels = chatListModels;
        notifyPropertyChanged(BR.chatListModels);
    }

    public MutableLiveData<List<ChatListModel>> getParentLiveData() {
        return parentLiveData;
    }

    public String getId() {
        return id;
    }

    @Bindable
    public int getOnMoonClick() {
        return onMoonClick;
    }

    public void setOnThemeClick(int onThemeClick) {
        this.onMoonClick = onThemeClick;
        notifyPropertyChanged(BR.onMoonClick);
    }

    @Bindable
    public String getUserName() {
        return userName;
    }

    @Bindable
    public String getSmallImageUrl() {
        return smallImageUrl;
    }

    public ObservableInt isBottomSheetRemoveVisible() {
        return isBottomSheetRemoveVisible;
    }

    @Bindable
    public String getConnectedToServerState() {
        return connectedToServerState;
    }

    public void setConnectedToServerState(String connectedToServerState) {
        this.connectedToServerState = connectedToServerState;
        notifyPropertyChanged(BR.connectedToServerState);
    }

    @Bindable
    public boolean isNavProgressVisible() {
        return isNavProgressVisible;
    }

    public void setNavProgressVisible(boolean navProgressVisible) {
        isNavProgressVisible = navProgressVisible;
        notifyPropertyChanged(BR.navProgressVisible);
    }

    @Override
    public void firebaseConnectionState(int state) {
        switch (state) {
            case NO_NET:
                setConnectedToServerState(appCompatActivity.getString(R.string.waiting_for_network));
                break;

            case CONNECTING:
                setConnectedToServerState(appCompatActivity.getString(R.string.connecting));
                break;

            case CONNECTED_TO_FIREBASE:
                setConnectedToServerState(appCompatActivity.getString(R.string.mr_chat));
                break;
        }
    }

    private int getFriendModel(ChatListModel chatListModel) {
        return chatListModels.indexOf(chatListModel);
    }

}
