package com.azeem99u.comazeem99uletschat99.SendNotificationPack;

import static com.azeem99u.comazeem99uletschat99.Constants.SENDER_ID_KEY;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.os.Build;
import android.os.SystemClock;
import android.provider.Settings;
import android.util.Log;
import android.widget.RemoteViews;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.concurrent.futures.CallbackToFutureAdapter;
import androidx.core.app.NotificationCompat;
import androidx.work.ForegroundInfo;
import androidx.work.ListenableWorker;
import androidx.work.WorkerParameters;

import com.azeem99u.comazeem99uletschat99.AudioCallActivity;
import com.azeem99u.comazeem99uletschat99.CallPickerActivity;
import com.azeem99u.comazeem99uletschat99.Constants;
import com.azeem99u.comazeem99uletschat99.HangUpBroadcast;
import com.azeem99u.comazeem99uletschat99.R;
import com.azeem99u.comazeem99uletschat99.VideoCallActivity;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class CallTaskListenAbleWorker extends ListenableWorker {

    private static final int NOTIFICATION_ID = 232;
    NotificationManager notificationManager;
    ValueEventListener valueEventListener;
    String senderId;
    String Channel = "mycallNot";
    boolean isAccepted = false;
    ValueEventListener callStatusValueEventListener;
    boolean isOnGoingEnd = false;
    boolean isRejected = false;

    public CallTaskListenAbleWorker(@NonNull Context appContext, @NonNull WorkerParameters workerParams) {
        super(appContext, workerParams);
        notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    @NonNull
    @Override
    public ListenableFuture<Result> startWork() {

        return CallbackToFutureAdapter.getFuture(completer -> {

            senderId = getInputData().getString(Constants.SENDER_ID_KEY);
            String clickAction = getInputData().getString(Constants.CLICK_ACTION_KEY);
            String channel_id = getInputData().getString(Constants.CHANNEL_ID_KEY);
            String receiverId = getInputData().getString(Constants.RECEIVER_ID_KEY);
            String messageKey = getInputData().getString(Constants.MESSAGE_KEY_W);
            String title = getInputData().getString(Constants.TITLE_KEY);
            String body = getInputData().getString(Constants.BODY_KEY);
            String image = getInputData().getString(Constants.IMAGE_KEY);
            Intent dialogIntent = new Intent(getApplicationContext(), CallPickerActivity.class);
            dialogIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            dialogIntent.putExtra(Constants.CALL_SENDER_ID, senderId);
            dialogIntent.putExtra(Constants.CALLER_NAME_KEY, title);
            dialogIntent.putExtra(Constants.CALLER_IMAGE_KEY, image);
            dialogIntent.putExtra(Constants.CALL_TYPE_KEY, body);
            //getApplicationContext().startActivity(dialogIntent);


            callStatusValueEventListener = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        if (snapshot.getValue().equals(Constants.CALLING)){
                            FirebaseDatabase.getInstance().getReference().child(Constants.VIDEO_CALLS).child(senderId).child(Constants.CALL_STATUS).removeValue();
                            FirebaseDatabase.getInstance().getReference().child(Constants.VIDEO_CALLS).child(senderId).child(Constants.CONNECTION_STATUS).removeValue();
                            userCalling(completer, channel_id, title, body, image);
                        }else if (snapshot.getValue().equals(Constants.MISSED_CALLING)){
                            FirebaseDatabase.getInstance().getReference().child(Constants.VIDEO_CALLS).child(senderId).child(Constants.CALL_STATUS).removeValue();
                            FirebaseDatabase.getInstance().getReference().child(Constants.VIDEO_CALLS).child(senderId).child(Constants.CONNECTION_STATUS).removeValue();
                            showMissedCallNotification(title,body);
                            completer.set(Result.success());
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            };
            FirebaseDatabase.getInstance().getReference().child(Constants.VIDEO_CALLS).child(senderId).child(Constants.CALL_STATUS).addValueEventListener(callStatusValueEventListener);
            FirebaseDatabase.getInstance().getReference().child(Constants.VIDEO_CALLS).child(senderId).child(Constants.CONNECTION_STATUS).setValue(Constants.CallConnection.Ringing.name());
            return completer;
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void userCalling(CallbackToFutureAdapter.Completer<Result> completer, String channel_id, String title, String body, String image) {
        setForegroundAsync(new ForegroundInfo(Constants.CALL_NOTIFICATION_ID, showCallNotifcation(title, body, channel_id, image, senderId)));

        valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists() && snapshot.hasChild(Constants.CONNECTION_STATUS)) {
                    if (snapshot.child(Constants.CONNECTION_STATUS).getValue(String.class).equals(Constants.CallConnection.END_ONGOING.name())) {
                        isOnGoingEnd = true;
                        FirebaseDatabase.getInstance().getReference().child(Constants.VIDEO_CALLS).child(senderId).child(Constants.CONNECTION_STATUS).removeValue();
                        FirebaseDatabase.getInstance().getReference().child(Constants.VIDEO_CALLS).child(senderId).child(Constants.CALL_STATUS).setValue(Constants.MISSED_CALLING);
                        completer.set(Result.success());
                    }
                    if (snapshot.child(Constants.CONNECTION_STATUS).getValue(String.class).equals(Constants.CallConnection.Rejected.name())) {
                        FirebaseDatabase.getInstance().getReference().child(Constants.VIDEO_CALLS).child(senderId).child(Constants.CONNECTION_STATUS).removeValue();
                        isRejected = true;
                        completer.set(Result.success());
                    }
                    if (snapshot.child(Constants.CONNECTION_STATUS).getValue(String.class).equals(Constants.CallConnection.Accepted.name())) {
                        FirebaseDatabase.getInstance().getReference().child(Constants.VIDEO_CALLS).child(senderId).child(Constants.CONNECTION_STATUS).removeValue();
                        isAccepted = true;
                        Log.d("mytaggg", "onDataChange: a");
                        completer.set(Result.success());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };

        FirebaseDatabase.getInstance().getReference().child(Constants.VIDEO_CALLS).child(senderId).addValueEventListener(valueEventListener);


        new Thread(() -> {
            for (int i = 1; i <= 30; i++) {
                if (isOnGoingEnd) {
                    break;
                }
                if (isAccepted) {
                    break;
                }
                if (isRejected) {
                    break;
                }
                if (i == 30) {
                    FirebaseDatabase.getInstance().getReference().child(Constants.VIDEO_CALLS).child(senderId).child(Constants.CONNECTION_STATUS).setValue(Constants.CallConnection.NoAnswer.name());
                    FirebaseDatabase.getInstance().getReference().child(Constants.VIDEO_CALLS).child(senderId).child(Constants.CALL_STATUS).setValue(Constants.MISSED_CALLING);
                    break;
                }
                SystemClock.sleep(1000);
            }
        }).start();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onStopped() {
        super.onStopped();
        if (callStatusValueEventListener!= null){
            FirebaseDatabase.getInstance().getReference().child(Constants.VIDEO_CALLS).child(senderId).child(Constants.CALL_STATUS).removeEventListener(callStatusValueEventListener);
        }
        if (valueEventListener != null) {
            FirebaseDatabase.getInstance().getReference().child(Constants.VIDEO_CALLS).child(senderId).removeEventListener(valueEventListener);
        }
    }


    @RequiresApi(api = Build.VERSION_CODES.M)
    private Notification showCallNotifcation(String title, String body, String channel_id, String image, String senderId) {
        Notification incomingCall = null;
        RemoteViews customView = new RemoteViews(getApplicationContext().getPackageName(), R.layout.custom_call_notification);
        Intent notificationIntent = new Intent(getApplicationContext(), CallPickerActivity.class);
        notificationIntent.putExtra(Constants.CALL_SENDER_ID, senderId);
        notificationIntent.putExtra(Constants.CALLER_NAME_KEY, title);
        notificationIntent.putExtra(Constants.CALLER_IMAGE_KEY, image);
        notificationIntent.putExtra(Constants.CALL_TYPE_KEY, body);
        notificationIntent.putExtra("WORKERID", getId().toString());

        Intent broadcastMessageIntent = new Intent(getApplicationContext(), HangUpBroadcast.class);
        broadcastMessageIntent.setAction(Constants.HANG_UP_BROADCAST_ACTION);
        broadcastMessageIntent.putExtra(SENDER_ID_KEY, senderId);
        broadcastMessageIntent.putExtra("WORKERID", getId().toString());
        Intent answerIntent;
        if (body.equals(Constants.VIDEO_CALL)) {
            answerIntent = new Intent(getApplicationContext(), VideoCallActivity.class);
        } else {
            answerIntent = new Intent(getApplicationContext(), AudioCallActivity.class);
        }
        answerIntent.putExtra(Constants.CALLER_NAME_KEY, title);
        answerIntent.putExtra(Constants.CALLER_IMAGE_KEY, image);
        answerIntent.putExtra(Constants.CALL_TYPE_KEY, body);
        answerIntent.putExtra(Constants.CALL_SENDER_KEY_FOR_CONN, senderId);
        customView.setTextViewText(R.id.callerName, title);
        customView.setTextViewText(R.id.callType, body);
        //customView.setImageViewUri(R.id.callType, Uri.parse(image));


        @SuppressLint("UnspecifiedImmutableFlag") PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_MUTABLE);

        PendingIntent hangUpPendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, broadcastMessageIntent, PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_MUTABLE);
        PendingIntent answerPendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, answerIntent, PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_MUTABLE);

        customView.setOnClickPendingIntent(R.id.btnAnswer, answerPendingIntent);
        customView.setOnClickPendingIntent(R.id.btnDecline, hangUpPendingIntent);

        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
                .build();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
            NotificationChannel notificationChannel = new NotificationChannel("IncomingCall",
                    "Incoming Call", NotificationManager.IMPORTANCE_HIGH);

            notificationChannel.setSound(Settings.System.DEFAULT_RINGTONE_URI, audioAttributes);

            notificationManager.createNotificationChannel(notificationChannel);
            incomingCall = new NotificationCompat.Builder(getApplicationContext(), "IncomingCall")
                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                    .setSmallIcon(R.drawable.ic_baseline_call_24)
                    .setOngoing(true)
                    .setContent(customView)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setStyle(new NotificationCompat.DecoratedCustomViewStyle())
                    .setCustomContentView(customView)
                    .setCustomBigContentView(customView)
                    .setContentTitle(title)
                    .setContentText(body)
                    .setSound(Settings.System.DEFAULT_RINGTONE_URI)
                    .setDefaults(Notification.DEFAULT_LIGHTS | Notification.DEFAULT_SOUND)
                    .setContentIntent(pendingIntent)
                    .setFullScreenIntent(pendingIntent, true)
                    .setContentIntent(pendingIntent)
                    .setDefaults(Notification.DEFAULT_VIBRATE)
                    .setCategory(NotificationCompat.CATEGORY_CALL).build();
        }
        return incomingCall;
    }


    @RequiresApi(api = Build.VERSION_CODES.M)
    public void showMissedCallNotification(String title, String body) {
        Intent intent = new Intent("");
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                .build();

        NotificationChannel notificationChannel = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            notificationChannel = new NotificationChannel("MissedCall_",
                    "Missed Call", NotificationManager.IMPORTANCE_HIGH);
            notificationChannel.setSound(Settings.System.DEFAULT_NOTIFICATION_URI, audioAttributes);

            notificationManager.createNotificationChannel(notificationChannel);
            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(getApplicationContext(), "MissedCall_")
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle("Missed call by "+title)
                    .setContentText(body)
                    .setSound(Settings.System.DEFAULT_NOTIFICATION_URI)
                    .setAutoCancel(true)
                    .setContentIntent(pendingIntent);
            notificationManager.notify(1233, mBuilder.build());
        }
    }



}
