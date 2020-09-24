package com.mohammadreza.mrchat.cropper;

import android.graphics.Bitmap;
import android.net.Uri;

import androidx.appcompat.app.AppCompatActivity;

import com.mohammadreza.mrchat.constant.ChatConstant;
import com.mohammadreza.mrchat.model.EventBusModel;

import org.greenrobot.eventbus.EventBus;

import java.io.IOException;

import id.zelory.compressor.Compressor;


public class MyImageCrop {
    public static String stringComeFrom = "";
    private AppCompatActivity activity;

    public MyImageCrop(AppCompatActivity activity) {
        this.activity = activity;
    }

    public static void pickFromGallery(AppCompatActivity appCompatActivity) {
        stringComeFrom = ChatConstant.COME_FROM_GALLERY;
        openCropActivity(appCompatActivity);

    }

    public static void openCamera(AppCompatActivity appCompatActivity) {
        stringComeFrom = ChatConstant.COME_FROM_CAMERA;
        openCropActivity(appCompatActivity);


    }


    private static void openCropActivity(AppCompatActivity appCompatActivity) {
        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .setCropShape(CropImageView.CropShape.OVAL)
                .setFixAspectRatio(true)
                .setAllowFlipping(false)
                .setAllowRotation(false)
                .setRotationDegrees(0)
                .setRequestedSize(500, 500)
                .start(appCompatActivity);
    }

    public void selectImageFromDeviceAction(final Uri uri) {
        if (uri == null) {
            return;
        }
//        new Thread(() -> {
        try {
            Bitmap bitmap = new Compressor(activity)
                    .setCompressFormat(Bitmap.CompressFormat.JPEG)
//                        .setMaxHeight(960)
//                        .setMaxWidth(640)
                    .setQuality(85)
                    .compressToBitmap(FileUtil.from(activity, uri));

            sendBitmapToProfile(bitmap);

        } catch (IOException e) {
            e.printStackTrace();
        }


//        }).start();


    }

    private void sendBitmapToProfile(Bitmap bitmap) {
        if (bitmap != null) {
            EventBusModel busModel = new EventBusModel();
            busModel.setBitmap(bitmap);
            EventBus.getDefault().post(busModel);
        }
    }


}
