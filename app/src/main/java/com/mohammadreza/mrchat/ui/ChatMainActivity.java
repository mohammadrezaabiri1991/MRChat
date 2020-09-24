package com.mohammadreza.mrchat.ui;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.view.GravityCompat;
import androidx.databinding.DataBindingUtil;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.mohammadreza.mrchat.R;
import com.mohammadreza.mrchat.constant.ChatConstant;
import com.mohammadreza.mrchat.cropper.CropImage;
import com.mohammadreza.mrchat.cropper.MyImageCrop;
import com.mohammadreza.mrchat.databinding.BottomSheetDrawerBinding;
import com.mohammadreza.mrchat.databinding.ChatDrawerBinding;
import com.mohammadreza.mrchat.databinding.NavHeaderChatBinding;
import com.mohammadreza.mrchat.utils.GetPermissions;
import com.mohammadreza.mrchat.viewmodel.ChatMainViewModel;

import org.greenrobot.eventbus.EventBus;

public class ChatMainActivity extends MainAndMessageParentActivity {
    private ChatDrawerBinding chatBinding;
    private BottomSheetDialog bottomSheerDialogDrawer;
    private ChatMainViewModel chatMainViewModel;
    private SharedPreferences sharedPreferences;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        sharedPreferences = getSharedPreferences(getString(R.string.mr_chat), MODE_PRIVATE);
        setMyTheme();
        super.onCreate(savedInstanceState);
        if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
        overridePendingTransition(0, 0);
        chatBinding = DataBindingUtil.setContentView(this, R.layout.chat_drawer);

        View headerView = chatBinding.navViewChat.getHeaderView(0);
        final NavHeaderChatBinding headerBinding = NavHeaderChatBinding.bind(headerView);

        bottomSheerDialogDrawer = new BottomSheetDialog(this);
        BottomSheetDrawerBinding drawerSheetBinding = DataBindingUtil.inflate(LayoutInflater.from(this), R.layout.bottom_sheet_drawer, null, false);
        bottomSheerDialogDrawer.setContentView(drawerSheetBinding.getRoot());

        chatMainViewModel = new ChatMainViewModel(this, chatBinding, headerBinding, drawerSheetBinding, bottomSheerDialogDrawer);
        chatBinding.setChat(chatMainViewModel);
        headerBinding.setNavChat(chatMainViewModel);
        drawerSheetBinding.setSheetDrawer(chatMainViewModel);


    }


    public void setMyTheme() {
        setTheme(sharedPreferences.getBoolean(getString(R.string.theme), false) ? R.style.DarkTheme : R.style.AppTheme);
    }

    @Override
    public void onBackPressed() {
        if (chatBinding.drawerLayoutChat.isDrawerOpen(GravityCompat.START)) {
            chatBinding.drawerLayoutChat.closeDrawer(GravityCompat.START);
            return;
        }
        super.onBackPressed();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);
        if (imageReturnedIntent == null) {
            return;
        }

        if (requestCode == ChatConstant.PICK_CONTACT_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                actionSendSms(imageReturnedIntent.getData());
            }
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(imageReturnedIntent);
            if (resultCode == RESULT_OK) {
                MyImageCrop myImageCrop = new MyImageCrop(this);
                myImageCrop.selectImageFromDeviceAction(result.getUri());
            }

        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case ChatConstant.EX_STORAGE_REQ_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    MyImageCrop.pickFromGallery(this);
                    bottomSheerDialogDrawer.cancel();
                }
                break;

            case ChatConstant.CAMERA_PER_REQUEST:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    MyImageCrop.openCamera(this);
                    bottomSheerDialogDrawer.cancel();
                }
                break;

            case ChatConstant.CONTACT_PER_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
                    startActivityForResult(intent, ChatConstant.PICK_CONTACT_CODE);
                }
                break;
        }
    }

    private void actionSendSms(Uri contactData) {
        Cursor cursor = managedQuery(contactData, null, null, null, null);
        if (cursor.moveToFirst()) {
            String id = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.Contacts._ID));
            String hasPhone = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER));
            if (hasPhone.equalsIgnoreCase("1")) {
                Cursor phones = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + id,
                        null, null);
                if (phones != null) {
                    phones.moveToFirst();
                    String cNumber = phones.getString(phones.getColumnIndex(getString(R.string.data1)));
                    if (cNumber != null && !cNumber.isEmpty()) {
                        if (GetPermissions.requestSmsPermission(this)) {
                            sendSMSToInvite(cNumber);
                        }
                    }
                    phones.close();
                    cursor.close();
                }


            }
        }
    }

    public void sendSMSToInvite(String number) {
        Uri uri = Uri.parse(getString(R.string.sms_to) + number);
        Intent intent = new Intent(Intent.ACTION_SENDTO, uri);
        intent.putExtra(getString(R.string.sms_body), getString(R.string.invite_text));
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        if (EventBus.getDefault().isRegistered(chatMainViewModel)) {
            EventBus.getDefault().unregister(chatMainViewModel);
        }
        if (chatMainViewModel.networkStateReceiverMain != null) {
            try {
                unregisterReceiver(chatMainViewModel.networkStateReceiverMain);
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            }
        }
        super.onDestroy();

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!EventBus.getDefault().isRegistered(chatMainViewModel)) {
            EventBus.getDefault().register(chatMainViewModel);
        }
        chatMainViewModel.chatActivityIsRunning = true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        chatMainViewModel.chatActivityIsRunning = false;
    }
}
