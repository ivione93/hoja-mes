package com.ivione93.hojames.ui.trainings;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.Timestamp;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.ivione93.hojames.MainActivity;
import com.ivione93.hojames.R;
import com.ivione93.hojames.Utils;
import com.ivione93.hojames.dto.CuestasDto;
import com.ivione93.hojames.dto.FartlekDto;
import com.ivione93.hojames.dto.GymDto;
import com.ivione93.hojames.dto.SeriesDto;
import com.ivione93.hojames.model.Cuestas;
import com.ivione93.hojames.model.Fartlek;
import com.ivione93.hojames.model.Gym;
import com.ivione93.hojames.model.Series;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

public class ViewTrainingActivity extends AppCompatActivity {

    private FirebaseAnalytics mFirebaseAnalytics;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference series = db.collection("series");
    private AdapterSeries adapterSeries;
    private CollectionReference cuestas = db.collection("cuestas");
    private AdapterCuestas adapterCuestas;
    private CollectionReference fartlek = db.collection("fartlek");
    private AdapterFartlek adapterFartlek;
    private CollectionReference gym = db.collection("gym");
    private AdapterGym adapterGym;

    TextInputLayout trainingTimeText, trainingDistanceText, trainingObservesText;
    TextInputEditText editTextTrainingTime;
    EditText trainingDateText;
    Button btnAddSeries, btnAddCuestas, btnAddFartlek, btnAddGym, btnShowExtras;
    RecyclerView rvSeries, rvCuestas, rvFartlek, rvGym;
    TabLayout tabLayout;

    String email, dateSelected, id;
    Boolean isNew;

    List<SeriesDto> listSeriesDto;
    List<CuestasDto> listCuestasDto;
    List<FartlekDto> listFartlekDto;
    List<GymDto> listGymDto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_training);

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
            //menu.findItem(R.id.menu_share_training).setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            getIntent().putExtra("email", email);
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
        adapterCuestas.startListening();
        adapterSeries.startListening();
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
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        listSeriesDto = new ArrayList<>();
        listCuestasDto = new ArrayList<>();
        listFartlekDto = new ArrayList<>();
        listGymDto = new ArrayList<>();

        trainingDateText = findViewById(R.id.trainingDateText);
        trainingTimeText = findViewById(R.id.trainingTimeText);
        trainingDistanceText = findViewById(R.id.trainingDistanceText);
        trainingObservesText = findViewById(R.id.trainingObservesText);
        editTextTrainingTime = findViewById(R.id.editTextTrainingTime);
        editTextTrainingTime.setOnClickListener(v -> selectTimePicker().show());

        btnAddSeries = findViewById(R.id.btnAddSeries);
        btnAddCuestas = findViewById(R.id.btnAddCuestas);
        btnAddFartlek = findViewById(R.id.btnAddFartlek);
        btnAddGym = findViewById(R.id.btnAddGym);
        btnShowExtras = findViewById(R.id.btnShowExtras);

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
            getSupportActionBar().setTitle("Entrenamiento");
            id = getIntent().getStringExtra("idTraining");
            loadTraining(id);
        } else {
            getSupportActionBar().setTitle("Nuevo entrenamiento");
            trainingDateText.setText(dateSelected);
        }

        btnAddSeries.setOnClickListener(v -> createAddSeriesDialog().show());
        btnAddCuestas.setOnClickListener(v -> createAddCuestasDialog().show());
        btnAddFartlek.setOnClickListener(v -> createAddFartlekDialog().show());
        btnAddGym.setOnClickListener(v -> createAddGymDialog().show());
        btnShowExtras.setOnClickListener(v -> createViewExtrasDialog().show());
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

    private void saveTraining() throws ParseException {
        String date = trainingDateText.getText().toString();
        String time = trainingTimeText.getEditText().getText().toString();
        String distance = trainingDistanceText.getEditText().getText().toString();

        String observes = "";
        if (trainingObservesText.getEditText().getText() != null) {
            observes = trainingObservesText.getEditText().getText().toString();
        }

        if (validateNewTraining(date, time, distance)) {
            if (Utils.validateDateFormat(date)) {
                String partial = Utils.calculatePartial(time, distance);
                Map<String,Object> training = new HashMap<>();
                if (isNew) {
                    id = UUID.randomUUID().toString();
                    //Evento nuevo entrenamiento Analytics
                    Bundle bundle = new Bundle();
                    bundle.putString("message", "Nuevo entrenamiento");
                    bundle.putString("user", email);
                    bundle.putString("id", id);
                    mFirebaseAnalytics.logEvent("add_training", bundle);
                }
                training.put("id", id);
                training.put("email", email);
                training.put("date", Utils.toTimestamp(date));
                training.put("time", time);
                training.put("distance", distance);
                training.put("partial", partial);
                training.put("observes", observes);
                // Firebase calendar
                training.put("name", "Entrenamiento");
                training.put("start", Utils.toStringCalendar(Utils.toTimestamp(date)));
                training.put("end", Utils.toStringCalendar(Utils.toTimestamp(date)));
                training.put("color", "#F60");
                training.put("details", distance + "kms: " + time);

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
                // Añadir fartlek
                if (!listFartlekDto.isEmpty()) {
                    for (FartlekDto dto : listFartlekDto) {
                        String idFartlek = UUID.randomUUID().toString();
                        Map<String, Object> fartlek = new HashMap<>();
                        fartlek.put("id", idFartlek);
                        fartlek.put("idTraining", training.get("id"));
                        fartlek.put("fartlek", dto.fartlek);
                        fartlek.put("date", training.get("date"));

                        db.collection("fartlek").document(idFartlek).set(fartlek);
                    }
                }
                // Añadir gym
                if (!listGymDto.isEmpty()) {
                    for (GymDto dto : listGymDto) {
                        String idGym = UUID.randomUUID().toString();
                        Map<String, Object> gym = new HashMap<>();
                        gym.put("id", idGym);
                        gym.put("idTraining", training.get("id"));
                        gym.put("exercise", dto.exercise);
                        gym.put("times", dto.times);
                        gym.put("kilos", dto.kilos);
                        gym.put("date", training.get("date"));

                        db.collection("gym").document(idGym).set(gym);
                    }
                }
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

    private void loadTraining(String id) {
        db.collection("trainings").whereEqualTo("id", id).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                QuerySnapshot document = task.getResult();
                if (!document.isEmpty()) {
                    trainingDateText.setText(Utils.toString((Timestamp) task.getResult().getDocuments().get(0).get("date")));
                    trainingTimeText.getEditText().setText(task.getResult().getDocuments().get(0).get("time").toString());
                    trainingDistanceText.getEditText().setText(task.getResult().getDocuments().get(0).get("distance").toString());
                    if (task.getResult().getDocuments().get(0).get("observes") != null) {
                        trainingObservesText.getEditText().setText(task.getResult().getDocuments().get(0).get("observes").toString());
                    }
                }
            }
        });
    }

    private void goProfile(String email) {
        Intent profileIntent = new Intent(this, MainActivity.class);
        profileIntent.putExtra("email", email);
        startActivity(profileIntent);
        finish();
    }

    public AlertDialog createAddSeriesDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View v = inflater.inflate(R.layout.dialog_add_series, null);

        builder.setTitle("Añadir serie");
        builder.setView(v)
                .setPositiveButton("Añadir", (dialog, which) -> {
                    addSeries(v);
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

        if (distance.equals("") || time.equals("")) {
            Toast.makeText(v.getContext(), "Todos los campos son obligatorios", Toast.LENGTH_LONG).show();
        } else {
            if (validateTimeSeries(time)) {
                SeriesDto seriesDto = new SeriesDto(distance, time);
                listSeriesDto.add(seriesDto);
                Toast.makeText(v.getContext(), "Serie añadida", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(v.getContext(), "Formato de tiempo incorrecto", Toast.LENGTH_LONG).show();
            }
        }
    }

    public boolean validateTimeSeries(String time) {
        String formatoHora = "\\d{1,2}h|((\\d{1,2}h )?\\d{2}:)?\\d{2}(\\.\\d{1,2})?";
        return Pattern.matches(formatoHora, time);
    }

    public AlertDialog createAddCuestasDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View v = inflater.inflate(R.layout.dialog_add_cuestas, null);

        builder.setTitle("Añadir cuestas");
        builder.setView(v)
                .setPositiveButton("Añadir", (dialog, which) -> {
                    addCuestas(v);
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
            Toast.makeText(v.getContext(), "Todos los campos son obligatorios", Toast.LENGTH_LONG).show();
        } else {
            CuestasDto cuestasDto = new CuestasDto(type, Integer.parseInt(times));
            listCuestasDto.add(cuestasDto);
            Toast.makeText(v.getContext(), "Cuesta añadida", Toast.LENGTH_SHORT).show();
        }
    }

    public AlertDialog createAddFartlekDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View v = inflater.inflate(R.layout.dialog_add_fartlek, null);

        builder.setTitle("Añadir fartlek");
        builder.setView(v)
                .setPositiveButton("Añadir", (dialog, which) -> {
                    addFartlek(v);
                })
                .setNegativeButton("Cancelar", (dialog, which) -> {

                });

        return builder.create();
    }

    private void addFartlek(View v) {
        EditText fartlekFartlek;
        fartlekFartlek = v.findViewById(R.id.fartlekFartlek);

        String fartlek = fartlekFartlek.getText().toString();

        if (fartlek.equals("")) {
            Toast.makeText(v.getContext(), "El campo es obligatorio", Toast.LENGTH_LONG).show();
        } else {
            FartlekDto fartlekDto = new FartlekDto(fartlek);
            listFartlekDto.add(fartlekDto);
            Toast.makeText(v.getContext(), "Fartlek añadido", Toast.LENGTH_SHORT).show();
        }
    }

    public AlertDialog createAddGymDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View v = inflater.inflate(R.layout.dialog_add_gym, null);

        builder.setTitle("Añadir rutina de gimnasio");
        builder.setView(v)
                .setPositiveButton("Añadir", (dialog, which) -> {
                    addGym(v);
                })
                .setNegativeButton("Cancelar", (dialog, which) -> {

                });

        return builder.create();
    }

    private void addGym(View v) {
        EditText ejercicioGym, repeticionesGym, kilosGym;
        ejercicioGym = v.findViewById(R.id.ejercicioGym);
        repeticionesGym = v.findViewById(R.id.repeticionesGym);
        kilosGym = v.findViewById(R.id.kilosGym);

        String exercise = ejercicioGym.getText().toString();
        String times = repeticionesGym.getText().toString();
        String kilos = kilosGym.getText().toString();

        if (exercise.equals("")) {
            Toast.makeText(v.getContext(), "El ejercicio es obligatorio", Toast.LENGTH_LONG).show();
        } else {
            GymDto gymDto = new GymDto(exercise, times, kilos);
            listGymDto.add(gymDto);
            Toast.makeText(v.getContext(), "Rutina de gym añadida", Toast.LENGTH_SHORT).show();
        }
    }

    @SuppressLint("ResourceAsColor")
    public AlertDialog createViewExtrasDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View v = inflater.inflate(R.layout.dialog_view_extras, null);
        TableLayout tableExtras = v.findViewById(R.id.tableExtras);
        // Series
        if (listSeriesDto.size() > 0) {
            TableRow rowSeriesTitle = new TableRow(this);
            rowSeriesTitle.setLayoutParams(new TableRow.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            TextView textViewTitleSeries = new TextView(this);
            textViewTitleSeries.setText("Series");
            textViewTitleSeries.setTextSize(18);
            rowSeriesTitle.addView(textViewTitleSeries);
            tableExtras.addView(rowSeriesTitle, new TableLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            for (SeriesDto dto : listSeriesDto) {
                TableRow rowSeries = new TableRow(this);
                rowSeries.setLayoutParams(new TableRow.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                TextView textViewDistance = new TextView(this);
                TextView textViewTime = new TextView(this);
                textViewDistance.setText(dto.distance);
                textViewDistance.setTextSize(16);
                textViewDistance.setGravity(View.FOCUS_LEFT);
                textViewTime.setText(dto.time);
                textViewTime.setTextSize(16);
                textViewTime.setGravity(View.FOCUS_RIGHT);
                rowSeries.addView(textViewDistance);
                rowSeries.addView(textViewTime);
                tableExtras.addView(rowSeries, new TableLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            }
            View viewSeries = new View(this);
            viewSeries.setLayoutParams(new TableLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1));
            viewSeries.setBackgroundColor(R.color.colorPrimaryDark);
            tableExtras.addView(viewSeries);
        }

        // Cuestas
        if (listCuestasDto.size() > 0) {
            TableRow rowCuestasTitle = new TableRow(this);
            rowCuestasTitle.setLayoutParams(new TableRow.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            TextView textViewTitleCuestas = new TextView(this);
            textViewTitleCuestas.setText("Cuestas");
            textViewTitleCuestas.setTextSize(18);
            rowCuestasTitle.addView(textViewTitleCuestas);
            tableExtras.addView(rowCuestasTitle, new TableLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            for (CuestasDto dto : listCuestasDto) {
                TableRow rowCuestas = new TableRow(this);
                rowCuestas.setLayoutParams(new TableRow.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                TextView textViewType = new TextView(this);
                TextView textViewTimes = new TextView(this);
                textViewTimes.setText(String.valueOf(dto.times));
                textViewTimes.setTextSize(16);
                textViewTimes.setGravity(View.FOCUS_LEFT);
                textViewType.setText(dto.type);
                textViewType.setTextSize(16);
                textViewType.setGravity(View.FOCUS_RIGHT);
                rowCuestas.addView(textViewTimes);
                rowCuestas.addView(textViewType);
                tableExtras.addView(rowCuestas, new TableLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            }
            View viewCuestas = new View(this);
            viewCuestas.setLayoutParams(new TableLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1));
            viewCuestas.setBackgroundColor(R.color.colorPrimaryDark);
            tableExtras.addView(viewCuestas);
        }
        // Fartlek
        if (listFartlekDto.size() > 0) {
            TableRow rowFartlekTitle = new TableRow(this);
            rowFartlekTitle.setLayoutParams(new TableRow.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            TextView textViewTitleFartlek = new TextView(this);
            textViewTitleFartlek.setText("Fartlek");
            textViewTitleFartlek.setTextSize(18);
            rowFartlekTitle.addView(textViewTitleFartlek);
            tableExtras.addView(rowFartlekTitle, new TableLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            for (FartlekDto dto : listFartlekDto) {
                TableRow rowFartlek = new TableRow(this);
                rowFartlek.setLayoutParams(new TableRow.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                TextView textViewFartlek = new TextView(this);
                textViewFartlek.setText(dto.fartlek);
                textViewFartlek.setTextSize(16);
                textViewFartlek.setGravity(View.FOCUS_LEFT);
                rowFartlek.addView(textViewFartlek);
                tableExtras.addView(rowFartlek, new TableLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            }
            View viewFartlek = new View(this);
            viewFartlek.setLayoutParams(new TableLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1));
            viewFartlek.setBackgroundColor(R.color.colorPrimaryDark);
            tableExtras.addView(viewFartlek);
        }
        // Gym
        if (listGymDto.size() > 0) {
            TableRow rowGymTitle = new TableRow(this);
            rowGymTitle.setLayoutParams(new TableRow.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            TextView textViewTitleGym = new TextView(this);
            textViewTitleGym.setText("Gym");
            textViewTitleGym.setTextSize(18);
            rowGymTitle.addView(textViewTitleGym);
            tableExtras.addView(rowGymTitle, new TableLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            for (GymDto dto : listGymDto) {
                TableRow rowGym = new TableRow(this);
                rowGym.setLayoutParams(new TableRow.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                TextView textViewExercise = new TextView(this);
                TextView textViewTimes = new TextView(this);
                TextView textViewKilos = new TextView(this);
                textViewExercise.setText(dto.exercise);
                textViewExercise.setTextSize(16);
                textViewExercise.setGravity(View.FOCUS_LEFT);
                rowGym.addView(textViewExercise);
                if (dto.times != null) {
                    textViewTimes.setText(dto.times + " rep.");
                    textViewTimes.setTextSize(16);
                    textViewTimes.setPadding(40,0,40,0);
                    rowGym.addView(textViewTimes);
                }
                if (dto.kilos != null) {
                    textViewKilos.setText(dto.kilos + " kgs");
                    textViewKilos.setTextSize(16);
                    rowGym.addView(textViewKilos);
                }
                tableExtras.addView(rowGym, new TableLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            }
            View viewGym = new View(this);
            viewGym.setLayoutParams(new TableLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1));
            viewGym.setBackgroundColor(R.color.colorPrimaryDark);
            tableExtras.addView(viewGym);
        }
        builder.setTitle("Extras añadidos");
        builder.setView(v)
                .setPositiveButton("Aceptar", (dialog, which) -> {
                });

        return builder.create();
    }

    private boolean validateNewTraining(String date, String time, String distance) {
        boolean isValid = true;
        if (date.isEmpty() || date == null) {
            trainingDateText.setError("La fecha es obligatoria");
            isValid = false;
        } else {
            trainingDateText.setError(null);
        }
        if (time.isEmpty() || time == null) {
            trainingTimeText.getEditText().setError("El tiempo es obligatorio");
            isValid = false;
        } else {
            trainingTimeText.setError(null);
        }
        if (distance.isEmpty() || distance == null) {
            trainingDistanceText.getEditText().setError("La distancia es obligatoria");
            isValid = false;
        } else {
            trainingDistanceText.setError(null);
        }
        return isValid;
    }

    public AlertDialog selectTimePicker() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View v = inflater.inflate(R.layout.fragment_number_picker_training, null);

        NumberPicker horas = v.findViewById(R.id.horas);
        horas.setMinValue(00);
        horas.setMaxValue(24);

        NumberPicker minutos = v.findViewById(R.id.minutos);
        minutos.setMinValue(00);
        minutos.setMaxValue(59);

        NumberPicker segundos = v.findViewById(R.id.segundos);
        segundos.setMinValue(00);
        segundos.setMaxValue(59);

        builder.setTitle("Introduce tiempo");
        builder.setView(v)
                .setPositiveButton("Añadir", (dialog, which) -> {
                    String hours, minutes, seconds;
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
                    trainingTimeText.getEditText().setText(hours + "h " + minutes + ":" + seconds);

                })
                .setNegativeButton("Cancelar", (dialog, which) -> {

                });

        return builder.create();
    }
}