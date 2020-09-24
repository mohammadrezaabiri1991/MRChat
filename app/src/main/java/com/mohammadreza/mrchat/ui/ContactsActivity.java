package com.mohammadreza.mrchat.ui;

import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.mohammadreza.mrchat.R;
import com.mohammadreza.mrchat.databinding.ContactsActivityBinding;
import com.mohammadreza.mrchat.viewmodel.ContactsViewModel;
import com.r0adkll.slidr.Slidr;

public class ContactsActivity extends AppCompatActivity {

    private ContactsViewModel contactsViewModel;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        sharedPreferences = getSharedPreferences(getString(R.string.mr_chat), MODE_PRIVATE);
        setMyTheme();
        super.onCreate(savedInstanceState);
        if(android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        ContactsActivityBinding contactsBinding = DataBindingUtil.setContentView(this, R.layout.contacts_activity);
        contactsViewModel = new ContactsViewModel(this);
        contactsBinding.setContacts(contactsViewModel);
        Slidr.attach(this);

        setSettingsToRecyclerView(contactsBinding.recyclerContacts);
    }

    private void setSettingsToRecyclerView(RecyclerView mRecyclerView) {
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setNestedScrollingEnabled(false);
        mRecyclerView.setItemViewCacheSize(20);
        mRecyclerView.setDrawingCacheEnabled(true);
        mRecyclerView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    protected void onDestroy() {
        if (contactsViewModel.networkStateReceiverContacts != null) {
            try {
                unregisterReceiver(contactsViewModel.networkStateReceiverContacts);
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            }
        }

        super.onDestroy();
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    public void setMyTheme() {
        setTheme(sharedPreferences.getBoolean(getString(R.string.theme), false) ? R.style.AppTheme_SliderActivityThemeDark : R.style.AppTheme_SliderActivityThemeLight);
    }

}
