package com.ivione93.hojames.ui.login;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.ivione93.hojames.R;

public class ForgotPasswordActivity extends AppCompatActivity {

    EditText emailEditText;
    Button btnRecuperar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        setup();
    }

    private void setup() {
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        emailEditText = findViewById(R.id.emailRecuEditText);
        btnRecuperar = findViewById(R.id.btnRecuperar);

        btnRecuperar.setOnClickListener(v -> {
            validate();
        });
    }

    public void validate() {
        String email = emailEditText.getText().toString().trim();

        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailEditText.setError("Correo inválido");
            return;
        }
        
        sendEmail(email);
    }

    private void sendEmail(String email) {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        auth.sendPasswordResetEmail(email).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(this, "Correo enviado!", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(this, AuthActivity.class);
                startActivity(intent);
            } else {
                Toast.makeText(this, "Correo inválido!", Toast.LENGTH_LONG).show();
            }
        });
    }
}