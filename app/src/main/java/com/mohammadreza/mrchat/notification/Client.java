package com.mohammadreza.mrchat.notification;

import androidx.annotation.Keep;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
@Keep
public class Client {
    private static Retrofit retrofit=null;

    public static Retrofit getClient(String url)
    {
        if(retrofit==null)
        {
            retrofit=new Retrofit.Builder().baseUrl(url).addConverterFactory(GsonConverterFactory.create()).build();
        }
        return  retrofit;
    }
}
