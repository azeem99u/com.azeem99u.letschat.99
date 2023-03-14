package com.azeem99u.comazeem99uletschat99;


import static com.azeem99u.comazeem99uletschat99.App.mAuth;
import static com.azeem99u.comazeem99uletschat99.HelperFunctions.startTimer;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.telecom.TelecomManager;
import android.view.View;
import android.webkit.PermissionRequest;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.azeem99u.comazeem99uletschat99.databinding.ActivityAudioCallBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AudioCallActivity extends AppCompatActivity {

    ActivityAudioCallBinding binding;
    String createdBy;
    ValueEventListener valueEventListenerForConn;
    boolean isPeerConnected = false;
    boolean isAudio = true;
    DatabaseReference firebaseRef;
    boolean pageExit = false;
    boolean done = false;


    @RequiresApi(api = Build.VERSION_CODES.M)
    @SuppressLint({"SetJavaScriptEnabled"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAudioCallBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        firebaseRef = FirebaseDatabase.getInstance().getReference().child(Constants.VIDEO_CALLS);
        binding.webView.addJavascriptInterface(new AudioCallInterfaceJava(AudioCallActivity.this), "Android");
        binding.webView.addJavascriptInterface(new EarpieceCallInterfaceJava(AudioCallActivity.this), "AndroidE");
        binding.speakerBtn.setBackgroundColor(Color.TRANSPARENT);

        if (getIntent() != null && getIntent().hasExtra(Constants.CALL_SENDER_KEY_FOR_CONN)) {

            String callerId = getIntent().getStringExtra(Constants.CALL_SENDER_KEY_FOR_CONN);
            FirebaseDatabase.getInstance().getReference().child(Constants.VIDEO_CALLS).child(callerId).child(Constants.CONNECTION_STATUS).setValue(Constants.CallConnection.Accepted.name());
            createdBy = callerId;
            String callerName = getIntent().getStringExtra(Constants.CALLER_NAME_KEY);
            String callerImage = getIntent().getStringExtra(Constants.CALLER_IMAGE_KEY);
            //String callTypeText = getIntent().getStringExtra(Constants.CALL_TYPE_KEY);

            binding.callerNameTxt.setText(callerName);

            if (callerImage != null) {
                Picasso.get().load(Uri.parse(callerImage)).error(R.drawable.profileimage).into(binding.callerImage);
            } else {
                binding.callerImage.setImageResource(R.drawable.profileimage);
            }
            binding.callTimeTxt.setText("Connecting...");

            String username = mAuth.getUid();

            FirebaseDatabase.getInstance().getReference().child(Constants.VIDEO_CALLS).child(callerId).addListenerForSingleValueEvent(new ValueEventListener() {
                @RequiresApi(api = Build.VERSION_CODES.P)
                @Override
                public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                    if (snapshot.child("status").exists()) {
                        if (snapshot.child("status").getValue(Integer.class) == 0) {

                            FirebaseDatabase.getInstance().getReference().child(Constants.VIDEO_CALLS).child(callerId).child("incoming").setValue(username);
                            FirebaseDatabase.getInstance().getReference().child(Constants.VIDEO_CALLS).child(callerId).child("status").setValue(1);
                            binding.webView.setWebChromeClient(new WebChromeClient() {
                                @SuppressLint("ObsoleteSdkInt")
                                @Override
                                public void onPermissionRequest(PermissionRequest request) {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                        request.grant(request.getResources());
                                    }
                                }
                            });

                            binding.webView.getSettings().setJavaScriptEnabled(true);
                            binding.webView.getSettings().setMediaPlaybackRequiresUserGesture(false);
                            String filePath = "file:android_asset/callaudio.html";
                            binding.webView.loadUrl(filePath);
                            binding.webView.setWebViewClient(new WebViewClient() {
                                @Override
                                public void onPageFinished(WebView view, String url) {
                                    super.onPageFinished(view, url);

                                    String uniqueId = getUniqueId();
                                    callJavaScriptFunction("javascript:init(\"" + uniqueId + "\")");
                                    new Handler().postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            if (!isPeerConnected) {
                                                FirebaseDatabase.getInstance().getReference().child(Constants.VIDEO_CALLS).child(createdBy).child(Constants.CONNECTION_STATUS).setValue(Constants.CallConnection.Failed.name());
                                                callJavaScriptFunction("javascript:closeStreamNow()");
                                                endCall();
                                                finish();
                                                return;
                                            }
                                            new Handler().post(new Runnable() {
                                                @Override
                                                public void run() {
                                                    firebaseRef.child(callerId).child("connId").addListenerForSingleValueEvent(new ValueEventListener() {
                                                        @RequiresApi(api = Build.VERSION_CODES.P)
                                                        @Override
                                                        public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                                                            if (snapshot.getValue() == null) {
                                                                callJavaScriptFunction("javascript:closeStreamNow()");
                                                                firebaseRef.child(createdBy).setValue(null);
                                                                endCall();
                                                                finish();
                                                                return;
                                                            }
                                                            String connId = snapshot.getValue(String.class);
                                                            callJavaScriptFunction("javascript:startCall(\"" + connId + "\")");
                                                            startCallTimer();
                                                        }

                                                        @Override
                                                        public void onCancelled(@NonNull @NotNull DatabaseError error) {

                                                        }
                                                    });
                                                }
                                            });
                                        }
                                    }, 4000);
                                }
                            });
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        } else {

            if (getIntent().hasExtra("image_key_s")) {
                String image_key_s = getIntent().getStringExtra("image_key_s");
                String name_key_s = getIntent().getStringExtra("name_key_s");
                binding.callerNameTxt.setText(name_key_s);
                if (image_key_s != null) {
                    Picasso.get().load(Uri.parse(image_key_s)).error(R.drawable.profileimage).into(binding.callerImage);
                } else {
                    binding.callerImage.setImageResource(R.drawable.profileimage);
                }

            }
            binding.callTimeTxt.setText("Connecting...");
            String username = getIntent().getStringExtra("username");
            createdBy = username;
            binding.webView.setWebChromeClient(new WebChromeClient() {
                @SuppressLint("ObsoleteSdkInt")
                @Override
                public void onPermissionRequest(PermissionRequest request) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        request.grant(request.getResources());
                    }
                }
            });


            binding.webView.getSettings().setJavaScriptEnabled(true);
            binding.webView.getSettings().setMediaPlaybackRequiresUserGesture(false);

            String filePath = "file:android_asset/callaudio.html";
            binding.webView.loadUrl(filePath);

            binding.webView.setWebViewClient(new WebViewClient() {
                @Override
                public void onPageFinished(WebView view, String url) {
                    super.onPageFinished(view, url);
                    String uniqueId = getUniqueId();
                    callJavaScriptFunction("javascript:init(\"" + uniqueId + "\")");
                    if (pageExit) {
                        return;
                    }
                    firebaseRef.child(username).child("connId").setValue(uniqueId);
                    new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            startCallTimer();
                        }
                    }, 4000);
                }
            });
        }

        valueEventListenerForConn = new ValueEventListener() {
            @RequiresApi(api = Build.VERSION_CODES.P)
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    if (Objects.equals(snapshot.getValue(String.class), Constants.CallConnection.End.name())) {
                        callJavaScriptFunction("javascript:closeStreamNow()");
                        endCall();
                        done = true;
                        FirebaseDatabase.getInstance().getReference().child(Constants.VIDEO_CALLS).child(createdBy).child(Constants.CONNECTION_STATUS).removeValue();
                        finish();
                    } else if (Objects.equals(snapshot.getValue(String.class), Constants.CallConnection.Failed.name())) {
                        callJavaScriptFunction("javascript:closeStreamNow()");
                        FirebaseDatabase.getInstance().getReference().child(Constants.VIDEO_CALLS).child(createdBy).child(Constants.CONNECTION_STATUS).removeValue();
                        endCall();
                        finish();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                done = true;
            }
        };
        if (createdBy != null) {
            FirebaseDatabase.getInstance().getReference().child(Constants.VIDEO_CALLS).child(createdBy).child(Constants.CONNECTION_STATUS).addValueEventListener(valueEventListenerForConn);
        }


        binding.micAudioCallBtn.setOnClickListener(view ->
        {
            isAudio = !isAudio;
            if (isAudio) {
                callJavaScriptFunction("javascript:toggleAudio(\"" + true + "\")");
                binding.micAudioCallBtn.setBackgroundColor(Color.TRANSPARENT);
            } else {
                callJavaScriptFunction("javascript:toggleAudio(\"" + false + "\")");
                binding.micAudioCallBtn.setBackgroundColor(Color.parseColor("#7B7B7C"));
            }
        });


        binding.endCallBtn.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.P)
            @Override
            public void onClick(View view) {
                done = true;
                FirebaseDatabase.getInstance().getReference().child(Constants.VIDEO_CALLS).child(createdBy).child(Constants.CONNECTION_STATUS).setValue(Constants.CallConnection.End.name());
                callJavaScriptFunction("javascript:closeStreamNow()");
                endCall();
                finish();
            }
        });

        binding.speakerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggleSpeaker();
            }
        });
    }

    private void startCall(String callerId) {

    }


    private void startCallTimer() {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(() -> {
            long i = 0;
            while (!done) {
                long finalI1 = i;
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        String s = startTimer(finalI1);
                        binding.callTimeTxt.setText(s);
                    }
                });
                SystemClock.sleep(1000);
                i++;
            }
            executorService.shutdown();
        });
    }


    public void toggleSpeaker() {
        AudioManager am = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
        boolean isSpeakerOn = am.isSpeakerphoneOn();
        if (isSpeakerOn) {
            binding.speakerBtn.setBackgroundColor(Color.TRANSPARENT);
            am.setSpeakerphoneOn(false);
        } else {
            am.setSpeakerphoneOn(true);
            binding.speakerBtn.setBackgroundColor(Color.parseColor("#7B7B7C"));
        }
    }

    void setEarpiece() {
        AudioManager am = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
        am.setSpeakerphoneOn(false);
    }

    public void onPeerConnected() {
        isPeerConnected = true;
    }


    @RequiresApi(api = Build.VERSION_CODES.P)
    void endCall() {

        TelecomManager tm = (TelecomManager) getSystemService(Context.TELECOM_SERVICE);

        if (tm != null) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ANSWER_PHONE_CALLS) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            boolean success = tm.endCall();
        }
    }

    void callJavaScriptFunction(String function) {
        binding.webView.post(() -> binding.webView.evaluateJavascript(function, null));
    }

    String getUniqueId() {
        return UUID.randomUUID().toString();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        done = true;
        pageExit = true;
        if (valueEventListenerForConn != null) {
            FirebaseDatabase.getInstance().getReference().child(Constants.VIDEO_CALLS).child(createdBy).child(Constants.CONNECTION_STATUS).addValueEventListener(valueEventListenerForConn);
        }
        callJavaScriptFunction("javascript:closeStreamNow()");
        firebaseRef.child(createdBy).setValue(null);
    }

    public void onCallConnected() {
        setEarpiece();
    }

}