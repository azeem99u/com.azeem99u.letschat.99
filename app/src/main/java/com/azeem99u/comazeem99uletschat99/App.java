package com.azeem99u.comazeem99uletschat99;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.media.AudioAttributes;
import android.media.RingtoneManager;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ProcessLifecycleOwner;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;


public class App extends Application{
    public static FirebaseAuth mAuth;
    public static final String MESSAGE_CHANNEL_ID = "messages_ch";
    public static final String CALL_CHANNEL_ID = "call_ch";
    public static final String GROUP_MESSAGE_CHANNEL_ID = "group_messages_ch";
    public static final String REQUESTS_CHANNEL_ID = "requests_ch";
    public static final String DOWNLOAD_CHANNEL_ID = "downloads_ch";
    public static final String UPLOAD_CHANNEL_ID = "upload_ch";
    public static GoogleSignInClient mGoogleSignInClient;

    @Override
    public void onCreate() {
        super.onCreate();





        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                .build();

        ProcessLifecycleOwner.get().getLifecycle().addObserver(new LifecycleObs());
        mAuth = FirebaseAuth.getInstance();

        GoogleSignInOptions googleSignInOptions =
                new GoogleSignInOptions.Builder()
                        .requestEmail()
                        .requestIdToken(getString(R.string.default_web_client_id))
                        .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, googleSignInOptions);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel groups_ch = new NotificationChannel(
                    GROUP_MESSAGE_CHANNEL_ID,
                    "Groups Notification",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            groups_ch.setVibrationPattern(new long[]{0, 250, 250, 250});


            NotificationChannel requests_ch = new NotificationChannel(
                    REQUESTS_CHANNEL_ID,
                    "Requests Notification",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            requests_ch.setVibrationPattern(new long[]{0, 250, 250, 250});
            requests_ch.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION),audioAttributes);

            NotificationChannel message_ch = new NotificationChannel(
                    MESSAGE_CHANNEL_ID,
                    "Messages Notification",
                    NotificationManager.IMPORTANCE_HIGH
            );
            message_ch.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION),audioAttributes);
            message_ch.setVibrationPattern(new long[]{0, 250, 250, 250});


            NotificationChannel call_ch = new NotificationChannel(
                    CALL_CHANNEL_ID,
                    "Call Notification",
                    NotificationManager.IMPORTANCE_HIGH
            );
            call_ch.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION),audioAttributes);
            call_ch.setVibrationPattern(new long[]{0, 250, 250, 250});

            NotificationChannel download_ch = new NotificationChannel(
                    DOWNLOAD_CHANNEL_ID,
                    "Downloads File Notification",
                    NotificationManager.IMPORTANCE_MIN
            );
            download_ch.setSound(null,null);

            NotificationChannel upload_ch = new NotificationChannel(
                    UPLOAD_CHANNEL_ID,
                    "Upload File Notification",
                    NotificationManager.IMPORTANCE_MIN
            );
            download_ch.setSound(null,null);

            NotificationManager manager = getSystemService(NotificationManager.class);
            ArrayList<NotificationChannel> list = new ArrayList<>();
            list.add(groups_ch);
            list.add(message_ch);
            list.add(requests_ch);
            list.add(download_ch);
            list.add(call_ch);
            list.add(upload_ch);
            manager.createNotificationChannels(list);
        }
    }

    class LifecycleObs implements DefaultLifecycleObserver{

        @Override
        public void onStart(@NonNull LifecycleOwner owner) {
            DefaultLifecycleObserver.super.onStart(owner);
            if (mAuth.getCurrentUser()!= null){
                ConnectivityStatusHelper.updateConnectionStatus();
            }
        }

        @Override
        public void onStop(@NonNull LifecycleOwner owner) {
            DefaultLifecycleObserver.super.onStop(owner);
            if (mAuth.getCurrentUser()!= null){
                ConnectivityStatusHelper.stopConnectionStatus();
            }
        }


    }

    private String getDate() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat currentDateFormat = new SimpleDateFormat("MM dd, yyyy");
        return currentDateFormat.format(calendar.getTime());
    }

    private String getTime() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat currentTimeFormat = new SimpleDateFormat("hh:mm a");
        return currentTimeFormat.format(calendar.getTime());
    }







}

