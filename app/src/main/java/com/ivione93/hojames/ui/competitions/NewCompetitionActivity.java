package com.ivione93.hojames.ui.competitions;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.FirebaseFirestore;
import com.ivione93.hojames.MainActivity;
import com.ivione93.hojames.R;
import com.ivione93.hojames.Utils;

import java.util.HashMap;
import java.util.Map;

public class NewCompetitionActivity extends AppCompatActivity {

    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    TextInputLayout placeText, competitionNameText, trackText, resultText;
    EditText dateText;

    String email;
    String license;
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
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            getIntent().putExtra("email", email);
            onBackPressed();
            return true;
        }
        if (item.getItemId() == R.id.menu_new_competition) {
            saveCompetition();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setup(boolean isNew) {
        placeText = findViewById(R.id.placeText);
        competitionNameText = findViewById(R.id.competitionNameText);
        trackText = findViewById(R.id.surnameText);
        resultText = findViewById(R.id.resultText);
        placeText = findViewById(R.id.placeText);
        dateText = findViewById(R.id.dateText);

        if (!isNew) {
            //loadCompetition();
        }
    }

    private void loadCompetition() {
    }

    private void saveCompetition() {
        String place = placeText.getEditText().getText().toString();
        String competitionName = competitionNameText.getEditText().getText().toString();
        String track = trackText.getEditText().getText().toString();
        String result = resultText.getEditText().getText().toString();
        String date = dateText.getText().toString();

        if (validateNewCompetition(place, competitionName, track, result, date)) {
            if (Utils.validateDateFormat(date)) {
                Map<String,Object> competition = new HashMap<>();
                competition.put("license", license);
                competition.put("place", place);
                competition.put("name", competitionName);
                competition.put("date", date);
                competition.put("track", track);
                competition.put("result", result);

                db.collection("competitions").document().set(competition);

                goProfile(email);
            } else {
                Toast toast = Toast.makeText(getApplicationContext(), "Formato de fecha incorrecto", Toast.LENGTH_LONG);
                toast.show();
            }
        } else {
            Toast toast = Toast.makeText(getApplicationContext(), "Faltan campos por completar", Toast.LENGTH_LONG);
            toast.show();
        }
    }

    private void goProfile(String email) {
        Intent profileIntent = new Intent(this, MainActivity.class);
        profileIntent.putExtra("email", email);
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