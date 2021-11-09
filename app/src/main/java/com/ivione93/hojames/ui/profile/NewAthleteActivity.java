package com.ivione93.hojames.ui.profile;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Patterns;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.ivione93.hojames.MainActivity;
import com.ivione93.hojames.R;
import com.ivione93.hojames.Utils;

import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class NewAthleteActivity extends AppCompatActivity {

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseAnalytics mFirebaseAnalytics;

    ConstraintLayout newAthleteLayout;
    TextView nombreEditText, apellidosEditText, birthEditText, emailEditText, passwordEditText, policiesUrl;
    CheckBox checkPolicies;

    String email;
    String date = Utils.toString(new Date());

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
        getSupportActionBar().hide();
        newAthleteLayout.setVisibility(View.INVISIBLE);
        if (email != null) {
            db.collection("athlete").document(email).get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        //Evento inicio sesion Analytics
                        Bundle bundle = new Bundle();
                        bundle.putString("message", "Inicio de sesion");
                        bundle.putString("user", email);
                        mFirebaseAnalytics.logEvent("login", bundle);
                        goProfile(email);
                    } else {
                        getSupportActionBar().show();
                        newAthleteLayout.setVisibility(View.VISIBLE);
                        emailEditText.setText(email);
                        emailEditText.setEnabled(false);
                    }
                }
            });
        } else {
            getSupportActionBar().show();
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
            try {
                saveAthlete();
            } catch (ParseException e) {
                e.printStackTrace();
            }
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
        prefs.remove("email");
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
        checkPolicies = findViewById(R.id.checkPolicies);

        policiesUrl = findViewById(R.id.policiesUrl);
        policiesUrl.setOnClickListener(v -> {
            //Evento lectura de condiciones de uso Analytics
            mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
            Bundle bundle = new Bundle();
            bundle.putString("message", "Lectura de condiciones de uso");
            mFirebaseAnalytics.logEvent("visit_policies", bundle);

            String url = "https://ivione93.github.io/hoja-mes-policies/";
            Uri uri = Uri.parse(url);
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(uri);
            startActivity(intent);
        });
    }

    private void goProfile(String email) {
        Intent profileIntent = new Intent(this, MainActivity.class);
        profileIntent.putExtra("email", email);
        startActivity(profileIntent);
        finish();
    }

    private void saveAthlete() throws ParseException {
        String name = nombreEditText.getText().toString();
        String surname = apellidosEditText.getText().toString();
        String birthdate = birthEditText.getText().toString();
        String email = emailEditText.getText().toString();
        String password = passwordEditText.getText().toString();
        Boolean isChecked = checkPolicies.isChecked();

        if (validateNewAthlete(name, surname, birthdate, email, password, isChecked)) {
            Map<String,Object> athlete = new HashMap<>();
            athlete.put("email", email);
            athlete.put("name", nombreEditText.getText().toString());
            athlete.put("surname", apellidosEditText.getText().toString());
            athlete.put("birth", birthEditText.getText().toString());
            athlete.put("creationDate", Utils.toTimestamp(date));

            if (this.email != null) {
                FirebaseAuth.getInstance().getCurrentUser().delete();
            }

            FirebaseAuth.getInstance().createUserWithEmailAndPassword(emailEditText.getText().toString(),
                    passwordEditText.getText().toString()).addOnCompleteListener(it -> {
                if (it.isSuccessful()) {
                    db.collection("athlete").document(email).set(athlete);
                    //Evento registro usuario Analytics
                    Bundle bundle = new Bundle();
                    bundle.putString("message", "Registro de usuario");
                    bundle.putString("user", email);
                    mFirebaseAnalytics.logEvent("signup", bundle);
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

    private boolean validateNewAthlete(String name, String surname, String birthdate, String email, String password, Boolean isCheked) {
        boolean isValid = true;
        if (name.isEmpty() || name == null) {
            nombreEditText.setError("El nombre es obligatorio");
            isValid = false;
        } else {
            nombreEditText.setError(null);
        }
        if (surname.isEmpty() || surname == null) {
            apellidosEditText.setError("Los apellidos es obligatorio");
            isValid = false;
        } else {
            apellidosEditText.setError(null);
        }
        if (birthdate.isEmpty() || birthdate == null) {
            birthEditText.setError("La fecha de nacimiento es obligatorio");
            isValid = false;
        } else {
            birthEditText.setError(null);
        }
        if (email.isEmpty() || email == null) {
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                emailEditText.setError("Correo inválido");
            }
            emailEditText.setError("El email es obligatorio");
            isValid = false;
        } else {
            emailEditText.setError(null);
        }
        if (password.isEmpty() || password == null) {
            passwordEditText.setError("La contraseña es obligatoria");
            isValid = false;
        } else {
            passwordEditText.setError(null);
        }
        if (isCheked) {
            checkPolicies.setError(null);
        } else {
            checkPolicies.setError("Tiene que aceptar las condiciones de uso");
            isValid = false;
        }
        return isValid;
    }
}