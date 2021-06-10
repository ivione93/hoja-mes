package com.ivione93.hojames.ui.trainings;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.Task;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.ivione93.hojames.MainActivity;
import com.ivione93.hojames.R;
import com.ivione93.hojames.Utils;
import com.ivione93.hojames.dto.CuestasDto;
import com.ivione93.hojames.dto.SeriesDto;
import com.ivione93.hojames.model.Cuestas;
import com.ivione93.hojames.model.Series;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

public class ViewTrainingActivity extends AppCompatActivity {

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference series = db.collection("series");
    private AdapterSeries adapterSeries;
    private CollectionReference cuestas = db.collection("cuestas");
    private AdapterCuestas adapterCuestas;

    TextInputLayout trainingTimeText, trainingDistanceText;
    EditText trainingDateText;
    Button btnAddSeries, btnAddCuestas, btnAddFartlek;
    TextView tvListSeries;
    RecyclerView rvSeries, rvCuestas, rvFartlek;
    TabLayout tabLayout;

    String license, email, dateSelected, id;
    Boolean isNew;

    List<SeriesDto> listSeriesDto;
    List<CuestasDto> listCuestasDto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_training);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        license = getIntent().getStringExtra("license");
        email = getIntent().getStringExtra("email");
        dateSelected = getIntent().getStringExtra("dateSelected");
        isNew = getIntent().getBooleanExtra("isNew", true);

        setup(isNew);
        setupRecyclerSeries();
        setupRecyclerCuestas();
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
            try {
                saveTraining();
            } catch (ParseException e) {
                e.printStackTrace();
            }
            return true;
        }
        if (item.getItemId() == R.id.menu_share_training) {
            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            String msg = "*Hoja del mes*\n" +
                    "Entrenamiento " + license + " día " + trainingDateText.getText() + ":\n" +
                    "- Tiempo: " + trainingTimeText.getEditText().getText().toString() + "\n" +
                    "- Distancia: " + trainingDistanceText.getEditText().getText().toString();
            sendIntent.putExtra(Intent.EXTRA_TEXT, msg);
            sendIntent.setType("text/plain");

            Intent shareIntent = Intent.createChooser(sendIntent, null);
            startActivity(shareIntent);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        super.onStart();
        adapterSeries.startListening();
        adapterCuestas.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        adapterSeries.stopListening();
        adapterCuestas.stopListening();
    }

    private void setup(Boolean isNew) {
        listSeriesDto = new ArrayList<>();
        listCuestasDto = new ArrayList<>();

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
            id = getIntent().getStringExtra("idTraining");
            loadTraining(id);
        } else {
            getSupportActionBar().setTitle("Nuevo entrenamiento");
            trainingDateText.setText(dateSelected);
        }

        btnAddSeries.setOnClickListener(v -> {
            createAddSeriesDialog().show();
        });

        btnAddCuestas.setOnClickListener(v -> {
            createAddCuestasDialog().show();
        });

        btnAddFartlek.setOnClickListener(v -> {
            //createAddFartlekDialog().show();
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

    private void saveTraining() throws ParseException {
        String date = trainingDateText.getText().toString();
        String time = trainingTimeText.getEditText().getText().toString();
        String distance = trainingDistanceText.getEditText().getText().toString();

        if (validateNewTraining(date, time, distance)) {
            if (Utils.validateDateFormat(date)) {
                String partial = Utils.calculatePartial(time, distance);
                Map<String,Object> training = new HashMap<>();
                if (isNew) {
                    id = UUID.randomUUID().toString();
                }
                training.put("id", id);
                training.put("license", license);
                training.put("email", email);
                training.put("date", Utils.toTimestamp(date));
                training.put("time", time);
                training.put("distance", distance);
                training.put("partial", partial);

                db.collection("trainings").document(id).set(training);
                // Añadir series
                if (!listSeriesDto.isEmpty()) {
                    for (SeriesDto dto : listSeriesDto) {
                        String idSerie = UUID.randomUUID().toString();
                        Map<String, Object> serie = new HashMap<>();
                        serie.put("id", idSerie);
                        serie.put("idTraining", training.get("id"));
                        serie.put("distance", dto.distance);
                        serie.put("time", dto.time);
                        serie.put("date", training.get("date"));

                        db.collection("series").document(idSerie).set(serie);
                    }
                }
                // Añadir cuestas
                if (!listCuestasDto.isEmpty()) {
                    for (CuestasDto dto : listCuestasDto) {
                        String idCuesta = UUID.randomUUID().toString();
                        Map<String, Object> cuesta = new HashMap<>();
                        cuesta.put("id", idCuesta);
                        cuesta.put("idTraining", training.get("id"));
                        cuesta.put("type", dto.type);
                        cuesta.put("times", dto.times);
                        cuesta.put("date", training.get("date"));

                        db.collection("cuestas").document(idCuesta).set(cuesta);
                    }
                }

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

    private void loadTraining(String id) {
        db.collection("trainings").whereEqualTo("id", id).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                QuerySnapshot document = task.getResult();
                if (!document.isEmpty()) {
                    trainingDateText.setText(Utils.toString((Timestamp) task.getResult().getDocuments().get(0).get("date")));
                    trainingTimeText.getEditText().setText(task.getResult().getDocuments().get(0).get("time").toString());
                    trainingDistanceText.getEditText().setText(task.getResult().getDocuments().get(0).get("distance").toString());
                }
            }
        });
    }

    private void goProfile(String email, String license) {
        Intent profileIntent = new Intent(this, MainActivity.class);
        profileIntent.putExtra("email", email);
        profileIntent.putExtra("license", license);
        startActivity(profileIntent);
    }

    public AlertDialog createAddSeriesDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View v = inflater.inflate(R.layout.dialog_add_series, null);

        builder.setTitle("Añadir serie");
        builder.setView(v)
                .setPositiveButton("Añadir", (dialog, which) -> {
                    addSeries(v);
                    showExtras();
                })
                .setNegativeButton("Cancelar", (dialog, which) -> {

                });

        return builder.create();
    }

    private void addSeries(View v) {
        EditText distanceSeries, timeSeries;
        distanceSeries = v.findViewById(R.id.distance_series);
        timeSeries = v.findViewById(R.id.time_series);

        String distance = distanceSeries.getText().toString();
        String time = timeSeries.getText().toString();

        if (distance.equals("") && time.equals("")) {
            Toast.makeText(v.getContext(), "Campos incompletos", Toast.LENGTH_LONG).show();
        } else {
            SeriesDto seriesDto = new SeriesDto(distance, time);
            listSeriesDto.add(seriesDto);
        }
    }

    public AlertDialog createAddCuestasDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View v = inflater.inflate(R.layout.dialog_add_cuestas, null);

        builder.setTitle("Añadir cuestas");
        builder.setView(v)
                .setPositiveButton("Añadir", (dialog, which) -> {
                    addCuestas(v);
                    showExtras();
                })
                .setNegativeButton("Cancelar", (dialog, which) -> {

                });

        return builder.create();
    }

    private void addCuestas(View v) {
        EditText repeticionesCuestas, tipoCuestas;
        tipoCuestas = v.findViewById(R.id.tipoCuestas);
        repeticionesCuestas = v.findViewById(R.id.repeticionesCuestas);

        String type = tipoCuestas.getText().toString();
        String times = repeticionesCuestas.getText().toString();

        if (type.equals("") && times.equals("")) {
            Toast.makeText(v.getContext(), "Campos incompletos", Toast.LENGTH_LONG).show();
        } else {
            CuestasDto cuestasDto = new CuestasDto(type, Integer.parseInt(times));
            listCuestasDto.add(cuestasDto);
        }
    }

    private void showExtras() {
        tvListSeries.setVisibility(View.VISIBLE);
        String txt = "";

        for (SeriesDto dto : listSeriesDto) {
            txt += "S[" + dto.distance + ", " + dto.time + "] ";
            tvListSeries.setText(txt);
        }
        for (CuestasDto dto : listCuestasDto) {
            txt += "C[" + dto.type + ", " + dto.times + "] ";
            tvListSeries.setText(txt);
        }
    }

    private boolean validateNewTraining(String date, String time, String distance) {
        boolean isValid = true;
        if (date.isEmpty() || date == null) {
            isValid = false;
        }
        if (time.isEmpty() || time == null) {
            isValid = false;
        }
        if (distance.isEmpty() || distance == null) {
            isValid = false;
        }
        return isValid;
    }
}