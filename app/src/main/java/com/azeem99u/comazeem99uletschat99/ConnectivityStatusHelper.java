package com.azeem99u.comazeem99uletschat99;


import static com.azeem99u.comazeem99uletschat99.App.mAuth;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ConnectivityStatusHelper {

    public static void updateConnectionStatus() {
        mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser()!= null){
            final FirebaseDatabase database = FirebaseDatabase.getInstance();
            final DatabaseReference myConnectionsRef = database.getReference(Constants.USERS + "/" + mAuth.getCurrentUser().getUid() + "/" + Constants.USER_STATUS);
            final DatabaseReference lastOnlineRef = database.getReference(Constants.USERS + "/" + mAuth.getCurrentUser().getUid() + "/" + Constants.USER_STATUS);
            final DatabaseReference connectedRef;
            connectedRef = database.getReference(".info/connected");
            connectedRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    boolean connected = Boolean.TRUE.equals(snapshot.getValue(Boolean.class));
                    if (connected) {
                        myConnectionsRef.onDisconnect().removeValue();
                        lastOnlineRef.onDisconnect().setValue(System.currentTimeMillis());
                        myConnectionsRef.setValue("online");
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.w("mytag", "Listener was cancelled at .info/connected");
                }
            });

        }

    }


    public static void stopConnectionStatus() {
        mAuth = FirebaseAuth.getInstance();
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        if (mAuth.getCurrentUser()!= null){
            final DatabaseReference myConnectionsRef = database.getReference(Constants.USERS + "/" + mAuth.getCurrentUser().getUid() + "/" + Constants.USER_STATUS);
            myConnectionsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override

                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        myConnectionsRef.setValue(System.currentTimeMillis());
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                }
            });

        }

    }


}
