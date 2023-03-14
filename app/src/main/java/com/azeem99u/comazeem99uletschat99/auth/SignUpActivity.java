package com.azeem99u.comazeem99uletschat99.auth;

import static com.azeem99u.comazeem99uletschat99.App.mAuth;
import static com.azeem99u.comazeem99uletschat99.ProgressUtils.hideProgressBar;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.azeem99u.comazeem99uletschat99.Constants;
import com.azeem99u.comazeem99uletschat99.ProgressUtils;
import com.azeem99u.comazeem99uletschat99.R;
import com.azeem99u.comazeem99uletschat99.TokenHelper;
import com.azeem99u.comazeem99uletschat99.activities.SettingsActivity;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

public class SignUpActivity extends AppCompatActivity {

    private TextInputLayout mEmailLayout, mPasswordLayout, mRePasswordLayout;
    private CardView mBtnSignup;
    DatabaseReference mRootRef;
    ValueEventListener mValueEventListener;
    String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        initViews();
        setUpProgressBar();
        mRootRef = FirebaseDatabase.getInstance().getReference();
        mBtnSignup.setOnClickListener(this::createUser);
    }


    private void createUser(View view) {

        if (!validateEmailAddress() | !validatePassword() | !re_validatePassword()) {
            return;
        }
        ProgressUtils.showProgressBar();
        //Email and Password valid, create user here
        String userEmail = Objects.requireNonNull(mEmailLayout.getEditText()).getText().toString().trim();
        String userPassword = Objects.requireNonNull(mPasswordLayout.getEditText()).getText().toString().trim();

        Task<AuthResult> authResultTask = mAuth.createUserWithEmailAndPassword(userEmail, userPassword);
        authResultTask.addOnCompleteListener(task -> {
            hideProgressBar();
            if (task.isSuccessful()) {
                goToSettingsActivity(mAuth.getCurrentUser().getUid());
            } else {

                Toast.makeText(SignUpActivity.this, "" + task.getException().getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                hideProgressBar();
                if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                    Toast.makeText(SignUpActivity.this, "Email Already Exists", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void goToSettingsActivity(String currentUserId) {
        mRootRef.child(Constants.USERS).child(currentUserId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.child("name").exists()) {
                    TokenHelper.addNewToken(getApplicationContext());
                    startActivity(new Intent(SignUpActivity.this, SettingsActivity.class));
                    finishAffinity();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(SignUpActivity.this, "" + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    private boolean validateEmailAddress() {

        String email = mEmailLayout.getEditText().getText().toString().trim();

        if (email.isEmpty()) {
            mEmailLayout.setError("Email is required. Can't be empty.");
            return false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            mEmailLayout.setError("Invalid Email. Enter valid email address.");
            return false;
        } else {
            mEmailLayout.setError(null);
            return true;
        }
    }

    private boolean validatePassword() {

        String password = mPasswordLayout.getEditText().getText().toString().trim();

        if (password.isEmpty()) {
            mPasswordLayout.setError("Password is required. Can't be empty.");
            return false;
        } else if (password.length() < 8) {
            mPasswordLayout.setError("Password length short. Minimum 8 characters required.");
            return false;
        } else {
            mPasswordLayout.setError(null);
            return true;
        }

    }

    private boolean re_validatePassword() {

        String password = mRePasswordLayout.getEditText().getText().toString().trim();

        if (password.isEmpty()) {
            mRePasswordLayout.setError("Password is required. Can't be empty.");
            return false;
        } else if (password.length() < 8) {
            mRePasswordLayout.setError("Password length short. Minimum 8 characters required.");
            return false;

        } else if (!(isValidateDoubleCheckCorrect())) {
            mRePasswordLayout.setError("Password does not match.");
            return false;
        } else {
            mRePasswordLayout.setError(null);
            return true;
        }
    }

    private boolean isValidateDoubleCheckCorrect() {
        String password = mPasswordLayout.getEditText().getText().toString().trim();
        String rePassword = mRePasswordLayout.getEditText().getText().toString().trim();
        if (!password.isEmpty() && !rePassword.isEmpty()) {
            return password.equals(rePassword);
        }
        return false;
    }

    private void setUpProgressBar() {
        ProgressUtils.initAddProgress(this);
        hideProgressBar();
    }

    private void initViews() {
        mEmailLayout = findViewById(R.id.et_email_signup);
        mPasswordLayout = findViewById(R.id.et_password_signup);
        mRePasswordLayout = findViewById(R.id.et_re_password_signup);
        mBtnSignup = findViewById(R.id.btn_signup);
    }
}


