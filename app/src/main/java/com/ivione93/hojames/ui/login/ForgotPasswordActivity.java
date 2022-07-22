package com.ivione93.hojames.ui.login;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.ivione93.hojames.R;

public class ForgotPasswordActivity extends AppCompatActivity {

    private FirebaseAnalytics mFirebaseAnalytics;

    EditText emailEditText;
    Button btnRecuperar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        setup();
    }

    private void setup() {
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        emailEditText = findViewById(R.id.emailRecuEditText);
        btnRecuperar = findViewById(R.id.btnRecuperar);

        btnRecuperar.setOnClickListener(v -> validate());
    }

    public void validate() {
        String email = emailEditText.getText().toString().trim();

        if (!email.isEmpty()) {
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                emailEditText.setError(getString(R.string.email_invalid));
                return;
            }
        } else {
            emailEditText.setError(getString(R.string.email_mandatory));
            return;
        }
        
        sendEmail(email);
    }

    private void sendEmail(String email) {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        auth.sendPasswordResetEmail(email).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(this, getString(R.string.email_sent), Toast.LENGTH_LONG).show();
                //Evento recuperar contraseña Analytics
                Bundle bundle = new Bundle();
                bundle.putString("message", "Recuperar contraseña");
                bundle.putString("user", email);
                mFirebaseAnalytics.logEvent("forgot_password", bundle);

                Intent intent = new Intent(this, AuthActivity.class);
                startActivity(intent);
            } else {
                Toast.makeText(this, getString(R.string.email_invalid), Toast.LENGTH_LONG).show();
            }
        });
    }
}