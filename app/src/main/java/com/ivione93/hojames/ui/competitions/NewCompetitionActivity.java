package com.ivione93.hojames.ui.competitions;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.ivione93.hojames.MainActivity;
import com.ivione93.hojames.R;
import com.ivione93.hojames.Utils;

import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class NewCompetitionActivity extends AppCompatActivity {

    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    TextInputLayout placeText, competitionNameText, trackText, resultText;
    TextInputEditText editTextResult;
    EditText dateText;

    String email, license, id;
    Boolean isNew;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_competition);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        email = getIntent().getStringExtra("email");
        license = getIntent().getStringExtra("license");
        isNew = getIntent().getBooleanExtra("isNew", true);

        setup(isNew);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.new_competition_menu, menu);
        if (isNew) {
            menu.findItem(R.id.menu_share_competition).setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            getIntent().putExtra("email", email);
            getIntent().putExtra("license", license);
            onBackPressed();
            return true;
        }
        if (item.getItemId() == R.id.menu_new_competition) {
            try {
                saveCompetition();
            } catch (ParseException e) {
                e.printStackTrace();
            }
            return true;
        }
        if (item.getItemId() == R.id.menu_share_competition) {
            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            String msg = "*Hoja del mes - " + license + "*\n" +
                    "_Mira mi competición del " + dateText.getText().toString() + ":_\n\n" +
                    "*" + competitionNameText.getEditText().getText() + "*\n" +
                    "- Lugar: " + placeText.getEditText().getText().toString() + "\n" +
                    "- Prueba: " + trackText.getEditText().getText().toString() + "\n" +
                    "- Marca: " + resultText.getEditText().getText().toString();
            sendIntent.putExtra(Intent.EXTRA_TEXT, msg);
            sendIntent.setType("text/plain");

            Intent shareIntent = Intent.createChooser(sendIntent, null);
            startActivity(shareIntent);
        }
        return super.onOptionsItemSelected(item);
    }

    private void setup(boolean isNew) {
        placeText = findViewById(R.id.placeText);
        competitionNameText = findViewById(R.id.competitionNameText);
        trackText = findViewById(R.id.surnameText);
        resultText = findViewById(R.id.resultText);
        editTextResult = findViewById(R.id.editTextResult);
        placeText = findViewById(R.id.placeText);
        dateText = findViewById(R.id.dateText);

        editTextResult.setOnClickListener(v -> selectTimePicker().show());

        if (!isNew) {
            getSupportActionBar().setTitle("Competición");
            id = getIntent().getStringExtra("idCompetition");
            loadCompetition(id);
        } else {
            getSupportActionBar().setTitle("Nueva competición");
        }
    }

    public AlertDialog selectTimePicker() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View v = inflater.inflate(R.layout.fragment_number_picker, null);

        NumberPicker horas = v.findViewById(R.id.horas);
        horas.setMinValue(00);
        horas.setMaxValue(24);

        NumberPicker minutos = v.findViewById(R.id.minutos);
        minutos.setMinValue(00);
        minutos.setMaxValue(59);

        NumberPicker segundos = v.findViewById(R.id.segundos);
        segundos.setMinValue(00);
        segundos.setMaxValue(59);

        NumberPicker milisegundos = v.findViewById(R.id.milisegundos);
        milisegundos.setMinValue(00);
        milisegundos.setMaxValue(99);

        builder.setTitle("Introduce marca");
        builder.setView(v)
                .setPositiveButton("Añadir", (dialog, which) -> {
                    String hours, minutes, seconds, miliseconds;
                    if (horas.getValue() < 10) {
                        hours = "0" + horas.getValue();
                    } else {
                        hours = "" + horas.getValue();
                    }
                    if (minutos.getValue() < 10) {
                        minutes = "0" + minutos.getValue();
                    } else {
                        minutes = "" + minutos.getValue();
                    }
                    if (segundos.getValue() < 10) {
                        seconds = "0" + segundos.getValue();
                    } else {
                        seconds = "" + segundos.getValue();
                    }
                    if (milisegundos.getValue() < 10) {
                        miliseconds = "0" + milisegundos.getValue();
                    } else {
                        miliseconds = "" + milisegundos.getValue();
                    }
                    resultText.getEditText().setText(hours + "h " + minutes + ":" + seconds + "." + miliseconds);
                })
                .setNegativeButton("Cancelar", (dialog, which) -> {

                });

        return builder.create();
    }

    private void loadCompetition(String id) {
        db.collection("competitions").whereEqualTo("id", id).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                QuerySnapshot document = task.getResult();
                if (!document.isEmpty()) {
                    placeText.getEditText().setText(task.getResult().getDocuments().get(0).get("place").toString());
                    competitionNameText.getEditText().setText(task.getResult().getDocuments().get(0).get("name").toString());
                    trackText.getEditText().setText(task.getResult().getDocuments().get(0).get("track").toString());
                    resultText.getEditText().setText(task.getResult().getDocuments().get(0).get("result").toString());
                    dateText.setText(Utils.toString((Timestamp) task.getResult().getDocuments().get(0).get("date")));
                }
            }
        });
    }

    private void saveCompetition() throws ParseException {
        String place = placeText.getEditText().getText().toString();
        String competitionName = competitionNameText.getEditText().getText().toString();
        String track = trackText.getEditText().getText().toString();
        String result = resultText.getEditText().getText().toString();
        String date = dateText.getText().toString();

        if (validateNewCompetition(place, competitionName, track, result, date)) {
            if (Utils.validateDateFormat(date)) {
                Map<String,Object> competition = new HashMap<>();
                if (isNew) {
                    id = UUID.randomUUID().toString();
                }
                competition.put("id", id);
                competition.put("license", license);
                competition.put("email", email);
                competition.put("place", place);
                competition.put("name", competitionName);
                competition.put("date", Utils.toTimestamp(date));
                competition.put("track", track);
                competition.put("result", result);

                db.collection("competitions").document(id).set(competition);

                goProfile(email, license);
            } else {
                Toast toast = Toast.makeText(getApplicationContext(), "Formato de fecha incorrecto", Toast.LENGTH_LONG);
                toast.show();
            }
        } else {
            Toast toast = Toast.makeText(getApplicationContext(), "Faltan campos por completar", Toast.LENGTH_LONG);
            toast.show();
        }
    }

    private void goProfile(String email, String license) {
        Intent profileIntent = new Intent(this, MainActivity.class);
        profileIntent.putExtra("email", email);
        profileIntent.putExtra("license", license);
        startActivity(profileIntent);
    }

    private boolean validateNewCompetition(String place, String competitionName, String track, String result, String date) {
        boolean isValid = true;
        if(place.isEmpty() || place == null) {
            isValid = false;
        }
        if(competitionName.isEmpty() || competitionName == null) {
            isValid = false;
        }
        if(track.isEmpty() || track == null) {
            isValid = false;
        }
        if(result.isEmpty() || result == null) {
            isValid = false;
        }
        if(date.isEmpty() || date == null) {
            isValid = false;
        }
        return isValid;
    }

}