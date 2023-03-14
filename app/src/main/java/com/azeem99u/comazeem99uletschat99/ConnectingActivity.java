package com.azeem99u.comazeem99uletschat99;

import static com.azeem99u.comazeem99uletschat99.App.mAuth;
import static com.azeem99u.comazeem99uletschat99.HelperFunctions.sendNotifications;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.azeem99u.comazeem99uletschat99.SendNotificationPack.Token;
import com.azeem99u.comazeem99uletschat99.databinding.ActivityConnectingBinding;
import com.azeem99u.comazeem99uletschat99.models.UserProfile;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.annotations.NotNull;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

public class ConnectingActivity extends AppCompatActivity {

    ValueEventListener valueEventListener;
    ActivityConnectingBinding binding;
    ValueEventListener valueEventListenerStatus;
    UserProfile selectedPersonProfile;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityConnectingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        String currentUserId = mAuth.getUid();

        binding.rejectBtn.setOnClickListener(view -> {
            FirebaseDatabase.getInstance().getReference().child(Constants.VIDEO_CALLS).child(currentUserId).child(Constants.CONNECTION_STATUS).setValue(Constants.CallConnection.END_ONGOING);
            finish();
        });

        valueEventListenerStatus = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists() && snapshot.hasChild(Constants.CONNECTION_STATUS)) {

                    if (snapshot.child(Constants.CONNECTION_STATUS).getValue(String.class).equals(Constants.CallConnection.NoAnswer.name())) {
                        String key = snapshot.getKey();
                        FirebaseDatabase.getInstance().getReference().child(Constants.VIDEO_CALLS).child(key).child(Constants.CONNECTION_STATUS).removeValue();
                        finish();
                    } else if (snapshot.child(Constants.CONNECTION_STATUS).getValue(String.class).equals(Constants.CallConnection.Rejected.name())) {
                        String key = snapshot.getKey();
                        FirebaseDatabase.getInstance().getReference().child(Constants.VIDEO_CALLS).child(key).child(Constants.CONNECTION_STATUS).removeValue();
                        finish();
                    }
                    if (snapshot.exists() && snapshot.child(Constants.CONNECTION_STATUS).getValue(String.class).equals(Constants.CallConnection.Ringing.name())) {
                        binding.connectivityTxt.setText("Ringing...");
                        String key = snapshot.getKey();
                        FirebaseDatabase.getInstance().getReference().child(Constants.VIDEO_CALLS).child(key).child(Constants.CALL_STATUS).setValue(Constants.CALLING);
                        FirebaseDatabase.getInstance().getReference().child(Constants.VIDEO_CALLS).child(key).child(Constants.CONNECTION_STATUS).removeValue();
                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };

        FirebaseDatabase.getInstance().getReference().child(Constants.VIDEO_CALLS).child(currentUserId).addValueEventListener(valueEventListenerStatus);


     if (getIntent() != null && getIntent().hasExtra(Constants.SELECTED_USER_ID_KEY) && getIntent().getStringExtra(Constants.CALL_TYPE).equals(Constants.VIDEO_CALL)) {
            binding.connectivityTxt.setText("Connecting...");
            binding.calltypeTxtC.setText("Video Call");
            String selectedUserId = getIntent().getStringExtra(Constants.SELECTED_USER_ID_KEY);
            HashMap<String, Object> room = new HashMap<>();
            room.put("incoming", currentUserId);
            room.put("createdBy", currentUserId);
            room.put("status", 0);
            room.put(Constants.CALL_TYPE,Constants.VIDEO_CALL);

            FirebaseDatabase.getInstance().getReference().child(Constants.VIDEO_CALLS).child(currentUserId)
                    .setValue(room).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            FirebaseDatabase.getInstance().getReference().child(Constants.USERS).child(currentUserId).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if (snapshot.exists() && snapshot.child("username").getValue(String.class) != null) {
                                        String sendName = snapshot.child("username").getValue(String.class);
                                        FirebaseDatabase.getInstance().getReference().child(Constants.USERS).child(selectedUserId).addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot snapshot2) {
                                                if (snapshot2.exists() && snapshot2.hasChild("deviceToken")) {
                                                    UserProfile profile = snapshot2.getValue(UserProfile.class);
                                                    if (profile.getUserImage() != null) {
                                                        Picasso.get().load(Uri.parse(profile.getUserImage())).error(R.drawable.profileimage).into(binding.callingPersonImage);
                                                    } else {
                                                        binding.callingPersonImage.setImageResource(R.drawable.profileimage);
                                                    }
                                                    if (profile.getUsername() != null) {
                                                        binding.callingPersonNameC.setText(profile.getUsername());
                                                    }

                                                    String deviceToken = (String) snapshot2.child("deviceToken").getValue();
                                                    Token token = new Token(deviceToken);
                                                    sendNotifications(token.getToken(), sendName + " calling...", Constants.VIDEO_CALL, Constants.ACTION_CALL, App.CALL_CHANNEL_ID, "", "", currentUserId, selectedUserId, null);
                                                }
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) {

                                            }

                                        });

                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });


                        }
                    });

        } else if (getIntent() != null && getIntent().hasExtra(Constants.SELECTED_USER_ID_KEY) && getIntent().getStringExtra(Constants.CALL_TYPE).equals(Constants.VOICE_CALL)) {
            binding.connectivityTxt.setText("Connecting...");
            binding.calltypeTxtC.setText("Voice Call");
            String selectedUserId = getIntent().getStringExtra(Constants.SELECTED_USER_ID_KEY);
            HashMap<String, Object> room = new HashMap<>();
            room.put("incoming", currentUserId);
            room.put("createdBy", currentUserId);
            room.put("status", 0);
            room.put(Constants.CALL_TYPE,Constants.VOICE_CALL);

            FirebaseDatabase.getInstance().getReference().child(Constants.VIDEO_CALLS).child(currentUserId)
                    .setValue(room).addOnSuccessListener(unused -> {
                        FirebaseDatabase.getInstance().getReference().child(Constants.USERS).child(currentUserId).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot1) {
                                if (snapshot1.exists() && snapshot1.child("username").getValue(String.class) != null) {

                                    Log.d("mytaggg", "onDataChange: " + 3);
                                    String sendName = snapshot1.child("username").getValue(String.class);
                                    FirebaseDatabase.getInstance().getReference().child(Constants.USERS).child(selectedUserId).addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot2) {
                                            if (snapshot2.exists() && snapshot2.hasChild("deviceToken")) {


                                                selectedPersonProfile = snapshot2.getValue(UserProfile.class);

                                                if (selectedPersonProfile.getUserImage() != null) {
                                                    Picasso.get().load(Uri.parse(selectedPersonProfile.getUserImage())).error(R.drawable.profileimage).into(binding.callingPersonImage);
                                                } else {
                                                    binding.callingPersonImage.setImageResource(R.drawable.profileimage);
                                                }
                                                if (selectedPersonProfile.getUsername() != null) {
                                                    binding.callingPersonNameC.setText(selectedPersonProfile.getUsername());
                                                } else {
                                                    binding.callingPersonNameC.setText("Unknown");
                                                }
                                                String deviceToken = (String) snapshot2.child("deviceToken").getValue();
                                                Token token = new Token(deviceToken);
                                                sendNotifications(token.getToken(), sendName + " calling...", Constants.VOICE_CALL, Constants.ACTION_CALL, App.CALL_CHANNEL_ID, "", "", currentUserId, selectedUserId, null);
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {

                                        }

                                    });

                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }

                        });
                    });
        }


        valueEventListener = new ValueEventListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                if (snapshot.child("status").exists()) {

                    if (snapshot.child("status").getValue(Integer.class) == 1) {
                        Intent intent;
                        if (snapshot.child(Constants.CALL_TYPE).getValue(String.class).equals(Constants.VIDEO_CALL)){
                            intent = new Intent(ConnectingActivity.this, VideoCallActivity.class);
                        }else {
                            intent = new Intent(ConnectingActivity.this, AudioCallActivity.class);
                        }
                        if (selectedPersonProfile != null){
                            intent.putExtra("image_key_s",selectedPersonProfile.getUserImage());
                            intent.putExtra("name_key_s",selectedPersonProfile.getUsername());
                        }
                        String incoming = snapshot.child("incoming").getValue(String.class);
                        String createdBy = snapshot.child("createdBy").getValue(String.class);
                        intent.putExtra("username", currentUserId);
                        intent.putExtra("incoming", incoming);
                        intent.putExtra("createdBy", createdBy);
                        startActivity(intent);
                        finish();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        };
        FirebaseDatabase.getInstance().getReference().child(Constants.VIDEO_CALLS).child(currentUserId).addValueEventListener(this.valueEventListener);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        FirebaseDatabase.getInstance().getReference().child(Constants.VIDEO_CALLS).child(mAuth.getUid()).child(Constants.CONNECTION_STATUS).removeEventListener(valueEventListenerStatus);
        FirebaseDatabase.getInstance().getReference().child(Constants.VIDEO_CALLS).child(mAuth.getUid()).removeEventListener(valueEventListener);
    }


    @Override
    protected void onResume() {
        super.onResume();
        binding.rippleBackground.startRippleAnimation();
    }

    @Override
    public void onPause() {
        super.onPause();
        binding.rippleBackground.stopRippleAnimation();
    }

}