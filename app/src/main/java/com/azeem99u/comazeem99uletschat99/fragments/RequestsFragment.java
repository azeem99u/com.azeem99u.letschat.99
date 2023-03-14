package com.azeem99u.comazeem99uletschat99.fragments;

import static com.azeem99u.comazeem99uletschat99.App.mAuth;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.azeem99u.comazeem99uletschat99.Constants;
import com.azeem99u.comazeem99uletschat99.DividerItemDecorator;
import com.azeem99u.comazeem99uletschat99.LinearLayoutManagerWrapper;
import com.azeem99u.comazeem99uletschat99.ProgressUtils;
import com.azeem99u.comazeem99uletschat99.R;
import com.azeem99u.comazeem99uletschat99.activities.EmptyListHelper;
import com.azeem99u.comazeem99uletschat99.activities.FindFriendsActivity;
import com.azeem99u.comazeem99uletschat99.activities.RequestsFragmentAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import java.util.Objects;

public class RequestsFragment extends Fragment {

    private RecyclerView mRecyclerView;
    RequestsFragmentAdapter mAdapter;
    DatabaseReference mContactRef;
    Query mQuery;
    String mCurrentUserKey;
    FloatingActionButton floatingActionButton;
    DatabaseReference mRequestRef;
    public RequestsFragment() {}



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View inflate = inflater.inflate(R.layout.fragment_requests, container, false);
        initViews(inflate);
        floatingActionButton = inflate.findViewById(R.id.findFriendsFB);
        mCurrentUserKey = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
        mRequestRef = FirebaseDatabase.getInstance().getReference().child(Constants.REQUESTS);
        mContactRef = FirebaseDatabase.getInstance().getReference().child(Constants.CONTACTS);
        mQuery = FirebaseDatabase.getInstance().getReference().child(Constants.REQUESTS).child(Objects.requireNonNull(mAuth.getCurrentUser()).getUid()).orderByChild(Constants.REQUEST_TYPE).equalTo(Constants.RECEIVED);

        return inflate;
    }

    private void initViews(View inflate) {
        ProgressUtils.initAddProgress(inflate);
        EmptyListHelper.initEmptyView(inflate);
        mRecyclerView = inflate.findViewById(R.id.recyclerViewR);
        mRecyclerView.setLayoutManager(new LinearLayoutManagerWrapper(getActivity(), LinearLayoutManager.VERTICAL,false));
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        floatingActionButton.setOnClickListener(view1 -> requireContext().startActivity(new Intent(requireActivity(), FindFriendsActivity.class)));

        FirebaseRecyclerOptions<Object> options = 
                new FirebaseRecyclerOptions.Builder<Object>()
                        .setQuery(mQuery, Object.class)
                        .build();

        mAdapter = new RequestsFragmentAdapter(options,mCurrentUserKey,mRequestRef,mContactRef);
        mRecyclerView.setAdapter(mAdapter);
        RecyclerView.ItemDecoration dividerItemDecoration = new DividerItemDecorator(ContextCompat.getDrawable(requireContext(), R.drawable.divider_gray));
        mRecyclerView.addItemDecoration(dividerItemDecoration);
    }

    @Override
    public void onStart() {
        super.onStart();
        mAdapter.startListening();
        ProgressUtils.showProgressBar();
    }


    @Override
    public void onStop() {
        super.onStop();
        mAdapter.stopListening();
    }
}