package com.azeem99u.comazeem99uletschat99.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.view.View;
import android.widget.LinearLayout;

import com.azeem99u.comazeem99uletschat99.R;

public class EmptyListHelper {

    @SuppressLint("StaticFieldLeak")
    private static LinearLayout emptyListLayout;
    public static void initEmptyView(Activity activity){
        emptyListLayout = activity.findViewById(R.id.empty_list_layout);
    }

    public static void initEmptyView(View inflate){
        emptyListLayout = inflate.findViewById(R.id.empty_list_layout);
    }

    public static void showEmptyView() {
        emptyListLayout.setVisibility(View.VISIBLE);
    }

    public static void hideEmptyView() {
        emptyListLayout.setVisibility(View.GONE);
    }

}
