package com.ivione93.hojames;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;

public class AuthActivity extends AppCompatActivity {

    private static int GOOGLE_SIGN_IN = 100;

    ConstraintLayout loginLayout;
    EditText emailEditText, passwordEditText;
    Button btnSignIn, btnSignUp;
    SignInButton btnSignInGoogle;

    private FirebaseAnalytics mFirebaseAnalytics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);

        // Obtain the FirebaseAnalytics instance.
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        Bundle bundle = new Bundle();
        bundle.putString("message", "Integración de Firebase");
        mFirebaseAnalytics.logEvent("InitScreen", bundle);

        // Setup
        initReferences();
        session();
    }

    @Override
    protected void onStart() {
        super.onStart();
        loginLayout.setVisibility(View.VISIBLE);
    }

    private void initReferences() {
        getSupportActionBar().hide();

        loginLayout = findViewById(R.id.loginLayout);
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        btnSignIn = findViewById(R.id.btnSignIn);
        btnSignUp = findViewById(R.id.btnSignUp);
        btnSignInGoogle = findViewById(R.id.btnSignInGoogle);

        btnSignUp.setOnClickListener(v -> {
            if (!emailEditText.getText().toString().isEmpty() && !passwordEditText.getText().toString().isEmpty()) {
                FirebaseAuth.getInstance().createUserWithEmailAndPassword(emailEditText.getText().toString(),
                        passwordEditText.getText().toString()).addOnCompleteListener(it -> {
                    if (it.isSuccessful()) {
                        goProfile(it.getResult().getUser().getEmail());
                    } else {
                        showAlert();
                    }
                });
            }
        });

        btnSignIn.setOnClickListener(v -> {
            if (!emailEditText.getText().toString().isEmpty() && !passwordEditText.getText().toString().isEmpty()) {
                FirebaseAuth.getInstance().signInWithEmailAndPassword(emailEditText.getText().toString(),
                        passwordEditText.getText().toString()).addOnCompleteListener(it -> {
                    if (it.isSuccessful()) {
                        goProfile(it.getResult().getUser().getEmail());
                    } else {
                        showAlert();
                    }
                });
            }
        });

        btnSignInGoogle.setOnClickListener(v -> {
            GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(getString(R.string.default_web_client_id))
                    .requestEmail()
                    .build();

            GoogleSignInClient gClient = GoogleSignIn.getClient(this, gso);
            gClient.signOut();

            startActivityForResult(gClient.getSignInIntent(), GOOGLE_SIGN_IN);
        });
    }

    private void session() {
        SharedPreferences prefs = getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE);
        String email = prefs.getString("email", null);

        if (email != null) {
            loginLayout.setVisibility(View.INVISIBLE);
            goProfile(email);
        }
    }

    private void goProfile(String email) {
        Intent profileIntent = new Intent(this, MainActivity.class);
        profileIntent.putExtra("email", email);
        startActivity(profileIntent);

    }

    private void showAlert() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Error");
        builder.setMessage("Se ha producido un error en la autenticación");
        builder.create().show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GOOGLE_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);

                if (account != null) {
                    AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
                    FirebaseAuth.getInstance().signInWithCredential(credential).addOnCompleteListener(it -> {
                        if (it.isSuccessful()) {
                            goProfile(account.getEmail());
                        } else {
                            showAlert();
                        }
                    });
                }
            } catch (ApiException e) {
                showAlert();
            }
        }
    }
}