package com.ivione93.hojames.ui.profile;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;
import com.ivione93.hojames.MainActivity;
import com.ivione93.hojames.R;
import com.ivione93.hojames.Utils;
import com.ivione93.hojames.ui.login.AuthActivity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class EditProfileActivity extends AppCompatActivity {

    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    CircleImageView photoEditProfile;
    TextView emailEditProfile;
    TextInputLayout nameEditProfile, surnameEditProfile;
    EditText birthEditProfile;
    Button btnDeleteUser;

    String email;
    Uri photoUrl;

    private ProgressDialog progressDialog;

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
        nameEditProfile = findViewById(R.id.nameEditProfile);
        surnameEditProfile = findViewById(R.id.surnameEditProfile);
        birthEditProfile = findViewById(R.id.birthEditProfile);
        btnDeleteUser = findViewById(R.id.btnDeleteUser);

        btnDeleteUser.setOnClickListener(v -> {
            AlertDialog.Builder deleteConfirm = new AlertDialog.Builder(this);
            deleteConfirm.setTitle("Eliminar usuario");
            deleteConfirm.setMessage("¿Está seguro que quiere eliminar el usuario?");
            deleteConfirm.setCancelable(false);
            deleteConfirm.setPositiveButton("Aceptar", (dialog, which) -> {
                deleteUser();
                Intent auth = new Intent(this, AuthActivity.class);
                startActivity(auth);
            });
            deleteConfirm.setNegativeButton("Cancelar", (dialog, which) -> finish());
            deleteConfirm.show();
        });
    }

    private void deleteUser() {
        deleteProfileInformation(email);

        //deleteProfileAuthentication();

        // Borrado datos inicio de sesion
        SharedPreferences.Editor prefs = getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE).edit();
        prefs.clear();
        prefs.apply();
    }

    private void deleteProfileInformation(String email) {
        // Eliminar entrenamientos e hijos
        db.collection("trainings").whereEqualTo("email", email).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot training : task.getResult()) {
                    db.collection("trainings").document(training.getId()).delete();
                    // Eliminar series
                    db.collection("series").whereEqualTo("idTraining", training.getId()).get().addOnCompleteListener(task1 -> {
                        if (task1.isSuccessful()) {
                            for (QueryDocumentSnapshot serie : task1.getResult()) {
                                db.collection("series").document(serie.getId()).delete().addOnCompleteListener(task2 -> {
                                    Log.d("DEL", "Series eliminadas");
                                });
                            }
                        }
                    });
                    // Eliminar cuestas
                    db.collection("cuestas").whereEqualTo("idTraining", training.getId()).get().addOnCompleteListener(task1 -> {
                        if (task1.isSuccessful()) {
                            for (QueryDocumentSnapshot cuesta : task1.getResult()) {
                                db.collection("cuestas").document(cuesta.getId()).delete().addOnCompleteListener(task2 -> {
                                    Log.d("DEL", "Cuestas eliminadas");
                                });
                            }
                        }
                    });
                    // Eliminar fartlek
                    db.collection("fartlek").whereEqualTo("idTraining", training.getId()).get().addOnCompleteListener(task1 -> {
                        if (task1.isSuccessful()) {
                            for (QueryDocumentSnapshot fartlek : task1.getResult()) {
                                db.collection("fartlek").document(fartlek.getId()).delete().addOnCompleteListener(task2 -> {
                                    Log.d("DEL", "Fartlek eliminados");
                                });
                            }
                        }
                    });
                    // Eliminar gym
                    db.collection("gym").whereEqualTo("idTraining", training.getId()).get().addOnCompleteListener(task1 -> {
                        if (task1.isSuccessful()) {
                            for (QueryDocumentSnapshot gym : task1.getResult()) {
                                db.collection("gym").document(gym.getId()).delete().addOnCompleteListener(task2 -> {
                                    Log.d("DEL", "Gym eliminados");
                                });
                            }
                        }
                    });
                }
            }
        });

        // Eliminar entrenamientos
        db.collection("trainings").whereEqualTo("email", email).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot doc : task.getResult()) {
                    db.collection("trainings").document(doc.getId()).delete().addOnSuccessListener(aVoid -> {
                        Log.d("DEL", "Entrenamientos eliminados");
                        Toast.makeText(EditProfileActivity.this, "Entrenamientos eliminados", Toast.LENGTH_LONG).show();
                    });
                }
            }
        });

        // Eliminar competiciones
        db.collection("competitions").whereEqualTo("email", email).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot doc : task.getResult()) {
                    db.collection("competitions").document(doc.getId()).delete().addOnSuccessListener(aVoid -> {
                        Log.d("DEL", "Competiciones eliminadas");
                        Toast.makeText(EditProfileActivity.this, "Competiciones eliminadas", Toast.LENGTH_LONG).show();
                    });
                }
            }
        });

        // Eliminar atleta (athlete)
        db.collection("athlete").document(email).delete().addOnCompleteListener(task2 -> {
            Log.d("DEL", "Atleta eliminado");
        });
    }

    private void deleteProfileAuthentication() {
        // Eliminar cuenta autenticada
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        user.delete().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Log.d("DEL", "Cuenta de usuario eliminada");
                Toast.makeText(EditProfileActivity.this, "Usuario eliminado", Toast.LENGTH_LONG).show();
            }
        });
        FirebaseAuth.getInstance().signOut();
    }

    private void loadAthlete(String email) {
        db.collection("athlete").document(email).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                if (photoUrl != null) {
                    Glide.with(this).load(photoUrl).into(photoEditProfile);
                }
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    emailEditProfile.setText(task.getResult().get("email").toString());
                    nameEditProfile.getEditText().setText(task.getResult().get("name").toString());
                    surnameEditProfile.getEditText().setText(task.getResult().get("surname").toString());
                    birthEditProfile.setText(task.getResult().get("birth").toString());
                }
            }
        });
    }

    private void saveEditProfile() {
        String editName, editSurname, editBirth;
        editName = nameEditProfile.getEditText().getText().toString();
        editSurname = surnameEditProfile.getEditText().getText().toString();
        editBirth = birthEditProfile.getText().toString();

        if (validateEditProfile(editName, editSurname, editBirth)) {
            if (Utils.validateDateFormat(editBirth)) {
                Map<String,Object> user = new HashMap<>();
                user.put("email", email);
                user.put("name", editName);
                user.put("surname", editSurname);
                user.put("birth", editBirth);

                db.collection("athlete").document(email).set(user);

                Intent intent = new Intent(this, MainActivity.class);
                intent.putExtra("email", email);
                startActivity(intent);
            } else {
                Toast toast = Toast.makeText(getApplicationContext(), "Formato de fecha incorrecto", Toast.LENGTH_LONG);
                toast.show();
            }
        } else {
            Toast toast = Toast.makeText(getApplicationContext(), "Faltan campos por completar", Toast.LENGTH_LONG);
            toast.show();
        }
    }

    private boolean validateEditProfile(String name, String surname, String birthdate) {
        boolean isValid = true;
        if(name.isEmpty() || name == null) {
            nameEditProfile.setError("El nombre es obligatorio");
            isValid = false;
        } else {
            nameEditProfile.setError(null);
        }
        if(surname.isEmpty() || surname == null) {
            surnameEditProfile.setError("Los apellidos son obligatorios");
            isValid = false;
        } else {
            surnameEditProfile.setError(null);
        }
        if(birthdate.isEmpty() || birthdate == null) {
            birthEditProfile.setError("La fecha de nacimiento es obligatoria");
            isValid = false;
        } else {
            birthEditProfile.setError(null);
        }
        return isValid;
    }
}