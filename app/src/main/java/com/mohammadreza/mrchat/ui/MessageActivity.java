package com.mohammadreza.mrchat.ui;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.EditText;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.mohammadreza.mrchat.R;
import com.mohammadreza.mrchat.databinding.ActivityMessageBinding;
import com.mohammadreza.mrchat.model.EventBusModel;
import com.mohammadreza.mrchat.utils.Utils;
import com.mohammadreza.mrchat.viewmodel.MessageViewModel;
import com.r0adkll.slidr.Slidr;
import com.r0adkll.slidr.model.SlidrInterface;

import org.greenrobot.eventbus.EventBus;


public class MessageActivity extends MainAndMessageParentActivity {

    public static boolean isMessageActivityRunning;
    private MessageViewModel messageViewModel;
    private SharedPreferences sharedPreferences;
    private int pastVisibleItems, visibleItemCount, totalItemCount;
    private boolean loading = true;
    private LinearLayoutManager recyclerLManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        sharedPreferences = getSharedPreferences(getString(R.string.mr_chat), MODE_PRIVATE);
        setMyTheme();
        super.onCreate(savedInstanceState);
        if(android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        ActivityMessageBinding messageBinding = DataBindingUtil.setContentView(this, R.layout.activity_message);
        setSettingsToRecyclerView(messageBinding.recyclerMessage);
        messageViewModel = new MessageViewModel(this, messageBinding);
        setLazyLoadToRecyclerView(messageBinding.recyclerMessage);
        messageBinding.setMessage(messageViewModel);

        setFabBehavior(messageBinding.recyclerMessage, messageBinding.floatingMessage);

        SlidrInterface sliderInterface = Slidr.attach(this);

        changeWindowBackground(messageBinding.activityMessageFooterId.edtEnterMessage);
        lockSliderWhenKeyboardIsOpen(messageBinding.activityMessageFooterId.linearMessageFooter, sliderInterface);
    }

    private void setLazyLoadToRecyclerView(RecyclerView mRecyclerView) {
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                if (dy > 0) { //check for scroll down
                    visibleItemCount = recyclerLManager.getChildCount();
                    totalItemCount = recyclerLManager.getItemCount();
                    pastVisibleItems = recyclerLManager.findFirstVisibleItemPosition();

                    if (loading) {
                        if ((visibleItemCount + pastVisibleItems) >= totalItemCount) {
                            loading = false;
                        }
                    }
                }
            }
        });
    }

    private void setSettingsToRecyclerView(RecyclerView mRecyclerView) {
        recyclerLManager = new LinearLayoutManager(this);
        recyclerLManager.setStackFromEnd(true);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setNestedScrollingEnabled(false);
        mRecyclerView.setItemViewCacheSize(20);
        mRecyclerView.setItemAnimator(null);
        mRecyclerView.setDrawingCacheEnabled(true);
        mRecyclerView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
        mRecyclerView.setLayoutManager(recyclerLManager);


        mRecyclerView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                mRecyclerView.setVisibility(View.VISIBLE);
                mRecyclerView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });
    }


    private void setFabBehavior(RecyclerView recyclerView, FloatingActionButton fabGoToFirst) {
        fabGoToFirst.hide();
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                if (dy > 1 && !fabGoToFirst.isShown()) {
                    fabGoToFirst.show();
                } else if (dy < 1 && fabGoToFirst.isShown()) {
                    fabGoToFirst.hide();
                }
            }

            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                if (recyclerLManager.getItemCount() > 0) {
                    if (recyclerLManager.findLastVisibleItemPosition() >= recyclerLManager.getItemCount() - 1) {
                        fabGoToFirst.hide();
                    }
                }
                super.onScrollStateChanged(recyclerView, newState);
            }
        });
    }


    @SuppressLint("ClickableViewAccessibility")
    private void changeWindowBackground(EditText edtEnterMessage) {
        edtEnterMessage.setOnTouchListener((view, motionEvent) -> {
            getWindow().getDecorView().setBackgroundColor(Color.WHITE);
            return false;
        });

    }

    private void lockSliderWhenKeyboardIsOpen(LinearLayout contentView, SlidrInterface sliderInterface) {
        contentView.getViewTreeObserver().addOnGlobalLayoutListener(() -> {
            Rect r = new Rect();
            contentView.getWindowVisibleDisplayFrame(r);
            int screenHeight = contentView.getRootView().getHeight();
            int keypadHeight = screenHeight - r.bottom;
            if (keypadHeight > screenHeight * 0.15) {
                sliderInterface.lock();

            } else {
                getWindow().getDecorView().setBackgroundColor(getResources().getColor(R.color.colorTransparent));
                sliderInterface.unlock();
            }
        });
    }


    @Override
    protected void onStop() {
        super.onStop();
        EventBusModel busModel = new EventBusModel();
        busModel.setMessageActivityPaused(true);
        EventBus.getDefault().post(busModel);
        isMessageActivityRunning = false;
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!EventBus.getDefault().isRegistered(messageViewModel)) {
            EventBus.getDefault().register(messageViewModel);
        }
        isMessageActivityRunning = true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        Utils.hideKeyboard(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(messageViewModel.connection);
        if (EventBus.getDefault().isRegistered(messageViewModel)) {
            EventBus.getDefault().unregister(messageViewModel);
        }
        if (messageViewModel.networkStateReceiverMessage != null) {
            try {
                unregisterReceiver(messageViewModel.networkStateReceiverMessage);
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            }
        }

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
