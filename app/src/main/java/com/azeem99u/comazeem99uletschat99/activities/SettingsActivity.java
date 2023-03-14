package com.azeem99u.comazeem99uletschat99.activities;


import static com.azeem99u.comazeem99uletschat99.App.mAuth;
import static com.azeem99u.comazeem99uletschat99.ProgressUtils.hideProgressBar;
import static com.azeem99u.comazeem99uletschat99.ProgressUtils.showProgressBar;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.azeem99u.comazeem99uletschat99.Constants;
import com.azeem99u.comazeem99uletschat99.MainActivity;
import com.azeem99u.comazeem99uletschat99.ProgressUtils;
import com.azeem99u.comazeem99uletschat99.R;
import com.azeem99u.comazeem99uletschat99.models.UserProfile;
import com.google.android.gms.tasks.Task;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.yalantis.ucrop.UCrop;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class SettingsActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST_CODE = 1000;
    String mImageUrl;
    private ShapeableImageView mUserImage,uploadImage;
    private TextInputLayout mUsername;
    private TextInputLayout mUserAboutInfo;
    private CardView mSaveProfile;
    DatabaseReference mRootRef;
    ValueEventListener mValueEventListener;
    StorageReference mProfile_images;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        initViews();
        setUpProgressBar();


        mProfile_images = FirebaseStorage.getInstance().getReference().child("Profile Images/");
        mRootRef = FirebaseDatabase.getInstance().getReference();
        mSaveProfile.setOnClickListener(this::SaveProfileInfo);
        mUserImage.setOnClickListener(this::loadImage);
        uploadImage.setOnClickListener(this::loadImage);

        mValueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                hideProgressBar();
                if (snapshot.hasChildren()) {
                    UserProfile data = snapshot.getValue(UserProfile.class);
                    mUsername.getEditText().setText(data.getUsername());
                    mUserAboutInfo.getEditText().setText(data.getUserAbout());

                    mImageUrl = data.getUserImage();
                    if (mImageUrl != null) {
                        Uri parse = Uri.parse(mImageUrl);
                        Picasso.get().load(parse).error(R.drawable.profileimage).placeholder(R.drawable.profileimage).into(mUserImage);
                    } else {
                        mUserImage.setImageResource(R.drawable.profileimage);
                    }
                } else {
                    mUserImage.setImageResource(R.drawable.profileimage);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                hideProgressBar();
                Toast.makeText(SettingsActivity.this, "" + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        };
        mRootRef.child(Constants.USERS).child(mAuth.getCurrentUser().getUid()).addValueEventListener(mValueEventListener);

    }

    private void loadImage(View view) {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select image"), PICK_IMAGE_REQUEST_CODE);

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == PICK_IMAGE_REQUEST_CODE && data != null) {
                startCrop(data.getData());
            }
            if (requestCode == UCrop.REQUEST_CROP) {
                handleCropResult(data);
            }
        } else if (resultCode == UCrop.RESULT_ERROR) {
            handleCropError(data);
        }
    }

    private void startCrop(Uri uri) {
        String destinationFileName = new StringBuilder(UUID.randomUUID().toString()).append(".jpg").toString();
        UCrop ucrop = UCrop.of(uri, Uri.fromFile(new File(getCacheDir(), destinationFileName)));
        ucrop.start(this);
    }

    private void handleCropResult(Intent data) {

        final Uri resultUri = UCrop.getOutput(data);
        showProgressBar();
        if (resultUri != null) {
            UploadTask uploadTask = mProfile_images.child(Objects.requireNonNull(mAuth.getCurrentUser()).getUid() + ".jpg").putFile(resultUri);
            uploadTask.addOnCompleteListener(task ->
            {

                hideProgressBar();
                if (task.isSuccessful()) {
                    showImageNow();
                    Toast.makeText(SettingsActivity.this, "Image Uploaded.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(SettingsActivity.this, "" + Objects.requireNonNull(task.getException()).getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            hideProgressBar();
            Toast.makeText(this, "Can not retrieve Crop image", Toast.LENGTH_SHORT).show();
        }
    }

    private void handleCropError(Intent data) {
        final Throwable cropError = UCrop.getError(data);
        if (cropError != null) {
            Toast.makeText(this, "" + cropError.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Unexpected Error", Toast.LENGTH_SHORT).show();
        }
    }


    private void showImageNow() {
        Task<Uri> uriTask = mProfile_images.child(mAuth.getUid() + ".jpg").getDownloadUrl();
        uriTask.addOnSuccessListener(uri -> {
            mImageUrl = String.valueOf(uri);
            Picasso.get().load(uri).error(R.drawable.profileimage).placeholder(R.drawable.profileimage).into(mUserImage);
        });
    }

    private void SaveProfileInfo(View view) {

        String currentUserId = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
        String userName = Objects.requireNonNull(mUsername.getEditText()).getText().toString().trim();
        String userAboutInfo = Objects.requireNonNull(mUserAboutInfo.getEditText()).getText().toString().trim();

        if (!validateUserName()) {
            return;
        }
        ProgressUtils.showProgressBar();
        if (userAboutInfo.isEmpty()) {
            userAboutInfo = "";
        }

        Map<String,Object> userProfile = new HashMap<>();

        userProfile.put("/"+currentUserId+"/username",userName);
        userProfile.put("/"+currentUserId+"/userAbout",userAboutInfo);
        userProfile.put("/"+currentUserId+"/userImage",mImageUrl);
        userProfile.put("/"+currentUserId+"/userId",currentUserId);

        Task<Void> voidTask = mRootRef.child(Constants.USERS).updateChildren(userProfile);
        voidTask.addOnCompleteListener(task -> {
            ProgressUtils.hideProgressBar();
            if (task.isSuccessful()) {
                startActivity(new Intent(SettingsActivity.this, MainActivity.class));
                finish();
                Toast.makeText(SettingsActivity.this, "Profile Updated", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(SettingsActivity.this, "" + Objects.requireNonNull(task.getException()).getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void setUpProgressBar() {
        ProgressUtils.initAddProgress(this);
        showProgressBar();
    }

    private void initViews() {

        mSaveProfile = findViewById(R.id.btn_saveUserinfo);
        mUserImage = findViewById(R.id.userImageM);
        uploadImage = findViewById(R.id.uploadImage);
        mUsername = findViewById(R.id.et_username);
        mUserAboutInfo = findViewById(R.id.et_about_info);
    }





    private boolean validateUserName() {

        String name = mUsername.getEditText().getText().toString().trim();

        if (name.isEmpty()) {
            mUsername.setError("Name is required. Can't be empty.");
            return false;
        } else {
            mUsername.setError(null);
            return true;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mValueEventListener!= null){
            mRootRef.child(Constants.USERS).child(mAuth.getCurrentUser().getUid()).removeEventListener(mValueEventListener);
        }
    }
}