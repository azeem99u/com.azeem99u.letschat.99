package com.azeem99u.comazeem99uletschat99;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.core.os.BuildCompat;

public class bootBroadcastReceiver extends BroadcastReceiver {

    private static final String TAG = "BootBroadcastReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        boolean bootCompleted;
        String action = intent.getAction();

        if (BuildCompat.isAtLeastN()) {
            bootCompleted = Intent.ACTION_LOCKED_BOOT_COMPLETED.equals(action);
        } else {
            bootCompleted = Intent.ACTION_BOOT_COMPLETED.equals(action);
        }
        if (!bootCompleted) {
            return;
        }

    }
}