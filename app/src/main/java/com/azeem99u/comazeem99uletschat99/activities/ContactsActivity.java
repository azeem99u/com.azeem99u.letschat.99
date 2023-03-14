package com.azeem99u.comazeem99uletschat99.activities;

import static com.azeem99u.comazeem99uletschat99.ProgressUtils.hideProgressBar;
import static com.azeem99u.comazeem99uletschat99.ProgressUtils.showProgressBar;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.azeem99u.comazeem99uletschat99.Constants;
import com.azeem99u.comazeem99uletschat99.DividerItemDecorator;
import com.azeem99u.comazeem99uletschat99.LinearLayoutManagerWrapper;
import com.azeem99u.comazeem99uletschat99.ProgressUtils;
import com.azeem99u.comazeem99uletschat99.R;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Objects;

public class ContactsActivity extends AppCompatActivity {

    RecyclerView mRecyclerView;
    String mCurrantUserId;
    DatabaseReference mUserRef;

    ContactsActivityAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact);
        toolBarSetUp();
        initViews();

        mCurrantUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        mUserRef = FirebaseDatabase.getInstance().getReference().child(Constants.USERS);

        DatabaseReference mContactsRef = FirebaseDatabase.getInstance().getReference().child(Constants.CONTACTS).child(mCurrantUserId);
        FirebaseRecyclerOptions<Object> options =
                new FirebaseRecyclerOptions.Builder<Object>()
                        .setQuery(mContactsRef, Object.class)
                        .build();

        mAdapter = new ContactsActivityAdapter(this, options, mUserRef);
        mRecyclerView.setAdapter(mAdapter);
        RecyclerView.ItemDecoration dividerItemDecoration = new DividerItemDecorator(ContextCompat.getDrawable(getApplicationContext(), R.drawable.divider_gray));
        mRecyclerView.addItemDecoration(dividerItemDecoration);
        mAdapter.startListening();
        showProgressBar();

    }




    private void initViews() {
        ProgressUtils.initAddProgress(this);
        EmptyListHelper.initEmptyView(this);
        mRecyclerView = findViewById(R.id.recyclerViewC);
        mRecyclerView.setLayoutManager(new LinearLayoutManagerWrapper(this, LinearLayoutManager.VERTICAL, false));
    }


    private void toolBarSetUp() {
        MaterialToolbar toolbar = findViewById(R.id.contacts_toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        mAdapter.stopListening();
        hideProgressBar();
    }
}