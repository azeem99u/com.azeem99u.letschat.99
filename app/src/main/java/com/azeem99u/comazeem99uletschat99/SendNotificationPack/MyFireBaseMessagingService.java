package com.azeem99u.comazeem99uletschat99.SendNotificationPack;

import static com.azeem99u.comazeem99uletschat99.App.mAuth;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.ExistingWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.azeem99u.comazeem99uletschat99.App;
import com.azeem99u.comazeem99uletschat99.Constants;
import com.azeem99u.comazeem99uletschat99.R;
import com.azeem99u.comazeem99uletschat99.TokenHelper;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;


public class MyFireBaseMessagingService extends FirebaseMessagingService {

    private static final String YOUR_CHANNEL_ID = "callsChannel";

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        if (mAuth.getCurrentUser() != null) {
            Map<String, String> messageBody = remoteMessage.getData();
            String title = messageBody.get(Constants.TITLE);
            String body = messageBody.get(Constants.BODY);
            String click_action = messageBody.get(Constants.CLICK_ACTION);
            String channel_id = messageBody.get(Constants.CHANNEL_ID);
            String image = messageBody.get(Constants.IMAGE);
            String message_key = messageBody.get(Constants.MESSAGE_KEY);
            String sender_id = messageBody.get(Constants.SENDER_ID);
            String receiver_id = messageBody.get(Constants.RECEIVER_ID);

            if (channel_id.equals(App.MESSAGE_CHANNEL_ID) || channel_id.equals(App.GROUP_MESSAGE_CHANNEL_ID)) {

                Constraints constraints = new Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build();
                Data inputData = new Data.Builder()
                        .putString(Constants.CLICK_ACTION_KEY, click_action)
                        .putString(Constants.CHANNEL_ID_KEY, channel_id)
                        .putString(Constants.SENDER_ID_KEY, sender_id)
                        .putString(Constants.RECEIVER_ID_KEY, receiver_id)
                        .putString(Constants.MESSAGE_KEY_W, message_key)
                        .putString(Constants.TITLE_KEY, title)
                        .putString(Constants.BODY_KEY, body)
                        .putString(Constants.IMAGE_KEY, image)
                        .build();
                OneTimeWorkRequest oneTimeWorkRequest = new OneTimeWorkRequest.Builder(TaskListenAbleWorker.class)
                        .setInputData(inputData)
                        .setConstraints(constraints)
                        .build();
                assert message_key != null;
                WorkManager.getInstance(getApplicationContext()).enqueueUniqueWork(message_key, ExistingWorkPolicy.KEEP, oneTimeWorkRequest);

            } else if (channel_id.equals(App.CALL_CHANNEL_ID)) {

                Constraints constraints = new Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build();
                androidx.work.Data inputData = new androidx.work.Data.Builder()
                        .putString(Constants.CLICK_ACTION_KEY, click_action)
                        .putString(Constants.CHANNEL_ID_KEY, channel_id)
                        .putString(Constants.SENDER_ID_KEY, sender_id)
                        .putString(Constants.RECEIVER_ID_KEY, receiver_id)
                        .putString(Constants.MESSAGE_KEY_W, message_key)
                        .putString(Constants.TITLE_KEY, title)
                        .putString(Constants.BODY_KEY, body)
                        .putString(Constants.IMAGE_KEY, image)
                        .build();
                OneTimeWorkRequest oneTimeWorkRequest = new OneTimeWorkRequest.Builder(CallTaskListenAbleWorker.class)
                        .setInputData(inputData)
                        .setConstraints(constraints)
                        .build();

                WorkManager.getInstance(getApplicationContext()).enqueue(oneTimeWorkRequest);

            } else if (channel_id.equals(App.REQUESTS_CHANNEL_ID)) {
                showNotification(this, title, body, channel_id, click_action);
            }
        }
    }



    @RequiresApi(api = Build.VERSION_CODES.M)
    public void showNotification(Context context, String title, String body, String channelId, String action) {
        Intent intent = new Intent(action);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra(Constants.ACTION_REQUEST_EXTRA, "requests");
        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 232, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(body)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);
        notificationManager.notify(123, mBuilder.build());
    }

    @Override
    public void onNewToken(@NonNull String s) {
        super.onNewToken(s);
        if (mAuth.getCurrentUser() != null) {
            TokenHelper.updateNewToken(getApplicationContext(), s);
        }
    }


}
