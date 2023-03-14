package com.azeem99u.comazeem99uletschat99.fragments;

import static com.azeem99u.comazeem99uletschat99.App.mAuth;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.azeem99u.comazeem99uletschat99.Constants;
import com.azeem99u.comazeem99uletschat99.DividerItemDecorator;
import com.azeem99u.comazeem99uletschat99.LinearLayoutManagerWrapper;
import com.azeem99u.comazeem99uletschat99.MainActivity;
import com.azeem99u.comazeem99uletschat99.ProgressUtils;
import com.azeem99u.comazeem99uletschat99.R;
import com.azeem99u.comazeem99uletschat99.activities.ContactsActivity;
import com.azeem99u.comazeem99uletschat99.activities.EmptyListHelper;
import com.azeem99u.comazeem99uletschat99.activities.PersonalChatActivity;
import com.azeem99u.comazeem99uletschat99.models.UserProfile;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Objects;

public class ChatFragment extends Fragment implements ChatFragmentAdapter.ChatFragmentClickListener{
    RecyclerView mRecyclerView;
    FloatingActionButton openContactsFab;
    SearchView searchView;
    ChildEventListener contactChildEventListener;
    ChatFragmentAdapter chatFragmentAdapter;
    ArrayList<UserProfile> contacts = new ArrayList<>();
    public ChatFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View inflate = inflater.inflate(R.layout.fragment_chat, container, false);
        contacts = new ArrayList<>();
        initViews(inflate);
        return inflate;
    }


    private void initViews(View inflate) {
        ProgressUtils.initAddProgress(inflate);
        EmptyListHelper.initEmptyView(inflate);
        openContactsFab = inflate.findViewById(R.id.openContactsFab);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        searchView = MainActivity.searchView;

        openContactsFab.setOnClickListener(this::startContactActivity);

        initRecyclerView(view);
        searchView.setImeOptions(EditorInfo.IME_ACTION_DONE);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                chatFragmentAdapter.getFilter().filter(newText.toLowerCase(Locale.ROOT));
                return false;
            }
        });


        contactChildEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                if (snapshot.exists()) {
                    showContacts(snapshot);
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String key = snapshot.getKey();
                    FirebaseDatabase.getInstance().getReference().child(Constants.USERS).child(Objects.requireNonNull(key)).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                UserProfile value = snapshot.getValue(UserProfile.class);
                                contacts.remove(value);
                                chatFragmentAdapter.notifyDataSetChanged();
                            }
                        }



                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };

        FirebaseDatabase.getInstance().getReference().child(Constants.MESSAGES).child(currentUserId).addChildEventListener(contactChildEventListener);

    }

    @SuppressLint("NotifyDataSetChanged")
    private void showContacts(DataSnapshot snapshot) {
        String key = snapshot.getKey();

        FirebaseDatabase.getInstance().getReference().child(Constants.USERS).child(Objects.requireNonNull(key)).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    UserProfile value = snapshot.getValue(UserProfile.class);
                    contacts.add(value);
                    mRecyclerView.setAdapter(chatFragmentAdapter);
                    ChatFragmentAdapter.setContacts(contacts);
                    chatFragmentAdapter.notifyDataSetChanged();
                }
            }



            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });



    }

    private void initRecyclerView(View view) {

        mRecyclerView = view.findViewById(R.id.recyclerViewM);
        chatFragmentAdapter = new ChatFragmentAdapter(this);
        chatFragmentAdapter.setHasStableIds(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManagerWrapper(getActivity(), LinearLayoutManager.VERTICAL,false));
        RecyclerView.ItemDecoration dividerItemDecoration = new DividerItemDecorator(ContextCompat.getDrawable(requireContext(), R.drawable.divider_gray));
        mRecyclerView.addItemDecoration(dividerItemDecoration);

    }

    private void startContactActivity(View view) {
        startActivity(new Intent(getActivity(), ContactsActivity.class));
    }

    @Override
    public void onClick(String selectedPersonId, String username) {

        try {
            if (selectedPersonId != null && username!= null){
                Intent intent = new Intent(getActivity(), PersonalChatActivity.class);
                intent.putExtra(Constants.SELECTED_USER_ID_EXTRA_KEY, selectedPersonId);
                intent.putExtra(Constants.USER_NAME_EXTRA_KEY, username);
                startActivity(intent);
            }
        } catch (Exception e) {
            Log.d("mytag", "onBindViewHolder: " + e.getLocalizedMessage());
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mAuth.getCurrentUser() != null){
            FirebaseDatabase.getInstance().getReference().child(Constants.MESSAGES).child(mAuth.getCurrentUser().getUid()).removeEventListener(contactChildEventListener);
        }
    }
}