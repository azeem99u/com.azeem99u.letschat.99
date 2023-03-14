package com.azeem99u.comazeem99uletschat99.connectivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;

public class ConnectivityCheckBroadcast extends BroadcastReceiver {

    public ConnectivityCheckBroadcast(ConnectivityListener connectivityListener) {
        this.connectivityListener = connectivityListener;
    }

    public interface ConnectivityListener {
        void checkConnection(Boolean isConnected);
    }


    private ConnectivityListener connectivityListener;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction())) {
            boolean booleanExtra = intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, false);
            if (!booleanExtra) {
                connectivityListener.checkConnection(true);
            }else {
                connectivityListener.checkConnection(false);
            }
        }
    }
}
