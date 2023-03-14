package com.azeem99u.comazeem99uletschat99.activities;


import static com.azeem99u.comazeem99uletschat99.App.mAuth;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.azeem99u.comazeem99uletschat99.App;
import com.azeem99u.comazeem99uletschat99.Constants;
import com.azeem99u.comazeem99uletschat99.HelperFunctions;
import com.azeem99u.comazeem99uletschat99.R;
import com.azeem99u.comazeem99uletschat99.SendNotificationPack.Token;
import com.azeem99u.comazeem99uletschat99.models.ContactsModel;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.Objects;


public class ProfileDetailActivity extends AppCompatActivity {
    private static final String TAG = "myTag";
    String mReceiverId = null;
    String mSenderId = null;
    Button mRequestBtn, mRequestCancelBtn;
    DatabaseReference mRequestRef;
    TextView username, userAbout;
    ShapeableImageView userProfileImage;
    ImageView userProfileBackgroundImage;
    DatabaseReference mContactRef;
    DatabaseReference mUserRef;
    ValueEventListener mRequestValueEventListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_detail);
        initViews();

        mUserRef = FirebaseDatabase.getInstance().getReference().child(Constants.USERS);
        mContactRef = FirebaseDatabase.getInstance().getReference().child(Constants.CONTACTS);
        mRequestRef = FirebaseDatabase.getInstance().getReference().child(Constants.REQUESTS);
        mSenderId = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();

        if (getIntent().hasExtra(Constants.PROFILE_INTENT_EXTRA)) {
            mReceiverId = getIntent().getStringExtra(Constants.PROFILE_INTENT_EXTRA);
        }
        if (getIntent().hasExtra(Constants.SELECTED_USER_ID_KEY)) {
            mReceiverId = getIntent().getStringExtra(Constants.SELECTED_USER_ID_KEY);
        }


        mUserRef.child(mReceiverId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists() && snapshot.hasChildren()) {
                    if (mReceiverId.equals(mSenderId)) {
                        mRequestBtn.setVisibility(View.INVISIBLE);
                    }
                    ContactsModel contactsModel = snapshot.getValue(ContactsModel.class);
                    if (contactsModel != null) {
                        username.setText(contactsModel.getUsername());
                        userAbout.setText(contactsModel.getUserAbout());
                        Uri userImage = Uri.parse((contactsModel.getUserImage()));
                        if (userImage != null) {
                            Picasso.get().load(userImage).error(R.drawable.profileimage).placeholder(R.drawable.profileimage).into(userProfileImage);
                        } else {
                            userProfileImage.setImageResource(R.drawable.profileimage);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ProfileDetailActivity.this, "" + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });


        mRequestValueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot1) {
                String requestType = snapshot1.child(mSenderId).child(mReceiverId).child(Constants.REQUEST_TYPE).getValue(String.class);

                mContactRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot2) {

                        String savedContact = snapshot2.child(mSenderId).child(mReceiverId).child(Constants.CONTACT).getValue(String.class);
                        if (snapshot1.exists()) {
                            if (requestType != null && requestType.equals(Constants.SENT)) {
                                mRequestBtn.setText(getText(R.string.cancel_request));
                            } else if (requestType != null && requestType.equals(Constants.RECEIVED)) {
                                mRequestBtn.setText(getText(R.string.accept_request));
                                mRequestCancelBtn.setVisibility(View.VISIBLE);
                                mRequestCancelBtn.setEnabled(true);
                            } else if (savedContact != null && savedContact.equals(Constants.SAVE)) {
                                mRequestBtn.setText(getText(R.string.remove_contact));
                                mRequestCancelBtn.setVisibility(View.INVISIBLE);
                                mRequestCancelBtn.setEnabled(false);
                            } else {
                                mRequestBtn.setText(getText(R.string.send_request));
                                mRequestCancelBtn.setVisibility(View.INVISIBLE);
                                mRequestCancelBtn.setEnabled(false);
                            }

                        } else if (!snapshot1.exists() && savedContact != null) {
                            mRequestBtn.setText(getText(R.string.remove_contact));
                            mRequestCancelBtn.setVisibility(View.INVISIBLE);
                            mRequestCancelBtn.setEnabled(false);


                        } else if (!snapshot1.exists() && !snapshot2.exists()) {
                            mRequestBtn.setText(getText(R.string.send_request));
                            mRequestCancelBtn.setVisibility(View.INVISIBLE);
                            mRequestCancelBtn.setEnabled(false);

                        } else if (!snapshot1.exists() && savedContact == null) {
                            mRequestBtn.setText(getText(R.string.send_request));
                            mRequestCancelBtn.setVisibility(View.INVISIBLE);
                            mRequestCancelBtn.setEnabled(false);
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(ProfileDetailActivity.this, "" + error.getMessage(), Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "onCancelled: ");
                    }
                });
            }


            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ProfileDetailActivity.this, "" + error.getMessage(), Toast.LENGTH_SHORT).show();
                Log.d(TAG, "onCancelled: ");
            }
        };

        mRequestRef.addValueEventListener(mRequestValueEventListener);
        mRequestBtn.setOnClickListener(this::manageRequest);
        mRequestCancelBtn.setOnClickListener(this::cancelRequest);

    }

    private void cancelRequest(View view) {
        mRequestRef.child(mReceiverId).child(mSenderId).child(Constants.REQUEST_TYPE).removeValue().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                mRequestRef.child(mSenderId).child(mReceiverId).child(Constants.REQUEST_TYPE).removeValue().addOnCompleteListener(task1 -> {
                    if (task1.isSuccessful()) {
                        mRequestBtn.setText(R.string.send_request);
                        mRequestCancelBtn.setVisibility(View.INVISIBLE);
                        mRequestCancelBtn.setEnabled(false);
                        Log.d(TAG, "cancelRequest:");
                    }
                });
            } else {
                Toast.makeText(this, "" + Objects.requireNonNull(task.getException()).getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                Log.d(TAG, "cancelRequest: ");
            }
        });
    }

    private void initViews() {
        mRequestBtn = findViewById(R.id.btnRequest);
        userProfileBackgroundImage = findViewById(R.id.backgroundImage);
        userProfileImage = findViewById(R.id.userImageM);
        username = findViewById(R.id.userNameP);
        userAbout = findViewById(R.id.userAboutP);
        mRequestCancelBtn = findViewById(R.id.btnCancelRequest);
    }

    private void manageRequest(View view) {
        if (mRequestBtn.getText().toString().equals(getString(R.string.send_request))) {
            sendRequest();
        } else if (mRequestBtn.getText().toString().equals(getString(R.string.cancel_request))) {
            cancelRequest();
        } else if (mRequestBtn.getText().toString().equals(getString(R.string.accept_request))) {
            mRequestCancelBtn.setEnabled(false);
            mRequestCancelBtn.setVisibility(View.INVISIBLE);
            acceptRequest();
        } else if (mRequestBtn.getText().toString().equals(getString(R.string.remove_contact))) {
            mRequestCancelBtn.setEnabled(false);
            mRequestCancelBtn.setVisibility(View.INVISIBLE);
            removeRequest();
        }
    }

    private void acceptRequest() {
        mContactRef.child(mSenderId).child(mReceiverId).child(Constants.CONTACT).setValue(Constants.SAVE).addOnCompleteListener(acceptRequestTask1 ->
                mContactRef.child(mReceiverId).child(mSenderId).child(Constants.CONTACT).setValue(Constants.SAVE).addOnCompleteListener(acceptRequestTask2 -> {
                    cancelRequest();
                    Toast.makeText(ProfileDetailActivity.this, "Contact Saved Successfully!", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "acceptRequest: ");
                }));

    }

    private void removeRequest() {
        mContactRef.child(mSenderId).child(mReceiverId).child(Constants.CONTACT).removeValue().addOnCompleteListener(removeTask1 ->
                mContactRef.child(mReceiverId).child(mSenderId).child(Constants.CONTACT).removeValue().addOnCompleteListener(removeTask2 -> {
                    mRequestBtn.setText(R.string.send_request);
                    Log.d(TAG, "removeRequest: ");
                }));
    }

    private void sendRequest() {
        mRequestRef.child(mSenderId).child(mReceiverId).child(Constants.REQUEST_TYPE).setValue(Constants.SENT).addOnCompleteListener(sendRequestTask1 -> {
            if (sendRequestTask1.isSuccessful()) {
                mRequestRef.child(mReceiverId).child(mSenderId).child(Constants.REQUEST_TYPE).setValue(Constants.RECEIVED).addOnCompleteListener(sendRequestTask2 -> {
                    if (sendRequestTask2.isSuccessful()) {
                        FirebaseDatabase.getInstance().getReference().child(Constants.USERS).child(mSenderId).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.exists() && snapshot.hasChild("username")) {
                                    mRequestBtn.setText(R.string.cancel_request);
                                    String currentUsername = snapshot.child("username").getValue(String.class);
                                    FirebaseDatabase.getInstance().getReference().child(Constants.USERS).child(mReceiverId).addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            if (snapshot.exists() && snapshot.hasChild("deviceToken")) {
                                                String deviceToken = (String) snapshot.child("deviceToken").getValue();
                                                Token token = new Token(deviceToken);
                                                Log.d("token1", "onDataChange: "+token.getToken());
                                                HelperFunctions.sendNotifications(token.getToken(), "New Request", currentUsername + " sent you a request",Constants.ACTION_REQUEST, App.REQUESTS_CHANNEL_ID,"","","","",null);
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
            }
        });

    }

    private void cancelRequest() {

        mRequestRef.child(mSenderId).child(mReceiverId).child(Constants.REQUEST_TYPE).removeValue().addOnCompleteListener(requestTask1 -> {
            if (requestTask1.isSuccessful()) {
                mRequestRef.child(mReceiverId).child(mSenderId).child(Constants.REQUEST_TYPE).removeValue().addOnCompleteListener(requestTask2 -> {
                    if (requestTask2.isSuccessful()) {
                        Log.d(TAG, "cancelRequest: ");
                    }
                });
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mRequestValueEventListener != null) {
            mRequestRef.removeEventListener(mRequestValueEventListener);
        }
    }


}