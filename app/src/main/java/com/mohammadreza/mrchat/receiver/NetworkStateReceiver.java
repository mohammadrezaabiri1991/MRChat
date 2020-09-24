package com.mohammadreza.mrchat.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;

import androidx.annotation.RequiresApi;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mohammadreza.mrchat.application.MyApp;
import com.mohammadreza.mrchat.constant.ChatConstant;
import com.mohammadreza.mrchat.utils.Utils;

import java.util.HashSet;
import java.util.Set;


public class NetworkStateReceiver extends BroadcastReceiver {

    public static final int CONNECTING = 100;
    public static final int NO_NET = 0;
    public static final int CONNECTED_TO_FIREBASE = 200;
    private final Context context;

    protected Set<NetworkStateReceiverListener> netListeners;

    protected Boolean connected;
    private boolean connect;
    private boolean isVpnConnect;

    public NetworkStateReceiver(Context context) {
        this.context = context;
        netListeners = new HashSet<>();
        connected = null;
    }


    public void onReceive(Context context, Intent intent) {
        if (intent == null || intent.getExtras() == null)
            return;
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = null;
        if (manager != null) {
            ni = manager.getActiveNetworkInfo();
        }

        if (ni != null && ni.getState() == NetworkInfo.State.CONNECTED) {
            connected = true;
        } else if (intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, Boolean.FALSE)) {
            connected = false;
        }
        notifyStateToAll();


    }

    private void notifyStateToAll() {
        for (NetworkStateReceiverListener listener : netListeners) {
            notifyState(listener);
        }
    }


    private void notifyState(NetworkStateReceiverListener listener) {
        if (connected != null && listener != null) {
            if (connected) {
                connect = false;
//                isVpnConnected();
                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        checkVPN();
                    } else {
                        isVpnConnect = true;
                    }
                } catch (Exception e) {
                    isVpnConnect = true;
                }

                listener.firebaseConnectionState(CONNECTING);
                try {
                    checkConnectivityManager(listener);
                } catch (Exception e) {
                    setOnFailure(listener);
                }
            } else {
                listener.firebaseConnectionState(NO_NET);
            }
        } else if (connected == null && listener != null) {
            listener.firebaseConnectionState(NO_NET);

        }
    }

    public void addListener(NetworkStateReceiverListener l) {
        netListeners.add(l);
        notifyState(l);
    }

    private void checkConnectivityManager(NetworkStateReceiverListener listener) {
        DatabaseReference connectedRef = FirebaseDatabase.getInstance().getReference(ChatConstant.SERVER_INFO);
        connectedRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                connect = false;
                connect = snapshot.getValue(Boolean.class);
                if (connect && isVpnConnect) {
                    listener.firebaseConnectionState(CONNECTED_TO_FIREBASE);
                } else {
                    setOnFailure(listener);
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                setOnFailure(listener);
            }
        });
    }

    private void setOnFailure(NetworkStateReceiverListener listener) {
        if (Utils.isOnline(MyApp.appContext)) {
            listener.firebaseConnectionState(CONNECTING);
        } else {
            listener.firebaseConnectionState(NO_NET);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void checkVPN() {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        isVpnConnect = cm.getNetworkInfo(ConnectivityManager.TYPE_VPN).isConnectedOrConnecting();
    }

    public interface NetworkStateReceiverListener {
        void firebaseConnectionState(int state);
    }
}