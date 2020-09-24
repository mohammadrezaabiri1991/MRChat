package com.mohammadreza.mrchat.utils;

import android.animation.Animator;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.animation.CycleInterpolator;
import android.view.animation.TranslateAnimation;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.mohammadreza.mrchat.R;

import static android.content.Context.MODE_PRIVATE;


public class MyAnimationUtils {
    public static void setVibrate(AppCompatActivity activity) {
        Vibrator vibrator = (Vibrator) activity.getSystemService(Context.VIBRATOR_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            assert vibrator != null;
            vibrator.vibrate(VibrationEffect.createOneShot(300, VibrationEffect.DEFAULT_AMPLITUDE));
        } else {
            if (vibrator != null) {
                vibrator.vibrate(300);
            }
        }
    }

    public static TranslateAnimation animationShakeError() {
        TranslateAnimation shake = new TranslateAnimation(0, 10, 0, 0);
        shake.setDuration(400);
        shake.setInterpolator(new CycleInterpolator(3));
        return shake;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public static void startCircularReveal(View startView, View parent, AppCompatActivity appCompatActivity) {
//        final View view = findViewById(R.id.linearLayout);
//        final View startView = findViewById(R.id.button_container);
        int cx = (startView.getLeft() + startView.getRight()) / 2;
        int cy = (startView.getTop() + startView.getBottom()) / 2;
//        view.setBackgroundColor(Color.parseColor("#6FA6FF"));
        int finalRadius = Math.max(cy, parent.getHeight() - cy);
        Animator anim = ViewAnimationUtils.createCircularReveal(parent, cx, cy, 0, finalRadius);
        anim.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                changeTheme(appCompatActivity);
            }

            @Override
            public void onAnimationEnd(Animator animation) {

            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        anim.setDuration(200);
        anim.start();
    }

    private static void changeTheme(AppCompatActivity appCompatActivity) {
        SharedPreferences.Editor editor = appCompatActivity.getSharedPreferences(appCompatActivity.getString(R.string.mr_chat), MODE_PRIVATE).edit();
        SharedPreferences prefs = appCompatActivity.getSharedPreferences(appCompatActivity.getString(R.string.mr_chat), MODE_PRIVATE);

        if (!prefs.getBoolean(appCompatActivity.getString(R.string.theme), false)) {
            appCompatActivity.setTheme(R.style.DarkTheme);
            editor.putBoolean(appCompatActivity.getString(R.string.theme), true);
        } else {
            appCompatActivity.setTheme(R.style.AppTheme);
            editor.putBoolean(appCompatActivity.getString(R.string.theme), false);
        }
        editor.apply();
        appCompatActivity.recreate();
    }
}
