package com.mohammadreza.mrchat.ui;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.mohammadreza.mrchat.R;
import com.mohammadreza.mrchat.constant.ChatConstant;
import com.mohammadreza.mrchat.model.ContactsModel;
import com.mohammadreza.mrchat.repository.ChatRealmDatabase;
import com.mohammadreza.mrchat.utils.Utils;

public class SplashActivity extends AppCompatActivity {

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_screen);
        intentToRegisterActivity();
        try {
            ((TextView) findViewById(R.id.txtAppVersion)).setText(getString(R.string.v) + getAppVersion());
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void intentToRegisterActivity() {
        ContactsModel user = ChatRealmDatabase.checkUserExist(ChatConstant.IS_ONLINE_FIELD_NAME, ChatConstant.KEEP_ONLINE, this);
        new Handler().postDelayed(() -> {
            if (user != null) {
                Utils.callIntent(this, ChatMainActivity.class, user);
            } else {
                startActivity(new Intent(SplashActivity.this, PhoneActivity.class));
            }
            finish();
        }, 2000);

    }
    private String getAppVersion() throws PackageManager.NameNotFoundException {
        PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
        return pInfo.versionName;
    }


}