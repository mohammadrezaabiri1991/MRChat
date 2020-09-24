package com.mohammadreza.mrchat.ui;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.mohammadreza.mrchat.R;
import com.mohammadreza.mrchat.constant.ChatConstant;
import com.mohammadreza.mrchat.cropper.CropImage;
import com.mohammadreza.mrchat.cropper.MyImageCrop;
import com.mohammadreza.mrchat.databinding.BottomSheetBinding;
import com.mohammadreza.mrchat.utils.MyAlertDialog;
import com.mohammadreza.mrchat.viewmodel.CreateProfileViewModel;

import org.greenrobot.eventbus.EventBus;

public class CreateProfileActivity extends AppCompatActivity {
    private BottomSheetDialog bottomSheerDialog;
    private CreateProfileViewModel createProfileVM;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        com.mohammadreza.mrchat.databinding.ActivityChatCreateProfileBinding createProfileBinding = DataBindingUtil.setContentView(this, R.layout.activity_chat_create_profile);
        bottomSheerDialog = new BottomSheetDialog(this);
        BottomSheetBinding sheetBinding = DataBindingUtil.inflate(LayoutInflater.from(this), R.layout.bottom_sheet, null, false);
        bottomSheerDialog.setContentView(sheetBinding.getRoot());
        createProfileVM = new CreateProfileViewModel(createProfileBinding, bottomSheerDialog);
        createProfileBinding.setCreateProfile(createProfileVM);
        sheetBinding.setBottomSheet(createProfileVM);


    }

    @Override
    public void onBackPressed() {
        MyAlertDialog.createAlertDialog(this, this.getString(R.string.stop_progress_title), ChatConstant.COME_FROM_STOP_PROGRESS).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (intent == null) {
            return;
        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(intent);
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
                    bottomSheerDialog.cancel();
                }
                break;

            case ChatConstant.CAMERA_PER_REQUEST:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    MyImageCrop.openCamera(this);
                    bottomSheerDialog.cancel();
                }
                break;


        }
    }

    @Override
    protected void onDestroy() {
        if (EventBus.getDefault().isRegistered(createProfileVM)) {
            EventBus.getDefault().unregister(createProfileVM);
        }
        if (createProfileVM.networkStateReceiverProfile != null) {
            try {
                unregisterReceiver(createProfileVM.networkStateReceiverProfile);
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            }
        }

        super.onDestroy();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!EventBus.getDefault().isRegistered(createProfileVM)) {
            EventBus.getDefault().register(createProfileVM);
        }

    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();
            if (v instanceof EditText) {
                Rect outRect = new Rect();
                v.getGlobalVisibleRect(outRect);
                if (!outRect.contains((int) event.getRawX(), (int) event.getRawY())) {
                    v.clearFocus();
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
            }
        }
        return super.dispatchTouchEvent(event);
    }

}
