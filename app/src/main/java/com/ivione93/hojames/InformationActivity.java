package com.ivione93.hojames;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.ivione93.hojames.ui.login.AuthActivity;

public class InformationActivity extends AppCompatActivity {

    ConstraintLayout informationLayout;
    FloatingActionButton enterApplication;

    Boolean showInformation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_information);
        getSupportActionBar().hide();

        setup();
        session();
    }

    @Override
    protected void onStart() {
        super.onStart();
        informationLayout.setVisibility(View.VISIBLE);
    }

    private void setup() {
        informationLayout = findViewById(R.id.informationLayout);

        enterApplication = findViewById(R.id.enterApplication);
        enterApplication.setOnClickListener(v -> goLogin());
    }

    private void session() {
        SharedPreferences prefs = getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE);
        showInformation = prefs.getBoolean("showInformation", true);

        if (showInformation != null && showInformation == false) {
            informationLayout.setVisibility(View.INVISIBLE);
            goLogin();
        }
    }

    private void goLogin() {
        Intent intent = new Intent(getApplicationContext(), AuthActivity.class);
        startActivity(intent);
        finish();
    }
}