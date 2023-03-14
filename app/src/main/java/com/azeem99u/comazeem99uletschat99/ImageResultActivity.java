package com.azeem99u.comazeem99uletschat99;

import static com.azeem99u.comazeem99uletschat99.HelperFunctions.convertContentTypeIntoFile;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.util.Log;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.azeem99u.comazeem99uletschat99.models.PersonalMessageModel;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;

public class ImageResultActivity extends AppCompatActivity {
    private static final int RESULT_LOAD_IMG = 1010;
    private TextInputEditText mEtMessage;
    private ImageView mSendMessageBtn;
    private DatabaseReference mUserMessageDatabaseRef;
    private String mCurrentUserId;
    private Uri phoneStoragePathData;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_result);
        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
        photoPickerIntent.setType("image/*");
        startActivityForResult(photoPickerIntent, RESULT_LOAD_IMG);
        initViews();


        mCurrentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        mUserMessageDatabaseRef = FirebaseDatabase.getInstance().getReference().child(Constants.MESSAGES);
        mSendMessageBtn.setOnClickListener(view -> {
            if (getIntent().hasExtra(Constants.SELECTED_USER_ID_KEY)) {
                String selectedUserKey = getIntent().getStringExtra(Constants.SELECTED_USER_ID_KEY);
                sendMessageWithImage(selectedUserKey);

            }

        });


    }

    private void sendMessageWithImage(String selectedUserId) {
        String message = (mEtMessage.getText()).toString().trim();
        String type = "image";
        String filePathName;
        String fileName = null;
        if (phoneStoragePathData!= null){
            filePathName = convertContentTypeIntoFile(getApplicationContext(), phoneStoragePathData);
            int lastIndexOf = 0;
            if (filePathName != null) {
                lastIndexOf = filePathName.lastIndexOf(".");
                String endPortion = filePathName.substring(lastIndexOf);
                fileName = System.currentTimeMillis() + endPortion;
            }
        }

        if (message.isEmpty()){
            message = "";
        }

        if (fileName == null) {
            return;
        }
        File savedFilePath = HelperFunctions.saveFileOnLocalStorage(getApplicationContext(),phoneStoragePathData, fileName);

        if (savedFilePath == null) {
            return;
        }

        String messageKey = mUserMessageDatabaseRef.child(selectedUserId).push().getKey();

        if (selectedUserId == null && phoneStoragePathData == null && savedFilePath == null && messageKey == null) {
            return;
        }

        PersonalMessageModel personalMessageModel = new PersonalMessageModel(messageKey,type,message,fileName,phoneStoragePathData.toString(),savedFilePath.toString(),"","","",getTime(),mCurrentUserId,Constants.Delivery.Loading.name());
        mUserMessageDatabaseRef.child(mCurrentUserId).child(selectedUserId).child(messageKey).setValue(personalMessageModel).addOnCompleteListener(task -> {
            if (task.isSuccessful()){
                Log.d("tag", "sendMessageWithImage: message added ");
            }
        });
        finish();
        mEtMessage.setText("");
    }

    private void initViews() {

        mEtMessage = findViewById(R.id.et_typeMessageR);
        mSendMessageBtn = findViewById(R.id.sendMessageImgBtnR);
    }




    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && requestCode == RESULT_LOAD_IMG && data.getData() != null) {
            try {

                phoneStoragePathData = data.getData();
                final InputStream imageStream = getContentResolver().openInputStream(phoneStoragePathData);
                final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                ImageView image = findViewById(R.id.selectedImageR);
                image.setImageBitmap(selectedImage);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }else if (requestCode == RESULT_LOAD_IMG && resultCode ==RESULT_CANCELED){
            finish();
        }

    }



    private String getNameFromUri(Uri imageUri) {
        return getNameFromURI(imageUri);
    }

    private String getTime() {
        Calendar calendar = Calendar.getInstance();
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat currentTimeFormat = new SimpleDateFormat("hh:mm aaa");
        currentTimeFormat.setTimeZone(TimeZone.getDefault());
        return currentTimeFormat.format(calendar.getTime());
    }

    private String getTime(Long aLong) {
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat currentTimeFormat = new SimpleDateFormat("hh:mm aaa");
        return currentTimeFormat.format(aLong);
    }

    @SuppressLint("Range")
    public String getNameFromURI(Uri uri) {
        Cursor c = getContentResolver().query(uri, null, null, null, null);
        c.moveToFirst();
        return c.getString(c.getColumnIndex(OpenableColumns.DISPLAY_NAME));
    }




}