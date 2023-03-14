package com.azeem99u.comazeem99uletschat99.auth;

import static android.content.ContentValues.TAG;
import static com.azeem99u.comazeem99uletschat99.App.mAuth;
import static com.azeem99u.comazeem99uletschat99.ProgressUtils.hideProgressBar;
import static com.azeem99u.comazeem99uletschat99.ProgressUtils.showProgressBar;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.azeem99u.comazeem99uletschat99.Constants;
import com.azeem99u.comazeem99uletschat99.MainActivity;
import com.azeem99u.comazeem99uletschat99.ProgressUtils;
import com.azeem99u.comazeem99uletschat99.R;
import com.azeem99u.comazeem99uletschat99.TokenHelper;
import com.azeem99u.comazeem99uletschat99.activities.SettingsActivity;
import com.azeem99u.comazeem99uletschat99.models.UserProfile;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class PhoneLoginActivity extends AppCompatActivity {

    PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;

    private TextInputLayout mPhoneInputLayout, mVerificationCInputLayout;
    private CardView mBtnPhoneSignIn, mBtnSendVerification;
    String mVerificationId;
    private FirebaseAuth.AuthStateListener authStateListener;
    PhoneAuthProvider.ForceResendingToken mResendToken;
    DatabaseReference mRootRef;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_login);
        initViews();
        setUpProgressBar();
        mRootRef = FirebaseDatabase.getInstance().getReference();
        mBtnSendVerification.setOnClickListener(this::sendVerificationCode);
        mBtnPhoneSignIn.setOnClickListener(this::phoneSignIn);
        authStateListener();

        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            @Override
            public void onVerificationCompleted(PhoneAuthCredential credential) {
                hideProgressBar();
                // This callback will be invoked in two situations:
                // 1 - Instant verification. In some cases the phone number can be instantly
                //     verified without needing to send or enter a verification code.
                // 2 - Auto-retrieval. On some devices Google Play services can automatically
                //     detect the incoming verification SMS and perform verification without
                //     user action.
                Log.d(TAG, "onVerificationCompleted:" + credential);
                signInWithPhoneAuthCredential(credential);
            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
                hideProgressBar();
                // This callback is invoked in an invalid request for verification is made,
                // for instance if the the phone number format is not valid.
                Log.w(TAG, "onVerificationFailed", e);

                mVerificationCInputLayout.setVisibility(View.GONE);
                mBtnPhoneSignIn.setVisibility(View.GONE);
                mPhoneInputLayout.setVisibility(View.VISIBLE);
                mBtnSendVerification.setVisibility(View.VISIBLE);

                if (e instanceof FirebaseAuthInvalidCredentialsException) {
                    // Invalid request
                    Toast.makeText(PhoneLoginActivity.this, "Invalid request", Toast.LENGTH_SHORT).show();
                } else if (e instanceof FirebaseTooManyRequestsException) {
                    // The SMS quota for the project has been exceeded
                    Toast.makeText(PhoneLoginActivity.this, "The SMS quota for the project has been exceeded", Toast.LENGTH_SHORT).show();

                }
            }

            @Override
            public void onCodeSent(@NonNull String verificationId,
                                   @NonNull PhoneAuthProvider.ForceResendingToken token) {
                hideProgressBar();

                // The SMS verification code has been sent to the provided phone number, we
                // now need to ask the user to enter the code and then construct a credential
                // by combining the code with a verification ID.
                Log.d(TAG, "onCodeSent:" + verificationId);
                // Save verification ID and resending token so we can use them later
                mVerificationId = verificationId;
                mResendToken = token;

                mVerificationCInputLayout.setVisibility(View.VISIBLE);
                mBtnPhoneSignIn.setVisibility(View.VISIBLE);
                mPhoneInputLayout.setVisibility(View.GONE);
                mBtnSendVerification.setVisibility(View.GONE);

            }
        };

    }

    private void authStateListener() {
        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                updateUI();
            }
        };
    }


    private void sendVerificationCode(View view) {
        String phoneNumber = mPhoneInputLayout.getEditText().getText().toString().trim();
        if (phoneNumber.isEmpty()) {
            return;
        }
        showProgressBar();
        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(mAuth)
                        .setPhoneNumber(phoneNumber)       // Phone number to verify
                        .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                        .setActivity(this)                 // Activity (for callback binding)
                        .setCallbacks(mCallbacks)          // OnVerificationStateChangedCallbacks
                        .build();
        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    private void phoneSignIn(View view) {
        String phoneText = mVerificationCInputLayout.getEditText().getText().toString().trim();
        if (phoneText.isEmpty()) {
            return;
        }
        showProgressBar();
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, phoneText);
        signInWithPhoneAuthCredential(credential);

    }


    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        hideProgressBar();
                        if (task.isSuccessful()) {
                            updateUI();
                        } else {
                            Toast.makeText(PhoneLoginActivity.this, "" + Objects.requireNonNull(task.getException()).getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                Toast.makeText(PhoneLoginActivity.this, "invalid Code", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });
    }


    private void updateUI() {
        FirebaseUser account = mAuth.getCurrentUser();
        ProgressUtils.hideProgressBar();
        if (account != null) {
            String uid = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
            mRootRef.child(Constants.USERS).child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {

                        if (!snapshot.hasChild("username")) {
                            startActivity(new Intent(PhoneLoginActivity.this, SettingsActivity.class));
                        }else if (snapshot.hasChild("username")){
                            UserProfile userProfile = snapshot.getValue(UserProfile.class);
                            String username = userProfile.getUsername();
                            if (!username.trim().isEmpty()){
                                startActivity(new Intent(PhoneLoginActivity.this, MainActivity.class));
                            }else {
                                startActivity(new Intent(PhoneLoginActivity.this, SettingsActivity.class));
                            }
                        }
                    }else {
                        startActivity(new Intent(PhoneLoginActivity.this, SettingsActivity.class));
                    }
                    TokenHelper.addNewToken(getApplicationContext());
                    finishAffinity();

                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(PhoneLoginActivity.this, "" + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

        }
    }


    private void setUpProgressBar() {
        ProgressUtils.initAddProgress(this);
        hideProgressBar();
    }

    private void initViews() {
        mBtnPhoneSignIn = findViewById(R.id.btn_verify);
        mBtnSendVerification = findViewById(R.id.btn_send_verification_code);
        mPhoneInputLayout = findViewById(R.id.et_phone_login);
        mVerificationCInputLayout = findViewById(R.id.et_verification_code);
    }


    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(authStateListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (authStateListener != null) {
                mAuth.removeAuthStateListener(authStateListener);
        }
    }
}