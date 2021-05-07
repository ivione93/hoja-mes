package com.ivione93.hojames.ui.profile;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.ivione93.hojames.AuthActivity;
import com.ivione93.hojames.R;

import java.util.HashMap;
import java.util.Map;

public class ProfileFragment extends Fragment {

    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    TextView emailTextView;
    TextView licenciaEditText, nombreEditText, birthEditText;

    Button btnGuardar;

    String email;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_profile, container, false);
        setHasOptionsMenu(true);

        // Setup
        Bundle bundle = getActivity().getIntent().getExtras();
        email = bundle.getString("email");
        initReferences(root, email);

        // Guardar datos
        SharedPreferences.Editor prefs = getActivity().getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE).edit();
        prefs.putString("email", email);
        prefs.apply();

        // Inflate the layout for this fragment
        return root;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.profile_menu, menu);
        super.onCreateOptionsMenu(menu,inflater);
    }

    @Override
    public void onStart() {
        db.collection("users").document(email).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    licenciaEditText.setText(task.getResult().get("license").toString());
                    nombreEditText.setText(task.getResult().get("name").toString() + " " + task.getResult().get("surname").toString());
                    birthEditText.setText(task.getResult().get("birth").toString());
                }
            }
        });
        /*db.collection("users").document(email).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                licenciaEditText.setText(task.getResult().get("license").toString());
                nombreEditText.setText(task.getResult().get("name").toString() + " " + task.getResult().get("surname").toString());
                birthEditText.setText(task.getResult().get("birth").toString());
            }
        });*/
        super.onStart();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.menu_log_out) {
            // Borrado datos inicio de sesion
            SharedPreferences.Editor prefs = getActivity().getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE).edit();
            prefs.clear();
            prefs.apply();

            FirebaseAuth.getInstance().signOut();

            Intent mainIntent = new Intent(getActivity().getApplicationContext(), AuthActivity.class);
            startActivity(mainIntent);
        }
        return super.onOptionsItemSelected(item);
    }

    private void initReferences(View root, String email) {

        emailTextView = root.findViewById(R.id.emailTextView);

        licenciaEditText = root.findViewById(R.id.licenciaEditText);
        nombreEditText = root.findViewById(R.id.nombreEditText);
        birthEditText = root.findViewById(R.id.birthEditText);

        btnGuardar = root.findViewById(R.id.btnGuardar);

        emailTextView.setText(email);

        btnGuardar.setOnClickListener(v -> {
            Map<String,Object> user = new HashMap<>();
            user.put("email", email);
            user.put("license", licenciaEditText.getText().toString());
            user.put("name", nombreEditText.getText().toString());
            user.put("birth", birthEditText.getText().toString());

            db.collection("users").document(email).set(user);
        });
    }
}