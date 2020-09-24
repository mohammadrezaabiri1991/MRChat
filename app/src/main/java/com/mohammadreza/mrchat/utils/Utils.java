package com.mohammadreza.mrchat.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.github.chrisbanes.photoview.PhotoView;
import com.mohammadreza.mrchat.R;
import com.mohammadreza.mrchat.application.MyApp;
import com.mohammadreza.mrchat.constant.ChatConstant;
import com.mohammadreza.mrchat.model.ContactsModel;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.mohammadreza.mrchat.ui.PhoneActivity.phoneActivity;


public class Utils {


    public static void callIntent(AppCompatActivity appCompatActivity, Class<?> nextClass) {
        Intent intent = new Intent(appCompatActivity, nextClass);
        appCompatActivity.startActivity(intent);

    }

    public static void callIntent(AppCompatActivity appCompatActivity, Class<?> nextClass, ContactsModel user) {
        Intent intent = new Intent(appCompatActivity, nextClass);
        intent.putExtra(ChatConstant.CHAT_USER_CLASS_INTENT_KEY, user);
        if (phoneActivity != null) {
//            intent.addFlags( Intent.FLAG_ACTIVITY_NO_HISTORY);
            phoneActivity.finish();
//            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_NEW_TASK); Intent.FLAG_ACTIVITY_REORDER_TO_FRONT |
        }
        appCompatActivity.startActivity(intent);
        appCompatActivity.finish();

    }

    public static void callIntent(AppCompatActivity appCompatActivity, Class<?> nextClass, ContactsModel user, String value) {
        Intent intent = new Intent(appCompatActivity, nextClass);
        intent.putExtra(ChatConstant.CHAT_USER_CLASS_INTENT_KEY, user);
        intent.putExtra(ChatConstant.CHAT_CURRENT_ID_INTENT_KEY, value);
        appCompatActivity.startActivity(intent);
    }


    public static void callIntent(AppCompatActivity appCompatActivity, Class<?> nextClass, String key, String value) {
        Intent intent = new Intent(appCompatActivity, nextClass);
        intent.putExtra(key, value);
        appCompatActivity.startActivity(intent);

    }

    public static void callIntent(AppCompatActivity appCompatActivity, Class<?> nextClass, String key, String value, String secondKey, String secondValue) {
        Intent intent = new Intent(appCompatActivity, nextClass);
        intent.putExtra(key, value);
        intent.putExtra(secondKey, secondValue);
        appCompatActivity.startActivity(intent);

    }


    public static void callIntent(AppCompatActivity appCompatActivity, Class<?> nextClass, String key, String value, String secondKey,
                                  String secondValue, String thirdKey, String thirdValue, String fourthKey, String fourValue) {
        Intent intent = new Intent(appCompatActivity, nextClass);
        intent.putExtra(key, value);
        intent.putExtra(secondKey, secondValue);
        intent.putExtra(thirdKey, thirdValue);
        intent.putExtra(fourthKey, fourValue);
        appCompatActivity.startActivity(intent);

    }

    public static boolean isOnline(Context context) {
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        assert cm != null;
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();

    }

    public static void saveToInternalStorage(final String imageName, Bitmap bitmap, Context context) {
        new Thread(() -> {
            if (context != null) {
                String imageNames = imageName + ChatConstant.JPG_FORMAT;
                ContextWrapper contextWrapper = new ContextWrapper(context);
                File directory = contextWrapper.getDir(ChatConstant.IMAGE_DIR, Context.MODE_PRIVATE);
                File path = new File(directory, imageNames);

                path.delete();
                directory.delete();
                path.mkdir();
                directory.mkdir();

//                if (directory.exists()) {
//                    directory.delete();
//                    directory.mkdir();
//                }
                FileOutputStream fileOutputStream = null;
                try {
                    fileOutputStream = new FileOutputStream(path);
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fileOutputStream);
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    try {
                        if (fileOutputStream != null) {
                            fileOutputStream.close();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    public static void hideKeyboard(AppCompatActivity appCompatActivity) {
        if (!appCompatActivity.isFinishing()) {
            View view = appCompatActivity.getCurrentFocus();
            if (view != null) {
                InputMethodManager imm = (InputMethodManager) appCompatActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }
            }
        }
    }

    public static void loadImageFromStorage(String imageNames, AppCompatActivity appCompatActivity, CircleImageView circleImageView, PhotoView photoView) {
        new Thread(() -> {
            String imageName = imageNames + ChatConstant.JPG_FORMAT;
            ContextWrapper contextWrapper = new ContextWrapper(appCompatActivity);
            String path = contextWrapper.getDir(ChatConstant.IMAGE_DIR, Context.MODE_PRIVATE).getAbsolutePath();
            try {
                Bitmap bitmap = BitmapFactory.decodeStream(new FileInputStream(new File(path, imageName)));
                appCompatActivity.runOnUiThread(() -> {
                    if (circleImageView != null) {
                        circleImageView.setImageBitmap(bitmap);

                    } else if (photoView != null) {
                        photoView.setImageBitmap(bitmap);
                    }
                });
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }).start();
    }


    public static void actionDownloadAndSaveImage(CircleImageView circleImageView, String userId, String smallImageUrl, AppCompatActivity appCompatActivity) {
        Glide.with(MyApp.appContext)
                .asBitmap()
                .load(smallImageUrl)
                .into(new CustomTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resources, @Nullable Transition<? super Bitmap> transition) {
                        circleImageView.setImageBitmap(resources);
                        Utils.saveToInternalStorage((userId + appCompatActivity.getString(R.string.small)), resources, appCompatActivity);
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {
                    }
                });
    }

    public static Bitmap scaleDown(Bitmap realImage, float maxWith, float maxHeight, boolean filter) {
        float ratio = Math.min(maxWith / realImage.getWidth(), maxHeight / realImage.getHeight());

        int width = Math.round(ratio * realImage.getWidth());
        int height = Math.round(ratio * realImage.getHeight());

        return Bitmap.createScaledBitmap(realImage, width, height, filter);
    }

    public static void removeFromStorage(String imageName, AppCompatActivity appCompatActivity) {
        String imageNames = imageName + ChatConstant.JPG_FORMAT;
        ContextWrapper contextWrapper = new ContextWrapper(appCompatActivity);
        File directory = contextWrapper.getDir(ChatConstant.IMAGE_DIR, Context.MODE_PRIVATE);
        File path = new File(directory, imageNames);


        File original = new File(path, imageName + appCompatActivity.getResources().getString(R.string.original));
        File small = new File(path, imageName + appCompatActivity.getResources().getString(R.string.small));

        original.delete();
        small.delete();
    }

    public static String compareOnTowTime(long timeCurrent, long userStatus, AppCompatActivity appCompatActivity, String comeFromMessage) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date(timeCurrent));

        int currentMinute = calendar.get(Calendar.MINUTE);
        int currentHour = calendar.get(Calendar.HOUR_OF_DAY);
        int currentDay = calendar.get(Calendar.DAY_OF_MONTH);
        int currentMonth = calendar.get(Calendar.MONTH);
        int currentYear = calendar.get(Calendar.YEAR);

        Calendar userDate = Calendar.getInstance();
        userDate.setTime(new Date(userStatus));

        int userStatusMinute = userDate.get(Calendar.MINUTE);
        int userStatusHOUR = userDate.get(Calendar.HOUR_OF_DAY);
        int userStatusDay = userDate.get(Calendar.DAY_OF_MONTH);
        int userStatusMonth = userDate.get(Calendar.MONTH);
        int userStatusYear = userDate.get(Calendar.YEAR);

        @SuppressLint("SimpleDateFormat") SimpleDateFormat dateFormat;


        if (currentYear == userStatusYear && currentMonth == userStatusMonth && currentDay == userStatusDay && currentHour == userStatusHOUR && currentMinute <= userStatusMinute + 2) {
            if (comeFromMessage.equals(ChatConstant.COME_FROM_MESSAGE)) {
                return appCompatActivity.getString(R.string.online);
            } else {
                dateFormat = new SimpleDateFormat(appCompatActivity.getString(R.string.date_format_day));
                return dateFormat.format(userDate.getTime());
            }


        } else if (currentYear == userStatusYear && currentMonth == userStatusMonth && currentDay == userStatusDay && currentHour >= userStatusHOUR) {
            if (comeFromMessage.equals(ChatConstant.COME_FROM_MESSAGE)) {
                return appCompatActivity.getString(R.string.str_last_seen_today) + " " + userStatusHOUR + ":" + (userStatusMinute < 10 ? "0" + userStatusMinute : userStatusMinute);
            } else {
                dateFormat = new SimpleDateFormat(appCompatActivity.getString(R.string.date_format_day));
                return dateFormat.format(userDate.getTime());
            }


        } else if (currentYear == userStatusYear && currentMonth == userStatusMonth && currentDay == userStatusDay + 1) {
            if (comeFromMessage.equals(ChatConstant.COME_FROM_MESSAGE)) {
                return (appCompatActivity.getString(R.string.str_seen_yesterday) + " " + userStatusHOUR + ":" + (userStatusMinute < 10 ? "0" + userStatusMinute : userStatusMinute));
            } else {
                SimpleDateFormat dayFormat = new SimpleDateFormat(appCompatActivity.getResources().getString(R.string.date_format_day_of_week));
                return dayFormat.format(userDate.getTime());
            }


        } else if (currentYear == userStatusYear && currentMonth >= userStatusMonth) {
            if (comeFromMessage.equals(ChatConstant.COME_FROM_MESSAGE)) {
                dateFormat = new SimpleDateFormat(appCompatActivity.getString(R.string.date_format_month_day));
                return (appCompatActivity.getString(R.string.str_last_seen) + " " + dateFormat.format(userDate.getTime()));

            } else {
                dateFormat = new SimpleDateFormat(appCompatActivity.getString(R.string.date_format_month));
                return dateFormat.format(userDate.getTime());
            }


        } else {
            Date date = new Date(userStatus);
            if (comeFromMessage.equals(ChatConstant.COME_FROM_MESSAGE)) {
                return (appCompatActivity.getString(R.string.last_seen_mr));
            } else {
                dateFormat = new SimpleDateFormat(appCompatActivity.getString(R.string.date_format_month));
                return dateFormat.format(calendar.getTime());
            }


        }
    }



    public static String getTimeDay(long date) {
        Calendar now = Calendar.getInstance();
        now.setTime(new Date(date));
        int day = now.get(Calendar.DAY_OF_MONTH);
        String month = String.format(Locale.US, ChatConstant.TIME_FORMAT, now);
        return day + "-" + month;
    }

    public static String getTimeHour(long date) {
        Calendar now = Calendar.getInstance();
        now.setTime(new Date(date));
        int hour = now.get(Calendar.HOUR_OF_DAY);
        String minute = "";
        if (now.get(Calendar.MINUTE) < 10) {
            minute = "0" + now.get(Calendar.MINUTE);
        } else {
            minute = String.valueOf(now.get(Calendar.MINUTE));
        }
        return hour + ":" + minute;
    }
}
