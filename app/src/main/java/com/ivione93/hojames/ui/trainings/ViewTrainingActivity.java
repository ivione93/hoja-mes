package com.ivione93.hojames.ui.trainings;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Switch;
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
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.DateValidatorPointBackward;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.textview.MaterialTextView;
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

    TextInputLayout trainingTimeText, trainingDistanceText, trainingObservesText, trainingPartialText;
    TextInputEditText editTextTrainingTime;
    EditText trainingDateText;
    Spinner spinnerTypes;
    Button btnAddSeries, btnAddCuestas, btnAddFartlek, btnAddGym, btnShowExtras;
    RecyclerView rvSeries, rvCuestas, rvFartlek, rvGym;
    TabLayout tabLayout;

    String email, dateSelected, id, typeTrainingSelected;
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
        menu.findItem(R.id.menu_edit_training).setVisible(false);
        menu.findItem(R.id.menu_options_training).setVisible(false);
        //menu.findItem(R.id.menu_share_training).setVisible(false);
        return true;
    }

    @Override
    public void onBackPressed() {
        android.app.AlertDialog.Builder cancelTraining = new android.app.AlertDialog.Builder(this);
        cancelTraining.setTitle(R.string.exit_title);
        cancelTraining.setMessage(R.string.exit_message);
        cancelTraining.setPositiveButton(R.string.exit, (dialog, which) -> {
            super.onBackPressed();
        });
        cancelTraining.setNegativeButton(R.string.cancel, (dialog, which) -> {
            dialog.dismiss();
        });
        cancelTraining.show();
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

        // Material Date Picker
        CalendarConstraints.Builder constraints = new CalendarConstraints.Builder();
        constraints.setValidator(DateValidatorPointBackward.now());
        MaterialDatePicker.Builder builder = MaterialDatePicker.Builder.datePicker();
        builder.setTitleText(R.string.select_date);
        builder.setCalendarConstraints(constraints.build());
        MaterialDatePicker datePicker = builder.build();
        trainingDateText = findViewById(R.id.trainingDateText);
        trainingDateText.setOnClickListener(v -> datePicker.show(getSupportFragmentManager(), "DATE_PICKER"));
        datePicker.addOnPositiveButtonClickListener(selection -> trainingDateText.setText(datePicker.getHeaderText()));
        trainingTimeText = findViewById(R.id.trainingTimeText);
        trainingDistanceText = findViewById(R.id.trainingDistanceText);
        trainingObservesText = findViewById(R.id.trainingObservesText);
        trainingPartialText = findViewById(R.id.trainingPartialText);
        editTextTrainingTime = findViewById(R.id.editTextTrainingTime);
        editTextTrainingTime.setOnClickListener(v -> selectTimePicker().show());

        spinnerTypes = findViewById(R.id.spinnerTypes);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.trainning_types, R.layout.support_simple_spinner_dropdown_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTypes.setAdapter(adapter);
        spinnerTypes.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    typeTrainingSelected = "Carrera";
                } else if (position == 1) {
                    typeTrainingSelected = "Carrera en cinta";
                }
                else if (position == 2) {
                    typeTrainingSelected = "Elíptica";
                }
                else if (position == 3) {
                    typeTrainingSelected = "Ciclismo";
                }
                else if (position == 4) {
                    typeTrainingSelected = "Ciclismo en sala";
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                typeTrainingSelected = "Carrera";
            }
        });

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
            getSupportActionBar().setTitle(getString(R.string.title_training));
            id = getIntent().getStringExtra("idTraining");
            loadTraining(id);
        } else {
            getSupportActionBar().setTitle(getString(R.string.title_activity_new_training));
            trainingDateText.setText(dateSelected);
        }

        btnAddSeries.setOnClickListener(v -> showBottomSheetSeriesDialog());//createAddSeriesDialog().show());
        btnAddCuestas.setOnClickListener(v -> showBottomSheetCuestasDialog());//createAddCuestasDialog().show());
        btnAddFartlek.setOnClickListener(v -> showBottomSheetFartlekDialog());//createAddFartlekDialog().show());
        btnAddGym.setOnClickListener(v -> showBottomSheetGymDialog());//createAddGymDialog().show());
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
            String partial = "-";
            if (typeTrainingSelected.equals("Carrera") || typeTrainingSelected.equals("Carrera en cinta") || typeTrainingSelected.equals("Elíptica")) {
                partial = Utils.calculatePartial(time, distance);
            } else {
                partial = Utils.calculatePartialCycling(time, distance);
            }

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
            training.put("date", Utils.toTimestamp(date, getString(R.string.format_date)));
            training.put("type", typeTrainingSelected);
            training.put("time", time);
            training.put("distance", distance);
            training.put("partial", partial);
            training.put("observes", observes);
            // Firebase calendar
            training.put("name", "Entrenamiento");
            training.put("start", Utils.toStringCalendar(Utils.toTimestamp(date, getString(R.string.format_date))));
            training.put("end", Utils.toStringCalendar(Utils.toTimestamp(date, getString(R.string.format_date))));
            training.put("color", "#212B39");
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
                    serie.put("hurdles", dto.hurdles);
                    serie.put("drags", dto.drags);
                    serie.put("shoes", dto.shoes);

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
            Toast toast = Toast.makeText(getApplicationContext(), R.string.all_fields_mandatories, Toast.LENGTH_LONG);
            toast.show();
        }
    }

    private void loadTraining(String id) {
        db.collection("trainings").whereEqualTo("id", id).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                QuerySnapshot document = task.getResult();
                if (!document.isEmpty()) {
                    trainingDateText.setText(Utils.toString((Timestamp) task.getResult().getDocuments().get(0).get("date"), getString(R.string.format_date)));
                    trainingTimeText.getEditText().setText(task.getResult().getDocuments().get(0).get("time").toString());
                    trainingDistanceText.getEditText().setText(task.getResult().getDocuments().get(0).get("distance").toString());
                    if (task.getResult().getDocuments().get(0).get("observes") != null) {
                        trainingObservesText.getEditText().setText(task.getResult().getDocuments().get(0).get("observes").toString());
                    }
                    String partialFormat = " /km";
                    if (task.getResult().getDocuments().get(0).get("type") != null) {
                        if (task.getResult().getDocuments().get(0).get("type").equals("Carrera")) {
                            spinnerTypes.setSelection(0);
                            partialFormat = " /km";
                        } else if (task.getResult().getDocuments().get(0).get("type").equals("Carrera en cinta")) {
                            spinnerTypes.setSelection(1);
                            partialFormat = " /km";
                        } else if (task.getResult().getDocuments().get(0).get("type").equals("Elíptica")) {
                            spinnerTypes.setSelection(2);
                            partialFormat = " km/h";
                        } else if (task.getResult().getDocuments().get(0).get("type").equals("Ciclismo")) {
                            spinnerTypes.setSelection(3);
                            partialFormat = " km/h";
                        } else if (task.getResult().getDocuments().get(0).get("type").equals("Ciclismo en sala")) {
                            spinnerTypes.setSelection(4);
                            partialFormat = " km/h";
                        }
                    } else {
                        spinnerTypes.setSelection(0);
                        partialFormat = " /km";
                    }
                    trainingPartialText.getEditText().setText(task.getResult().getDocuments().get(0).get("partial").toString() + partialFormat);
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

    private void showBottomSheetSeriesDialog() {
        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        bottomSheetDialog.setContentView(R.layout.bottom_sheet_add_series);
        bottomSheetDialog.setCanceledOnTouchOutside(false);
        bottomSheetDialog.setCancelable(false);

        ImageView saveSerieIV = bottomSheetDialog.findViewById(R.id.saveSerieIV);
        MaterialTextView saveSerieTV = bottomSheetDialog.findViewById(R.id.saveSerieTV);
        EditText timeSeries = bottomSheetDialog.findViewById(R.id.time_series);
        timeSeries.setOnClickListener((View.OnClickListener) v1 -> {
            selectSerieTimePicker(v1).show();
        });
        ImageView cancel = bottomSheetDialog.findViewById(R.id.cancel);

        bottomSheetDialog.show();

        saveSerieIV.setOnClickListener(v -> {
            bottomSheetDialog.dismiss();
            addSeries(bottomSheetDialog);
        });

        saveSerieTV.setOnClickListener(v -> {
            bottomSheetDialog.dismiss();
            addSeries(bottomSheetDialog);
        });

        cancel.setOnClickListener(v -> bottomSheetDialog.dismiss());
    }

    private void showBottomSheetCuestasDialog() {
        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        bottomSheetDialog.setContentView(R.layout.bottom_sheet_add_cuestas);
        bottomSheetDialog.setCanceledOnTouchOutside(false);
        bottomSheetDialog.setCancelable(false);

        ImageView saveCuestaIV = bottomSheetDialog.findViewById(R.id.saveCuestaIV);
        MaterialTextView saveCuestaTV = bottomSheetDialog.findViewById(R.id.saveCuestaTV);
        ImageView cancel = bottomSheetDialog.findViewById(R.id.cancel);

        bottomSheetDialog.show();

        saveCuestaIV.setOnClickListener(v -> {
            bottomSheetDialog.dismiss();
            addCuestas(bottomSheetDialog);
        });
        saveCuestaTV.setOnClickListener(v -> {
            bottomSheetDialog.dismiss();
            addCuestas(bottomSheetDialog);
        });

        cancel.setOnClickListener(v -> bottomSheetDialog.dismiss());
    }

    private void showBottomSheetFartlekDialog() {
        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        bottomSheetDialog.setContentView(R.layout.bottom_sheet_add_fartlek);
        bottomSheetDialog.setCanceledOnTouchOutside(false);
        bottomSheetDialog.setCancelable(false);

        ImageView saveFartlekIV = bottomSheetDialog.findViewById(R.id.saveFartlekIV);
        MaterialTextView saveFartlekTV = bottomSheetDialog.findViewById(R.id.saveFartlekTV);
        ImageView cancel = bottomSheetDialog.findViewById(R.id.cancel);

        bottomSheetDialog.show();

        saveFartlekIV.setOnClickListener(v -> {
            bottomSheetDialog.dismiss();
            addFartlek(bottomSheetDialog);
        });
        saveFartlekTV.setOnClickListener(v -> {
            bottomSheetDialog.dismiss();
            addFartlek(bottomSheetDialog);
        });

        cancel.setOnClickListener(v -> bottomSheetDialog.dismiss());
    }

    private void showBottomSheetGymDialog() {
        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        bottomSheetDialog.setContentView(R.layout.bottom_sheet_add_gym);
        bottomSheetDialog.setCanceledOnTouchOutside(false);
        bottomSheetDialog.setCancelable(false);

        ImageView saveGymIV = bottomSheetDialog.findViewById(R.id.saveGymIV);
        MaterialTextView saveGymTV = bottomSheetDialog.findViewById(R.id.saveGymTV);
        ImageView cancel = bottomSheetDialog.findViewById(R.id.cancel);

        bottomSheetDialog.show();

        saveGymIV.setOnClickListener(v -> {
            bottomSheetDialog.dismiss();
            addGym(bottomSheetDialog);
        });
        saveGymTV.setOnClickListener(v -> {
            bottomSheetDialog.dismiss();
            addGym(bottomSheetDialog);
        });

        cancel.setOnClickListener(v -> bottomSheetDialog.dismiss());
    }

    private void addSeries(BottomSheetDialog v) {
        EditText distanceSeries, timeSeries;
        distanceSeries = v.findViewById(R.id.distance_series);
        timeSeries = v.findViewById(R.id.time_series);

        String distance = distanceSeries.getText().toString();
        String time = timeSeries.getText().toString();

        Switch hurdles = v.findViewById(R.id.swHurdles);
        Boolean withHurdles = hurdles.isChecked();

        Switch drags = v.findViewById(R.id.swDrags);
        Boolean withDrags = drags.isChecked();

        String shoes = null;
        RadioGroup radioGroupShoes = v.findViewById(R.id.radioGroupShoes);
        RadioButton radioNormal, radioFlying, radioSpikes;
        radioNormal = v.findViewById(R.id.radio_normal);
        radioFlying = v.findViewById(R.id.radio_flying);
        radioSpikes = v.findViewById(R.id.radio_spikes);
        if (radioGroupShoes.getCheckedRadioButtonId() == radioNormal.getId()) {
            shoes = getString(R.string.normal_shoe);
        } else if (radioGroupShoes.getCheckedRadioButtonId() == radioFlying.getId()) {
            shoes = getString(R.string.flying_shoe);
        } else if (radioGroupShoes.getCheckedRadioButtonId() == radioSpikes.getId()) {
            shoes = getString(R.string.spike_shoe);
        }

        if (distance.equals("") || time.equals("")) {
            Toast.makeText(v.getContext(), R.string.all_fields_mandatories, Toast.LENGTH_LONG).show();
        } else {
            if (validateTimeSeries(time)) {
                SeriesDto seriesDto = new SeriesDto(distance, time, withHurdles, withDrags, shoes);
                listSeriesDto.add(seriesDto);
                Toast.makeText(v.getContext(), getString(R.string.serie_added), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(v.getContext(), getString(R.string.wrong_time_format), Toast.LENGTH_LONG).show();
            }
        }
    }

    public boolean validateTimeSeries(String time) {
        String formatoHora = "\\d{1,2}h \\d{2}:\\d{2}\\.\\d{2}";
        return Pattern.matches(formatoHora, time);
    }

    private void addCuestas(BottomSheetDialog v) {
        EditText repeticionesCuestas, tipoCuestas;
        tipoCuestas = v.findViewById(R.id.tipoCuestas);
        repeticionesCuestas = v.findViewById(R.id.repeticionesCuestas);

        String type = tipoCuestas.getText().toString();
        String times = repeticionesCuestas.getText().toString();

        if (type.equals("") && times.equals("")) {
            Toast.makeText(v.getContext(), R.string.all_fields_mandatories, Toast.LENGTH_LONG).show();
        } else {
            CuestasDto cuestasDto = new CuestasDto(type, Integer.parseInt(times));
            listCuestasDto.add(cuestasDto);
            Toast.makeText(v.getContext(), getString(R.string.cuesta_added), Toast.LENGTH_SHORT).show();
        }
    }

    private void addFartlek(BottomSheetDialog v) {
        EditText fartlekFartlek;
        fartlekFartlek = v.findViewById(R.id.fartlekFartlek);

        String fartlek = fartlekFartlek.getText().toString();

        if (fartlek.equals("")) {
            Toast.makeText(v.getContext(), R.string.all_fields_mandatories, Toast.LENGTH_LONG).show();
        } else {
            FartlekDto fartlekDto = new FartlekDto(fartlek);
            listFartlekDto.add(fartlekDto);
            Toast.makeText(v.getContext(), getString(R.string.fartlek_added), Toast.LENGTH_SHORT).show();
        }
    }

    private void addGym(BottomSheetDialog v) {
        EditText ejercicioGym, repeticionesGym, kilosGym;
        ejercicioGym = v.findViewById(R.id.ejercicioGym);
        repeticionesGym = v.findViewById(R.id.repeticionesGym);
        kilosGym = v.findViewById(R.id.kilosGym);

        String exercise = ejercicioGym.getText().toString();
        String times = repeticionesGym.getText().toString();
        String kilos = kilosGym.getText().toString();

        if (exercise.equals("")) {
            Toast.makeText(v.getContext(), getString(R.string.exercise_mandatory), Toast.LENGTH_LONG).show();
        } else {
            GymDto gymDto = new GymDto(exercise, times, kilos);
            listGymDto.add(gymDto);
            Toast.makeText(v.getContext(), getString(R.string.gym_added), Toast.LENGTH_SHORT).show();
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
                TextView textHurdles = new TextView(this);
                TextView textDrags = new TextView(this);
                TextView textViewTime = new TextView(this);
                TextView textViewShoe = new TextView(this);
                textViewDistance.setText(dto.distance + " m");
                textViewDistance.setTextSize(16);
                textViewDistance.setGravity(View.FOCUS_LEFT);
                if(dto.hurdles) {
                    textHurdles.setText(R.string.ind_hurdles);
                    textHurdles.setTextSize(16);
                }
                if(dto.drags) {
                    textDrags.setText(getResources().getString(R.string.ind_drags) + " - ");
                    textDrags.setTextSize(16);
                }
                textViewTime.setText(dto.time);
                textViewTime.setTextSize(16);
                textViewTime.setGravity(View.FOCUS_RIGHT);
                textViewShoe.setText(dto.shoes + " - ");
                textViewShoe.setTextSize(16);
                textViewShoe.setGravity(View.FOCUS_RIGHT);
                rowSeries.addView(textViewDistance);
                rowSeries.addView(textHurdles);
                rowSeries.addView(textDrags);
                rowSeries.addView(textViewShoe);
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
        builder.setTitle(R.string.extras_added);
        builder.setView(v)
                .setPositiveButton(R.string.accept, (dialog, which) -> {
                });

        return builder.create();
    }

    private boolean validateNewTraining(String date, String time, String distance) {
        boolean isValid = true;
        if (date.isEmpty() || date == null) {
            trainingDateText.setError(getString(R.string.date_mandatory));
            isValid = false;
        } else {
            trainingDateText.setError(null);
        }
        if (time.isEmpty() || time == null) {
            trainingTimeText.getEditText().setError(getString(R.string.time_mandatory));
            isValid = false;
        } else {
            trainingTimeText.setError(null);
        }
        if (distance.isEmpty() || distance == null) {
            trainingDistanceText.getEditText().setError(getString(R.string.distance_mandatory));
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

        builder.setTitle(R.string.insert_time);
        builder.setView(v)
                .setPositiveButton(R.string.add, (dialog, which) -> {
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
                .setNegativeButton(R.string.cancel, (dialog, which) -> {

                });

        return builder.create();
    }

    public AlertDialog selectSerieTimePicker(View v1) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View v = inflater.inflate(R.layout.fragment_number_picker, null);

        CheckBox checkAbandono = v.findViewById(R.id.checkAbandono);
        checkAbandono.setVisibility(View.INVISIBLE);

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

        builder.setTitle(R.string.insert_time);
        builder.setView(v)
                .setPositiveButton(R.string.add, (dialog, which) -> {
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
                    EditText timeSeries = v1.findViewById(R.id.time_series);
                    timeSeries.setText(hours + "h " + minutes + ":" + seconds + "." + miliseconds);
                })
                .setNegativeButton(R.string.cancel, (dialog, which) -> {

                });

        return builder.create();
    }
}