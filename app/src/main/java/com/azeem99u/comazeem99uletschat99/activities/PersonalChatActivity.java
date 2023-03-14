package com.azeem99u.comazeem99uletschat99.activities;

import static android.content.Intent.ACTION_PICK;
import static com.azeem99u.comazeem99uletschat99.App.mAuth;
import static com.azeem99u.comazeem99uletschat99.Constants.getTime;
import static com.azeem99u.comazeem99uletschat99.HelperFunctions.convertContentTypeIntoFile;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.work.Constraints;
import androidx.work.ExistingWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.azeem99u.comazeem99uletschat99.ConnectingActivity;
import com.azeem99u.comazeem99uletschat99.Constants;
import com.azeem99u.comazeem99uletschat99.HelperFunctions;
import com.azeem99u.comazeem99uletschat99.R;
import com.azeem99u.comazeem99uletschat99.connectivity.ConnectivityCheckBroadcast;
import com.azeem99u.comazeem99uletschat99.downloadFiles.DownloadFilesWorker;
import com.azeem99u.comazeem99uletschat99.eventhandler.UpdateAdapterPosition;
import com.azeem99u.comazeem99uletschat99.fragments.ChooseFileBottomSheet;
import com.azeem99u.comazeem99uletschat99.models.PersonalMessageModel;
import com.azeem99u.comazeem99uletschat99.models.UserProfile;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.util.ArrayList;
import java.util.Objects;

public class PersonalChatActivity extends AppCompatActivity implements ConnectivityCheckBroadcast.ConnectivityListener, ChooseFileBottomSheet.Listener, PersonalChatActivityAdapter.DownloadFilesListener, PersonalChatActivityAdapter.SnackBarCallbackListener {

    volatile boolean isConnectionOk;
    ArrayList<PersonalMessageModel> mChatMessages = new ArrayList<>();
    String mUsername;
    Menu toolbarMenu;
    String mSelectedPersonId;
    TextInputEditText mEtMessage;
    ImageView mSendMessageBtn;
    ShapeableImageView userProfileImage;
    ShapeableImageView onlineStatusImage;
    TextView usernameTxt;
    TextView userLastSeenTxt;
    LinearLayout keyBoardLayout;
    public RecyclerView mRecyclerView;
    LinearLayout linearLayout;
    ValueEventListener valueEventListener;
    PersonalChatActivityAdapter mPersonalChatActivityAdapter;
    String mCurrentUserId;
    DatabaseReference mUserMessageDatabaseRef;
    ChildEventListener mChildEventListener;
    ConstraintLayout toolbarName;
    ValueEventListener mStatusValueEventListener;
    ConnectivityCheckBroadcast connectivityCheckBroadcast;
    ImageView choseFilesImageBtn;
    ConstraintLayout backButtonCons;
    ValueEventListener name_and_image_valueEventListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personal_chat);
        toolBarSetUp();
        initViews();

        backButtonCons.setOnClickListener(view -> {
            onBackPressed();
        });

        toolbarName = findViewById(R.id.rootViewCons);
        keyBoardLayout = findViewById(R.id.keyboardLayout);
        mCurrentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    if (snapshot.child(Constants.CONTACT).getValue(String.class).equals(Constants.SAVE)) {
                        keyBoardLayout.setVisibility(View.VISIBLE);
                        if (toolbarMenu != null) {
                            toolbarMenu.findItem(R.id.videoCall).setEnabled(true);
                            toolbarMenu.findItem(R.id.voiceCall).setEnabled(true);
                        }

                    } else {
                        keyBoardLayout.setVisibility(View.GONE);
                        if (toolbarMenu != null) {
                            toolbarMenu.findItem(R.id.voiceCall).setEnabled(false);
                            toolbarMenu.findItem(R.id.videoCall).setEnabled(false);
                        }

                    }
                } else {
                    keyBoardLayout.setVisibility(View.GONE);
                    if (toolbarMenu != null) {
                        toolbarMenu.findItem(R.id.voiceCall).setEnabled(false);
                        toolbarMenu.findItem(R.id.videoCall).setEnabled(false);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };



        if (getIntent() != null) {

            if (getIntent().hasExtra(Constants.SELECTED_USER_ID_NOTIFICATION)) {
                mSelectedPersonId = getIntent().getStringExtra(Constants.SELECTED_USER_ID_NOTIFICATION);
                initRecyclerView();
                mUserMessageDatabaseRef = FirebaseDatabase.getInstance().getReference().child(Constants.MESSAGES);
            }

            if (getIntent().hasExtra(Constants.SELECTED_USER_ID_EXTRA_KEY)) {
                mSelectedPersonId = getIntent().getStringExtra(Constants.SELECTED_USER_ID_EXTRA_KEY);
                mUsername = getIntent().getStringExtra(Constants.USER_NAME_EXTRA_KEY);
                usernameTxt.setText(mUsername);
                mUserMessageDatabaseRef = FirebaseDatabase.getInstance().getReference().child(Constants.MESSAGES);
                initRecyclerView();

            }


            toolbarName.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    startActivity(new Intent(PersonalChatActivity.this, ProfileDetailActivity.class).putExtra(Constants.SELECTED_USER_ID_KEY, mSelectedPersonId));
                }
            });
        }

        FirebaseDatabase.getInstance().getReference().child(Constants.CONTACTS).child(mSelectedPersonId).child(mCurrentUserId).addValueEventListener(valueEventListener);


        name_and_image_valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists() && snapshot.hasChildren()) {
                    UserProfile userProfile = snapshot.getValue(UserProfile.class);
                    usernameTxt.setText(userProfile.getUsername());
                    String userImage = userProfile.getUserImage();
                    if (userImage != null) {
                        Picasso.get().load(Uri.parse(userImage)).placeholder(R.drawable.profileimage).error(R.drawable.profileimage).into(userProfileImage);
                    } else {
                        userProfileImage.setImageResource(R.drawable.profileimage);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };


        FirebaseDatabase.getInstance().getReference().child(Constants.USERS).child(mSelectedPersonId).addValueEventListener(name_and_image_valueEventListener);

        choseFilesImageBtn.setOnClickListener(view -> {
            ChooseFileBottomSheet choseReviewListBottomSheet = new ChooseFileBottomSheet();
            Bundle bundle = new Bundle();
            bundle.putString(Constants.SELECTED_USER_ID_KEY, mSelectedPersonId);
            choseReviewListBottomSheet.setArguments(bundle);
            choseReviewListBottomSheet.show(getSupportFragmentManager(), "chooseFilesBottomSheet");

        });

        connectivityCheckBroadcast = new ConnectivityCheckBroadcast(this);

        mSendMessageBtn.setOnClickListener(this::sendMessage);

        mStatusValueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    userLastSeenTxt.setVisibility(View.GONE);
                    Object value = snapshot.getValue(Object.class);
                    if (value instanceof String) {
                        userLastSeenTxt.setVisibility(View.VISIBLE);
                        userLastSeenTxt.setText("Online");

                    } else if (value instanceof Long) {
                        userLastSeenTxt.setVisibility(View.VISIBLE);
                        userLastSeenTxt.setText(getTime(((Long) value)));
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.w("mytag", "Listener was cancelled at .info/connected");
            }
        };

        mChildEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                if (snapshot.exists()) {
                    displayMessages(snapshot);
                }
                Log.d("jiya", "onChildAdded: ");
            }

            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                if (snapshot.exists()) {
                    PersonalMessageModel messages = snapshot.getValue(PersonalMessageModel.class);
                    for (PersonalMessageModel personalMessageModel : mChatMessages) {
                        if (Objects.equals(personalMessageModel.getMessageKey(), messages.getMessageKey())) {
                            int i = mChatMessages.indexOf(messages);
                            String from = messages.getFrom();
                            if (from.equals(mAuth.getUid())) {
                                mChatMessages.set(i, messages);
                                mPersonalChatActivityAdapter.notifyItemChanged(i);
                            }
                        }
                    }
                }
                Log.d("jiya", "onChildChanged: ");
            }

            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

                if (snapshot.exists()) {
                    PersonalMessageModel messageModel = snapshot.getValue(PersonalMessageModel.class);
                    mChatMessages.remove(messageModel);
                    mPersonalChatActivityAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
        mUserMessageDatabaseRef.child(mCurrentUserId).child(mSelectedPersonId).addChildEventListener(mChildEventListener);

    }




    @SuppressLint("NotifyDataSetChanged")
    private void displayMessages(DataSnapshot snapshot) {
        PersonalMessageModel messages = snapshot.getValue(PersonalMessageModel.class);
        mChatMessages.add(messages);
        mPersonalChatActivityAdapter.notifyDataSetChanged();
        mRecyclerView.scrollToPosition(mChatMessages.size() - 1);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        FirebaseDatabase.getInstance().getReference().child(Constants.USERS).child(mSelectedPersonId).removeEventListener(name_and_image_valueEventListener);

        if (mChildEventListener != null) {
            mUserMessageDatabaseRef.removeEventListener(mChildEventListener);
        }
        FirebaseDatabase.getInstance().getReference().child(Constants.CONTACTS).child(mSelectedPersonId).child(mCurrentUserId).removeEventListener(valueEventListener);

    }


    //Simple Message
    private void sendMessage(View view) {
        String message = (mEtMessage.getText()).toString().trim();
        String type = "text";
        if (mSelectedPersonId == null || message.isEmpty()) {
            return;
        }
        String messageKey = mUserMessageDatabaseRef.child(mSelectedPersonId).push().getKey();
        PersonalMessageModel personalMessageModel = new PersonalMessageModel(messageKey, type, message, getTime(), mCurrentUserId, Constants.Delivery.Loading.name());
        mUserMessageDatabaseRef.child(mCurrentUserId).child(mSelectedPersonId).child(messageKey).setValue(personalMessageModel).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

            }
        });
        mEtMessage.setText("");
    }


    //send file
    private void sendFileMessage(Uri phoneStoragePathData) {
        String type = "file";
        String filePathName = convertContentTypeIntoFile(getApplicationContext(), phoneStoragePathData);

        int lastIndexOf = filePathName.lastIndexOf(".");
        String endPortion = filePathName.substring(lastIndexOf);
        String fileName = System.currentTimeMillis() + endPortion;
        File savedFilePath = HelperFunctions.saveFileOnLocalStorage(getApplicationContext(), phoneStoragePathData, fileName);


        String messageKey = mUserMessageDatabaseRef.child(mSelectedPersonId).push().getKey();

        if (mSelectedPersonId == null && phoneStoragePathData == null && savedFilePath == null && messageKey == null) {
            return;
        }

        PersonalMessageModel personalMessageModel = new PersonalMessageModel(messageKey, type, fileName, phoneStoragePathData.toString(), savedFilePath.toString(), "", "", getTime(), mCurrentUserId, Constants.Delivery.Loading.name());

        mUserMessageDatabaseRef.child(mCurrentUserId).child(mSelectedPersonId).child(messageKey).setValue(personalMessageModel).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Log.d("mytagg", "sendMessage: ");
            }
        });
    }


    private void initRecyclerView() {

        mRecyclerView = findViewById(R.id.personalChatRecyclerView);
        mPersonalChatActivityAdapter = new PersonalChatActivityAdapter(this, mChatMessages, mSelectedPersonId, this, this);
        mPersonalChatActivityAdapter.setHasStableIds(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setAdapter(mPersonalChatActivityAdapter);
        mRecyclerView.scrollToPosition(mPersonalChatActivityAdapter.getItemCount());
    }

    void initViews() {

        linearLayout = findViewById(R.id.linearLayout3);
        mEtMessage = findViewById(R.id.et_typeMessageP);
        mSendMessageBtn = findViewById(R.id.sendMessageImgBtnP);
        choseFilesImageBtn = findViewById(R.id.choseFilesImageBtn);
        backButtonCons = findViewById(R.id.backButtonCons);

    }


    @SuppressLint("ResourceType")
    private void toolBarSetUp() {
        MaterialToolbar toolbar = findViewById(R.id.personalChatPage_toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setDefaultDisplayHomeAsUpEnabled(false);
        userProfileImage = findViewById(R.id.userImageM);
        onlineStatusImage = findViewById(R.id.userOnlineStatus);
        usernameTxt = findViewById(R.id.userNameToolbar);
        userLastSeenTxt = findViewById(R.id.userLastMessage);

    }

    @Override
    public void checkConnection(Boolean isConnected) {
        if (!isConnected) {
            isConnectionOk = false;
            userLastSeenTxt.setVisibility(View.GONE);
            onlineStatusImage.setVisibility(View.GONE);
            if (mStatusValueEventListener != null) {
                final FirebaseDatabase database = FirebaseDatabase.getInstance();
                final DatabaseReference connectionRef = database.getReference(Constants.USERS + "/" + mSelectedPersonId + "/" + Constants.USER_STATUS);
                connectionRef.removeEventListener(mStatusValueEventListener);
            }
        } else {
            isConnectionOk = true;
            if (mSelectedPersonId != null) {
                final FirebaseDatabase database = FirebaseDatabase.getInstance();
                final DatabaseReference connectionRef = database.getReference(Constants.USERS + "/" + mSelectedPersonId + "/" + Constants.USER_STATUS);
                connectionRef.addValueEventListener(mStatusValueEventListener);
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
        if (connectivityCheckBroadcast != null) {
            unregisterReceiver(connectivityCheckBroadcast);
        }
        if (mStatusValueEventListener != null) {
            final FirebaseDatabase database = FirebaseDatabase.getInstance();
            final DatabaseReference connectionRef = database.getReference(Constants.USERS + "/" + mSelectedPersonId + "/" + Constants.USER_STATUS);
            connectionRef.removeEventListener(mStatusValueEventListener);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        super.onStart();
        EventBus.getDefault().register(this);
        IntentFilter intentFilter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(connectivityCheckBroadcast, intentFilter);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == Constants.PICK_FILE_REQUEST_CODE && data.getData() != null) {
            sendFileMessage(data.getData());
        } else if (requestCode == Constants.PICK_FILE_REQUEST_CODE && resultCode == RESULT_CANCELED) {
            Toast.makeText(this, "Select any File", Toast.LENGTH_SHORT).show();
        }
    }


    @SuppressLint("Range")
    public String getNameFromURI(Uri uri) {
        Cursor c = getContentResolver().query(uri, null, null, null, null);
        c.moveToFirst();
        return c.getString(c.getColumnIndex(OpenableColumns.DISPLAY_NAME));
    }

    @Override
    public void invoke() {
        Intent intent = new Intent(ACTION_PICK);
        intent.setType("*/*");
        startActivityForResult(intent, Constants.PICK_FILE_REQUEST_CODE);
    }


    @Override
    public void downloadFile(String downloadUrl, String fileName, String filePath, String fileSize, int position) {
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();
        androidx.work.Data inputData = new androidx.work.Data.Builder()
                .putString(Constants.URL_KEY, downloadUrl)
                .putString(Constants.FILE_NAME_KEY, fileName)
                .putString(Constants.FILE_PATH_KEY, filePath)
                .putString(Constants.FILE_SIZE_KEY, fileSize)
                .putInt(Constants.ADAPTER_POSITION, position)
                .build();
        OneTimeWorkRequest oneTimeWorkRequest = new OneTimeWorkRequest.Builder(DownloadFilesWorker.class)
                .setInputData(inputData)
                .setConstraints(constraints)
                .addTag(fileName)
                .build();
        WorkManager.getInstance(getApplicationContext()).enqueueUniqueWork(fileName, ExistingWorkPolicy.KEEP, oneTimeWorkRequest);
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(UpdateAdapterPosition adapterPosition) {
        int position = adapterPosition.getPosition();
        mPersonalChatActivityAdapter.notifyItemChanged(position);
    }

    @Override
    public void onSnackBarClicked(String message) {
        Snackbar.make(linearLayout, message, Snackbar.LENGTH_SHORT).show();
    }


    String[] permissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO};
    private int requestCode = 1;

    void askPermissions() {
        ActivityCompat.requestPermissions(this, permissions, requestCode);
    }

    private boolean isPermissionsGranted() {
        for (String permission : permissions) {
            if (ActivityCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED)
                return false;
        }
        return true;
    }


    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.videoCall: {
                if (isPermissionsGranted()) {
                    if (isConnectionOk) {
                        FirebaseDatabase.getInstance().getReference().child(Constants.VIDEO_CALLS).child(mCurrentUserId).child(Constants.CALL_STATUS).setValue(Constants.CALLING);
                        Intent intent = new Intent(PersonalChatActivity.this, ConnectingActivity.class);
                        intent.putExtra(Constants.SELECTED_USER_ID_KEY, mSelectedPersonId);
                        intent.putExtra(Constants.CALL_TYPE, Constants.VIDEO_CALL);
                        startActivity(intent);
                    } else {
                        Toast.makeText(this, "Check Internet Connection!", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    askPermissions();
                }
                return true;
            }
            case R.id.voiceCall: {
                if (isPermissionsGranted()) {
                    if (isConnectionOk) {
                        FirebaseDatabase.getInstance().getReference().child(Constants.VIDEO_CALLS).child(mCurrentUserId).child(Constants.CALL_STATUS).setValue(Constants.CALLING);
                        Intent intent = new Intent(PersonalChatActivity.this, ConnectingActivity.class);
                        intent.putExtra(Constants.SELECTED_USER_ID_KEY, mSelectedPersonId);
                        intent.putExtra(Constants.CALL_TYPE, Constants.VOICE_CALL);
                        startActivity(intent);
                    } else {
                        Toast.makeText(this, "Check Internet Connection!", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    askPermissions();
                }
                return true;
            }
            case R.id.deleteAll: {
                mUserMessageDatabaseRef.removeValue();
                return true;
            }
            default: {
                return super.onOptionsItemSelected(item);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.personal_chat_activity_menu, menu);
        toolbarMenu = menu;

        int visibility = keyBoardLayout.getVisibility();
        if (visibility == 8){
            if (toolbarMenu != null) {
                toolbarMenu.findItem(R.id.voiceCall).setEnabled(false);
                toolbarMenu.findItem(R.id.videoCall).setEnabled(false);
            }
        }
        return true;
    }

}