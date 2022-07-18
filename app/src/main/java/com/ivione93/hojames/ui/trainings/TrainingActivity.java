package com.ivione93.hojames.ui.trainings;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.ivione93.hojames.R;
import com.ivione93.hojames.Utils;
import com.ivione93.hojames.model.Cuestas;
import com.ivione93.hojames.model.Fartlek;
import com.ivione93.hojames.model.Gym;
import com.ivione93.hojames.model.Series;

import java.text.ParseException;

public class TrainingActivity extends AppCompatActivity {

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference series = db.collection("series");
    private AdapterSeries adapterSeries;
    private CollectionReference cuestas = db.collection("cuestas");
    private AdapterCuestas adapterCuestas;
    private CollectionReference fartlek = db.collection("fartlek");
    private AdapterFartlek adapterFartlek;
    private CollectionReference gym = db.collection("gym");
    private AdapterGym adapterGym;

    TextView tDate, tType, tTime, tDistance, tPartial, tObserves;
    TabLayout tabLayout;
    RecyclerView rvSeries, rvCuestas, rvFartlek, rvGym;

    String email, dateSelected, id;
    Boolean isNew;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_training);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        email = getIntent().getStringExtra("email");
        dateSelected = getIntent().getStringExtra("dateSelected");
        isNew = getIntent().getBooleanExtra("isNew", true);

        setup(isNew);

        setupRecyclerSeries();
        setupRecyclerCuestas();
        setupRecyclerFartlek();
        setupRecyclerGym();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.new_training_menu, menu);
        if (isNew) {
            menu.findItem(R.id.menu_edit_training).setVisible(false);
        } else {
            menu.findItem(R.id.menu_new_training).setVisible(false);
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            getIntent().putExtra("email", email);
            onBackPressed();
            return true;
        }
        if (item.getItemId() == R.id.menu_edit_training) {
            Intent newTraining = new Intent(this, ViewTrainingActivity.class);
            newTraining.putExtra("isNew", false);
            newTraining.putExtra("idTraining", id);
            newTraining.putExtra("email", email);
            startActivity(newTraining);

            return true;
        }
        /*if (item.getItemId() == R.id.menu_share_training) {
            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            String msg = "*Hoja del mes*\n" +
                    "_Mira mi entrenamiento del " + trainingDateText.getText() + ":_\n\n" +
                    "*Calentamiento:*\n" +
                    "- Tiempo: " + trainingTimeText.getEditText().getText().toString() + " min\n" +
                    "- Distancia: " + trainingDistanceText.getEditText().getText().toString() + " kms\n";
            if (trainingObservesText.getEditText().getText() != null || !trainingObservesText.getEditText().getText().equals("")) {
                msg +=  "- Observaciones: " + trainingObservesText.getEditText().getText().toString();
            }

            sendIntent.putExtra(Intent.EXTRA_TEXT, msg);
            sendIntent.setType("text/plain");

            Intent shareIntent = Intent.createChooser(sendIntent, null);
            startActivity(shareIntent);
        }*/
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        super.onStart();
        adapterSeries.startListening();
        adapterCuestas.startListening();
        adapterFartlek.startListening();
        adapterGym.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        adapterSeries.stopListening();
        adapterCuestas.stopListening();
        adapterFartlek.stopListening();
        adapterGym.stopListening();
    }

    private void setup(Boolean isNew) {
        tDate = findViewById(R.id.tDate);
        tType = findViewById(R.id.tType);
        tDistance = findViewById(R.id.tDistance);
        tTime = findViewById(R.id.tTime);
        tPartial = findViewById(R.id.tPartial);
        tObserves = findViewById(R.id.tObserves);

        rvSeries = findViewById(R.id.rvSeries);
        rvCuestas = findViewById(R.id.rvCuestas);
        rvFartlek = findViewById(R.id.rvFartlek);
        rvGym = findViewById(R.id.rvGym);
        rvSeries.setVisibility(View.VISIBLE);
        rvCuestas.setVisibility(View.INVISIBLE);
        rvFartlek.setVisibility(View.INVISIBLE);
        rvGym.setVisibility(View.INVISIBLE);

        tabLayout = findViewById(R.id.tabLayout);
        tabLayout.addTab(tabLayout.newTab().setText("Series"));
        tabLayout.addTab(tabLayout.newTab().setText("Cuestas"));
        tabLayout.addTab(tabLayout.newTab().setText("Fartlek"));
        tabLayout.addTab(tabLayout.newTab().setText("Gym"));

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == 0) {
                    rvSeries.setVisibility(View.VISIBLE);
                    rvCuestas.setVisibility(View.INVISIBLE);
                    rvFartlek.setVisibility(View.INVISIBLE);
                    rvGym.setVisibility(View.INVISIBLE);
                } else if (tab.getPosition() == 1) {
                    rvSeries.setVisibility(View.INVISIBLE);
                    rvCuestas.setVisibility(View.VISIBLE);
                    rvFartlek.setVisibility(View.INVISIBLE);
                    rvGym.setVisibility(View.INVISIBLE);
                } else if (tab.getPosition() == 2) {
                    rvSeries.setVisibility(View.INVISIBLE);
                    rvCuestas.setVisibility(View.INVISIBLE);
                    rvFartlek.setVisibility(View.VISIBLE);
                    rvGym.setVisibility(View.INVISIBLE);
                } else if (tab.getPosition() == 3) {
                    rvSeries.setVisibility(View.INVISIBLE);
                    rvCuestas.setVisibility(View.INVISIBLE);
                    rvFartlek.setVisibility(View.INVISIBLE);
                    rvGym.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });

        if (!isNew) {
            getSupportActionBar().setTitle(getString(R.string.title_training));
            id = getIntent().getStringExtra("idTraining");
            loadTraining(id);
        }
    }

    private void loadTraining(String id) {
        db.collection("trainings").whereEqualTo("id", id).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                QuerySnapshot document = task.getResult();
                if (!document.isEmpty()) {
                    tDate.setText(Utils.toString((Timestamp) task.getResult().getDocuments().get(0).get("date"), getString(R.string.format_date)));
                    tType.setText(task.getResult().getDocuments().get(0).get("type").toString());
                    tTime.setText(Utils.getFormattedTime(task.getResult().getDocuments().get(0).get("time").toString()).concat(" min"));
                    tDistance.setText(task.getResult().getDocuments().get(0).get("distance").toString().concat(" kms"));
                    String partialFormat = " /km";
                    if (task.getResult().getDocuments().get(0).get("type") != null) {
                        if (task.getResult().getDocuments().get(0).get("type").equals("Carrera")) {
                            partialFormat = " /km";
                        } else if (task.getResult().getDocuments().get(0).get("type").equals("Carrera en cinta")) {
                            partialFormat = " /km";
                        } else if (task.getResult().getDocuments().get(0).get("type").equals("Elíptica")) {
                            partialFormat = " km/h";
                        } else if (task.getResult().getDocuments().get(0).get("type").equals("Ciclismo")) {
                            partialFormat = " km/h";
                        } else if (task.getResult().getDocuments().get(0).get("type").equals("Ciclismo en sala")) {
                            partialFormat = " km/h";
                        }
                    } else {
                        partialFormat = " /km";
                    }
                    tPartial.setText(task.getResult().getDocuments().get(0).get("partial").toString() + partialFormat);
                    tObserves.setText(task.getResult().getDocuments().get(0).get("observes").toString());
                }
            }
        });
    }

    private void setupRecyclerSeries() {
        // Query
        Query query = series.whereEqualTo("idTraining", id)
                .orderBy("date", Query.Direction.DESCENDING);

        // Recycler options
        FirestoreRecyclerOptions<Series> options = new FirestoreRecyclerOptions.Builder<Series>()
                .setQuery(query, Series.class)
                .build();

        adapterSeries = new AdapterSeries(options);

        rvSeries = findViewById(R.id.rvSeries);
        rvSeries.setHasFixedSize(true);
        rvSeries.setLayoutManager(new LinearLayoutManager(this));
        rvSeries.setAdapter(adapterSeries);
    }

    private void setupRecyclerCuestas() {
        // Query
        Query query = cuestas.whereEqualTo("idTraining", id)
                .orderBy("date", Query.Direction.DESCENDING);

        // Recycler options
        FirestoreRecyclerOptions<Cuestas> options = new FirestoreRecyclerOptions.Builder<Cuestas>()
                .setQuery(query, Cuestas.class)
                .build();

        adapterCuestas = new AdapterCuestas(options);

        rvCuestas = findViewById(R.id.rvCuestas);
        rvCuestas.setHasFixedSize(true);
        rvCuestas.setLayoutManager(new LinearLayoutManager(this));
        rvCuestas.setAdapter(adapterCuestas);
    }

    private void setupRecyclerFartlek() {
        // Query
        Query query = fartlek.whereEqualTo("idTraining", id)
                .orderBy("date", Query.Direction.DESCENDING);

        // Recycler options
        FirestoreRecyclerOptions<Fartlek> options = new FirestoreRecyclerOptions.Builder<Fartlek>()
                .setQuery(query, Fartlek.class)
                .build();

        adapterFartlek = new AdapterFartlek(options);

        rvFartlek = findViewById(R.id.rvFartlek);
        rvFartlek.setHasFixedSize(true);
        rvFartlek.setLayoutManager(new LinearLayoutManager(this));
        rvFartlek.setAdapter(adapterFartlek);
    }

    private void setupRecyclerGym() {
        // Query
        Query query = gym.whereEqualTo("idTraining", id)
                .orderBy("date", Query.Direction.DESCENDING);

        // Recycler options
        FirestoreRecyclerOptions<Gym> options = new FirestoreRecyclerOptions.Builder<Gym>()
                .setQuery(query, Gym.class)
                .build();

        adapterGym = new AdapterGym(options);

        rvGym = findViewById(R.id.rvGym);
        rvGym.setHasFixedSize(true);
        rvGym.setLayoutManager(new LinearLayoutManager(this));
        rvGym.setAdapter(adapterGym);
    }
}