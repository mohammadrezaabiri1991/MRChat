package com.mohammadreza.mrchat.notification;

import androidx.annotation.Keep;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;
@Keep
public interface APIService {
    @Headers(
            {
                    "Content-Type:application/json",
                    "Authorization:key=AAAA6UG_HrE:APA91bG34nixDbYHRKTqj6iOQOBSn6OmxbyPys3pMDJbZfhu6vW8WL8INRb40T0KHTBcli2JJa9H5X5q3ec4TOz0wze57xlUp6z_kD8i9Vx-LbZtbM7W6yq0K126m8vEmoPjWRyk-s2h" // Your server key refer to video for finding your server key
            }
    )

    @POST("fcm/send")
    Call<MyResponse> sendNotifcation(@Body NotificationSender body);
}

