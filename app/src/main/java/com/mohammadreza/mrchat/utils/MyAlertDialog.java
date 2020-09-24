package com.mohammadreza.mrchat.utils;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.text.Html;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.github.chrisbanes.photoview.PhotoView;
import com.mohammadreza.mrchat.R;
import com.mohammadreza.mrchat.application.MyApp;
import com.mohammadreza.mrchat.constant.ChatConstant;
import com.mohammadreza.mrchat.repository.ChatRealmDatabase;

import jp.wasabeef.blurry.Blurry;

public class MyAlertDialog {

    public static AlertDialog createAlertDialog(AppCompatActivity appCompatActivity, String titleMessage, String strComeFrom) {
        AlertDialog.Builder builder = new AlertDialog.Builder(appCompatActivity);
        builder.setTitle(appCompatActivity.getString(R.string.mr_chat));
        builder.setIcon(R.mipmap.ic_launcher_round);
        builder.setMessage(titleMessage);

        builder.setCancelable(true);

        builder.setPositiveButton(Html.fromHtml("<font color='#777777'>Yes</font>"), (dialog, id) -> {
            switch (strComeFrom) {
                case ChatConstant.COME_FROM_LOG_OUT_ACC:
                    actionLogOut(appCompatActivity);
                    break;

                case ChatConstant.COME_FROM_STOP_PROGRESS:
                    appCompatActivity.finish();
                    break;
            }
        });

        builder.setNegativeButton(
                Html.fromHtml("<font color='#777777'>No</font>"),
                (dialog, id) -> dialog.cancel());

        return builder.create();
    }


    private static void actionLogOut(AppCompatActivity appCompatActivity) {
        ChatRealmDatabase.removeAllDatabases(appCompatActivity);


    }

    public static void createFullScreenImageDialog(AppCompatActivity appCompatActivity, Drawable smallDrawable, String originalImageUrl, String userId) {
        if (!appCompatActivity.isFinishing()) {
            AlertDialog.Builder alert = new AlertDialog.Builder(appCompatActivity, R.style.MyCustomTheme);
            LayoutInflater factory = LayoutInflater.from(appCompatActivity);
            final View view = factory.inflate(R.layout.full_screen_image, null);
            alert.setView(view);
            final AlertDialog dialog = alert.create();
            WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
            if (dialog.getWindow() != null) {
                layoutParams.copyFrom(dialog.getWindow().getAttributes());
                layoutParams.gravity = Gravity.CENTER;
                dialog.getWindow().setGravity(Gravity.CENTER);
                layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
                layoutParams.height = WindowManager.LayoutParams.MATCH_PARENT;
            }
            PhotoView imageView = view.findViewById(R.id.imgFullScreen);
            ImageView imgBack = view.findViewById(R.id.imgBackFullScreenImage);
            ProgressBar progressBar = view.findViewById(R.id.ProgressLoadImage);
            initializeImageView(imageView, smallDrawable, originalImageUrl, appCompatActivity, userId, progressBar);
            dialog.getWindow().setAttributes(layoutParams);
            dialog.show();
            imgBack.setOnClickListener(v -> dialog.dismiss());

        }
    }

    private static void initializeImageView(PhotoView imageView, Drawable drawable, String originalImageUrl, AppCompatActivity appCompatActivity, String userId, ProgressBar progressBar) {
        imageView.setImageDrawable(drawable);
        Utils.loadImageFromStorage(userId + appCompatActivity.getResources().getString(R.string.original), appCompatActivity, null, imageView);

        Glide.with(MyApp.appContext)
                .asBitmap()
                .load(originalImageUrl)
                .into(new CustomTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resources, @Nullable Transition<? super Bitmap> transition) {
                        imageView.setImageBitmap(resources);
                        Utils.saveToInternalStorage(userId + appCompatActivity.getString(R.string.original), resources, appCompatActivity);
                        progressBar.setVisibility(View.GONE);
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {
                    }

                    @Override
                    public void onLoadStarted(@Nullable Drawable placeholder) {
                        super.onLoadStarted(placeholder);
                        try {
                            Blurry.with(appCompatActivity).radius(10)
                                    .sampling(8).capture(imageView.getRootView()).into(imageView);
                        } catch (Exception ignore) {
                        }

                        progressBar.setVisibility(View.VISIBLE);

                    }
                });


    }

    public static ProgressDialog showProgressDialog(AppCompatActivity appCompatActivity) {
        ProgressDialog progressDialog = new ProgressDialog(appCompatActivity);
        progressDialog.setMessage(ChatConstant.PLEASE_WAIT);
        progressDialog.setCancelable(false);
        progressDialog.show();
        return progressDialog;
    }


    public static void showAlertDialog(final AppCompatActivity appCompatActivity, final String message) {
        new android.app.AlertDialog.Builder(appCompatActivity)
                .setTitle(appCompatActivity.getResources().getString(R.string.mr_chat))
                .setMessage(message)
                .setPositiveButton(android.R.string.ok, null)
                .setIcon(R.mipmap.ic_launcher_round)
                .show();
    }

    public static void createInfoDialog(AppCompatActivity appCompatActivity) {
        if (!appCompatActivity.isFinishing()) {
            AlertDialog.Builder alert = new AlertDialog.Builder(appCompatActivity, 0);
            AlertDialog alertDialog = alert.create();
            LayoutInflater factory = LayoutInflater.from(appCompatActivity);
            final View view = factory.inflate(R.layout.drawer_info_layout, null);
            alertDialog.setView(view);
            alertDialog.show();
            view.findViewById(R.id.headerViewInfo).setOnClickListener(view1 -> alertDialog.cancel());


        }
    }
}
