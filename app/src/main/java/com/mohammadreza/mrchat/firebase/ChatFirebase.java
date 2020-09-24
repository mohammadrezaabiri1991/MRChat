package com.mohammadreza.mrchat.firebase;

import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.mohammadreza.mrchat.R;
import com.mohammadreza.mrchat.constant.ChatConstant;
import com.mohammadreza.mrchat.model.ChatBusModel;
import com.mohammadreza.mrchat.model.ContactsModel;
import com.mohammadreza.mrchat.model.ContactsRealmModel;
import com.mohammadreza.mrchat.repository.ChatRealmDatabase;
import com.mohammadreza.mrchat.ui.ChatMainActivity;
import com.mohammadreza.mrchat.ui.CreateProfileActivity;
import com.mohammadreza.mrchat.ui.VerifyActivity;
import com.mohammadreza.mrchat.utils.MyAlertDialog;
import com.mohammadreza.mrchat.utils.Utils;

import org.greenrobot.eventbus.EventBus;

import java.io.ByteArrayOutputStream;
import java.util.concurrent.TimeUnit;

import static com.mohammadreza.mrchat.utils.Utils.callIntent;


public class ChatFirebase {
    private static final long VERIFY_TIMEOUT = 70;
    private static String verificationCode = "";


    public static void getVerificationCode(String phoneNumber, AppCompatActivity appCompatActivity, ImageButton v, String comFrom) {
        PhoneAuthProvider.OnVerificationStateChangedCallbacks callback = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                if (comFrom.contentEquals(ChatConstant.COME_FROM_PHONE)) {
                    if (phoneAuthCredential.getSmsCode() == null) {
                        MyAlertDialog.showAlertDialog(appCompatActivity, appCompatActivity.getString(R.string.too_many_try));
                        v.setVisibility(View.VISIBLE);
                    }
                }
            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {
                if (comFrom.contentEquals(ChatConstant.COME_FROM_PHONE)) {
                    if (Utils.isOnline(appCompatActivity)) {
                        if (e.getMessage() != null && e.getMessage().equals(appCompatActivity.getString(R.string.error_too_many_att))) {
                            MyAlertDialog.showAlertDialog(appCompatActivity, appCompatActivity.getString(R.string.too_many_try));
                        } else {
                            MyAlertDialog.showAlertDialog(appCompatActivity, appCompatActivity.getString(R.string.no_access_to_net));
                        }

                    } else {
                        MyAlertDialog.showAlertDialog(appCompatActivity, appCompatActivity.getString(R.string.no_access_to_net));
                    }
                    v.setVisibility(View.VISIBLE);

                    Log.i("MY_TAG", "e" + e.getMessage());
                }
            }

            @Override
            public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                super.onCodeSent(s, forceResendingToken);
                verificationCode = s;
                if (comFrom.contentEquals(ChatConstant.COME_FROM_PHONE)) {
                    v.setVisibility(View.VISIBLE);
                    callIntent(appCompatActivity, VerifyActivity.class, ChatConstant.CODE_AND_PHONE_NUMBER, phoneNumber);
                    Log.i("MY_TAG", "onCodeSent");
                }


            }
        };

//        Log.i("MY_TAG", "phoneNumber" + phoneNumber);
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber,        // Phone number to verify
                VERIFY_TIMEOUT,                 // Timeout duration
                TimeUnit.SECONDS,   // Unit of timeout
                appCompatActivity,               // Activity (for callback binding)
                callback);


    }

    public static void verifyWithPhoneNumber(FirebaseAuth auth, String strVerifyCode) {
        if (!verificationCode.isEmpty()) {
            PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationCode, strVerifyCode);
            signInWithPhone(auth, credential);
        } else {
            EventBus.getDefault().post(new ChatBusModel(false));
            Log.i("MY_TAG", "verificationCode.isEmpty()");
        }


    }

    private static void signInWithPhone(final FirebaseAuth auth, PhoneAuthCredential credential) {
        auth.signInWithCredential(credential).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                EventBus.getDefault().post(new ChatBusModel(true));
                Log.i("MY_TAG", "isSuccessful");

            } else {
                EventBus.getDefault().post(new ChatBusModel(false));
                Log.i("MY_TAG", "its failed");
                if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                    Log.i("MY_TAG", " FirebaseAuthInvalidCredentialsException");
                }
            }
        });
    }

    public static void getUserDataFromRealTimeDatabase(String phoneNumber, AppCompatActivity appCompatActivity) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference(ChatConstant.USERS).child(phoneNumber);
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.i("MY_TAG", "onDataChange");
                ContactsModel contactsModel = dataSnapshot.getValue(ContactsModel.class);
                if (contactsModel != null) {
                    ContactsRealmModel realmModel = new ContactsRealmModel(contactsModel);
                    realmModel.setIsOnline(ChatConstant.KEEP_ONLINE);
                    ChatRealmDatabase.addNewUserToChatDatabase(realmModel, appCompatActivity);
                    callIntent(appCompatActivity, ChatMainActivity.class, contactsModel);


                } else {
                    callIntent(appCompatActivity, CreateProfileActivity.class, ChatConstant.CODE_AND_PHONE_NUMBER, phoneNumber);
                }
                appCompatActivity.finish();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.i("MY_TAG", "databaseError  " + databaseError.getMessage());
//                btnDoneVerify.setVisibility(View.VISIBLE);
                MyAlertDialog.showAlertDialog(appCompatActivity, appCompatActivity.getString(R.string.could_not_verify));
            }
        });

    }


    //    --------------------------- STORAGE CREATE TASK
    public static Task<Uri> createOriginalUriTask(StorageReference storage, Bitmap originalProfilePicture, String phoneNumber) {
        StorageReference originalReference = storage.child(ChatConstant.STORAGE_ORIGINAL_IMAGE_PATH + phoneNumber + ChatConstant.JPG);

        ByteArrayOutputStream outputStreamOriginal = new ByteArrayOutputStream();
        originalProfilePicture.compress(Bitmap.CompressFormat.JPEG, 90, outputStreamOriginal);
        byte[] dataOriginal = outputStreamOriginal.toByteArray();
        UploadTask uploadTaskOriginal = originalReference.putBytes(dataOriginal);


        return uploadTaskOriginal.continueWithTask(taskOriginal -> {
            if (!taskOriginal.isSuccessful() && taskOriginal.getException() != null) {
                throw taskOriginal.getException();
            }
            return originalReference.getDownloadUrl();
        });
    }

    public static Task<Uri> createSmallUriTask(StorageReference storage, Bitmap smallProfilePicture, String phoneNumber) {
        StorageReference smallReference = storage.child(ChatConstant.STORAGE_SMALL_IMAGE_PATH + phoneNumber + ChatConstant.JPG);
        ByteArrayOutputStream outputStreamSmall = new ByteArrayOutputStream();
        smallProfilePicture.compress(Bitmap.CompressFormat.JPEG, 100, outputStreamSmall);
        byte[] dataSmall = outputStreamSmall.toByteArray();
        UploadTask uploadTaskSmall = smallReference.putBytes(dataSmall);


        return uploadTaskSmall.continueWithTask(smallTask -> {
            if (!smallTask.isSuccessful() && smallTask.getException() != null) {
                throw smallTask.getException();
            }
            return smallReference.getDownloadUrl();
        });
    }
}

