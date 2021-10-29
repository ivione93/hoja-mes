package com.ivione93.hojames.ui.profile;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Patterns;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.ivione93.hojames.MainActivity;
import com.ivione93.hojames.R;

import java.util.HashMap;
import java.util.Map;

public class NewAthleteActivity extends AppCompatActivity {

    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    ConstraintLayout newAthleteLayout;
    TextView nombreEditText, apellidosEditText, birthEditText, emailEditText, passwordEditText;

    String email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_athlete);

        // Setup
        Bundle bundle = getIntent().getExtras();
        email = bundle.getString("email");
        setup();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (email != null) {
            db.collection("athlete").document(email).get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        goProfile(email);
                    }
                }
            });
            newAthleteLayout.setVisibility(View.VISIBLE);
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.new_athlete_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.menu_new_athlete) {
            saveAthlete();
            return true;
        }
        if (item.getItemId() == R.id.menu_cancel_new_athlete) {
            cancelNewAthlete();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        cancelNewAthlete();
    }

    private void cancelNewAthlete() {
        // Borrado datos inicio de sesion
        SharedPreferences.Editor prefs = getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE).edit();
        prefs.clear();
        prefs.apply();

        if (email != null) {
            FirebaseAuth.getInstance().getCurrentUser().delete();
            FirebaseAuth.getInstance().signOut();
        }
        finish();
    }

    private void setup() {
        newAthleteLayout = findViewById(R.id.newAthleteLayout);

        nombreEditText = findViewById(R.id.nombreEditText);
        apellidosEditText = findViewById(R.id.apellidosEditText);
        birthEditText = findViewById(R.id.birthEditText);
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
    }

    private void goProfile(String email) {
        Intent profileIntent = new Intent(this, MainActivity.class);
        profileIntent.putExtra("email", email);
        startActivity(profileIntent);
    }

    private void saveAthlete() {
        String name = nombreEditText.getText().toString();
        String surname = apellidosEditText.getText().toString();
        String birthdate = birthEditText.getText().toString();
        String email = emailEditText.getText().toString();
        String password = passwordEditText.getText().toString();

        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailEditText.setError("Correo inválido");
            return;
        }
        if (validateNewAthlete(name, surname, birthdate, email, password)) {
            Map<String,Object> user = new HashMap<>();
            user.put("email", email);
            user.put("name", nombreEditText.getText().toString());
            user.put("surname", apellidosEditText.getText().toString());
            user.put("birth", birthEditText.getText().toString());

            db.collection("athlete").document(email).set(user);

            FirebaseAuth.getInstance().createUserWithEmailAndPassword(emailEditText.getText().toString(),
                    passwordEditText.getText().toString()).addOnCompleteListener(it -> {
                if (it.isSuccessful()) {
                    goProfile(email);
                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("Error");
                    builder.setMessage("Ya existe un registro con ese email");
                    builder.create().show();
                }
            });
        } else {
            Toast toast = Toast.makeText(getApplicationContext(), "Todos los campos son obligatorios", Toast.LENGTH_LONG);
            toast.show();
        }
    }

    private boolean validateNewAthlete(String name, String surname, String birthdate, String email, String password) {
        boolean isValid = true;
        if (name.isEmpty() || name == null) {
            isValid = false;
        }
        if (surname.isEmpty() || surname == null) {
            isValid = false;
        }
        if (birthdate.isEmpty() || birthdate == null) {
            isValid = false;
        }
        if (email.isEmpty() || email == null) {
            isValid = false;
        }
        if (password.isEmpty() || password == null) {
            isValid = false;
        }
        return isValid;
    }
}