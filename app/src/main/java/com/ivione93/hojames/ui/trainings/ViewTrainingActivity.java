package com.ivione93.hojames.ui.trainings;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.textfield.TextInputLayout;
import com.ivione93.hojames.R;

public class ViewTrainingActivity extends AppCompatActivity {

    TextInputLayout trainingTimeText, trainingDistanceText;
    EditText trainingDateText;
    Button btnAddSeries, btnAddCuestas, btnAddFartlek;
    TextView tvListSeries;
    RecyclerView rvSeries, rvCuestas, rvFartlek;
    TabLayout tabLayout;

    String license, dateSelected;
    Boolean isNew;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_training);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        license = getIntent().getStringExtra("license");
        dateSelected = getIntent().getStringExtra("dateSelected");
        isNew = getIntent().getBooleanExtra("isNew", true);

        setup(isNew);
    }

    private void setup(Boolean isNew) {
        tvListSeries = findViewById(R.id.tvListSeries);

        trainingDateText = findViewById(R.id.trainingDateText);
        trainingTimeText = findViewById(R.id.trainingTimeText);
        trainingDistanceText = findViewById(R.id.trainingDistanceText);

        btnAddSeries = findViewById(R.id.btnAddSeries);
        btnAddCuestas = findViewById(R.id.btnAddCuestas);
        btnAddFartlek = findViewById(R.id.btnAddFartlek);

        rvSeries = findViewById(R.id.rvSeries);
        rvCuestas = findViewById(R.id.rvCuestas);
        rvFartlek = findViewById(R.id.rvFartlek);

        tabLayout = findViewById(R.id.tabLayout);
        tabLayout.addTab(tabLayout.newTab().setText("Series"));
        tabLayout.addTab(tabLayout.newTab().setText("Cuestas"));
        tabLayout.addTab(tabLayout.newTab().setText("Fartlek"));

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == 0) {
                    rvSeries.setVisibility(View.VISIBLE);
                    rvCuestas.setVisibility(View.INVISIBLE);
                    rvFartlek.setVisibility(View.INVISIBLE);
                } else if (tab.getPosition() == 1) {
                    rvSeries.setVisibility(View.INVISIBLE);
                    rvCuestas.setVisibility(View.VISIBLE);
                    rvFartlek.setVisibility(View.INVISIBLE);
                } else if (tab.getPosition() == 2) {
                    rvSeries.setVisibility(View.INVISIBLE);
                    rvCuestas.setVisibility(View.INVISIBLE);
                    rvFartlek.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });

        if (!isNew) {
            getSupportActionBar().setTitle("Entrenamiento");
            //loadTraining();
        } else {
            getSupportActionBar().setTitle("Nuevo entrenamiento");
            trainingDateText.setText(dateSelected);
        }

        btnAddSeries.setOnClickListener(v -> {
            //createAddSeriesDialog().show();
        });

        btnAddCuestas.setOnClickListener(v -> {
            //createAddCuestasDialog().show();
        });

        btnAddFartlek.setOnClickListener(v -> {
            //createAddFartlekDialog().show();
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.new_training_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            getIntent().putExtra("license", license);
            onBackPressed();
            return true;
        }
        if (item.getItemId() == R.id.menu_new_training) {
            //saveTraining();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}