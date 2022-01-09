package com.ivione93.hojames.ui.competitions;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.DateValidatorPointBackward;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.Timestamp;
import com.google.firebase.analytics.FirebaseAnalytics;
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

    private FirebaseAnalytics mFirebaseAnalytics;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    TextInputLayout placeText, competitionNameText, trackText, resultText;
    TextInputEditText editTextResult;
    EditText dateText;
    RadioGroup radioGroupTypeCompetition;
    RadioButton radioPC, radioAL, radioCross, radioRoad;

    String email, id;
    Boolean isNew;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_competition);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        email = getIntent().getStringExtra("email");
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
    public void onBackPressed() {
        android.app.AlertDialog.Builder cancelCompetition = new android.app.AlertDialog.Builder(this);
        cancelCompetition.setTitle(R.string.exit_title);
        cancelCompetition.setMessage(R.string.exit_message);
        cancelCompetition.setPositiveButton(R.string.exit, (dialog, which) -> {
            super.onBackPressed();
        });
        cancelCompetition.setNegativeButton(R.string.cancel, (dialog, which) -> {
            dialog.dismiss();
        });
        cancelCompetition.show();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            getIntent().putExtra("email", email);
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
            String msg = "*Hoja del mes*\n" +
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
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        placeText = findViewById(R.id.placeText);
        competitionNameText = findViewById(R.id.competitionNameText);
        trackText = findViewById(R.id.trackText);
        resultText = findViewById(R.id.resultText);
        editTextResult = findViewById(R.id.editTextResult);
        placeText = findViewById(R.id.placeText);
        // Material Date Picker
        CalendarConstraints.Builder constraints = new CalendarConstraints.Builder();
        constraints.setValidator(DateValidatorPointBackward.now());
        MaterialDatePicker.Builder builder = MaterialDatePicker.Builder.datePicker();
        builder.setTitleText(R.string.select_date);
        builder.setCalendarConstraints(constraints.build());
        MaterialDatePicker datePicker = builder.build();
        dateText = findViewById(R.id.dateText);
        dateText.setOnClickListener(v -> datePicker.show(getSupportFragmentManager(), "DATE_PICKER"));
        datePicker.addOnPositiveButtonClickListener(selection -> dateText.setText(datePicker.getHeaderText()));

        editTextResult.setOnClickListener(v -> selectTimePicker().show());

        if (!isNew) {
            getSupportActionBar().setTitle(R.string.title_competition);
            id = getIntent().getStringExtra("idCompetition");
            loadCompetition(id);
        } else {
            getSupportActionBar().setTitle(R.string.title_activity_new_competition);
        }

        radioGroupTypeCompetition = findViewById(R.id.radioGroupTypeCompetition);
        radioPC = findViewById(R.id.radio_pc);
        radioAL = findViewById(R.id.radio_al);
        radioCross = findViewById(R.id.radio_cross);
        radioRoad = findViewById(R.id.radio_road);
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

        builder.setTitle(R.string.insert_result);
        builder.setView(v)
                .setPositiveButton(R.string.add, (dialog, which) -> {
                    String hours, minutes, seconds, miliseconds;
                    CheckBox checkAbandono = v.findViewById(R.id.checkAbandono);

                    if (checkAbandono.isChecked()) {
                        resultText.getEditText().setText("AB");
                    } else {
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
                    }
                })
                .setNegativeButton(R.string.cancel, (dialog, which) -> {

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
                    dateText.setText(Utils.toString((Timestamp) task.getResult().getDocuments().get(0).get("date"), getString(R.string.format_date)));
                    if(task.getResult().getDocuments().get(0).get("type") != null) {
                        if(task.getResult().getDocuments().get(0).get("type").equals(getString(R.string.type_pc))) {
                            radioPC.setChecked(true);
                        }
                        if(task.getResult().getDocuments().get(0).get("type").equals(getString(R.string.type_al))) {
                            radioAL.setChecked(true);
                        }
                        if(task.getResult().getDocuments().get(0).get("type").equals(getString(R.string.type_cross))) {
                            radioCross.setChecked(true);
                        }
                        if(task.getResult().getDocuments().get(0).get("type").equals(getString(R.string.type_road))) {
                            radioRoad.setChecked(true);
                        }
                    }
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
        String typeCompetition = null;

        if (radioGroupTypeCompetition.getCheckedRadioButtonId() == radioPC.getId()) {
            typeCompetition = getString(R.string.type_pc);
        } else if (radioGroupTypeCompetition.getCheckedRadioButtonId() == radioAL.getId()) {
            typeCompetition = getString(R.string.type_al);
        } else if (radioGroupTypeCompetition.getCheckedRadioButtonId() == radioCross.getId()) {
            typeCompetition = getString(R.string.type_cross);
        } else if (radioGroupTypeCompetition.getCheckedRadioButtonId() == radioRoad.getId()) {
            typeCompetition = getString(R.string.type_road);
        }

        if (validateNewCompetition(place, competitionName, track, result, date)) {
            Map<String,Object> competition = new HashMap<>();
            if (isNew) {
                id = UUID.randomUUID().toString();//Evento nueva competicion Analytics
                Bundle bundle = new Bundle();
                bundle.putString("message", "Nueva competicion");
                bundle.putString("user", email);
                bundle.putString("id", id);
                mFirebaseAnalytics.logEvent("add_competition", bundle);
            }
            competition.put("id", id);
            competition.put("email", email);
            competition.put("place", place);
            competition.put("name", competitionName);
            competition.put("date", Utils.toTimestamp(date, getString(R.string.format_date)));
            competition.put("track", track);
            competition.put("result", result);
            competition.put("type", typeCompetition);
            // Firebase calendar
            competition.put("start", Utils.toStringCalendar(Utils.toTimestamp(date, getString(R.string.format_date))));
            competition.put("end", Utils.toStringCalendar(Utils.toTimestamp(date, getString(R.string.format_date))));
            competition.put("color", "#039BE5");
            competition.put("details", track + ": " + result);

            db.collection("competitions").document(id).set(competition);

            goProfile(email);
        } else {
            Toast toast = Toast.makeText(getApplicationContext(), R.string.all_fields_mandatories, Toast.LENGTH_LONG);
            toast.show();
        }
    }

    private void goProfile(String email) {
        Intent profileIntent = new Intent(this, MainActivity.class);
        profileIntent.putExtra("email", email);
        startActivity(profileIntent);
        finish();
    }

    private boolean validateNewCompetition(String place, String competitionName, String track, String result, String date) {
        boolean isValid = true;
        if(place.isEmpty() || place == null) {
            placeText.getEditText().setError(getString(R.string.place_mandatory));
            isValid = false;
        } else {
            placeText.setError(null);
        }
        if(competitionName.isEmpty() || competitionName == null) {
            competitionNameText.getEditText().setError(getString(R.string.championship_mandatory));
            isValid = false;
        } else {
            competitionNameText.setError(null);
        }
        if(track.isEmpty() || track == null) {
            trackText.getEditText().setError(getString(R.string.track_mandatory));
            isValid = false;
        } else {
            trackText.setError(null);
        }
        if(result.isEmpty() || result == null) {
            resultText.getEditText().setError(getString(R.string.result_mandatory));
            isValid = false;
        } else {
            resultText.setError(null);
        }
        if(date.isEmpty() || date == null) {
            dateText.setError(getString(R.string.date_mandatory));
            isValid = false;
        } else {
            dateText.setError(null);
        }
        return isValid;
    }

}