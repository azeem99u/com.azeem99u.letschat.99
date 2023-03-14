package com.azeem99u.comazeem99uletschat99;

import static com.azeem99u.comazeem99uletschat99.App.mAuth;
import static com.azeem99u.comazeem99uletschat99.Constants.getTime;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.azeem99u.comazeem99uletschat99.activities.SettingsActivity;
import com.azeem99u.comazeem99uletschat99.databinding.FragmentSettingFragmentBinding;
import com.azeem99u.comazeem99uletschat99.models.UserProfile;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class SettingFragment extends BottomSheetDialogFragment {

    public static final String DF_TAG ="SettingFragment" ;
    ValueEventListener valueEventListener;

    private FragmentSettingFragmentBinding binding;
    public SettingFragment() {}
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentSettingFragmentBinding.inflate(inflater);
        return binding.getRoot();
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        String uid = mAuth.getCurrentUser().getUid();


        valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if (snapshot.exists() && snapshot.hasChildren()){
                    UserProfile profile = snapshot.getValue(UserProfile.class);
                    String username = profile.getUsername();
                    String userImage = profile.getUserImage();

                    if (userImage != null){
                        Picasso.get().load(Uri.parse(userImage)).error(R.drawable.profileimage).into(binding.userImage);
                    }else {
                        binding.userImage.setImageResource(R.drawable.profileimage);
                    }
                    binding.username.setText(username);

                }

                if (snapshot.exists() && snapshot.hasChild(Constants.USER_STATUS)) {
                    Object value = snapshot.child(Constants.USER_STATUS).getValue(Object.class);
                    if (value != null){
                        binding.userStatus.setVisibility(View.VISIBLE);
                        if (value instanceof String) {
                            binding.userStatus.setText("Online");

                        } else if (value instanceof Long) {
                            binding.userStatus.setText(getTime(((Long) value)));
                        }
                    }else {
                        binding.userStatus.setVisibility(View.GONE);
                    }
                }else {
                    binding.userStatus.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };

        FirebaseDatabase.getInstance().getReference().child(Constants.USERS).child(uid).addValueEventListener(valueEventListener);


        binding.editProfile.setOnClickListener(view14 -> {
                startActivity(new Intent(requireActivity(), SettingsActivity.class));
                dismiss();
        });

        binding.inviteFriend.setOnClickListener(view13 -> {
            Toast.makeText(requireActivity(), "imp", Toast.LENGTH_SHORT).show();
        });

        binding.helpAndSupport.setOnClickListener(view12 -> {
            Toast.makeText(requireActivity(), "imp", Toast.LENGTH_SHORT).show();
        });

        binding.logoutTxt.setOnClickListener(view1 -> {
            signOut();
            dismiss();
        });
    }


    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    private void signOut() {


        TokenHelper.deleteMyToken(requireContext());
    }
}