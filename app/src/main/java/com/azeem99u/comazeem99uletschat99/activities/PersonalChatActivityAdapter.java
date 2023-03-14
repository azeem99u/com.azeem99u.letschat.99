package com.azeem99u.comazeem99uletschat99.activities;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;
import static com.azeem99u.comazeem99uletschat99.HelperFunctions.createFileName;
import static com.azeem99u.comazeem99uletschat99.HelperFunctions.observingFileFromMemoryAndCreating;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.ArrayMap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;
import androidx.lifecycle.LifecycleOwner;
import androidx.recyclerview.widget.RecyclerView;
import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.ExistingWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import com.azeem99u.comazeem99uletschat99.Constants;
import com.azeem99u.comazeem99uletschat99.HelperFunctions;
import com.azeem99u.comazeem99uletschat99.R;
import com.azeem99u.comazeem99uletschat99.UploadTaskWorker;
import com.azeem99u.comazeem99uletschat99.models.PersonalMessageModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import jp.wasabeef.transformers.picasso.BlurTransformation;

public class PersonalChatActivityAdapter extends RecyclerView.Adapter<PersonalChatActivityAdapter.MyViewHolder> {


    private final ArrayList<PersonalMessageModel> messages;
    private final String mSelectedPersonId;
    StorageReference fileRef;
    StorageReference imageRef;
    FirebaseAuth mAuth;
    Context mContext;

    private final DownloadFilesListener downloadFilesListener;

    private final SnackBarCallbackListener snackBarCallbackListener;

    DatabaseReference mUserMessageDatabaseRef;

    public PersonalChatActivityAdapter(Context mContext, ArrayList<PersonalMessageModel> messages, String mSelectedPersonId, DownloadFilesListener downloadFilesListener, SnackBarCallbackListener snackBarCallbackListener) {
        this.mContext = mContext;
        this.messages = messages;
        this.mSelectedPersonId = mSelectedPersonId;
        this.downloadFilesListener = downloadFilesListener;
        this.snackBarCallbackListener = snackBarCallbackListener;
        imageRef = FirebaseStorage.getInstance().getReference("Images/");

        fileRef = FirebaseStorage.getInstance().getReference("Files/");
        mUserMessageDatabaseRef = FirebaseDatabase.getInstance().getReference().child(Constants.MESSAGES);
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        mAuth = FirebaseAuth.getInstance();
        return new MyViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.personal_chat_activity_adapter_item, parent, false));
    }


    @SuppressLint({"QueryPermissionsNeeded", "SetTextI18n", "ResourceType", "NotifyDataSetChanged"})
    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, @SuppressLint("RecyclerView") int position) {


        PersonalMessageModel personalMessageModel = messages.get(position);
        String mCurrentUserId = mAuth.getCurrentUser().getUid();

        holder.senderLinearLayout.setOnClickListener(view -> {
            String type = personalMessageModel.getType();
            if (type.equals("image") || Objects.equals(type, "file")) {
                openIntent(personalMessageModel);
            }

        });

        holder.senderLinearLayout.setOnClickListener(view -> {
            String type = personalMessageModel.getType();
            if (type.equals("location")) {
                Intent intent = new Intent(mContext, MapActivity.class);
                intent.putExtra(Constants.LOCATION_KEY, personalMessageModel.getMessage());
                intent.putExtra(Constants.SELECTED_USER_ID_KEY, mSelectedPersonId);
                intent.putExtra("message","m");
                mContext.startActivity(intent);
            }

        });

        holder.receiverLinearLayout.setOnClickListener(view -> {
            String type = personalMessageModel.getType();
            if (type.equals("location")) {
                mContext.startActivity(new Intent(mContext, MapActivity.class).putExtra(Constants.LOCATION_KEY, personalMessageModel.getMessage()));
            }
        });

        holder.senderLinearLayout.setOnLongClickListener(view -> {

            PopupMenu popupMenu = new PopupMenu(mContext, view);
            popupMenu.getMenuInflater().inflate(R.menu.popup_menu, popupMenu.getMenu());
            popupMenu.show();
            popupMenu.setOnMenuItemClickListener(menuItem -> {
                switch (menuItem.getItemId()) {
                    case R.id.delete_for_all:
                        deleteForAll(personalMessageModel, mCurrentUserId);
                        break;
                    case R.id.delete_for_me:
                        deleteForMe(personalMessageModel, mCurrentUserId);
                        break;
                    case R.id.edit_message:
                        editMessage(personalMessageModel, mCurrentUserId);
                        break;
                }
                return true;
            });
            return false;
        });

        holder.receiverLinearLayout.setOnClickListener(view -> {
            String type = personalMessageModel.getType();
            if (type.equals("image") || Objects.equals(type, "file")) {
                openIntent(mContext, personalMessageModel, position);
            }
        });

        deliveryCheckForCurrent(holder, position, personalMessageModel, mCurrentUserId);
        deliveryCheckForSelected(personalMessageModel, mCurrentUserId);

        switch (personalMessageModel.getType()) {
            case "text":
                showTextMessage(holder, personalMessageModel, mCurrentUserId);
                break;
            case "image":
                showAndSendImageMessage(holder, personalMessageModel, mCurrentUserId);
                break;
            case "file":
                showAndSendFileMessage(holder, personalMessageModel, mCurrentUserId);
                break;
            case "location":
                showAndSendLocationMessage(holder, personalMessageModel, mCurrentUserId);
                break;

        }


    }


    @Override
    public int getItemCount() {
        return messages.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        TextView senderMessage, senderTime;
        TextView receiverMessage, receiverTime;
        LinearLayout senderLinearLayout, receiverLinearLayout;
        ShapeableImageView senderImage, receiverImage;
        ProgressBar senderUploadProgress;
        TextView senderProgressText;
        ProgressBar receiverDownloadProgress;
        TextView receiverProgressText;
        ShapeableImageView messageDeliveryIcon;


        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            messageDeliveryIcon = itemView.findViewById(R.id.messageDeliveryStatus);
            senderLinearLayout = itemView.findViewById(R.id.senderLinearLayout);
            receiverLinearLayout = itemView.findViewById(R.id.receiverLinearLayout);
            receiverMessage = itemView.findViewById(R.id.messageReceiver);
            receiverTime = itemView.findViewById(R.id.timeReceiver);
            senderMessage = itemView.findViewById(R.id.messageSender);
            senderTime = itemView.findViewById(R.id.timeSender);
            senderImage = itemView.findViewById(R.id.personalChatImageS);
            receiverImage = itemView.findViewById(R.id.personalChatImageR);
            senderProgressText = itemView.findViewById(R.id.progressTxtS);
            senderUploadProgress = itemView.findViewById(R.id.progressBarS);
            receiverProgressText = itemView.findViewById(R.id.progressTxtR);
            receiverDownloadProgress = itemView.findViewById(R.id.progressBarR);
        }

    }


    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public void setHasStableIds(boolean hasStableIds) {
        super.setHasStableIds(hasStableIds);
    }

    public interface SnackBarCallbackListener {
        void onSnackBarClicked(String message);
    }

    public interface DownloadFilesListener {
        void downloadFile(String downloadUrl, String fileName, String myFilePath, String myFileSize, int position);
    }

    @SuppressLint("SetTextI18n")
    private void showAndSendFileMessage(@NonNull MyViewHolder holder, PersonalMessageModel personalMessageModel, String mCurrentUserId) {
        if (mCurrentUserId.equals(personalMessageModel.getFrom())) {
            holder.receiverImage.setVisibility(View.GONE);
            holder.senderImage.setVisibility(View.GONE);
            holder.senderLinearLayout.setVisibility(View.VISIBLE);
            holder.receiverLinearLayout.setVisibility(View.GONE);
            holder.senderMessage.setPadding(8, 12, 8, 12);
            holder.senderMessage.setBackgroundResource(R.drawable.background);
            holder.senderMessage.setText(" : " + personalMessageModel.getFileName());
            holder.senderMessage.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_file, 0, 0, 0);
            holder.senderTime.setText(personalMessageModel.getTime());

            String key = personalMessageModel.getMessageKey();
            FirebaseDatabase.getInstance().getReference().child(Constants.MESSAGES).child(mSelectedPersonId).child(mCurrentUserId).child(key).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {

                    if (snapshot.exists() && snapshot.hasChildren()) {
                        String downloadUrl = snapshot.child("downloadUrl").getValue(String.class);
                        if (downloadUrl == null) {
                            FirebaseDatabase.getInstance().getReference().child(Constants.MESSAGES).child(mCurrentUserId).child(mSelectedPersonId).child(key).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if (snapshot.exists() && snapshot.getValue() != null) {

                                        PersonalMessageModel messageModel = snapshot.getValue(PersonalMessageModel.class);
                                        uploadFile(messageModel, holder, mSelectedPersonId);

                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                }
                            });
                        }
                    } else {

                        FirebaseDatabase.getInstance().getReference().child(Constants.MESSAGES).child(mCurrentUserId).child(mSelectedPersonId).child(key).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.exists() && snapshot.getValue() != null) {
                                    PersonalMessageModel messageModel = snapshot.getValue(PersonalMessageModel.class);
                                    uploadFile(messageModel, holder, mSelectedPersonId);
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

        } else {

            holder.receiverDownloadProgress.setVisibility(View.GONE);
            holder.receiverProgressText.setVisibility(View.GONE);
            holder.senderLinearLayout.setVisibility(View.GONE);
            holder.receiverLinearLayout.setVisibility(View.VISIBLE);
            holder.receiverMessage.setPadding(8, 12, 8, 12);
            holder.receiverMessage.setBackgroundResource(R.drawable.background);
            holder.receiverMessage.setText(":" + personalMessageModel.getMessage());
            holder.receiverMessage.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_file, 0, 0, 0);
            holder.receiverMessage.setText(personalMessageModel.getFileName());
            holder.receiverTime.setText(personalMessageModel.getTime());
            holder.senderImage.setVisibility(View.GONE);
            holder.receiverImage.setVisibility(View.GONE);

            WorkManager.getInstance(mContext.getApplicationContext()).getWorkInfosForUniqueWorkLiveData(personalMessageModel.getFileName()).observe((LifecycleOwner) mContext, workInfos -> {
                for (WorkInfo workInfo : workInfos) {

                    if (workInfo.getState().equals(WorkInfo.State.RUNNING)) {
                        int anInt = workInfo.getProgress().getInt(Constants.PROGRESS_DOWNLOAD, 0);
                        holder.receiverProgressText.setVisibility(View.VISIBLE);
                        holder.receiverDownloadProgress.setVisibility(View.VISIBLE);
                        holder.receiverProgressText.setText(anInt + " %");
                        holder.receiverDownloadProgress.setProgress(anInt);
                    }
                    if (workInfo.getState().isFinished()) {
                        holder.receiverProgressText.setText(0 + " %");
                        holder.receiverDownloadProgress.setProgress(0);
                        holder.receiverProgressText.setVisibility(View.GONE);
                        holder.receiverDownloadProgress.setVisibility(View.GONE);
                    }
                }
            });


        }
    }

    private void showAndSendImageMessage(@NonNull MyViewHolder holder, PersonalMessageModel personalMessageModel, String mCurrentUserId) {

        if (mCurrentUserId.equals(personalMessageModel.getFrom())) {
            holder.receiverLinearLayout.setVisibility(View.GONE);
            holder.senderImage.setVisibility(View.VISIBLE);
            holder.senderLinearLayout.setVisibility(View.VISIBLE);

            holder.senderMessage.setText(personalMessageModel.getMessage());
            holder.senderTime.setText(personalMessageModel.getTime());
            String filePath = personalMessageModel.getMyStoragePath();
            File file = new File(filePath);
            Picasso.get().load(file).error(R.drawable.ic_baseline_file_24).into(holder.senderImage);
            String key = personalMessageModel.getMessageKey();
            FirebaseDatabase.getInstance().getReference().child(Constants.MESSAGES).child(mSelectedPersonId).child(mCurrentUserId).child(key).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {

                    if (snapshot.exists() && snapshot.hasChildren()) {
                        String downloadUrl = snapshot.child("downloadUrl").getValue(String.class);
                        if (downloadUrl == null) {
                            PersonalMessageModel messageModel = snapshot.getValue(PersonalMessageModel.class);
                            uploadImage(messageModel, holder, mSelectedPersonId);
                        }
                    } else {
                        FirebaseDatabase.getInstance().getReference().child(Constants.MESSAGES).child(mCurrentUserId).child(mSelectedPersonId).child(key).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.exists() && snapshot.getValue() != null) {
                                    PersonalMessageModel messageModel = snapshot.getValue(PersonalMessageModel.class);
                                    uploadImage(messageModel, holder, mSelectedPersonId);

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
        } else {

            holder.receiverDownloadProgress.setVisibility(View.GONE);
            holder.receiverProgressText.setVisibility(View.GONE);
            holder.senderLinearLayout.setVisibility(View.GONE);
            holder.receiverLinearLayout.setVisibility(View.VISIBLE);
            holder.receiverMessage.setText(personalMessageModel.getMessage());
            holder.receiverTime.setText(personalMessageModel.getTime());
            holder.senderImage.setVisibility(View.GONE);
            holder.receiverImage.setVisibility(View.VISIBLE);
            ArrayMap<String, String> imageMap = createFileName(personalMessageModel.getFileName(), "/Images");
            String checkFlag = imageMap.get(Constants.FLAG);
            if (checkFlag == "0") {

                String downloadUrl = personalMessageModel.getThumbDownloadUrl();
                if (downloadUrl != null) {
                    Picasso.get()
                            .load(downloadUrl)
                            .transform(new BlurTransformation(mContext, 25, 4))
                            .into(holder.receiverImage);
                }

            } else {
                String dataPath = imageMap.get(Constants.DATA_PATH);
                if (dataPath != null) {
                    Picasso.get().load(new File(dataPath)).into(holder.receiverImage);
                }
            }


            WorkManager.getInstance(mContext.getApplicationContext()).getWorkInfosForUniqueWorkLiveData(personalMessageModel.getFileName()).observe((LifecycleOwner) mContext, workInfos -> {
                for (WorkInfo workInfo : workInfos) {

                    if (workInfo.getState().equals(WorkInfo.State.RUNNING)) {
                        holder.receiverProgressText.setVisibility(View.VISIBLE);
                        holder.receiverDownloadProgress.setVisibility(View.VISIBLE);
                        int anInt = workInfo.getProgress().getInt(Constants.PROGRESS_DOWNLOAD, 0);
                        holder.receiverProgressText.setVisibility(View.VISIBLE);
                        holder.receiverDownloadProgress.setVisibility(View.VISIBLE);
                        holder.receiverProgressText.setText(anInt + " %");
                        holder.receiverDownloadProgress.setProgress(anInt);
                    }
                    if (workInfo.getState().isFinished()) {
                        holder.receiverProgressText.setText(0 + " %");
                        holder.receiverDownloadProgress.setProgress(0);
                        holder.receiverProgressText.setVisibility(View.GONE);
                        holder.receiverDownloadProgress.setVisibility(View.GONE);
                    }
                }
            });


        }
    }

    private void showTextMessage(@NonNull MyViewHolder holder, PersonalMessageModel personalMessageModel, String mCurrentUserId) {
        holder.receiverImage.setVisibility(View.GONE);
        holder.senderImage.setVisibility(View.GONE);

        if (mCurrentUserId.equals(personalMessageModel.getFrom())) {

            holder.senderLinearLayout.setVisibility(View.VISIBLE);
            holder.receiverLinearLayout.setVisibility(View.GONE);
            holder.senderMessage.setText(personalMessageModel.getMessage());
            holder.senderTime.setText(personalMessageModel.getTime());

            String key = personalMessageModel.getMessageKey();
            FirebaseDatabase.getInstance().getReference().child(Constants.MESSAGES).child(mSelectedPersonId).child(mCurrentUserId).child(key).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {

                    if (snapshot.exists() && snapshot.hasChildren()) {

                    } else {
                        FirebaseDatabase.getInstance().getReference().child(Constants.MESSAGES).child(mCurrentUserId).child(mSelectedPersonId).child(key).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.exists() && snapshot.getValue() != null) {
                                    PersonalMessageModel messageModel = snapshot.getValue(PersonalMessageModel.class);
                                    sendMessage(messageModel, holder, mSelectedPersonId);
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
        } else {
            holder.senderLinearLayout.setVisibility(View.GONE);
            holder.receiverLinearLayout.setVisibility(View.VISIBLE);
            holder.receiverMessage.setText(personalMessageModel.getMessage());
            holder.receiverTime.setText(personalMessageModel.getTime());
        }
    }


    private void showAndSendLocationMessage(@NonNull MyViewHolder holder, PersonalMessageModel personalMessageModel, String mCurrentUserId) {

        holder.receiverImage.setVisibility(View.GONE);
        holder.senderImage.setVisibility(View.GONE);
        if (mCurrentUserId.equals(personalMessageModel.getFrom())) {
            holder.senderLinearLayout.setVisibility(View.VISIBLE);
            holder.receiverLinearLayout.setVisibility(View.GONE);
            holder.senderMessage.setText("Live Location");
            holder.senderMessage.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_location_on_24, 0, 0, 0);
            holder.senderTime.setText(personalMessageModel.getTime());
            String key = personalMessageModel.getMessageKey();
            FirebaseDatabase.getInstance().getReference().child(Constants.MESSAGES).child(mSelectedPersonId).child(mCurrentUserId).child(key).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists() && snapshot.hasChildren()) {

                    } else {
                        FirebaseDatabase.getInstance().getReference().child(Constants.MESSAGES).child(mCurrentUserId).child(mSelectedPersonId).child(key).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.exists() && snapshot.getValue() != null) {
                                    PersonalMessageModel messageModel = snapshot.getValue(PersonalMessageModel.class);
                                    sendMessage(messageModel, holder, mSelectedPersonId);
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
        } else {
            holder.senderLinearLayout.setVisibility(View.GONE);
            holder.receiverLinearLayout.setVisibility(View.VISIBLE);
            holder.receiverMessage.setText("Live Location");
            holder.receiverMessage.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_location_on_24, 0, 0, 0);
            holder.receiverTime.setText(personalMessageModel.getTime());
        }
    }


    private void sendMessage(PersonalMessageModel messageModel, MyViewHolder holder, String selectedPersonId) {
        uploadTaskWorker(messageModel, holder, selectedPersonId);
    }

    @SuppressLint("NotifyDataSetChanged")
    private void deliveryCheckForSelected(PersonalMessageModel personalMessageModel, String mCurrentUserId) {
        if (mSelectedPersonId.equals(personalMessageModel.getFrom()) && ((personalMessageModel.getDelivery().equals(Constants.Delivery.Delivered.name())) || (personalMessageModel.getDelivery().equals(Constants.Delivery.Sent.name()))) && personalMessageModel.getType() != null) {
            HashMap<String, Object> map = new HashMap<>();
            map.put(Constants.DELIVERY, Constants.Delivery.Seen.name());
            mUserMessageDatabaseRef.child(mCurrentUserId).child(mSelectedPersonId).child(personalMessageModel.getMessageKey()).updateChildren(map).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    mUserMessageDatabaseRef.child(mSelectedPersonId).child(mCurrentUserId).child(personalMessageModel.getMessageKey()).updateChildren(map).addOnCompleteListener(task1 -> {

                    });
                }
            });
        }
    }


    private void deliveryCheckForCurrent(@NonNull MyViewHolder holder, int position, PersonalMessageModel personalMessageModel, String mCurrentUserId) {
        //holder.messageDeliveryIcon.setVisibility(View.GONE);
        if (mCurrentUserId.equals(personalMessageModel.getFrom())) {
            String delivery = personalMessageModel.getDelivery();
            if (Constants.Delivery.Delivered.name().equals(delivery)) {
                holder.messageDeliveryIcon.setVisibility(View.VISIBLE);
                holder.messageDeliveryIcon.setImageResource(R.drawable.ic_baseline_done_all_24);
            } else if (Constants.Delivery.Sent.name().equals(delivery)) {
                holder.messageDeliveryIcon.setVisibility(View.VISIBLE);
                holder.messageDeliveryIcon.setImageResource(R.drawable.ic_baseline_done_24);
            } else if (Constants.Delivery.Seen.name().equals(delivery)) {
                holder.messageDeliveryIcon.setVisibility(View.VISIBLE);
                holder.messageDeliveryIcon.setImageResource(R.drawable.ic_baseline_done_all_seen_24);
            } else if (Constants.Delivery.Loading.name().equals(delivery)) {
                holder.messageDeliveryIcon.setVisibility(View.VISIBLE);
                holder.messageDeliveryIcon.setImageResource(R.drawable.ic_baseline_cloud_upload_24);
            }

//            if (getItemCount() - 1 != position) {
//                holder.messageDeliveryIcon.setVisibility(View.GONE);
//            }
        }
    }


    @SuppressLint("QueryPermissionsNeeded")
    private void openIntent(PersonalMessageModel personalMessageModel) {

        String myFilePath = personalMessageModel.getMyStoragePath();
        //String storageContentPath = personalMessageModel.getPhoneStoragePath();
        File file = new File(myFilePath);
        Uri uriForFile = FileProvider.getUriForFile(Objects.requireNonNull(mContext.getApplicationContext()), mContext.getApplicationContext().getPackageName() + ".provider", file);
        MimeTypeMap myMime = MimeTypeMap.getSingleton();
        Intent newIntent = new Intent(Intent.ACTION_VIEW);
        String mimeType = myMime.getMimeTypeFromExtension(myFilePath.substring(1));
        newIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_ACTIVITY_SINGLE_TOP | FLAG_ACTIVITY_NEW_TASK);

        newIntent.setDataAndType(uriForFile, mimeType);
        PackageManager pm = mContext.getPackageManager();
        if (newIntent.resolveActivity(pm) != null) {
            mContext.startActivity(newIntent);
        }

    }

    @SuppressLint("QueryPermissionsNeeded")
    private void openIntent(Context context, PersonalMessageModel personalMessageModel,
                            int position) {

        String downloadUrl = personalMessageModel.getDownloadUrl();
        String fileName = personalMessageModel.getFileName();

        ArrayMap<String, String> dataFile = observingFileFromMemoryAndCreating(fileName);

        String flagValue = dataFile.get(Constants.FLAG);
        String myFilePath = dataFile.get(Constants.DATA_PATH);
        String myFileSize = personalMessageModel.getFileSize();

        if (flagValue == "0") {
            if (fileName != null && downloadUrl != null) {
                downloadFilesListener.downloadFile(downloadUrl, personalMessageModel.getFileName(), myFilePath, myFileSize, position);
            }
        } else {

            File file = new File(myFilePath);
            Uri uriForFile = FileProvider.getUriForFile(Objects.requireNonNull(mContext.getApplicationContext()), mContext.getApplicationContext().getPackageName() + ".provider", file);
            MimeTypeMap myMime = MimeTypeMap.getSingleton();
            Intent newIntent = new Intent(Intent.ACTION_VIEW);
            String mimeType = myMime.getMimeTypeFromExtension(myFilePath.substring(1));
            newIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_ACTIVITY_SINGLE_TOP | FLAG_ACTIVITY_NEW_TASK);
            newIntent.setDataAndType(uriForFile, mimeType);
            PackageManager pm = mContext.getPackageManager();
            if (newIntent.resolveActivity(pm) != null) {
                mContext.startActivity(newIntent);
            }
        }


    }

    @SuppressLint("SetTextI18n")
    private void uploadImage(PersonalMessageModel messageModel, MyViewHolder holder, String
            selectedPersonId) {
        uploadTaskWorker(messageModel, holder, selectedPersonId);
    }

    @SuppressLint("SetTextI18n")
    private void uploadFile(PersonalMessageModel messageModel, MyViewHolder holder, String
            selectedPersonId) {
        uploadTaskWorker(messageModel, holder, selectedPersonId);
    }


    private void uploadTaskWorker(PersonalMessageModel messageModel, MyViewHolder
            holder, String selectedPersonId) {
        String object = HelperFunctions.serializeToJsonPersonalMessageModel(messageModel);
        Data.Builder builder = new Data.Builder();
        builder.putString(Constants.MESSAGE_POJO_S_KEY, object);
        builder.putString(Constants.SELECTED_USER_ID_KEY, selectedPersonId);
        Data data = builder.build();
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();
        OneTimeWorkRequest oneTimeWorkRequest = new OneTimeWorkRequest.Builder(UploadTaskWorker.class)
                .setConstraints(constraints)
                .setInputData(data)
                .build();
        WorkManager.getInstance(mContext).enqueueUniqueWork(messageModel.getFileName(), ExistingWorkPolicy.KEEP, oneTimeWorkRequest);

        WorkManager.getInstance(mContext.getApplicationContext()).getWorkInfosForUniqueWorkLiveData(messageModel.getFileName()).observe((LifecycleOwner) mContext, workInfos -> {
            for (WorkInfo workInfo : workInfos) {
                if (workInfo.getState().equals(WorkInfo.State.RUNNING)) {
                    holder.senderProgressText.setVisibility(View.VISIBLE);
                    holder.senderUploadProgress.setVisibility(View.VISIBLE);
                    int anInt = workInfo.getProgress().getInt(Constants.PROGRESS_UPLOAD, 0);
                    holder.senderProgressText.setText(anInt + " %");
                    holder.senderUploadProgress.setProgress(anInt);
                }
                if (workInfo.getState().isFinished()) {
                    holder.senderProgressText.setText(0 + " %");
                    holder.senderUploadProgress.setProgress(0);
                    holder.senderUploadProgress.setVisibility(View.GONE);
                    holder.senderProgressText.setVisibility(View.GONE);
                }
            }
        });
    }


    Bitmap readPublicImage(String filePath) {

        ExecutorService executorService = Executors.newFixedThreadPool(2);
        Bitmap bitmap = null;
        try {
            Future<Bitmap> submit = executorService.submit(new MyCallBackInfo(filePath));
            bitmap = submit.get();
            executorService.shutdownNow();
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
            executorService.shutdownNow();
            return null;
        }

        return bitmap;
    }

    static class MyCallBackInfo implements Callable<Bitmap> {

        private String filePath;

        public MyCallBackInfo(String filePath) {
            this.filePath = filePath;
        }

        @Override
        public Bitmap call() throws Exception {

            FileInputStream fileInputStream = null;
            try {
                fileInputStream = new FileInputStream(filePath);
                return BitmapFactory.decodeStream(fileInputStream);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                return null;
            } finally {
                if (fileInputStream != null) {
                    try {
                        fileInputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

    }


    private void editMessage(PersonalMessageModel personalMessageModel, String
            mCurrentUserId) {
        snackBarCallbackListener.onSnackBarClicked("Todo");
    }

    private void deleteForMe(PersonalMessageModel personalMessageModel, String
            mCurrentUserId) {
        String messageKey = personalMessageModel.getMessageKey();
        FirebaseDatabase.getInstance().getReference().child(Constants.MESSAGES).child(mCurrentUserId).child(mSelectedPersonId).child(messageKey).removeValue().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                snackBarCallbackListener.onSnackBarClicked("Message Deleted");
            } else {
                snackBarCallbackListener.onSnackBarClicked("Can't be Deleted");
            }
        });
    }

    private void deleteForAll(PersonalMessageModel personalMessageModel, String
            mCurrentUserId) {
        String messageKey = personalMessageModel.getMessageKey();

        FirebaseDatabase.getInstance().getReference().child(Constants.MESSAGES).child(mCurrentUserId).child(mSelectedPersonId).child(messageKey).removeValue().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {

                FirebaseDatabase.getInstance().getReference().child(Constants.MESSAGES).child(mSelectedPersonId).child(mCurrentUserId).child(messageKey).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            snackBarCallbackListener.onSnackBarClicked("Message Deleted");
                        } else {
                            snackBarCallbackListener.onSnackBarClicked("Can't be Deleted");
                        }
                    }
                });
            }

        });
    }


}