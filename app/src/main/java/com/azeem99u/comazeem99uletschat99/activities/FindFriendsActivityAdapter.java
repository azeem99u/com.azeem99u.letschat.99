package com.azeem99u.comazeem99uletschat99.activities;


import static com.azeem99u.comazeem99uletschat99.ProgressUtils.hideProgressBar;
import static com.azeem99u.comazeem99uletschat99.activities.EmptyListHelper.hideEmptyView;
import static com.azeem99u.comazeem99uletschat99.activities.EmptyListHelper.showEmptyView;

import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.azeem99u.comazeem99uletschat99.Constants;
import com.azeem99u.comazeem99uletschat99.R;
import com.azeem99u.comazeem99uletschat99.models.ContactsModel;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.imageview.ShapeableImageView;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

public class FindFriendsActivityAdapter extends FirebaseRecyclerAdapter<ContactsModel, FindFriendsActivityAdapter.MyContactsHolder> implements Filterable {


    private FindFriendsActivity context;
    private ArrayList<ContactsModel> contactList = new ArrayList<>();
    private ArrayList<ContactsModel> contactListFull;
    public FindFriendsActivityAdapter(FindFriendsActivity findFriendsActivity, @NonNull FirebaseRecyclerOptions<ContactsModel> options) {
        super(options);
        context = findFriendsActivity;
        contactList.clear();
        contactListFull  = new ArrayList<>();
    }

    @NonNull
    @Override
    public MyContactsHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MyContactsHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.find_friends_activity_adapter_item, parent, false));
    }


    @Override
    protected void onBindViewHolder(@NonNull MyContactsHolder holder, int position, @NonNull ContactsModel contactsModel) {
        contactList.add(contactsModel);
        contactListFull.clear();
        contactListFull.addAll(contactList);

        ContactsModel contactsModel1 = contactList.get(position);
        Log.d("azeem88", "onBindViewHolder: ");
        hideProgressBar();
        holder.mUsername.setText(contactsModel1.getUsername());

        if (contactsModel1.getUserAbout().isEmpty()) {
            holder.mUserAboutInfo.setVisibility(View.GONE);
        } else {
            holder.mUserAboutInfo.setText(contactsModel.getUserAbout());
        }

        if (!contactsModel1.getUserImage().isEmpty()) {
            Picasso.get().load(contactsModel1.getUserImage()).error(R.drawable.profileimage).placeholder(R.drawable.profileimage).into(holder.mUserImage);
        } else {
            holder.mUserImage.setImageResource(R.drawable.profileimage);
        }


        holder.itemView.setOnClickListener(view -> {

            try {
                String userKey = getRef(position).getKey();
                if (userKey != null) {
                    Intent intent = new Intent(context, ProfileDetailActivity.class);
                    intent.putExtra(Constants.PROFILE_INTENT_EXTRA, userKey);
                    context.startActivity(intent);
                }
            } catch (IndexOutOfBoundsException e) {
                Log.d("mytag", "onBindViewHolder: " + e.getLocalizedMessage());
            }
        });


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
    @Override
    public Filter getFilter() {
        return filter;
    }
    private Filter filter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence charSequence) {
            List<ContactsModel> filteredList = new ArrayList<>();
            if (charSequence ==null || charSequence.length() == 0){
                filteredList.addAll(contactListFull);
            }
            else {
                String filter =charSequence.toString().toLowerCase(Locale.ROOT).trim();
                for (ContactsModel item:contactListFull) {
                    if (item.getUsername().toLowerCase(Locale.ROOT).contains(filter)){
                        filteredList.add(item);
                    }
                }
            }

            FilterResults filterResults = new FilterResults();
            filterResults.values = filteredList;
            return filterResults;
        }

        @Override
        protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
            contactList.clear();
            contactList.addAll((Collection<? extends ContactsModel>) filterResults.values);
            notifyDataSetChanged();
        }
    };

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