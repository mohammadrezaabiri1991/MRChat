package com.mohammadreza.mrchat.viewmodel;

import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Handler;
import android.util.TypedValue;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;
import androidx.databinding.BindingAdapter;

import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.mohammadreza.mrchat.BR;
import com.mohammadreza.mrchat.R;
import com.mohammadreza.mrchat.constant.ChatConstant;
import com.mohammadreza.mrchat.databinding.ActivityChatCreateProfileBinding;
import com.mohammadreza.mrchat.firebase.ChatFirebase;
import com.mohammadreza.mrchat.model.ContactsModel;
import com.mohammadreza.mrchat.model.ContactsRealmModel;
import com.mohammadreza.mrchat.model.EventBusModel;
import com.mohammadreza.mrchat.receiver.NetworkStateReceiver;
import com.mohammadreza.mrchat.repository.ChatRealmDatabase;
import com.mohammadreza.mrchat.ui.ChatMainActivity;
import com.mohammadreza.mrchat.ui.PhoneActivity;
import com.mohammadreza.mrchat.utils.GetPermissions;
import com.mohammadreza.mrchat.utils.MyAlertDialog;
import com.mohammadreza.mrchat.utils.MyAnimationUtils;
import com.mohammadreza.mrchat.utils.Utils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.mohammadreza.mrchat.receiver.NetworkStateReceiver.CONNECTED_TO_FIREBASE;
import static com.mohammadreza.mrchat.receiver.NetworkStateReceiver.CONNECTING;
import static com.mohammadreza.mrchat.receiver.NetworkStateReceiver.NO_NET;


public class CreateProfileViewModel extends BaseObservable implements NetworkStateReceiver.NetworkStateReceiverListener {
    public NetworkStateReceiver networkStateReceiverProfile;
    private AppCompatActivity appCompatActivity;
    private EditText edtNameProfile;
    private BottomSheetDialog bottomSheerDialog;
    private CircleImageView imgChoosePicture;
    private boolean isConstraintClickable = true;
    private String userName = "";
    private String family = "";
    private String phoneNumber;
    private String strTxtNetStateProfile = "";
    private TextView txtNetStateProfile;
    private Bitmap smallProfilePicture;
    private Bitmap originalProfilePicture;
    private TextInputLayout textInputLayoutName;


    public CreateProfileViewModel(ActivityChatCreateProfileBinding profileBinding, BottomSheetDialog bottomSheerDialog) {
        this.appCompatActivity = (AppCompatActivity) profileBinding.getRoot().getContext();
        this.edtNameProfile = profileBinding.edtNameProfile;
        this.bottomSheerDialog = bottomSheerDialog;
        this.imgChoosePicture = profileBinding.imgChoosePicture;
        this.txtNetStateProfile = profileBinding.txtNetStateCreatePro;
        this.textInputLayoutName = profileBinding.textInputLayoutName;
        registerBroadCast();
        getIntentData();
    }

    @BindingAdapter("app:bottomSheetBehaviorState")
    public static void setState(View v, int peekHeight) {
        BottomSheetBehavior<View> viewBottomSheetBehavior = BottomSheetBehavior.from(v);
        viewBottomSheetBehavior.setPeekHeight(peekHeight);
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, peekHeight, v.getContext().getResources().getDisplayMetrics());

    }


    private void getIntentData() {
        Intent intent = appCompatActivity.getIntent();
        if (intent != null && intent.getExtras() != null) {
            phoneNumber = intent.getStringExtra(ChatConstant.CODE_AND_PHONE_NUMBER);
        }
    }

    public void onClickHandlerCreateProfile(View view) {
        if (view.getTag().equals(appCompatActivity.getResources().getString(R.string.tagGotoProfile))) {
            actionBtnGotoProfile(view);
        } else if (view.getTag().equals(appCompatActivity.getResources().getString(R.string.tagSelectPhoto))) {
            bottomSheerDialog.show();
        } else if (view.getTag().equals(appCompatActivity.getResources().getString(R.string.tagCamera))) {
            GetPermissions.checkCameraPermission(appCompatActivity, bottomSheerDialog);
        } else if (view.getTag().equals(appCompatActivity.getResources().getString(R.string.tagGallery))) {
            GetPermissions.checkStoragePermission(appCompatActivity, bottomSheerDialog);
        }


    }

    private void actionBtnGotoProfile(View view) {
        if (getUserName().trim().isEmpty()) {
            textInputLayoutName.startAnimation(MyAnimationUtils.animationShakeError());
            MyAnimationUtils.setVibrate(appCompatActivity);
        } else {
            if (Utils.isOnline(appCompatActivity)) {
                saveUserImageProfileInFirebaseStorage(view);
                view.setVisibility(View.INVISIBLE);
                setConstraintClickable(false);
            } else {
                view.setVisibility(View.INVISIBLE);
                setConstraintClickable(false);
                new Handler().postDelayed(() -> {
                    view.setVisibility(View.VISIBLE);
                    setConstraintClickable(true);
                    MyAlertDialog.showAlertDialog(appCompatActivity, appCompatActivity.getString(R.string.no_access_to_net));
                }, 3000);

            }


        }
    }

    private void saveUserImageProfileInFirebaseStorage(View view) {
        if (originalProfilePicture != null && smallProfilePicture != null) {
            StorageReference storage = FirebaseStorage.getInstance().getReference();

            Task<Uri> originalUriTask = ChatFirebase.createOriginalUriTask(storage, originalProfilePicture, phoneNumber);
            Task<Uri> smallUriTask = ChatFirebase.createSmallUriTask(storage, smallProfilePicture, phoneNumber);

            originalUriTask.addOnCompleteListener(original -> {
                if (original.isSuccessful()) {
                    smallUriTask.addOnCompleteListener(small -> {
                        if (small.isSuccessful()) {
                            saveUserAccount(view, String.valueOf(original.getResult()), String.valueOf(small.getResult()));
                        }
                    });
                }
            });


        } else {
            saveUserAccount(view, "", "");
        }
    }

    private void registerBroadCast() {
        networkStateReceiverProfile = new NetworkStateReceiver(appCompatActivity);
        networkStateReceiverProfile.addListener(this);
        appCompatActivity.registerReceiver(networkStateReceiverProfile, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true, priority = 1)
    public void onMessageEvent(EventBusModel model) {
        if (model.getBitmap() != null && model.getBitmap() != null) {
            originalProfilePicture = model.getBitmap();
            smallProfilePicture = Utils.scaleDown(model.getBitmap(), appCompatActivity.getResources().getDimension(R.dimen.small_image_size), appCompatActivity.getResources().getDimension(R.dimen.small_image_size), false);
            imgChoosePicture.setImageBitmap(smallProfilePicture);
        }
        if (EventBus.getDefault().isRegistered(CreateProfileViewModel.this)) {
            EventBus.getDefault().unregister(CreateProfileViewModel.this);
        }
    }


    private void saveUserAccount(View view, String strOriginalImageUrl, String strSmallImageUrl) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        ContactsModel chatUser = new ContactsModel();
        chatUser.setId(phoneNumber);
        if (!getFamily().trim().isEmpty()) {
            chatUser.setUserName(getUserName().trim() + " " + getFamily().trim());
        } else {
            chatUser.setUserName(getUserName().trim());
        }
        chatUser.setOriginalImageUri(strOriginalImageUrl);
        chatUser.setSmallImageUri(strSmallImageUrl);

        ContactsRealmModel realmModel = new ContactsRealmModel(chatUser);
        realmModel.setIsOnline(ChatConstant.KEEP_ONLINE);
        databaseReference.child(ChatConstant.USERS).child(phoneNumber).setValue(chatUser).addOnSuccessListener(task -> {
            ChatRealmDatabase.addNewUserToChatDatabase(realmModel, appCompatActivity);
            if (PhoneActivity.phoneActivity != null) {
                PhoneActivity.phoneActivity.finish();
            }
            Utils.callIntent(appCompatActivity, ChatMainActivity.class, chatUser);
            appCompatActivity.finish();


        }).addOnFailureListener(e -> {
            view.setVisibility(View.VISIBLE);
            isConstraintClickable = true;
        });


    }

    public void onClickBackCreateProfile() {
        MyAlertDialog.createAlertDialog(appCompatActivity, appCompatActivity.getString(R.string.stop_progress_title), ChatConstant.COME_FROM_STOP_PROGRESS).show();
    }


    @Bindable
    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
        notifyPropertyChanged(BR.userName);
    }

    @Bindable
    public String getFamily() {
        return family;
    }

    public void setFamily(String family) {
        this.family = family;
        notifyPropertyChanged(BR.family);
    }


    @Bindable
    public boolean isConstraintClickable() {
        return isConstraintClickable;
    }

    public void setConstraintClickable(boolean constraintClickable) {
        isConstraintClickable = constraintClickable;
        notifyPropertyChanged(BR.constraintClickable);
    }

    @Bindable
    public String getStrTxtNetStateProfile() {
        return strTxtNetStateProfile;
    }

    public void setStrTxtNetStateProfile(String strTxtNetStateProfile) {
        this.strTxtNetStateProfile = strTxtNetStateProfile;
        notifyPropertyChanged(BR.strTxtNetStateProfile);
    }

    @Override
    public void firebaseConnectionState(int state) {
        switch (state) {
            case NO_NET:
                setStrTxtNetStateProfile(appCompatActivity.getString(R.string.waiting_for_network));
                txtNetStateProfile.setTextColor(appCompatActivity.getResources().getColor(R.color.colorNoNet));
                break;

            case CONNECTING:
                setStrTxtNetStateProfile(appCompatActivity.getString(R.string.connecting_to_server));
                txtNetStateProfile.setTextColor(appCompatActivity.getResources().getColor(R.color.colorConnecting));
                break;

            case CONNECTED_TO_FIREBASE:
                setStrTxtNetStateProfile(appCompatActivity.getString(R.string.connected_to_server));
                txtNetStateProfile.setTextColor(appCompatActivity.getResources().getColor(R.color.colorConnected));
                break;

        }
    }
}
