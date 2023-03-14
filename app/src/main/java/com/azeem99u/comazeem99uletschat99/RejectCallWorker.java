package com.azeem99u.comazeem99uletschat99;

import android.app.NotificationManager;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.concurrent.futures.CallbackToFutureAdapter;
import androidx.work.Data;
import androidx.work.ListenableWorker;
import androidx.work.WorkManager;
import androidx.work.WorkerParameters;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.firebase.database.FirebaseDatabase;

import java.util.UUID;

public class RejectCallWorker extends ListenableWorker {
    NotificationManager notificationManager;
    public RejectCallWorker(@NonNull Context appContext, @NonNull WorkerParameters workerParams) {
        super(appContext, workerParams);
        notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
    }

    @NonNull
    @Override
    public ListenableFuture<Result> startWork() {
        Data data = getInputData();
        String senderId = data.getString("senderIdExtra");
        String workerId = data.getString("WORKERID_key");
        return CallbackToFutureAdapter.getFuture(completer -> {
            FirebaseDatabase.getInstance().getReference().child(Constants.VIDEO_CALLS).child(senderId).child(Constants.CONNECTION_STATUS).setValue(Constants.CallConnection.Rejected.name());
            WorkManager.getInstance(getApplicationContext()).cancelWorkById(UUID.fromString(workerId));
            completer.set(Result.success());
            return completer;
        });
    }
}
