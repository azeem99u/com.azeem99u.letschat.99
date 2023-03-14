package com.azeem99u.comazeem99uletschat99.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.azeem99u.comazeem99uletschat99.Constants;
import com.azeem99u.comazeem99uletschat99.ImageResultActivity;
import com.azeem99u.comazeem99uletschat99.R;
import com.azeem99u.comazeem99uletschat99.activities.MapActivity;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;


public class ChooseFileBottomSheet extends BottomSheetDialogFragment {

    LinearLayout galleryBtn;
    LinearLayout cameraBtn;
    LinearLayout documentsBtn;
    LinearLayout musicBtn;
    LinearLayout locationBtn;
    Listener listener;
    LinearLayout contactBtn;

    public ChooseFileBottomSheet() {}
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View inflate = inflater.inflate(R.layout.choose_files_bottom_sheet_layout, container, false);
        initViews(inflate);
        inflate.setBackgroundColor(getResources().getColor(android.R.color.transparent));
        return inflate;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        galleryBtn.setOnClickListener(view1 -> {
            if (getArguments() != null) {
                String string = getArguments().getString(Constants.SELECTED_USER_ID_KEY);
                startActivity(new Intent(getActivity(), ImageResultActivity.class).putExtra(Constants.SELECTED_USER_ID_KEY,string));
                this.dismiss();
            }
        });
        //"image/* video/*"
        documentsBtn.setOnClickListener(view1 -> {

            listener.invoke();
            this.dismiss();
        });

        locationBtn.setOnClickListener(view1 -> {
            String string = getArguments().getString(Constants.SELECTED_USER_ID_KEY);
            startActivity(new Intent(getActivity(), MapActivity.class).putExtra(Constants.SELECTED_USER_ID_KEY,string));
            this.dismiss();
        });

    }

    public interface Listener{
        void invoke();
    }



    void initViews(View view){
        galleryBtn = view.findViewById(R.id.galleryLayout);
        cameraBtn = view.findViewById(R.id.cameraLayout);
        documentsBtn = view.findViewById(R.id.documentLayout);
        musicBtn = view.findViewById(R.id.musicLayout);
        locationBtn = view.findViewById(R.id.locationLayout);
        contactBtn = view.findViewById(R.id.contentLayout);

    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        listener = (Listener) context;
    }
}