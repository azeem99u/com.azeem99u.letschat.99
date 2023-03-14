package com.azeem99u.comazeem99uletschat99;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.os.Build.VERSION.SDK_INT;
import static com.azeem99u.comazeem99uletschat99.App.mAuth;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.app.ActivityCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.azeem99u.comazeem99uletschat99.auth.LoginActivity;
import com.azeem99u.comazeem99uletschat99.fragments.ViewPagerAdapter;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    public static SearchView searchView;
    ImageView backButton;
    private FirebaseAuth.AuthStateListener authStateListener;
    TabLayout mTabLayout;
    ViewPager2 mViewPager;

    ShapeableImageView shapeableImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toolBarSetUp();
        setSearchView();

        mViewPager = findViewById(R.id.mainPageViewPager);
        mTabLayout = findViewById(R.id.mainPageTabLayout);
        authStateListener();
        shapeableImageView = findViewById(R.id.userImage);
        shapeableImageView.setOnClickListener(view -> {
            new SettingFragment().show(getSupportFragmentManager(), SettingFragment.DF_TAG);
        });

    }





    private void setSearchView() {

        searchView = findViewById(R.id.searchView);
        backButton = findViewById(R.id.backButtonImage);
        searchView.setOnSearchClickListener(view -> {
            backButton.setVisibility(View.VISIBLE);
        });

        backButton.setOnClickListener(view -> {
            hideSearchView();
        });

        searchView.setOnCloseListener(() -> {
            backButton.setVisibility(View.GONE);
            return false;
        });


    }

    private void hideSearchView() {
        if (!searchView.isIconified()) {
            searchView.setIconified(true);
            searchView.onActionViewCollapsed();
        }
        backButton.setVisibility(View.GONE);
    }


    private void authStateListener() {
        authStateListener = firebaseAuth -> {
            updateUI();
        };
    }


    private void toolBarSetUp() {
        MaterialToolbar toolbar = findViewById(R.id.mainPage_toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);

    }


    private void updateUI() {
        FirebaseUser account = mAuth.getCurrentUser();
        if (account == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();

        } else {


            if (getIntent()!=null && getIntent().hasExtra(Constants.ACTION_REQUEST_EXTRA)){
                    if ( getIntent().getStringExtra(Constants.ACTION_REQUEST_EXTRA).equals("requests")){
                        tabLayoutAndViewPagerSetting(1);
                        getIntent().putExtra(Constants.ACTION_REQUEST_EXTRA,"setNull");
                    }else {
                        tabLayoutAndViewPagerSetting(0);
                    }
                }else {
                    tabLayoutAndViewPagerSetting(0);
                }

            FirebaseDatabase.getInstance().getReference().child(Constants.USERS).child(mAuth.getUid()).child("userImage").get().addOnSuccessListener(dataSnapshot -> {
                if (dataSnapshot.exists()){
                    if (!Objects.requireNonNull(dataSnapshot.getValue(String.class)).isEmpty()){
                        Picasso.get().load(Uri.parse(dataSnapshot.getValue(String.class))).error(R.drawable.profileimage).into(shapeableImageView);
                    }else {
                        shapeableImageView.setImageResource(R.drawable.profileimage);
                    }
                }
            });

        }

    }

    @SuppressLint("ResourceType")
    private void tabLayoutAndViewPagerSetting(int itemPosition) {

        ViewPagerAdapter mViewPagerAdapter = new ViewPagerAdapter(this);
        mViewPager.setAdapter(mViewPagerAdapter);
        if (itemPosition == 1){
            mViewPager.setCurrentItem(itemPosition,false);
        }
        new TabLayoutMediator(mTabLayout, mViewPager,
                (tab, position) -> {
                    switch (position) {
                        case 0: {
                            tab.setText("Friends");
                            break;
                        }
                        case 1: {
                            tab.setText("Requests");
                            break;
                        }
                    }
                }).attach();
    }


    @Override
    protected void onStart() {
        super.onStart();
        if (!HelperFunctions.checkPermission(this)){
            showPermissionDialog();
        }
        mAuth.addAuthStateListener(authStateListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (authStateListener != null) {
            if (mAuth != null) {
                mAuth.removeAuthStateListener(authStateListener);
            }
        }
    }

    private void showPermissionDialog() {
        if (SDK_INT >= Build.VERSION_CODES.R) {
            try {
                Intent intent = new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                intent.addCategory("android.intent.category.DEFAULT");
                intent.setData(Uri.parse(String.format("package:%s", getApplicationContext().getPackageName())));
                startActivityForResult(intent, 2000);
            } catch (Exception e) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                startActivityForResult(intent, 2000);
            }
        } else
            ActivityCompat.requestPermissions(this, new String[]{WRITE_EXTERNAL_STORAGE, READ_EXTERNAL_STORAGE}, 333);
    }

    @Override
    public void onBackPressed() {
        hideSearchView();
        super.onBackPressed();

    }
}
