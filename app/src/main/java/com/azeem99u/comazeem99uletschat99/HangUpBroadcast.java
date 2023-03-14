package com.azeem99u.comazeem99uletschat99;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.ExistingWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

public class HangUpBroadcast extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Constants.HANG_UP_BROADCAST_ACTION)){
            String callerId = intent.getStringExtra(Constants.SENDER_ID_KEY);
            String workerId = intent.getStringExtra("WORKERID");
            Data.Builder builder = new Data.Builder();
            builder.putString("senderIdExtra",callerId);
            builder.putString("WORKERID_key",workerId);
            Data data = builder.build();
            Constraints constraints = new Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build();
            OneTimeWorkRequest oneTimeWorkRequest = new OneTimeWorkRequest.Builder(RejectCallWorker.class)
                    .setConstraints(constraints)
                    .setInputData(data)
                    .build();
            WorkManager.getInstance(context).enqueueUniqueWork(callerId, ExistingWorkPolicy.KEEP, oneTimeWorkRequest);

        }
    }
}
