package com.mohammadreza.mrchat.viewmodel;

import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;
import androidx.databinding.BindingAdapter;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.OnLifecycleEvent;
import androidx.lifecycle.ProcessLifecycleOwner;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mohammadreza.mrchat.BR;
import com.mohammadreza.mrchat.R;
import com.mohammadreza.mrchat.adapter.ContactsListAdapter;
import com.mohammadreza.mrchat.constant.ChatConstant;
import com.mohammadreza.mrchat.model.ContactsModel;
import com.mohammadreza.mrchat.model.ContactsRealmModel;
import com.mohammadreza.mrchat.receiver.NetworkStateReceiver;
import com.mohammadreza.mrchat.repository.ChatRealmDatabase;
import com.mohammadreza.mrchat.ui.ContactsActivity;

import java.util.ArrayList;
import java.util.List;

import static com.mohammadreza.mrchat.receiver.NetworkStateReceiver.CONNECTED_TO_FIREBASE;
import static com.mohammadreza.mrchat.receiver.NetworkStateReceiver.CONNECTING;
import static com.mohammadreza.mrchat.receiver.NetworkStateReceiver.NO_NET;


public class ContactsViewModel extends BaseObservable implements NetworkStateReceiver.NetworkStateReceiverListener, LifecycleObserver {

    public NetworkStateReceiver networkStateReceiverContacts;
    private AppCompatActivity appCompatActivity;
    private String currentUserId = "";
    private String netStateContacts = "";
    private boolean progressLoadContact;
    private MutableLiveData<List<ContactsModel>> contactsUserModelLiveData = new MutableLiveData<>();
    private ArrayList<ContactsModel> contactsModels = new ArrayList<>();


    public ContactsViewModel(ContactsActivity appCompatActivity) {
        this.appCompatActivity = appCompatActivity;
        getIntentData();
        registerBroadCast();
        setProgressLoadContact(true);
        try {
            getUsersFromOfflineDatabase();
        } catch (Exception e) {
        }
        ProcessLifecycleOwner.get().getLifecycle().addObserver(this);
    }

    @BindingAdapter(value = {"app:recycler_chat_user", "currentId", "app:net_state", "app:appCompatActivity"})
    public static void setChatUserRecyclerviewData(final RecyclerView recyclerView, final MutableLiveData<List<ContactsModel>> liveData, String currentUserId, String netStateContacts, AppCompatActivity appCompatActivity) {
        liveData.observe((LifecycleOwner) recyclerView.getContext(), chatUserModels -> {
            ContactsListAdapter adapter = new ContactsListAdapter(chatUserModels, appCompatActivity, currentUserId, netStateContacts);
            recyclerView.setAdapter(adapter);
        });
    }

//    @BindingAdapter("app:src_user_img_profile")
//    public static void setImgProfileChatUser(CircleImageView circleImageView, String url) {
//        Picasso.get().load(url).into(circleImageView);
//    }

    private void getIntentData() {
        Intent intent = appCompatActivity.getIntent();
        if (intent != null && intent.getExtras() != null) {
            currentUserId = intent.getStringExtra(ChatConstant.CHAT_CURRENT_ID_INTENT_KEY);
        }
    }

    public void onClickBachContacts() {
        appCompatActivity.finish();
    }

    private void getUsers() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(ChatConstant.USERS);
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshots) {
                contactsModels.clear();
                for (DataSnapshot snapshot : snapshots.getChildren()) {
                    ContactsModel contactsModel = snapshot.getValue(ContactsModel.class);
                    if (contactsModel != null) {
                        if (!contactsModel.getId().equals(getCurrentUserId())) {
                            addUsersToOfflineDatabase(contactsModel);
                            contactsModels.add(contactsModel);
                        }
                    }
                }
                contactsUserModelLiveData.setValue(contactsModels);
                setProgressLoadContact(false);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getUsersFromOfflineDatabase() {
        List<ContactsRealmModel> contacts = ChatRealmDatabase.getUsers(appCompatActivity);
        if (contacts != null && contacts.size() > 0) {
            contactsModels.clear();
            for (ContactsRealmModel realmModel : contacts) {
                if (!realmModel.getId().equals(getCurrentUserId())) {
                    contactsModels.add(new ContactsModel(realmModel));
                }

            }
            contactsUserModelLiveData.setValue(contactsModels);
            if (contactsModels.size() > 0) {
                setProgressLoadContact(false);
            }
        }
    }

    private void addUsersToOfflineDatabase(ContactsModel contactsModel) {
        ContactsModel user = ChatRealmDatabase.checkUserExist(ChatConstant.TABLE_ID_FIELD_NAME, contactsModel.getId(), appCompatActivity);
        if (user == null) {
            ContactsRealmModel realmModel = new ContactsRealmModel(contactsModel);
            realmModel.setIsOnline(ChatConstant.KEEP_OFFLINE);
            ChatRealmDatabase.addNewUserToChatDatabase(realmModel, appCompatActivity);
        }
    }

    private void registerBroadCast() {
        networkStateReceiverContacts = new NetworkStateReceiver(appCompatActivity);
        networkStateReceiverContacts.addListener(this);
        appCompatActivity.registerReceiver(networkStateReceiverContacts, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
    }


    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    public void oContactStart() {
        getUsers();
    }


    public AppCompatActivity getAppCompatActivity() {
        return appCompatActivity;
    }

    @Bindable
    public String getCurrentUserId() {
        return currentUserId;
    }


    @Bindable
    public boolean isProgressLoadContact() {
        return progressLoadContact;
    }

    public void setProgressLoadContact(boolean progressLoadContact) {
        this.progressLoadContact = progressLoadContact;
        notifyPropertyChanged(BR.progressLoadContact);
    }

    public MutableLiveData<List<ContactsModel>> getContactsUserModelLiveData() {
        return contactsUserModelLiveData;

    }

    @Override
    public void firebaseConnectionState(int state) {
        switch (state) {
            case NO_NET:
                setNetStateContacts(appCompatActivity.getString(R.string.waiting_for_network));
                break;

            case CONNECTING:
                setNetStateContacts(appCompatActivity.getString(R.string.connecting));
                break;

            case CONNECTED_TO_FIREBASE:
                setNetStateContacts(appCompatActivity.getString(R.string.new_message));
                break;
        }

    }


    @Bindable
    public String getNetStateContacts() {
        return netStateContacts;
    }

    public void setNetStateContacts(String netStateContacts) {
        this.netStateContacts = netStateContacts;
        notifyPropertyChanged(BR.netStateContacts);
    }
}
