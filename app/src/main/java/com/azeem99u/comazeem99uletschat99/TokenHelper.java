package com.azeem99u.comazeem99uletschat99;

import static com.azeem99u.comazeem99uletschat99.App.mAuth;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class TokenHelper {

    TokenHelper() {
    }


    public static void deleteMyToken(Context context) {
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        database.getReference(Constants.USERS + "/" + FirebaseAuth.getInstance().getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists() && snapshot.hasChild("username")) {
                    ConnectivityStatusHelper.stopConnectionStatus();
                    updateNewToken(context, "");
                    App.mGoogleSignInClient.revokeAccess().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            App.mAuth.signOut();
                        }
                    });

                } else {
                    database.getReference(Constants.USERS + "/" + mAuth.getCurrentUser().getUid()).removeValue().addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            SharedPreferences pref = context.getSharedPreferences("mypref", 0);
                            SharedPreferences.Editor editor = pref.edit();
                            editor.remove("regId");
                            editor.apply();
                            App.mGoogleSignInClient.revokeAccess().addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    App.mAuth.signOut();
                                }
                            });
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    public static void updateNewToken(Context context, String token) {

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(Constants.USERS + "/" + mAuth.getCurrentUser().getUid());
        Map<String, Object> map = new HashMap<>();
        map.put("/deviceToken", token);
        reference.updateChildren(map).addOnCompleteListener(task1 -> {
            if (task1.isSuccessful()) {
                SharedPreferences pref = context.getSharedPreferences("mypref", 0);
                SharedPreferences.Editor editor = pref.edit();
                editor.putString("regId", token);
                editor.apply();
            }
        });
    }


    public static void addNewToken(Context context) {

        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                DatabaseReference reference = FirebaseDatabase.getInstance().getReference(Constants.USERS + "/" + mAuth.getCurrentUser().getUid());
                Map<String, Object> map = new HashMap<>();
                map.put("/deviceToken", task.getResult());
                reference.updateChildren(map).addOnCompleteListener(task1 -> {
                    if (task1.isSuccessful()) {
                        SharedPreferences pref = context.getSharedPreferences("mypref", 0);
                        SharedPreferences.Editor editor = pref.edit();
                        editor.putString("regId", task.getResult());
                        editor.apply();
                    }
                });

            } else {
                String s = Objects.requireNonNull(task.getException()).getLocalizedMessage();
                Log.d("mytag", "onComplete: " + s);
            }
        });
    }


}
