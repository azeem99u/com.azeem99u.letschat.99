package com.azeem99u.comazeem99uletschat99;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Observer;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.skyfishjy.library.RippleBackground;
import com.squareup.picasso.Picasso;

import java.util.UUID;

public class CallPickerActivity extends AppCompatActivity {
    ImageView rejectCallBtn;
    ImageView acceptCallBtn;
    TextView username, callType;
    ShapeableImageView userImage;
    String callerId;


    private final int MIC_PERMISSION_REQUEST_CODE = 1;
    private AudioManager audioManager;
    private SoundPoolManager soundPoolManager;
    RippleBackground rippleBackground;
    ValueEventListener valueEventListener;
    String callerName;
    String callerImage;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call_picker);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON|
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD|
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED|
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        initViews();

        soundPoolManager = SoundPoolManager.getInstance(this);

        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        audioManager.setSpeakerphoneOn(true);
        rippleBackground = (RippleBackground) findViewById(R.id.content);




        setVolumeControlStream(AudioManager.STREAM_VOICE_CALL);
        if (!checkPermissionForMicrophone()) {
            requestPermissionForMicrophone();
        }

        if (getIntent() != null && getIntent().hasExtra(Constants.CALL_SENDER_ID)) {

            String workerid = getIntent().getStringExtra("WORKERID");

            WorkManager.getInstance(getApplicationContext()).getWorkInfoByIdLiveData(UUID.fromString(workerid)).observe(this, new Observer<WorkInfo>() {
                @Override
                public void onChanged(WorkInfo workInfo) {
                    if (workInfo.getState().isFinished()){
                        finish();
                    }
                }
            });

            callerId = getIntent().getStringExtra(Constants.CALL_SENDER_ID);
            callerName = getIntent().getStringExtra(Constants.CALLER_NAME_KEY);
            callerImage = getIntent().getStringExtra(Constants.CALLER_IMAGE_KEY);

            if (callerImage != null) {
                Picasso.get().load(Uri.parse(callerImage)).error(R.drawable.profileimage).into(userImage);
            } else {
                userImage.setImageResource(R.drawable.profileimage);
            }
            username.setText(callerName);

            Log.d("mytagg", "onCreate: " + callerId);
            String callTypeText = getIntent().getStringExtra(Constants.CALL_TYPE_KEY);
            if (callTypeText.equals(Constants.VIDEO_CALL)) {
                callType.setText(callTypeText);
                acceptCallBtn.setImageResource(R.drawable.btn_video_normal);

            } else {
                callType.setText(callTypeText);
                acceptCallBtn.setImageResource(R.drawable.btn_startcall_normal);
            }


            acceptCallBtn.setOnClickListener(view -> {
                FirebaseDatabase.getInstance().getReference().child(Constants.VIDEO_CALLS).child(callerId).child(Constants.CONNECTION_STATUS).setValue(Constants.CallConnection.Accepted.name());
                Intent intent;
                if (callTypeText.equals(Constants.VIDEO_CALL)) {
                    intent = new Intent(this, VideoCallActivity.class);
                } else {
                    intent = new Intent(this, AudioCallActivity.class);
                }
                intent.putExtra(Constants.CALL_SENDER_KEY_FOR_CONN, callerId);
                startActivity(intent);
                finish();

            });

            rejectCallBtn.setOnClickListener(view -> {
                FirebaseDatabase.getInstance().getReference().child(Constants.VIDEO_CALLS).child(callerId).child(Constants.CONNECTION_STATUS).setValue(Constants.CallConnection.Rejected.name());
                finish();
            });
        }


    }

    private void initViews() {
        userImage = findViewById(R.id.callingPersonImage);
        username = findViewById(R.id.callingPersonName);
        callType = findViewById(R.id.calltypeTxtC);
        acceptCallBtn = findViewById(R.id.acceptBtn);
        rejectCallBtn = findViewById(R.id.rejectBtn);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        SoundPoolManager.getInstance(this).release();
        if (valueEventListener != null) {
            FirebaseDatabase.getInstance().getReference().child(Constants.VIDEO_CALLS).child(callerId).child(Constants.CONNECTION_STATUS).removeEventListener(valueEventListener);
        }

    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
    }

    @Override
    protected void onResume() {
        super.onResume();
        soundPoolManager.playRinging();
        rippleBackground.startRippleAnimation();
    }

    @Override
    public void onPause() {
        super.onPause();
        rippleBackground.stopRippleAnimation();
    }

    private boolean checkPermissionForMicrophone() {
        int resultMic = ContextCompat.checkSelfPermission(this, android.Manifest.permission.RECORD_AUDIO);
        return resultMic == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermissionForMicrophone() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.RECORD_AUDIO)) {

        } else {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.RECORD_AUDIO},
                    MIC_PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        /*
         * Check if microphone permissions is granted
         */
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MIC_PERMISSION_REQUEST_CODE && permissions.length > 0) {
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {

            } else {

            }
        }
    }


}