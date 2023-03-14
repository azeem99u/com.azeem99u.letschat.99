package com.azeem99u.comazeem99uletschat99.auth;

import static com.azeem99u.comazeem99uletschat99.App.mAuth;
import static com.azeem99u.comazeem99uletschat99.App.mGoogleSignInClient;
import static com.azeem99u.comazeem99uletschat99.ProgressUtils.hideProgressBar;
import static com.azeem99u.comazeem99uletschat99.ProgressUtils.showProgressBar;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.azeem99u.comazeem99uletschat99.Constants;
import com.azeem99u.comazeem99uletschat99.MainActivity;
import com.azeem99u.comazeem99uletschat99.ProgressUtils;
import com.azeem99u.comazeem99uletschat99.R;
import com.azeem99u.comazeem99uletschat99.TokenHelper;
import com.azeem99u.comazeem99uletschat99.activities.SettingsActivity;
import com.azeem99u.comazeem99uletschat99.models.UserProfile;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInStatusCodes;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

public class LoginActivity extends AppCompatActivity {


    private static final int SING_IN_REQUEST_CODE = 1232;
    String currentUserId;

    private FirebaseAuth.AuthStateListener authStateListener;
    private TextInputLayout mEmailLayout, mPasswordLayout;
    private CardView mBtnSignIn, mBtnPhoneSignIn;
    private TextView mBtnRegisterUser;
    private SignInButton mSignInButton;
    private DatabaseReference mRootRef;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        initViews();
        setUpProgressBar();
        mRootRef = FirebaseDatabase.getInstance().getReference();
        authStateListener();
        mBtnRegisterUser.setOnClickListener(this::createUser);
        mBtnSignIn.setOnClickListener(this::singInUser);
        mBtnPhoneSignIn.setOnClickListener(this::phoneSingIn);
        mSignInButton.setOnClickListener(this::signInWithGoogle);
    }

    private void phoneSingIn(View view) {
        startActivity(new Intent(this, PhoneLoginActivity.class));
    }

    private void createUser(View view) {
        startActivity(new Intent(this, SignUpActivity.class));
    }

    private void updateUI() {
        FirebaseUser account = mAuth.getCurrentUser();
        ProgressUtils.hideProgressBar();
        if (account != null) {
            currentUserId = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
            mRootRef.child(Constants.USERS).child(currentUserId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {

                        if (!snapshot.hasChild("username")) {
                            startActivity(new Intent(LoginActivity.this, SettingsActivity.class));
                        } else if (snapshot.hasChild("username")) {
                            UserProfile userProfile = snapshot.getValue(UserProfile.class);
                            String username = userProfile.getUsername();
                            if (!username.trim().isEmpty()) {
                                startActivity(new Intent(LoginActivity.this, MainActivity.class));
                            } else {
                                startActivity(new Intent(LoginActivity.this, SettingsActivity.class));
                            }
                        }
                    } else {
                        startActivity(new Intent(LoginActivity.this, SettingsActivity.class));
                    }
                    TokenHelper.addNewToken(getApplicationContext());
                    finish();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(LoginActivity.this, "" + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

        }
    }

    private void singInUser(View view) {
        if (!validateEmailAddress() | !validatePassword()) {
            return;
        }
        showProgressBar();
        String userEmail = Objects.requireNonNull(mEmailLayout.getEditText()).getText().toString().trim();
        String userPassword = Objects.requireNonNull(mPasswordLayout.getEditText()).getText().toString().trim();
        Task<AuthResult> authResultTask = mAuth.signInWithEmailAndPassword(userEmail, userPassword);
        authResultTask.addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                hideProgressBar();
            } else {
                hideProgressBar();
                if (task.getException() instanceof FirebaseAuthInvalidUserException) {
                    Toast.makeText(LoginActivity.this, "Invalid Email", Toast.LENGTH_SHORT).show();
                } else if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                    Toast.makeText(LoginActivity.this, "Invalid Password", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    private void initViews() {
        mSignInButton = findViewById(R.id.googleSignInBtn);
        mEmailLayout = findViewById(R.id.et_email);
        mPasswordLayout = findViewById(R.id.et_password);
        mBtnPhoneSignIn = findViewById(R.id.btn_phone_login);
        mBtnSignIn = findViewById(R.id.btn_singin);
        mBtnRegisterUser = findViewById(R.id.btn_registeruser);
    }

    private void authStateListener() {
        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                updateUI();
            }
        };
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

    private void setUpProgressBar() {
        ProgressUtils.initAddProgress(this);
        hideProgressBar();
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

    private void signInWithGoogle(View view) {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent,SING_IN_REQUEST_CODE);
        showProgressBar();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SING_IN_REQUEST_CODE){
            Task<GoogleSignInAccount> accountTask = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleGoogleSignIn(accountTask);
        }
    }

    private void handleGoogleSignIn(Task<GoogleSignInAccount> accountTask) {
        try {
            GoogleSignInAccount account = accountTask.getResult(ApiException.class);
            firebaseAuthWithGoogle(account);

        } catch (ApiException e) {
            View pare = findViewById(android.R.id.content);
            String statusCodeString = GoogleSignInStatusCodes.getStatusCodeString(e.getStatusCode());
            hideProgressBar();
            Snackbar snackbar = Snackbar.make(pare,""+statusCodeString,Snackbar.LENGTH_LONG);
            snackbar.show();

//            mOutputText.setText(GoogleSignInStatusCodes.getStatusCodeString(e.getStatusCode()) +"\n"+ e.getStatusCode());
        }

    }


    private void firebaseAuthWithGoogle(GoogleSignInAccount account) {
        AuthCredential authCredential = GoogleAuthProvider.getCredential(account.getIdToken(),null);
        mAuth.signInWithCredential(authCredential)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        hideProgressBar();
                    }
                });
    }
}