package com.ivione93.hojames.ui.profile;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.chip.Chip;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.DateValidatorPointBackward;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.ivione93.hojames.MainActivity;
import com.ivione93.hojames.R;
import com.ivione93.hojames.Utils;
import com.ivione93.hojames.ui.login.AuthActivity;

import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;

import de.hdodenhof.circleimageview.CircleImageView;

public class EditProfileActivity extends AppCompatActivity {

    private FirebaseAnalytics mFirebaseAnalytics;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    CircleImageView photoEditProfile;
    Chip emailEditProfile;
    TextInputLayout nameEditProfile, surnameEditProfile;
    EditText birthEditProfile;
    ImageButton btnOptions;

    String email;
    Uri photoUrl;

    private ProgressDialog progressDialog;

    @RequiresApi(api = Build.VERSION_CODES.N)
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
    public void onBackPressed() {
        android.app.AlertDialog.Builder cancelEditProfile = new android.app.AlertDialog.Builder(this);
        cancelEditProfile.setTitle(R.string.exit_title);
        cancelEditProfile.setMessage(R.string.exit_message);
        cancelEditProfile.setPositiveButton(R.string.exit, (dialog, which) -> {
            super.onBackPressed();
        });
        cancelEditProfile.setNegativeButton(R.string.cancel, (dialog, which) -> {
            dialog.dismiss();
        });
        cancelEditProfile.show();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            getIntent().putExtra("email", email);
            onBackPressed();
            return true;
        }
        if (item.getItemId() == R.id.menu_save_edit_profile) {
            try {
                saveEditProfile();
            } catch (ParseException e) {
                e.printStackTrace();
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setup() {
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        progressDialog = new ProgressDialog(this);
        photoEditProfile = findViewById(R.id.photoEditProfile);
        emailEditProfile = findViewById(R.id.emailEditProfile);
        nameEditProfile = findViewById(R.id.nameEditProfile);
        surnameEditProfile = findViewById(R.id.surnameEditProfile);
        // Material Date Picker
        CalendarConstraints.Builder constraints = new CalendarConstraints.Builder();
        constraints.setValidator(DateValidatorPointBackward.now());
        MaterialDatePicker.Builder builder = MaterialDatePicker.Builder.datePicker();
        builder.setTitleText(R.string.select_date);
        builder.setCalendarConstraints(constraints.build());
        MaterialDatePicker datePicker = builder.build();
        birthEditProfile = findViewById(R.id.birthEditProfile);
        birthEditProfile.setOnClickListener(v -> datePicker.show(getSupportFragmentManager(), "DATE_PICKER"));
        datePicker.addOnPositiveButtonClickListener(selection -> birthEditProfile.setText(datePicker.getHeaderText()));

        btnOptions = findViewById(R.id.btnOptions);

        btnOptions.setOnClickListener(v -> {
            showBottomSheetDialog();
        });
    }

    private void showBottomSheetDialog() {

        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        bottomSheetDialog.setContentView(R.layout.bottom_sheet_edit_profile);

        LinearLayout delete = bottomSheetDialog.findViewById(R.id.deleteLinearLayout);
        LinearLayout cancel = bottomSheetDialog.findViewById(R.id.cancelLinearLayout);

        bottomSheetDialog.show();

        delete.setOnClickListener(v -> {
            AlertDialog.Builder deleteDataUser = new AlertDialog.Builder(this);
            deleteDataUser.setTitle(R.string.delete_user_data);
            deleteDataUser.setMessage(R.string.delete_user_data_confirm);
            deleteDataUser.setCancelable(false);
            deleteDataUser.setPositiveButton(R.string.accept, (dialog, which) -> {
                deleteProfileInformation(email);
                AlertDialog.Builder deleteUser = new AlertDialog.Builder(this);
                deleteUser.setTitle(R.string.delete_user);
                deleteUser.setMessage(R.string.delete_user_confirm);
                deleteUser.setCancelable(false);
                deleteUser.setPositiveButton(R.string.delete, (dialog1, which1) -> {
                    try {
                        Thread.sleep(15000);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    deleteUser();
                    //Evento eliminacion usuario Analytics
                    Bundle bundle = new Bundle();
                    bundle.putString("message", "Eliminación cuenta de usuario");
                    bundle.putString("user", email);
                    mFirebaseAnalytics.logEvent("delete_user", bundle);
                    Intent auth = new Intent(this, AuthActivity.class);
                    startActivity(auth);
                });
                deleteUser.show();
            });
            deleteDataUser.setNegativeButton(R.string.cancel, (dialog, which) -> bottomSheetDialog.dismiss());
            deleteDataUser.show();

        });

        cancel.setOnClickListener(v -> bottomSheetDialog.dismiss());
    }

    private void deleteUser() {
        deleteProfileAuthentication();

        // Borrado datos inicio de sesion
        SharedPreferences.Editor prefs = getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE).edit();
        prefs.remove("email");
        prefs.apply();
    }

    private boolean deleteProfileInformation(String email) {
        // Eliminar entrenamientos e hijos
        AtomicBoolean deletedSeries = new AtomicBoolean(false);
        AtomicBoolean deletedCuestas = new AtomicBoolean(false);
        AtomicBoolean deletedGym = new AtomicBoolean(false);
        AtomicBoolean deletedFartlek = new AtomicBoolean(false);
        db.collection("trainings").whereEqualTo("email", email).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot training : task.getResult()) {
                    // Eliminar series
                    db.collection("series").whereEqualTo("idTraining", training.getId()).get().addOnCompleteListener(task1 -> {
                        if (task1.isSuccessful()) {
                            for (QueryDocumentSnapshot serie : task1.getResult()) {
                                db.collection("series").document(serie.getId()).delete().addOnCompleteListener(task2 -> {
                                    Log.d("DEL", "Series eliminadas");
                                    deletedSeries.set(true);
                                });
                            }
                        } else {
                            deletedSeries.set(true);
                        }
                    });
                    // Eliminar cuestas
                    db.collection("cuestas").whereEqualTo("idTraining", training.getId()).get().addOnCompleteListener(task1 -> {
                        if (task1.isSuccessful()) {
                            for (QueryDocumentSnapshot cuesta : task1.getResult()) {
                                db.collection("cuestas").document(cuesta.getId()).delete().addOnCompleteListener(task2 -> {
                                    Log.d("DEL", "Cuestas eliminadas");
                                    deletedCuestas.set(true);
                                });
                            }
                        } else {
                            deletedCuestas.set(true);
                        }
                    });
                    // Eliminar fartlek
                    db.collection("fartlek").whereEqualTo("idTraining", training.getId()).get().addOnCompleteListener(task1 -> {
                        if (task1.isSuccessful()) {
                            for (QueryDocumentSnapshot fartlek : task1.getResult()) {
                                db.collection("fartlek").document(fartlek.getId()).delete().addOnCompleteListener(task2 -> {
                                    Log.d("DEL", "Fartlek eliminados");
                                    deletedFartlek.set(true);
                                });
                            }
                        } else {
                            deletedFartlek.set(true);
                        }
                    });
                    // Eliminar gym
                    db.collection("gym").whereEqualTo("idTraining", training.getId()).get().addOnCompleteListener(task1 -> {
                        if (task1.isSuccessful()) {
                            for (QueryDocumentSnapshot gym : task1.getResult()) {
                                db.collection("gym").document(gym.getId()).delete().addOnCompleteListener(task2 -> {
                                    Log.d("DEL", "Gym eliminados");
                                    deletedGym.set(true);
                                });
                            }
                        } else {
                            deletedGym.set(true);
                        }
                    });
                }
            }
        });

        // Eliminar entrenamientos
        AtomicBoolean deletedTrainings = new AtomicBoolean(false);
        db.collection("trainings").whereEqualTo("email", email).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot doc : task.getResult()) {
                    db.collection("trainings").document(doc.getId()).delete().addOnSuccessListener(aVoid -> {
                        Log.d("DEL", "Entrenamientos eliminados");
                        Toast.makeText(EditProfileActivity.this, "Entrenamientos eliminados", Toast.LENGTH_LONG).show();
                        deletedTrainings.set(true);
                    });
                }
            } else {
                deletedTrainings.set(true);
            }
        });

        // Eliminar competiciones
        AtomicBoolean deletedCompetitions = new AtomicBoolean(false);
        db.collection("competitions").whereEqualTo("email", email).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot doc : task.getResult()) {
                    db.collection("competitions").document(doc.getId()).delete().addOnSuccessListener(aVoid -> {
                        Log.d("DEL", "Competiciones eliminadas");
                        Toast.makeText(EditProfileActivity.this, "Competiciones eliminadas", Toast.LENGTH_LONG).show();
                        deletedCompetitions.set(true);
                    });
                }
            } else {
                deletedCompetitions.set(true);
            }
        });

        // Eliminar atleta (athlete)
        AtomicBoolean deletedAthlete = new AtomicBoolean(false);
        db.collection("athlete").document(email).delete().addOnCompleteListener(task2 -> {
            Log.d("DEL", "Atleta eliminado");
            Toast.makeText(EditProfileActivity.this, "Atleta eliminado", Toast.LENGTH_LONG).show();
            deletedAthlete.set(true);
        });

        return deletedCompetitions.get() && deletedAthlete.get() && deletedTrainings.get() &&
                deletedCuestas.get() && deletedGym.get() && deletedFartlek.get() && deletedSeries.get();
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

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void loadAthlete(String email) {
        progressDialog.setMessage("Cargando datos...");
        progressDialog.show();
        db.collection("athlete").document(email).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                progressDialog.dismiss();
                if (photoUrl != null) {
                    Glide.with(this).load(photoUrl).into(photoEditProfile);
                }
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    emailEditProfile.setText(task.getResult().get("email").toString());
                    nameEditProfile.getEditText().setText(task.getResult().get("name").toString());
                    surnameEditProfile.getEditText().setText(task.getResult().get("surname").toString());
                    if (validateDate(task.getResult().get("birth").toString())) {
                        birthEditProfile.setText(Utils.selectDateCalendarToString(task.getResult().get("birth").toString(), getString(R.string.format_date)));
                    } else {
                        birthEditProfile.setText(task.getResult().get("birth").toString());
                    }
                } else {
                    progressDialog.dismiss();
                }
            } else {
                progressDialog.dismiss();
            }
        });
    }

    public static boolean validateDate(String date) {
        String formatDate = "\\d{2}/\\d{2}/\\d{4}";
        return Pattern.matches(formatDate, date);
    }

    private void saveEditProfile() throws ParseException {
        String editName, editSurname, editBirth;
        editName = nameEditProfile.getEditText().getText().toString();
        editSurname = surnameEditProfile.getEditText().getText().toString();
        editBirth = Utils.toString(Utils.toTimestamp(birthEditProfile.getText().toString(), getString(R.string.format_date)), getString(R.string.format_date));

        if (validateEditProfile(editName, editSurname, editBirth)) {
            Map<String,Object> user = new HashMap<>();
            user.put("email", email);
            user.put("name", editName);
            user.put("surname", editSurname);
            user.put("birth", editBirth);

            db.collection("athlete").document(email).update(user);

            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra("email", email);
            startActivity(intent);
            finish();
        } else {
            Toast toast = Toast.makeText(getApplicationContext(), "Faltan campos por completar", Toast.LENGTH_LONG);
            toast.show();
        }
    }

    private boolean validateEditProfile(String name, String surname, String birthdate) {
        boolean isValid = true;
        if(name.isEmpty() || name == null) {
            nameEditProfile.getEditText().setError("El nombre es obligatorio");
            isValid = false;
        } else {
            nameEditProfile.setError(null);
        }
        if(surname.isEmpty() || surname == null) {
            surnameEditProfile.getEditText().setError("Los apellidos son obligatorios");
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