package com.mohammadreza.mrchat.ui;

import android.content.Context;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.mohammadreza.mrchat.R;
import com.mohammadreza.mrchat.constant.ChatConstant;
import com.mohammadreza.mrchat.databinding.ActivityVerifyBinding;
import com.mohammadreza.mrchat.utils.MyAlertDialog;
import com.mohammadreza.mrchat.viewmodel.VerifyViewModel;

import org.greenrobot.eventbus.EventBus;


public class VerifyActivity extends AppCompatActivity {

    private VerifyViewModel verifyViewModel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityVerifyBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_verify);
        verifyViewModel = new VerifyViewModel(binding);
        binding.setVerify(verifyViewModel);
    }

    @Override
    public void onBackPressed() {
        MyAlertDialog.createAlertDialog(this, this.getString(R.string.stop_progress_title), ChatConstant.COME_FROM_STOP_PROGRESS).show();
    }

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(verifyViewModel);
    }

    @Override
    protected void onStop() {
        EventBus.getDefault().unregister(verifyViewModel);
        super.onStop();
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
