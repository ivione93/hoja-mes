package com.ivione93.hojames;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity {

    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    TextView emailTextView;
    EditText licenciaEditText, nombreEditText, apellidosEditText, birthEditText;
    Button btnSignOut;

    Button btnGuardar, btnRecuperar, btnEliminar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Setup
        Bundle bundle = getIntent().getExtras();
        String email = bundle.getString("email");
        initReferences(email);

        // Guardar datos
        SharedPreferences.Editor prefs = getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE).edit();
        prefs.putString("email", email);
        prefs.apply();
    }

    private void initReferences(String email) {
        getSupportActionBar().setTitle("Perfil");

        emailTextView = findViewById(R.id.emailTextView);

        licenciaEditText = findViewById(R.id.licenciaEditText);
        nombreEditText = findViewById(R.id.nombreEditText);
        apellidosEditText = findViewById(R.id.apellidosEditText);
        birthEditText = findViewById(R.id.birthEditText);

        btnSignOut = findViewById(R.id.btnSignOut);
        btnGuardar = findViewById(R.id.btnGuardar);
        btnRecuperar = findViewById(R.id.btnRecuperar);
        btnEliminar = findViewById(R.id.btnEliminar);

        emailTextView.setText(email);

        btnSignOut.setOnClickListener(v -> {
            // Borrado de datos
            SharedPreferences.Editor prefs = getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE).edit();
            prefs.clear();
            prefs.apply();

            FirebaseAuth.getInstance().signOut();
            onBackPressed();
        });

        btnGuardar.setOnClickListener(v -> {
            Map<String,Object> user = new HashMap<>();
            user.put("email", email);
            user.put("license", licenciaEditText.getText().toString());
            user.put("name", nombreEditText.getText().toString());
            user.put("surname", apellidosEditText.getText().toString());
            user.put("birth", birthEditText.getText().toString());

            db.collection("users").document(email).set(user);
        });

        btnRecuperar.setOnClickListener(v -> {
            db.collection("users").document(email).get().addOnSuccessListener(it -> {
                licenciaEditText.setText(it.get("license").toString());
                nombreEditText.setText(it.get("name").toString());
                apellidosEditText.setText(it.get("surname").toString());
                birthEditText.setText(it.get("birth").toString());
            });
        });

        btnEliminar.setOnClickListener(v -> {
            db.collection("users").document(email).delete();
        });
    }
}