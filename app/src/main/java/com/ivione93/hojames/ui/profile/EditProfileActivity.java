package com.ivione93.hojames.ui.profile;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.ivione93.hojames.MainActivity;
import com.ivione93.hojames.R;
import com.ivione93.hojames.Utils;

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class EditProfileActivity extends AppCompatActivity {

    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    CircleImageView photoEditProfile;
    TextView emailEditProfile, licenseEditProfile;
    TextInputLayout nameEditProfile, surnameEditProfile;
    EditText birthEditProfile;

    String email;
    Uri photoUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        setup();

        email = getIntent().getStringExtra("email");
        if (getIntent().getStringExtra("photoUrl") != null) {
            photoUrl = Uri.parse(getIntent().getStringExtra("photoUrl"));
        }
        loadAthlete(email);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.edit_profile_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            getIntent().putExtra("email", email);
            onBackPressed();
            return true;
        }
        if (item.getItemId() == R.id.menu_save_edit_profile) {
            saveEditProfile();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setup() {
        photoEditProfile = findViewById(R.id.photoEditProfile);
        emailEditProfile = findViewById(R.id.emailEditProfile);
        licenseEditProfile = findViewById(R.id.licenseEditProfile);
        nameEditProfile = findViewById(R.id.nameEditProfile);
        surnameEditProfile = findViewById(R.id.surnameEditProfile);
        birthEditProfile = findViewById(R.id.birthEditProfile);
    }

    private void loadAthlete(String license) {
        db.collection("athlete").document(email).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                if (photoUrl != null) {
                    Glide.with(this).load(photoUrl).into(photoEditProfile);
                }
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    emailEditProfile.setText(task.getResult().get("email").toString());
                    licenseEditProfile.setText(task.getResult().get("license").toString());
                    nameEditProfile.getEditText().setText(task.getResult().get("name").toString());
                    surnameEditProfile.getEditText().setText(task.getResult().get("surname").toString());
                    birthEditProfile.setText(task.getResult().get("birth").toString());
                }
            }
        });
    }

    private void saveEditProfile() {
        String editName, editSurname, editBirth, editLicense;
        editName = nameEditProfile.getEditText().getText().toString();
        editSurname = surnameEditProfile.getEditText().getText().toString();
        editBirth = birthEditProfile.getText().toString();
        editLicense = licenseEditProfile.getText().toString();

        if (validateEditProfile(editName, editSurname, editBirth)) {
            if (Utils.validateDateFormat(editBirth)) {
                Map<String,Object> user = new HashMap<>();
                user.put("email", email);
                user.put("license", editLicense);
                user.put("name", editName);
                user.put("surname", editSurname);
                user.put("birth", editBirth);

                db.collection("athlete").document(email).set(user);

                //db.athleteDao().update(editName, editSurname, Utils.toDate(editBirth), license);
            } else {
                Toast toast = Toast.makeText(getApplicationContext(), "Formato de fecha incorrecto", Toast.LENGTH_LONG);
                toast.show();
            }
        } else {
            Toast toast = Toast.makeText(getApplicationContext(), "Faltan campos por completar", Toast.LENGTH_LONG);
            toast.show();
        }

        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("email", email);
        startActivity(intent);
    }

    private boolean validateEditProfile(String name, String surname, String birthdate) {
        boolean isValid = true;
        if(name.isEmpty() || name == null) {
            isValid = false;
        }
        if(surname.isEmpty() || surname == null) {
            isValid = false;
        }
        if(birthdate.isEmpty() || birthdate == null) {
            isValid = false;
        }
        return isValid;
    }
}