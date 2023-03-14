package com.azeem99u.comazeem99uletschat99.activities;


import static com.azeem99u.comazeem99uletschat99.ProgressUtils.showProgressBar;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.EditorInfo;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.azeem99u.comazeem99uletschat99.Constants;
import com.azeem99u.comazeem99uletschat99.DividerItemDecorator;
import com.azeem99u.comazeem99uletschat99.LinearLayoutManagerWrapper;
import com.azeem99u.comazeem99uletschat99.ProgressUtils;
import com.azeem99u.comazeem99uletschat99.R;
import com.azeem99u.comazeem99uletschat99.models.ContactsModel;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Objects;

public class FindFriendsActivity extends AppCompatActivity {

    FindFriendsActivityAdapter mAdapter;
    private RecyclerView mRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_friends);
        toolBarSetUp();
        initViews();

        DatabaseReference mUserRef = FirebaseDatabase.getInstance().getReference().child(Constants.USERS);
        FirebaseRecyclerOptions<ContactsModel> options =
                new FirebaseRecyclerOptions.Builder<ContactsModel>()
                        .setQuery(mUserRef, ContactsModel.class)
                        .build();

        mAdapter = new FindFriendsActivityAdapter(this,options);
        mRecyclerView.setAdapter(mAdapter);
        RecyclerView.ItemDecoration dividerItemDecoration = new DividerItemDecorator(ContextCompat.getDrawable(getApplicationContext(), R.drawable.divider_gray));
        mRecyclerView.addItemDecoration(dividerItemDecoration);
        mAdapter.startListening();
        showProgressBar();

    }


    private void toolBarSetUp() {
        MaterialToolbar toolbar = findViewById(R.id.findFriendsPage_toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Find Friends");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }



    private void initViews() {
        ProgressUtils.initAddProgress(this);
        EmptyListHelper.initEmptyView(this);
        mRecyclerView = findViewById(R.id.recyclerViewF);
        mRecyclerView.setLayoutManager(new LinearLayoutManagerWrapper(this, LinearLayoutManager.VERTICAL,false));

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mAdapter.stopListening();
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.find_friends_menu,menu);
        MenuItem searchItem = menu.findItem(R.id.app_bar_search);
        SearchView searchView =(SearchView)searchItem.getActionView();
        searchView.setImeOptions(EditorInfo.IME_ACTION_DONE);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                mAdapter.getFilter().filter(newText);
                return false;
            }
        });

        return true;
    }


}