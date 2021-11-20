package com.ivione93.hojames.ui.login;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

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
import com.ivione93.hojames.MainActivity;
import com.ivione93.hojames.R;
import com.ivione93.hojames.ui.profile.NewAthleteActivity;

public class AuthActivity extends AppCompatActivity {

    SharedPreferences.Editor prefs;

    private static int GOOGLE_SIGN_IN = 100;
    private FirebaseAnalytics mFirebaseAnalytics;

    ConstraintLayout loginLayout;
    EditText emailEditText, passwordEditText;
    TextView forgotPassword, createdBy;
    Button btnSignIn, btnSignUp;
    SignInButton btnSignInGoogle;

    String email;

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
        session();
        setup();
    }

    @Override
    protected void onStart() {
        super.onStart();
        loginLayout.setVisibility(View.VISIBLE);
    }

    private void setup() {
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        // Guardar pantalla de informacion
        prefs = getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE).edit();
        prefs.putBoolean("showInformation", false);
        prefs.apply();

        getSupportActionBar().hide();

        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        forgotPassword = findViewById(R.id.forgotPassword);
        btnSignIn = findViewById(R.id.btnSignIn);
        btnSignUp = findViewById(R.id.btnSignUp);
        btnSignInGoogle = findViewById(R.id.btnSignInGoogle);

        forgotPassword.setOnClickListener(v -> {
            Intent intent = new Intent(this, ForgotPasswordActivity.class);
            startActivity(intent);
        });

        btnSignUp.setOnClickListener(v -> goNewAthlete(null));

        btnSignIn.setOnClickListener(v -> {
            if (!emailEditText.getText().toString().isEmpty() && !passwordEditText.getText().toString().isEmpty()) {
                String emailUser = emailEditText.getText().toString().trim();
                if (emailUser.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(emailUser).matches()) {
                    emailEditText.setError(getString(R.string.email_invalid));
                    return;
                }
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

        createdBy = findViewById(R.id.createdBy);
        createdBy.setOnClickListener(v -> {
            //Evento ver perfil de linkedin Analytics
            Bundle bundle = new Bundle();
            bundle.putString("message", "Ver perfil Linkedin");
            bundle.putString("user", email);
            mFirebaseAnalytics.logEvent("visit_linkedin", bundle);

            String url = "https://ivione93.github.io/cv-online/";
            Uri uri = Uri.parse(url);
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(uri);
            startActivity(intent);
        });
    }

    private void session() {
        loginLayout = findViewById(R.id.loginLayout);

        SharedPreferences prefs = getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE);
        email = prefs.getString("email", null);

        if (email != null) {
            loginLayout.setVisibility(View.INVISIBLE);
            //Evento inicio sesion Analytics
            Bundle bundle = new Bundle();
            bundle.putString("message", "Inicio de sesion");
            bundle.putString("user", email);
            mFirebaseAnalytics.logEvent("login", bundle);
            goProfile(email);
        }
    }

    private void goProfile(String email) {
        Intent profileIntent = new Intent(this, MainActivity.class);
        profileIntent.putExtra("email", email);
        startActivity(profileIntent);
        finish();
    }

    private void goNewAthlete(String email) {
        Intent newAthlete = new Intent(this, NewAthleteActivity.class);
        newAthlete.putExtra("email", email);
        startActivity(newAthlete);
    }

    private void showAlert() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.error_title);
        builder.setMessage(R.string.error_message);
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
                            goNewAthlete(account.getEmail());
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