package com.mohammadreza.mrchat.viewmodel;

import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;

import com.google.firebase.auth.FirebaseAuth;
import com.jkb.vcedittext.VerificationCodeEditText;
import com.mohammadreza.mrchat.BR;
import com.mohammadreza.mrchat.R;
import com.mohammadreza.mrchat.constant.ChatConstant;
import com.mohammadreza.mrchat.databinding.ActivityVerifyBinding;
import com.mohammadreza.mrchat.firebase.ChatFirebase;
import com.mohammadreza.mrchat.model.ChatBusModel;
import com.mohammadreza.mrchat.receiver.NetworkStateReceiver;
import com.mohammadreza.mrchat.utils.MyAlertDialog;

import org.greenrobot.eventbus.Subscribe;

import static com.mohammadreza.mrchat.receiver.NetworkStateReceiver.CONNECTED_TO_FIREBASE;
import static com.mohammadreza.mrchat.receiver.NetworkStateReceiver.CONNECTING;
import static com.mohammadreza.mrchat.receiver.NetworkStateReceiver.NO_NET;


public class VerifyViewModel extends BaseObservable implements NetworkStateReceiver.NetworkStateReceiverListener {


    private static FirebaseAuth auth;
    private final VerificationCodeEditText verificationCodeEditText;
    private final TextView txtNetStateVerify;
    public NetworkStateReceiver networkStateReceiverVerify;
    private String codeAndPhoneNumber = "";
    private AppCompatActivity appCompatActivity;
    private String strTxtNetStateVerify;
    private boolean isCountDownFinish;
    private boolean isProgressVerifyVisible;
    private String txtResendCodeCountDown = "";
    private String strEditTextVerifyCode = "";

    public VerifyViewModel(ActivityVerifyBinding verifyBinding) {
        this.verificationCodeEditText = verifyBinding.edtVerificationCode;
        this.txtNetStateVerify = verifyBinding.txtNetStateVerify;
        this.appCompatActivity = (AppCompatActivity) verifyBinding.getRoot().getContext();
        auth = FirebaseAuth.getInstance();
        getIntentVerify();
        registerBroadCast();

        countDownToResendCode();
    }

    private void getIntentVerify() {
        Intent intent = appCompatActivity.getIntent();
        if (intent.getExtras() != null) {
            codeAndPhoneNumber = intent.getExtras().getString(ChatConstant.CODE_AND_PHONE_NUMBER);
        }
    }


    public void onClickLinearChangeNumber() {
        appCompatActivity.finish();
    }

    public void onClickBack(View view) {
        MyAlertDialog.createAlertDialog(appCompatActivity, appCompatActivity.getString(R.string.stop_progress_title), ChatConstant.COME_FROM_STOP_PROGRESS).show();
    }


    @Subscribe
    public void onEvent(ChatBusModel busModel) {
        if (busModel.isSuccess()) {
            ChatFirebase.getUserDataFromRealTimeDatabase(codeAndPhoneNumber, appCompatActivity);
        } else {
            MyAlertDialog.showAlertDialog(appCompatActivity, appCompatActivity.getString(R.string.could_not_verify));
            verificationCodeEditText.setEnabled(true);
            setProgressVerifyVisible(false);
        }
    }

    @Override
    public void firebaseConnectionState(int state) {
        switch (state) {
            case NO_NET:
                setStrTxtNetStateVerify(appCompatActivity.getString(R.string.waiting_for_network));
                txtNetStateVerify.setTextColor(appCompatActivity.getResources().getColor(R.color.colorNoNet));
                break;

            case CONNECTING:
                setStrTxtNetStateVerify(appCompatActivity.getString(R.string.connecting_to_server));
                txtNetStateVerify.setTextColor(appCompatActivity.getResources().getColor(R.color.colorConnecting));
                break;

            case CONNECTED_TO_FIREBASE:
                setStrTxtNetStateVerify(appCompatActivity.getString(R.string.connected_to_server));
                txtNetStateVerify.setTextColor(appCompatActivity.getResources().getColor(R.color.colorConnected));
                break;

        }
    }

    private void registerBroadCast() {
        networkStateReceiverVerify = new NetworkStateReceiver(appCompatActivity);
        networkStateReceiverVerify.addListener(this);
        appCompatActivity.registerReceiver(networkStateReceiverVerify, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
    }


    public void onClickResendCode(View view) {
        view.setClickable(false);
        countDownToResendCode();
        ChatFirebase.getVerificationCode(appCompatActivity.getString(R.string.iranCodeNumber) + codeAndPhoneNumber, appCompatActivity, null, ChatConstant.COME_FROM_VERIFY);
    }

    private void countDownToResendCode() {
        new CountDownTimer(60000, 1000) {
            public void onTick(long millisUntilFinished) {
                setCountDownFinish(false);
                setTxtResendCodeCountDown(millisUntilFinished / 1000 + "   " + appCompatActivity.getString(R.string.secondUntil));
            }

            public void onFinish() {
                setCountDownFinish(true);
                setTxtResendCodeCountDown(appCompatActivity.getString(R.string.resendCode));
            }
        }.start();
    }

    @Bindable
    public boolean isProgressVerifyVisible() {
        return isProgressVerifyVisible;
    }

    public void setProgressVerifyVisible(boolean progressVerifyVisible) {
        isProgressVerifyVisible = progressVerifyVisible;
        notifyPropertyChanged(BR.progressVerifyVisible);

    }

    @Bindable
    public String getStrTxtNetStateVerify() {
        return strTxtNetStateVerify;
    }

    public void setStrTxtNetStateVerify(String strTxtNetStateVerify) {
        this.strTxtNetStateVerify = strTxtNetStateVerify;
        notifyPropertyChanged(BR.strTxtNetStateVerify);
    }

    @Bindable

    public String getTxtResendCodeCountDown() {
        return txtResendCodeCountDown;
    }

    public void setTxtResendCodeCountDown(String txtResendCodeCountDown) {
        this.txtResendCodeCountDown = txtResendCodeCountDown;
        notifyPropertyChanged(BR.txtResendCodeCountDown);
    }

    @Bindable
    public boolean isCountDownFinish() {
        return isCountDownFinish;
    }

    public void setCountDownFinish(boolean countDownFinish) {
        isCountDownFinish = countDownFinish;
        notifyPropertyChanged(BR.countDownFinish);
    }

    @Bindable
    public String getCodeAndPhoneNumber() {
        return codeAndPhoneNumber;
    }

    @Bindable
    public String getStrEditTextVerifyCode() {
        if (strEditTextVerifyCode.length() == 6) {
            ChatFirebase.verifyWithPhoneNumber(auth, strEditTextVerifyCode);
            verificationCodeEditText.setEnabled(false);
            setProgressVerifyVisible(true);
        }
        return strEditTextVerifyCode;

    }


    public void setStrEditTextVerifyCode(String strEditTextVerifyCode) {
        this.strEditTextVerifyCode = strEditTextVerifyCode;
        notifyPropertyChanged(BR.strEditTextVerifyCode);
    }

}
