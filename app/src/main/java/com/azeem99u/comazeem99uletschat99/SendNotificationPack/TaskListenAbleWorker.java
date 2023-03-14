package com.azeem99u.comazeem99uletschat99.SendNotificationPack;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.SystemClock;

import androidx.annotation.NonNull;
import androidx.concurrent.futures.CallbackToFutureAdapter;
import androidx.core.app.NotificationCompat;
import androidx.work.ListenableWorker;
import androidx.work.WorkerParameters;

import com.azeem99u.comazeem99uletschat99.Constants;
import com.azeem99u.comazeem99uletschat99.R;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Objects;

public class TaskListenAbleWorker extends ListenableWorker {

    private final DatabaseReference mUserMessageDatabaseRef;
    private static final int NOTIFICATION_ID = 0;
    NotificationManager notificationManager;


    public TaskListenAbleWorker(@NonNull Context appContext, @NonNull WorkerParameters workerParams) {
        super(appContext, workerParams);
        mUserMessageDatabaseRef = FirebaseDatabase.getInstance().getReference().child(Constants.MESSAGES);
        notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
    }

    @NonNull
    @Override
    public ListenableFuture<Result> startWork() {

        return CallbackToFutureAdapter.getFuture(completer -> {

            String clickAction = getInputData().getString(Constants.CLICK_ACTION_KEY);
            String channel_id = getInputData().getString(Constants.CHANNEL_ID_KEY);
            String senderId = getInputData().getString(Constants.SENDER_ID_KEY);
            String receiverId = getInputData().getString(Constants.RECEIVER_ID_KEY);
            String messageKey = getInputData().getString(Constants.MESSAGE_KEY_W);
            String title = getInputData().getString(Constants.TITLE_KEY);
            String body = getInputData().getString(Constants.BODY_KEY);
            String image = getInputData().getString(Constants.IMAGE_KEY);

            HashMap<String, Object> map = new HashMap<>();
            SystemClock.sleep(2000);
            mUserMessageDatabaseRef.child(receiverId).child(senderId).child(messageKey).child(Constants.DELIVERY).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists() && !Objects.equals(snapshot.getValue(String.class), Constants.Delivery.Seen.name())){
                        map.put(Constants.DELIVERY, Constants.Delivery.Delivered.name());
                        mUserMessageDatabaseRef.child(senderId).child(receiverId).child(messageKey).updateChildren(map).addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                mUserMessageDatabaseRef.child(receiverId).child(senderId).child(messageKey).updateChildren(map).addOnCompleteListener(task1 -> {
                                    if (task1.isSuccessful()) {
                                        completer.set(Result.success());
                                        notificationManager.notify(NOTIFICATION_ID, createNotificationInfo(clickAction, channel_id, senderId, title, body, image));
                                    } else if (task1.getException() != null) {
                                        completer.set(Result.failure());
                                    } else {
                                        completer.set(Result.retry());
                                    }
                                });
                            } else if (task.getException() != null) {
                                completer.set(Result.failure());
                            } else {
                                completer.set(Result.retry());
                            }
                        });
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
            return completer;
        });
    }

    public Notification createNotificationInfo(String clickAction, String channel_id, String senderId, String title, String body, String image) {
        Intent intent = null;
        PendingIntent pendingIntent;
        intent = new Intent(clickAction);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra(Constants.SELECTED_USER_ID_NOTIFICATION, senderId);
        pendingIntent = PendingIntent.getActivity(getApplicationContext(), NOTIFICATION_ID, intent, PendingIntent.FLAG_ONE_SHOT);
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        int app_icon = R.mipmap.ic_launcher;

        return new NotificationCompat.Builder(getApplicationContext(), channel_id)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setAutoCancel(true)
                .setSmallIcon(app_icon)
                .setOngoing(false)
                .setPriority(Notification.PRIORITY_DEFAULT)
                .setLargeIcon(BitmapFactory.decodeResource(getApplicationContext().getResources(), app_icon))
                .setStyle(new NotificationCompat.DecoratedCustomViewStyle())
                .setSound(defaultSoundUri)
                .setContentTitle(title)
                .setContentText(body)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setContentIntent(pendingIntent)
                .setCategory(NotificationCompat.CATEGORY_MESSAGE).build();
    }

}
