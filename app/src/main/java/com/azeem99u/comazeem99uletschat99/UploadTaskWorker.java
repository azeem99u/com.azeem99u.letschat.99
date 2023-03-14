package com.azeem99u.comazeem99uletschat99;

import static android.content.ContentValues.TAG;
import static android.content.Context.NOTIFICATION_SERVICE;
import static com.azeem99u.comazeem99uletschat99.Constants.PROGRESS_UPLOAD;
import static com.azeem99u.comazeem99uletschat99.HelperFunctions.convertContentTypeIntoFile;
import static com.azeem99u.comazeem99uletschat99.HelperFunctions.readDataFromContentUri;
import static com.azeem99u.comazeem99uletschat99.HelperFunctions.sendNotifications;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.SystemClock;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.concurrent.futures.CallbackToFutureAdapter;
import androidx.core.app.NotificationCompat;
import androidx.work.Data;
import androidx.work.ForegroundInfo;
import androidx.work.ListenableWorker;
import androidx.work.WorkerParameters;

import com.azeem99u.comazeem99uletschat99.SendNotificationPack.Token;
import com.azeem99u.comazeem99uletschat99.models.PersonalMessageModel;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.net.URL;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.HashMap;

public class UploadTaskWorker extends ListenableWorker {
    DatabaseReference mUserMessageDatabaseRef = null;
    StorageReference imageRef = null;
    NotificationManager notificationManager;
    NotificationCompat.Builder builder;
    StorageReference fileRef;

    public UploadTaskWorker(@NonNull Context appContext, @NonNull WorkerParameters workerParams) {
        super(appContext, workerParams);
        mUserMessageDatabaseRef = FirebaseDatabase.getInstance().getReference().child(Constants.MESSAGES);
        imageRef = FirebaseStorage.getInstance().getReference("Images/");
        fileRef = FirebaseStorage.getInstance().getReference("Files/");
        builder = new NotificationCompat.Builder(getApplicationContext(), App.UPLOAD_CHANNEL_ID);
        builder.setPriority(Notification.PRIORITY_MIN);
        builder.setSmallIcon(R.drawable.ic_baseline_cloud_upload_24);
        builder.setContentTitle("Uploading");
        notificationManager = (NotificationManager) getApplicationContext().getSystemService(NOTIFICATION_SERVICE);
        setProgressAsync(new Data.Builder().putInt(PROGRESS_UPLOAD, 0).build());
        setForegroundAsync(createForegroundInfo(true));
    }

    @NonNull
    @Override
    public ListenableFuture<Result> startWork() {
        Data data = getInputData();
        String objectString = data.getString(Constants.MESSAGE_POJO_S_KEY);
        String selectedPersonId = data.getString(Constants.SELECTED_USER_ID_KEY);
        PersonalMessageModel messageModel = HelperFunctions.deserializeFromJsonPersonalMessageModel(objectString);
        return CallbackToFutureAdapter.getFuture(completer -> {
            final String[] downloadUrl = {null};
            String fileName = messageModel.getFileName();
            switch (messageModel.getType()) {
                case "text":
                    sendText(selectedPersonId, messageModel, completer);
                    break;
                case "location":
                    sendLocation(selectedPersonId, messageModel, completer);
                    break;
                case "image":
                    uploadImage(selectedPersonId, messageModel, completer, downloadUrl, fileName);
                    break;
                case "file":
                    uploadFile(selectedPersonId, messageModel, completer, downloadUrl, fileName);
                    break;
            }
            return completer;
        });
    }

    private void sendLocation(String selectedPersonId, PersonalMessageModel messageModel, CallbackToFutureAdapter.Completer<Result> completer) {
        String currentUserId = messageModel.getFrom();
        if (currentUserId == null) {
            completer.set(Result.failure());
        }
        HashMap<String, Object> map = new HashMap<>();
        map.put(Constants.DELIVERY, Constants.Delivery.Sent.name());
        mUserMessageDatabaseRef.child(currentUserId).child(selectedPersonId).child(messageModel.getMessageKey()).updateChildren(map).addOnCompleteListener(task -> {
            PersonalMessageModel personalMessageModel = new PersonalMessageModel(messageModel.getMessageKey(), messageModel.getType(), messageModel.getMessage(), messageModel.getTime(), currentUserId, Constants.Delivery.Sent.name());
            mUserMessageDatabaseRef.child(selectedPersonId).child(currentUserId).child(messageModel.getMessageKey()).setValue(personalMessageModel).addOnCompleteListener(task1 -> {
                FirebaseDatabase.getInstance().getReference().child(Constants.USERS).child(currentUserId).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists() && snapshot.hasChild("username")) {
                            String currentUsername = snapshot.child("username").getValue(String.class);
                            FirebaseDatabase.getInstance().getReference().child(Constants.USERS).child(selectedPersonId).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot1) {
                                    if (snapshot1.exists() && snapshot1.hasChild("deviceToken")) {
                                        String deviceToken = (String) snapshot1.child("deviceToken").getValue();
                                        Token token = new Token(deviceToken);
                                        sendNotifications(token.getToken(), "Live Location Message from " + currentUsername, "" + messageModel.getMessage(), Constants.ACTION_PERSONAL_MESSAGE, App.MESSAGE_CHANNEL_ID, "", messageModel.getMessageKey(), messageModel.getFrom(), selectedPersonId,completer);
                                    }
                                }
                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    completer.set(Result.failure());
                                }

                            });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        completer.set(Result.failure());
                    }
                });
            });

        });

    }

    private void sendText(String selectedPersonId, PersonalMessageModel messageModel, CallbackToFutureAdapter.Completer<Result> completer) {
        String currentUserId = messageModel.getFrom();
        if (currentUserId == null) {
            completer.set(Result.failure());
        }
        HashMap<String, Object> map = new HashMap<>();
        map.put(Constants.DELIVERY, Constants.Delivery.Sent.name());
        mUserMessageDatabaseRef.child(currentUserId).child(selectedPersonId).child(messageModel.getMessageKey()).updateChildren(map).addOnCompleteListener(task -> {
            PersonalMessageModel personalMessageModel = new PersonalMessageModel(messageModel.getMessageKey(), messageModel.getType(), messageModel.getMessage(), messageModel.getTime(), currentUserId, Constants.Delivery.Sent.name());
            mUserMessageDatabaseRef.child(selectedPersonId).child(currentUserId).child(messageModel.getMessageKey()).setValue(personalMessageModel).addOnCompleteListener(task1 -> {
                FirebaseDatabase.getInstance().getReference().child(Constants.USERS).child(currentUserId).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists() && snapshot.hasChild("username")) {
                            String currentUsername = snapshot.child("username").getValue(String.class);
                            FirebaseDatabase.getInstance().getReference().child(Constants.USERS).child(selectedPersonId).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot1) {
                                    if (snapshot1.exists() && snapshot1.hasChild("deviceToken")) {
                                        String deviceToken = (String) snapshot1.child("deviceToken").getValue();
                                        Token token = new Token(deviceToken);
                                        sendNotifications(token.getToken(), "Message from " + currentUsername, "" + messageModel.getMessage(), Constants.ACTION_PERSONAL_MESSAGE, App.MESSAGE_CHANNEL_ID, "", messageModel.getMessageKey(), messageModel.getFrom(), selectedPersonId,completer);

                                        //sendNotifications("e8ajGjFRQkmhvlTue2B-o8:APA91bE75NBnDrz-TRFNoql7PYJLnVdR9d4pT_wdculjlzFveEvea-5LBWG0Jla6uYM8oWvbGR5Rct_g_mC_q0v7ma8Pj9iVsLzNE2F8VqFz8K5GRaN8bBI2nl6daB40ncBpcuUps9Vi","azeem"+" Calling..." ,Constants.VIDEO_CALL , Constants.ACTION_CALL, App.CALL_CHANNEL_ID, "", "", currentUserId, selectedPersonId, null);

                                        //sendNotifications("dW8KhPNLRNemDQaKK4V6UI:APA91bF0RxPG2Oc5LLNXnQZJBHJnDPI4AOGVIcUreDMdnDf7mLc4_mBYuR568bCO7C6-R0jJu9eYGz-kjX1AXUbWwrswIib8kAwgDtVYi_3R1YVQWszIjfsSADi94xa5vMhE8YdYqzZ5", "Message from " + "", "" , Constants.ACTION_VIDEO_CALL, App.VIDEO_CALL_CHANNEL_ID, "", messageModel.getMessageKey(), messageModel.getFrom(), selectedPersonId,completer);
                                    }
                                }
                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    completer.set(Result.failure());
                                }

                            });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        completer.set(Result.failure());
                    }
                });
            });

        });

    }


    private void uploadFile(String selectedPersonId, PersonalMessageModel messageModel, CallbackToFutureAdapter.Completer<Result> completer, String[] downloadUrl, String fileName) {
        Uri getPhonePathStorage = Uri.parse(messageModel.getPhoneStoragePath());
        String filePath = convertContentTypeIntoFile(getApplicationContext(), getPhonePathStorage);
        if (filePath != null && !fileName.isEmpty()) {
            UploadTask uploadTask = fileRef.child(fileName).putFile(getPhonePathStorage);
            uploadTask.addOnProgressListener(snapshot -> {
                long l = (100 * snapshot.getBytesTransferred()) / snapshot.getTotalByteCount();
                setForegroundAsync(changeNotificationProgress(100, (int) l, snapshot.getBytesTransferred(), snapshot.getTotalByteCount(), false));
                setProgressAsync(new Data.Builder().putInt(Constants.PROGRESS_UPLOAD, (int) l).build());

            }).continueWithTask(task -> {
                if (!task.isSuccessful()) {
                    completer.set(Result.failure());
                    Log.d(TAG, "uploadFile: " + task.getException());
                }
                return fileRef.child(fileName).getDownloadUrl();
            }).continueWithTask(task -> {
                downloadUrl[0] = task.getResult().toString();
                if (!task.isSuccessful()) {
                    completer.set(Result.failure());
                    Log.d(TAG, "uploadFile: " + task.getException());
                }
                URL url = new URL(task.getResult().toString());
                return FirebaseStorage.getInstance().getReferenceFromUrl(url.toString()).getBytes(Long.MAX_VALUE);
            }).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    long fileSize = task.getResult().length;
                    PersonalMessageModel personalMessageModel = new PersonalMessageModel(messageModel.getMessageKey(), messageModel.getType(), fileName, "", "", "" + fileSize, downloadUrl[0], messageModel.getTime(), messageModel.getFrom(), Constants.Delivery.Sent.name());
                    sendMessageNow(selectedPersonId, messageModel.getFrom(), messageModel.getMessageKey(), personalMessageModel, "Send you a File", completer);
                }
            });
        }
    }

    private void uploadImage(String selectedPersonId, PersonalMessageModel messageModel, CallbackToFutureAdapter.Completer<Result> completer, String[] downloadUrl, String fileName) {
        String filePath = messageModel.getPhoneStoragePath();
        String fileThumb = "thumb" + fileName;
        Bitmap bitmap = readDataFromContentUri(getApplicationContext(), Uri.parse(filePath));
        if (bitmap == null && fileName.isEmpty()) {
            completer.set(Result.failure());
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        assert bitmap != null;
        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, baos);
        UploadTask uploadTask = imageRef.child(fileName).putBytes(baos.toByteArray());
        uploadTask.addOnProgressListener(snapshot -> {

            long l = (100 * snapshot.getBytesTransferred()) / snapshot.getTotalByteCount();
            setForegroundAsync(changeNotificationProgress(100, (int) l, snapshot.getBytesTransferred(), snapshot.getTotalByteCount(), false));
            setProgressAsync(new Data.Builder().putInt(Constants.PROGRESS_UPLOAD, (int) l).build());

        }).continueWithTask(task -> {
            if (!task.isSuccessful()) {
                Log.d(TAG, "uploadFile: " + task.getException());
            }
            return imageRef.child(fileName).getDownloadUrl();
        }).continueWithTask(task -> {

            downloadUrl[0] = task.getResult().toString();

            if (!task.isSuccessful()) {
                completer.set(Result.failure());
                Log.d(TAG, "uploadFile: " + task.getException());
            }
            URL url = new URL(task.getResult().toString());
            return FirebaseStorage.getInstance().getReferenceFromUrl(url.toString()).getBytes(Long.MAX_VALUE);
        }).addOnCompleteListener(task -> {

            if (task.isSuccessful()) {
                ByteArrayOutputStream baos2 = new ByteArrayOutputStream();
                int length = baos.size();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 5, baos2);
                UploadTask uploadTask2 = imageRef.child(fileThumb).putBytes(baos2.toByteArray());
                uploadTask2.continueWithTask(task2 -> {
                    if (!task2.isSuccessful()) {
                        completer.set(Result.failure());
                        Log.d(TAG, "uploadFile: " + task2.getException());
                    }
                    return imageRef.child(fileThumb).getDownloadUrl();
                }).addOnSuccessListener(uri -> {

                    String thumbDownloadUrl = uri.toString();
                    PersonalMessageModel personalMessageModel = new PersonalMessageModel(messageModel.getMessageKey(), messageModel.getType(), messageModel.getMessage(), fileName, "", "", "" + length, downloadUrl[0], thumbDownloadUrl, messageModel.getTime(), messageModel.getFrom(), Constants.Delivery.Sent.name());
                    sendMessageNow(selectedPersonId, messageModel.getFrom(), messageModel.getMessageKey(), personalMessageModel, "Send you an Image", completer);

                });
            }
        });
    }

    private void sendMessageNow(String selectedPersonId, String currentUserId, String key, PersonalMessageModel personalMessageModel, String notification_message, CallbackToFutureAdapter.Completer<ListenableWorker.Result> completer) {
        HashMap<String, Object> map = new HashMap<>();
        map.put(Constants.DELIVERY, Constants.Delivery.Sent.name());
        mUserMessageDatabaseRef.child(currentUserId).child(selectedPersonId).child(key).updateChildren(map).addOnCompleteListener(task12 -> {
            if (task12.isSuccessful()) {
                mUserMessageDatabaseRef.child(selectedPersonId).child(currentUserId).child(key).setValue(personalMessageModel).addOnCompleteListener(task1 -> {
                    if (task12.isSuccessful()) {
                        FirebaseDatabase.getInstance().getReference().child(Constants.USERS).child(currentUserId).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.exists() && snapshot.hasChild("username")) {
                                    String currentUsername = snapshot.child("username").getValue(String.class);
                                    FirebaseDatabase.getInstance().getReference().child(Constants.USERS).child(selectedPersonId).addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            if (snapshot.exists() && snapshot.hasChild("deviceToken")) {
                                                String deviceToken = (String) snapshot.child("deviceToken").getValue();
                                                Token token = new Token(deviceToken);
                                                Log.d("token1", "onDataChange: " + token.getToken());
                                                HelperFunctions.sendNotifications(token.getToken(), currentUsername, notification_message, Constants.ACTION_PERSONAL_MESSAGE, App.MESSAGE_CHANNEL_ID, "", personalMessageModel.getMessageKey(), currentUserId, selectedPersonId, completer);
                                            }
                                        }
                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {
                                            completer.set(ListenableWorker.Result.failure());
                                        }
                                    });

                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                completer.set(ListenableWorker.Result.failure());
                            }

                        });
                    }
                });
            }
        });
    }


    public ForegroundInfo createForegroundInfo(boolean indeterminate) {
        builder.setOngoing(true);
        builder.setProgress(100, 0, indeterminate);
        return new ForegroundInfo(Constants.NOTIFICATION_ID_UPLOAD, builder.build());
    }

    public ForegroundInfo changeNotificationProgress(int max, int progress, long downloading, long fileSize, boolean indeterminate) {
        String uploaded = humanReadableByteCountSI(downloading);
        String total = humanReadableByteCountSI(fileSize);
        builder.setContentText(uploaded + " / " + total);
        builder.setProgress(max, progress, indeterminate);
        if (!isStopped()) {
            SystemClock.sleep(1000);
            if (isStopped()) {
                return null;
            }
        }
        return new ForegroundInfo(Constants.NOTIFICATION_ID_UPLOAD, builder.build());
    }


    public String humanReadableByteCountSI(long bytes) {
        if (-1000 < bytes && bytes < 1000) {
            return bytes + " B";
        }
        CharacterIterator ci = new StringCharacterIterator("kMGTPE");
        while (bytes <= -999_950 || bytes >= 999_950) {
            bytes /= 1000;
            ci.next();
        }
        return String.format("%.1f %cB", bytes / 1000.0, ci.current());
    }
}
