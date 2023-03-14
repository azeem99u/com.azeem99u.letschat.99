package com.azeem99u.comazeem99uletschat99.activities;


import static com.azeem99u.comazeem99uletschat99.ProgressUtils.hideProgressBar;
import static com.azeem99u.comazeem99uletschat99.activities.EmptyListHelper.hideEmptyView;
import static com.azeem99u.comazeem99uletschat99.activities.EmptyListHelper.showEmptyView;

import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.azeem99u.comazeem99uletschat99.Constants;
import com.azeem99u.comazeem99uletschat99.R;
import com.azeem99u.comazeem99uletschat99.models.ContactsModel;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.Objects;

public class ContactsActivityAdapter extends FirebaseRecyclerAdapter<Object, ContactsActivityAdapter.MyContactsHolder> {


    private ContactsActivity context;
    private DatabaseReference mUserRef;
    public ContactsActivityAdapter(ContactsActivity contactsActivity, @NonNull FirebaseRecyclerOptions<Object> options, DatabaseReference mUserRef) {
        super(options);
        context = contactsActivity;
        this.mUserRef = mUserRef;
    }

    @NonNull
    @Override
    public MyContactsHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MyContactsHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.find_friends_activity_adapter_item, parent, false));
    }

    @Override
    protected void onBindViewHolder(@NonNull MyContactsHolder holder, int position, @NonNull Object contactsModel) {

        String userId = getRef(position).getKey();
        final String[] username = {null};
        final String[] userImage = {null};
        final String[] userStatus = {null};

        mUserRef.child(Objects.requireNonNull(userId)).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ContactsModel contacts = snapshot.getValue(ContactsModel.class);
                if (snapshot.exists() && contacts != null) {

                    username[0] = contacts.getUsername();
                    holder.mUsername.setText(contacts.getUsername());
                    userStatus[0] = "Online";
                    if (contacts.getUserAbout().isEmpty()) {
                        holder.mUserAboutInfo.setVisibility(View.GONE);
                    } else {
                        holder.mUserAboutInfo.setText(contacts.getUserAbout());
                    }

                    if (!contacts.getUserImage().isEmpty()) {
                        userImage[0] = contacts.getUserImage();
                        Picasso.get().load(contacts.getUserImage()).error(R.drawable.profileimage).placeholder(R.drawable.profileimage).into(holder.mUserImage);
                    } else {
                        holder.mUserImage.setImageResource(R.drawable.profileimage);
                    }
                }
            }



            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        holder.itemView.setOnClickListener(view -> {
            try {
                if (userId != null && username[0]!= null){
                    Intent intent = new Intent(context, PersonalChatActivity.class);
                    intent.putExtra(Constants.SELECTED_USER_ID_EXTRA_KEY, userId);
                    intent.putExtra(Constants.USER_NAME_EXTRA_KEY, username[0]);
                    intent.putExtra(Constants.USER_STATUS_EXTRA_KEY, userStatus[0]);
                    intent.putExtra(Constants.USER_IMAGE_EXTRA_KEY, userImage[0]);
                    context.startActivity(intent);
                    context.finish();
                }

            } catch (Exception e) {
                Log.d("mytag", "onBindViewHolder: " + e.getLocalizedMessage());
            }

        });


        hideProgressBar();

    }


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
        private TextView mUsername, mUserAboutInfo;

        public MyContactsHolder(@NonNull View itemView) {
            super(itemView);
            mUserImage = itemView.findViewById(R.id.userImageM);
            mUsername = itemView.findViewById(R.id.userNameToolbar);
            mUserAboutInfo = itemView.findViewById(R.id.userLastMessage);
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