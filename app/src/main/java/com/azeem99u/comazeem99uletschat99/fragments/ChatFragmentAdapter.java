package com.azeem99u.comazeem99uletschat99.fragments;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.azeem99u.comazeem99uletschat99.R;
import com.azeem99u.comazeem99uletschat99.models.UserProfile;
import com.google.android.material.imageview.ShapeableImageView;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

public class ChatFragmentAdapter extends RecyclerView.Adapter<ChatFragmentAdapter.MyViewHolder> implements Filterable {

    ChatFragmentClickListener mClickListener;


    private static ArrayList<UserProfile> contacts = new ArrayList<>();

    private static ArrayList<UserProfile> contactsFull;

    public ChatFragmentAdapter(ChatFragmentClickListener mClickListener) {
        this.mClickListener = mClickListener;
    }

    public static void setContacts(ArrayList<UserProfile> contacts) {
        ChatFragmentAdapter.contacts = contacts;
        contactsFull = new ArrayList<>();
        contactsFull.addAll(contacts);
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MyViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_fragment_adapter_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {

        UserProfile userProfile = contacts.get(position);
        String selectedUserId = userProfile.getUserId();
        String username = userProfile.getUsername();
        holder.mUsername.setText(username);

        if (userProfile.getUserImage() != null) {
            Picasso.get().load(userProfile.getUserImage()).error(R.drawable.profileimage).placeholder(R.drawable.profileimage).into(holder.mUserImage);
        } else {
            holder.mUserImage.setImageResource(R.drawable.profileimage);
        }

        holder.itemView.setOnClickListener(view -> {
            mClickListener.onClick(selectedUserId, username);
        });
    }

    @Override
    public Filter getFilter() {
        return filter;
    }
    private Filter filter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence charSequence) {
            List<UserProfile> filteredList = new ArrayList<>();
            if (charSequence ==null || charSequence.length() == 0){
                filteredList.addAll(contactsFull);
            }
            else {
                String filter =charSequence.toString().toLowerCase(Locale.ROOT).trim();
                for (UserProfile item:contactsFull) {
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
            contacts.clear();
            contacts.addAll((Collection<? extends UserProfile>) filterResults.values);
            notifyDataSetChanged();
        }
    };



    protected static class MyViewHolder extends RecyclerView.ViewHolder {
        private ShapeableImageView mUserImage;
        private TextView mUsername, mUserLastMessage;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            mUserImage = itemView.findViewById(R.id.userImageM);
            mUsername = itemView.findViewById(R.id.userNameM);
            mUserLastMessage = itemView.findViewById(R.id.userLastMessage);
        }
    }


    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return contacts.size();
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }


    @Override
    public void setHasStableIds(boolean hasStableIds) {
        super.setHasStableIds(hasStableIds);
    }

    public interface ChatFragmentClickListener {
        void onClick(String userId, String username);
    }


}