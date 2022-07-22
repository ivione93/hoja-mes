package com.ivione93.hojames.ui.trainings;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.ivione93.hojames.MainActivity;
import com.ivione93.hojames.R;
import com.ivione93.hojames.Utils;
import com.ivione93.hojames.model.Cuestas;
import com.ivione93.hojames.model.Fartlek;
import com.ivione93.hojames.model.Gym;
import com.ivione93.hojames.model.Series;

import java.util.Objects;

public class TrainingActivity extends AppCompatActivity {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final CollectionReference series = db.collection("series");
    private AdapterSeriesTraining adapterSeries;
    private final CollectionReference cuestas = db.collection("cuestas");
    private AdapterCuestasTraining adapterCuestas;
    private final CollectionReference fartlek = db.collection("fartlek");
    private AdapterFartlekTraining adapterFartlek;
    private final CollectionReference gym = db.collection("gym");
    private AdapterGymTraining adapterGym;

    TextView tDate, tType, tTime, tDistance, tPartial, tObserves;
    MaterialCardView cardObserves;
    TabLayout tabLayout;
    RecyclerView rvSeries, rvCuestas, rvFartlek, rvGym;
    ImageView ivIndicadorSeries, ivIndicadorCuestas, ivIndicadorGym, ivIndicadorFartlek;

    String email, dateSelected, id;
    Boolean isNew;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_training);

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

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
            menu.findItem(R.id.menu_options_training).setVisible(false);
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
        if (item.getItemId() == R.id.menu_options_training) {
            showBottomSheetDialog();
        }
        if (item.getItemId() == R.id.menu_edit_training) {
            editTraining();

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

    private void showBottomSheetDialog() {
        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        bottomSheetDialog.setContentView(R.layout.bottom_sheet_training);

        LinearLayout editTrainingL = bottomSheetDialog.findViewById(R.id.editTrainingL);
        LinearLayout deleteTrainingL = bottomSheetDialog.findViewById(R.id.deleteTrainingL);
        LinearLayout cancelL = bottomSheetDialog.findViewById(R.id.cancelL);

        bottomSheetDialog.show();

        if (editTrainingL != null) {
            editTrainingL.setOnClickListener(v -> {
                bottomSheetDialog.dismiss();
                editTraining();
            });
        }

        if (deleteTrainingL != null) {
            deleteTrainingL.setOnClickListener(v -> {
                bottomSheetDialog.dismiss();
                deleteTraining();
            });
        }

        if (cancelL != null) {
            cancelL.setOnClickListener(v -> bottomSheetDialog.dismiss());
        }
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

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void setup(Boolean isNew) {
        tDate = findViewById(R.id.tDate);
        tType = findViewById(R.id.tType);
        tDistance = findViewById(R.id.tDistance);
        tTime = findViewById(R.id.tTime);
        tPartial = findViewById(R.id.tPartial);
        cardObserves = findViewById(R.id.cardObserves);
        tObserves = findViewById(R.id.tObserves);

        ivIndicadorSeries = findViewById(R.id.ivIndicadorSeries);
        ivIndicadorCuestas = findViewById(R.id.ivIndicadorCuestas);
        ivIndicadorGym = findViewById(R.id.ivIndicadorGym);
        ivIndicadorFartlek = findViewById(R.id.ivIndicadorFartlek);

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
            Objects.requireNonNull(getSupportActionBar()).setTitle(getString(R.string.title_training));
            id = getIntent().getStringExtra("idTraining");
            loadTraining(id);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void loadTraining(String id) {
        db.collection("trainings").whereEqualTo("id", id).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                QuerySnapshot document = task.getResult();
                if (!document.isEmpty()) {
                    tDate.setText(Utils.toString((Timestamp) task.getResult().getDocuments().get(0).get("date"), getString(R.string.format_date)));
                    if (task.getResult().getDocuments().get(0).get("type") != null) {
                        tType.setText(task.getResult().getDocuments().get(0).get("type").toString());
                    } else {
                        tType.setText(getString(R.string.type_run));
                    }
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
                    if (task.getResult().getDocuments().get(0).get("observes") != null && !task.getResult().getDocuments().get(0).get("observes").equals("")) {
                        tObserves.setText(task.getResult().getDocuments().get(0).get("observes").toString());
                    } else {
                        cardObserves.setVisibility(View.INVISIBLE);
                    }

                }
            }
        });
        // check series
        db.collection("series").whereEqualTo("idTraining", id).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                QuerySnapshot document = task.getResult();
                if (document.isEmpty()) {
                    ivIndicadorSeries.setColorFilter(getResources().getColor(R.color.colorText));
                } else {
                    ivIndicadorSeries.setColorFilter(getResources().getColor(R.color.colorPrimary));
                }
            }
        });
        // check cuestas
        db.collection("cuestas").whereEqualTo("idTraining", id).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                QuerySnapshot document = task.getResult();
                if (document.isEmpty()) {
                    ivIndicadorCuestas.setColorFilter(getResources().getColor(R.color.colorText));
                } else {
                    ivIndicadorCuestas.setColorFilter(getResources().getColor(R.color.colorPrimary));
                }
            }
        });
        // check fartlek
        db.collection("fartlek").whereEqualTo("idTraining", id).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                QuerySnapshot document = task.getResult();
                if (document.isEmpty()) {
                    ivIndicadorFartlek.setColorFilter(getResources().getColor(R.color.colorText));
                } else {
                    ivIndicadorFartlek.setColorFilter(getResources().getColor(R.color.colorPrimary));
                }
            }
        });
        // check gym
        db.collection("gym").whereEqualTo("idTraining", id).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                QuerySnapshot document = task.getResult();
                if (document.isEmpty()) {
                    ivIndicadorGym.setColorFilter(getResources().getColor(R.color.colorText));
                } else {
                    ivIndicadorGym.setColorFilter(getResources().getColor(R.color.colorPrimary));
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

        adapterSeries = new AdapterSeriesTraining(options);

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

        adapterCuestas = new AdapterCuestasTraining(options);

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

        adapterFartlek = new AdapterFartlekTraining(options);

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

        adapterGym = new AdapterGymTraining(options);

        rvGym = findViewById(R.id.rvGym);
        rvGym.setHasFixedSize(true);
        rvGym.setLayoutManager(new LinearLayoutManager(this));
        rvGym.setAdapter(adapterGym);
    }

    private void editTraining() {
        Intent newTraining = new Intent(this, ViewTrainingActivity.class);
        newTraining.putExtra("isNew", false);
        newTraining.putExtra("idTraining", id);
        newTraining.putExtra("email", email);
        startActivity(newTraining);
    }

    private void deleteTraining() {
        AlertDialog.Builder deleteConfirm = new AlertDialog.Builder(this);
        deleteConfirm.setTitle(R.string.delete_training);
        deleteConfirm.setMessage(R.string.delete_training_confirm);
        deleteConfirm.setCancelable(false);
        deleteConfirm.setPositiveButton(R.string.accept, (dialog, which) -> {
            db.collection("trainings").document(id).delete();
            db.collection("series").whereEqualTo("idTraining", id).get().addOnCompleteListener(task -> {
                if (task.isSuccessful()){
                    for (DocumentSnapshot doc : task.getResult().getDocuments()) {
                        db.collection("series").document(doc.getId()).delete();
                    }
                }
            });
            db.collection("cuestas").whereEqualTo("idTraining", id).get().addOnCompleteListener(task -> {
                if (task.isSuccessful()){
                    for (DocumentSnapshot doc : task.getResult().getDocuments()) {
                        db.collection("cuestas").document(doc.getId()).delete();
                    }
                }
            });
            db.collection("fartlek").whereEqualTo("idTraining", id).get().addOnCompleteListener(task -> {
                if (task.isSuccessful()){
                    for (DocumentSnapshot doc : task.getResult().getDocuments()) {
                        db.collection("fartlek").document(doc.getId()).delete();
                    }
                }
            });
            db.collection("gym").whereEqualTo("idTraining", id).get().addOnCompleteListener(task -> {
                if (task.isSuccessful()){
                    for (DocumentSnapshot doc : task.getResult().getDocuments()) {
                        db.collection("gym").document(doc.getId()).delete();
                    }
                }
            });
            goProfile(email);
        });
        deleteConfirm.setNegativeButton(R.string.cancel, (dialog, which) -> dialog.cancel());
        deleteConfirm.show();
    }

    private void goProfile(String email) {
        Intent profileIntent = new Intent(this, MainActivity.class);
        profileIntent.putExtra("email", email);
        startActivity(profileIntent);
        finish();
    }
}