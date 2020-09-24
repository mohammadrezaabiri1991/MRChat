package com.mohammadreza.mrchat.viewmodel;

import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;

import com.google.android.material.textfield.TextInputLayout;
import com.mohammadreza.mrchat.BR;
import com.mohammadreza.mrchat.R;
import com.mohammadreza.mrchat.constant.ChatConstant;
import com.mohammadreza.mrchat.databinding.ActivityPhoneBinding;
import com.mohammadreza.mrchat.firebase.ChatFirebase;
import com.mohammadreza.mrchat.model.ChatBusModel;
import com.mohammadreza.mrchat.receiver.NetworkStateReceiver;

import org.greenrobot.eventbus.Subscribe;

import static com.mohammadreza.mrchat.receiver.NetworkStateReceiver.CONNECTED_TO_FIREBASE;
import static com.mohammadreza.mrchat.receiver.NetworkStateReceiver.CONNECTING;
import static com.mohammadreza.mrchat.receiver.NetworkStateReceiver.NO_NET;
import static com.mohammadreza.mrchat.utils.GetPermissions.requestSmsPermission;
import static com.mohammadreza.mrchat.utils.MyAnimationUtils.animationShakeError;
import static com.mohammadreza.mrchat.utils.MyAnimationUtils.setVibrate;


public class PhoneViewModel extends BaseObservable implements NetworkStateReceiver.NetworkStateReceiverListener {
    public NetworkStateReceiver networkStateReceiverPhone;
    private TextView txtConnectionState;
    private ImageButton btnSendCodePhone;
    private AppCompatActivity appCompatActivity;
    private String phoneNumber = "";
    private String txtNetStatePhone;
    private int keyDel;


    public PhoneViewModel(ActivityPhoneBinding activityPhoneBinding) {
        appCompatActivity = (AppCompatActivity) activityPhoneBinding.getRoot().getContext();
        btnSendCodePhone = activityPhoneBinding.btnSendCodePhone;
        txtConnectionState = activityPhoneBinding.txtNetStatePhone;

        registerBroadCast();

    }


    @Subscribe
    public void onEvent(ChatBusModel model) {
        if (model.isSuccess()) {
            actionIsPermissionSuccess(btnSendCodePhone);
        }

    }

    private void actionIsPermissionSuccess(ImageButton v) {
        v.setVisibility(View.INVISIBLE);
        ChatFirebase.getVerificationCode(appCompatActivity.getString(R.string.iranCodeNumber) + getPhoneNumber(), appCompatActivity, v, ChatConstant.COME_FROM_PHONE);
    }


    public void onClickBtnPhone(View v, TextInputLayout textInputLayout) {
        if (getPhoneNumber().length() == 10) {
            if (requestSmsPermission(appCompatActivity)) {
                actionIsPermissionSuccess((ImageButton) v);
            }
        } else if (getPhoneNumber().length() < 10) {
            textInputLayout.startAnimation(animationShakeError());
            setVibrate(appCompatActivity);
        }

    }

    @Override
    public void firebaseConnectionState(int state) {
        switch (state) {
            case NO_NET:
                setTxtNetStatePhone(appCompatActivity.getString(R.string.waiting_for_network));
                txtConnectionState.setTextColor(appCompatActivity.getResources().getColor(R.color.colorNoNet));
                break;

            case CONNECTING:
                setTxtNetStatePhone(appCompatActivity.getString(R.string.connecting_to_server));
                txtConnectionState.setTextColor(appCompatActivity.getResources().getColor(R.color.colorConnecting));
                break;

            case CONNECTED_TO_FIREBASE:
                setTxtNetStatePhone(appCompatActivity.getString(R.string.connected_to_server));
                txtConnectionState.setTextColor(appCompatActivity.getResources().getColor(R.color.colorConnected));
                break;

        }
    }

    private void registerBroadCast() {
        networkStateReceiverPhone = new NetworkStateReceiver(appCompatActivity);
        networkStateReceiverPhone.addListener(this);
        appCompatActivity.registerReceiver(networkStateReceiverPhone, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
    }


    @Bindable
    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
        notifyPropertyChanged(BR.phoneNumber);
    }

    @Bindable
    public String getTxtNetStatePhone() {
        return txtNetStatePhone;
    }

    public void setTxtNetStatePhone(String txtNetStatePhone) {
        this.txtNetStatePhone = txtNetStatePhone;
        notifyPropertyChanged(BR.txtNetStatePhone);

    }

}


