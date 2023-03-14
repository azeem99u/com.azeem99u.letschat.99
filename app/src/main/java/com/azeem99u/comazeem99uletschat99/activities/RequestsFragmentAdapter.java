package com.azeem99u.comazeem99uletschat99.activities;


import static com.azeem99u.comazeem99uletschat99.ProgressUtils.hideProgressBar;
import static com.azeem99u.comazeem99uletschat99.activities.EmptyListHelper.hideEmptyView;
import static com.azeem99u.comazeem99uletschat99.activities.EmptyListHelper.showEmptyView;

import android.annotation.SuppressLint;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.azeem99u.comazeem99uletschat99.Constants;
import com.azeem99u.comazeem99uletschat99.R;
import com.azeem99u.comazeem99uletschat99.models.UserProfile;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class RequestsFragmentAdapter extends FirebaseRecyclerAdapter<Object, RequestsFragmentAdapter.MyContactsHolder> {
    DatabaseReference mContactRef;
    String mCurrentUserKey;
    DatabaseReference mRequestRef;

    public RequestsFragmentAdapter(@NonNull FirebaseRecyclerOptions<Object> options, String mCurrentUserKey, DatabaseReference mRequestRef, DatabaseReference mContactRef) {
        super(options);
        this.mCurrentUserKey = mCurrentUserKey;
        this.mRequestRef = mRequestRef;
        this.mContactRef = mContactRef;
    }

    @NonNull
    @Override
    public MyContactsHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MyContactsHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.request_fragment_adapter_item, parent, false));
    }

    @Override
    protected void onBindViewHolder(@NonNull MyContactsHolder holder, int position, @NonNull Object contactsModel) {
        String mRequestedUserKey = getRef(position).getKey();
        FirebaseDatabase.getInstance().getReference().child(Constants.USERS).child(mRequestedUserKey).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    UserProfile userProfile = snapshot.getValue(UserProfile.class);
                    holder.mUsername.setText(userProfile.getUsername());
                    holder.mUserAboutInfo.setText(userProfile.getUserAbout());

                    String userImage = userProfile.getUserImage();
                    if (userImage != null) {
                        Picasso.get().load(Uri.parse(userImage)).error(R.drawable.profileimage).placeholder(R.drawable.profileimage).into(holder.mUserImage);
                    } else {
                        holder.mUserImage.setImageResource(R.drawable.profileimage);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        holder.acceptRequestBtn.setOnClickListener(view -> {
            acceptRequest(mRequestedUserKey);
        });

        holder.cancelRequestBtn.setOnClickListener(view -> {
            cancelRequest(mRequestedUserKey);

        });

        hideProgressBar();
    }


    private void acceptRequest(String mRequestedUserKey) {

        mContactRef.child(mCurrentUserKey).child(mRequestedUserKey).child(Constants.CONTACT).setValue(Constants.SAVE).addOnCompleteListener(acceptRequestTask1 ->
                mContactRef.child(mRequestedUserKey).child(mCurrentUserKey).child(Constants.CONTACT).setValue(Constants.SAVE).addOnCompleteListener(acceptRequestTask2 -> {
                    cancelRequest(mRequestedUserKey);
                }));

    }

    private void cancelRequest(String mRequestedUserKey) {
        mRequestRef.child(mCurrentUserKey).child(mRequestedUserKey).child(Constants.REQUEST_TYPE).removeValue().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                mRequestRef.child(mRequestedUserKey).child(mCurrentUserKey).child(Constants.REQUEST_TYPE).removeValue().addOnCompleteListener(task1 -> {
                    if (task1.isSuccessful()) {
                    }
                });
            }
        });
    }


    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void onDataChanged() {
        super.onDataChanged();
        if (getItemCount() < 1) {
            hideProgressBar();
            showEmptyView();
        } else {
            hideEmptyView();
            hideProgressBar();
        }

    }


    protected static class MyContactsHolder extends RecyclerView.ViewHolder {
        private ShapeableImageView mUserImage;
        private Button acceptRequestBtn, cancelRequestBtn;
        private TextView mUsername, mUserAboutInfo;

        public MyContactsHolder(@NonNull View itemView) {
            super(itemView);
            mUserImage = itemView.findViewById(R.id.userImageM);
            mUsername = itemView.findViewById(R.id.userNameToolbar);
            mUserAboutInfo = itemView.findViewById(R.id.userLastMessage);
            acceptRequestBtn = itemView.findViewById(R.id.acceptRequestBtnR);
            cancelRequestBtn = itemView.findViewById(R.id.cancelRequestBtnR);
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


}