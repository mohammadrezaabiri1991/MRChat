package com.mohammadreza.mrchat.ui;


import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.mohammadreza.mrchat.R;
import com.mohammadreza.mrchat.constant.ChatConstant;
import com.mohammadreza.mrchat.databinding.ActivityPhoneBinding;
import com.mohammadreza.mrchat.model.ChatBusModel;
import com.mohammadreza.mrchat.model.ContactsModel;
import com.mohammadreza.mrchat.repository.ChatRealmDatabase;
import com.mohammadreza.mrchat.utils.MyAlertDialog;
import com.mohammadreza.mrchat.utils.Utils;
import com.mohammadreza.mrchat.viewmodel.PhoneViewModel;

import org.greenrobot.eventbus.EventBus;

public class PhoneActivity extends AppCompatActivity {


    private PhoneViewModel phoneViewModel;
    public static AppCompatActivity phoneActivity;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityPhoneBinding phoneBinding = DataBindingUtil.setContentView(this, R.layout.activity_phone);
        phoneViewModel = new PhoneViewModel(phoneBinding);
        phoneBinding.setPhone(phoneViewModel);
        phoneActivity = this;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        try {
            if (requestCode == ChatConstant.SMS_REQ_CODE) {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    EventBus.getDefault().post(new ChatBusModel(true));
                } else {
                    MyAlertDialog.showAlertDialog(this, getString(R.string.need_permission));
                }
            }
        } catch (Exception e) {
        }


    }

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(phoneViewModel);
    }

    @Override
    protected void onStop() {
        EventBus.getDefault().unregister(phoneViewModel);
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        if (phoneViewModel.networkStateReceiverPhone != null) {
            try {
                unregisterReceiver(phoneViewModel.networkStateReceiverPhone);
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            }
        }
        super.onDestroy();

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
