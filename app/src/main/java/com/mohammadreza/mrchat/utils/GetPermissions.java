package com.mohammadreza.mrchat.utils;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.mohammadreza.mrchat.constant.ChatConstant;
import com.mohammadreza.mrchat.cropper.MyImageCrop;


public class GetPermissions {

    public static boolean requestSmsPermission(Activity activity) {
        String permission = Manifest.permission.READ_SMS;
        int grant = ContextCompat.checkSelfPermission(activity, permission);
        if (grant != PackageManager.PERMISSION_GRANTED) {
            String[] permission_list = new String[1];
            permission_list[0] = permission;
            ActivityCompat.requestPermissions(activity, permission_list, ChatConstant.SMS_REQ_CODE);
            return false;
        } else {
            return true;
        }
    }

    public static boolean requestContactsPermission(Activity activity) {
        String permission = Manifest.permission.READ_CONTACTS;
        String storage = Manifest.permission.READ_EXTERNAL_STORAGE;
        int grant = ContextCompat.checkSelfPermission(activity, permission);
        if (grant != PackageManager.PERMISSION_GRANTED) {
            String[] permission_list = new String[2];
            permission_list[0] = permission;
            permission_list[1] = storage;
            ActivityCompat.requestPermissions(activity, permission_list, ChatConstant.CONTACT_PER_CODE);
            return false;
        } else {
            return true;
        }
    }

    public static void checkStoragePermission(AppCompatActivity appCompatActivity, BottomSheetDialog bottomSheerDialog) {
        String[] permission = {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_APN_SETTINGS};
        int grantRead = ContextCompat.checkSelfPermission(appCompatActivity, permission[0]);
        int grantWrite = ContextCompat.checkSelfPermission(appCompatActivity, permission[1]);

        if (grantRead != PackageManager.PERMISSION_GRANTED && grantWrite != PackageManager.PERMISSION_GRANTED) {
            String[] permission_list;
            permission_list = permission;
            ActivityCompat.requestPermissions(appCompatActivity, permission_list, ChatConstant.EX_STORAGE_REQ_CODE);
        } else {
            MyImageCrop.pickFromGallery(appCompatActivity);
            if (bottomSheerDialog != null) {
                bottomSheerDialog.cancel();
            }
        }
    }

    public static void checkCameraPermission(AppCompatActivity appCompatActivity, BottomSheetDialog bottomSheerDialog) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && appCompatActivity.checkSelfPermission(
                Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            appCompatActivity.requestPermissions(new String[]{Manifest.permission.CAMERA}, ChatConstant.CAMERA_PER_REQUEST);
        } else {
            MyImageCrop.openCamera(appCompatActivity);
            bottomSheerDialog.cancel();
        }
    }


}
