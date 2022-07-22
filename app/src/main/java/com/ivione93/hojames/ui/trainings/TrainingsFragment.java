package com.ivione93.hojames.ui.trainings;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.ivione93.hojames.R;
import com.ivione93.hojames.Utils;
import com.ivione93.hojames.model.Training;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class TrainingsFragment extends Fragment {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private AdapterTrainings adapterTrainings;

    private final List<Training> listTrainings = new ArrayList<>();

    MaterialCalendarView calendarTrainings;
    RecyclerView rvTrainings;
    Chip chipTotals, chipCarrera, chipCiclismo, chipCinta, chipCiclismoSala, chipEliptica;

    String email;
    String dateSelected;

    private ProgressDialog progressDialog;

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_trainings, container, false);

        setHasOptionsMenu(true);

        // Setup
        Bundle bundle = getActivity().getIntent().getExtras();
        email = bundle.getString("email");

        listTrainings.clear();

        progressDialog = new ProgressDialog(getContext());
        progressDialog.setMessage(getString(R.string.loading_data));
        progressDialog.setCancelable(false);
        progressDialog.show();
        db.collection("trainings")
                .whereEqualTo("email", email)
                .orderBy("date", Query.Direction.DESCENDING)
                .get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot documentSnapshot : task.getResult()) {
                    Training training = documentSnapshot.toObject(Training.class);
                    listTrainings.add(training);
                    setupAdaptadorTrainings(root);
                }
            } else {
                progressDialog.dismiss();
                setupAdaptadorTrainings(root);
            }
        });

        setup(root);

        return root;
    }

    private void setupAdaptadorTrainings(View root) {
        rvTrainings = root.findViewById(R.id.rvTrainings);
        rvTrainings.setHasFixedSize(true);
        adapterTrainings = new AdapterTrainings(listTrainings);

        rvTrainings.setLayoutManager(new LinearLayoutManager(root.getContext()));
        Date now = Calendar.getInstance().getTime();
        DateFormat format = new SimpleDateFormat("dd/MM/yyyy");
        adapterTrainings.getFilter().filter(format.format(now));
        rvTrainings.setAdapter(adapterTrainings);
    }

    @Override
    public void onStop() { super.onStop(); }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.list_training_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.menu_add_training) {
            Intent newTraining = new Intent(getActivity(), ViewTrainingActivity.class);
            newTraining.putExtra("email", email);
            newTraining.putExtra("dateSelected", Utils.selectDateCalendarToString(dateSelected, getString(R.string.format_date)));
            getContext().startActivity(newTraining);
        }
        return super.onOptionsItemSelected(item);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void setup(View root) {
        dateSelected = Utils.initCalendarToString(new Date());

        chipTotals = root.findViewById(R.id.chipTotals);
        chipTotals.setText(getString(R.string.totals) + " " + getKms(dateSelected).toString());
        chipCarrera = root.findViewById(R.id.chipCarrera);
        chipCarrera.setText(getString(R.string.type_run) + " " + getKms(dateSelected).toString());
        chipCiclismo = root.findViewById(R.id.chipCiclismo);
        chipCiclismo.setText(getString(R.string.type_cycling) + " " + getKms(dateSelected).toString());
        chipCinta = root.findViewById(R.id.chipCinta);
        chipCinta.setText(getString(R.string.type_indoor_run) + " " + getKms(dateSelected).toString());
        chipCiclismoSala = root.findViewById(R.id.chipCiclismoSala);
        chipCiclismoSala.setText(getString(R.string.type_indoor_cycling) + " " + getKms(dateSelected).toString());
        chipEliptica = root.findViewById(R.id.chipEliptica);
        chipEliptica.setText(getString(R.string.type_elliptical) + " " + getKms(dateSelected).toString());
        calendarTrainings = root.findViewById(R.id.calendar_trainings);
        calendarTrainings.setDateSelected(Calendar.getInstance().getTime(), true);

        calendarTrainings.setOnDateChangedListener((widget, date, selected) -> {
            if (adapterTrainings != null) {
                getDateSelected(date);
                adapterTrainings.getFilter().filter(dateSelected);
            }
        });

        calendarTrainings.setOnMonthChangedListener((widget, date) -> {
            getDateSelected(date);
            chipTotals.setText(getString(R.string.totals) + " " + String.format("%.02f", getKms(dateSelected)));
            chipCarrera.setText(getString(R.string.type_run) + " " + String.format("%.02f", getKms(dateSelected)));
            chipCiclismo.setText(getString(R.string.type_cycling) + " " + String.format("%.02f", getKms(dateSelected)));
            chipCinta.setText(getString(R.string.type_indoor_run) + " " + String.format("%.02f", getKms(dateSelected)));
            chipCiclismoSala.setText(getString(R.string.type_indoor_cycling) + " " + String.format("%.02f", getKms(dateSelected)));
            chipEliptica.setText(getString(R.string.type_elliptical) + " " + String.format("%.02f", getKms(dateSelected)));
        });
    }

    private void getDateSelected(CalendarDay date) {
        int month = date.getMonth() + 1;
        if (month < 10) {
            if (date.getDay() < 10) {
                dateSelected = "0" + date.getDay() + "/0" + month + "/" + date.getYear();
            } else {
                dateSelected = date.getDay() + "/0" + month + "/" + date.getYear();
            }
        } else {
            if (date.getDay() < 10) {
                dateSelected = "0" + date.getDay() + "/" + month + "/" + date.getYear();
            } else {
                dateSelected = date.getDay() + "/" + month + "/" + date.getYear();
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public Float getKms(String dateSelected) {
        String month = dateSelected.substring(3,5);
        String year = dateSelected.substring(6,10);
        AtomicReference<Float> count = new AtomicReference<>(0.0f);
        AtomicReference<Float> countCarrera = new AtomicReference<>(0.0f);
        AtomicReference<Float> countCiclismo = new AtomicReference<>(0.0f);
        AtomicReference<Float> countCinta = new AtomicReference<>(0.0f);
        AtomicReference<Float> countEliptica = new AtomicReference<>(0.0f);
        AtomicReference<Float> countCiclismoSala = new AtomicReference<>(0.0f);
        db.collection("trainings").whereEqualTo("email", email).get()
                .addOnCompleteListener(task -> {
                    count.set(0f);
                    if (task.isSuccessful()) {
                        progressDialog.dismiss();
                        for (DocumentSnapshot snap : task.getResult()) {
                            Training training = snap.toObject(Training.class);
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                if (training.start.substring(5,7).equals(month) && training.start.substring(0,4).equals(year)) {
                                    count.updateAndGet(v -> v + Float.valueOf(training.distance));
                                    if (training.type == null || training.type.equals(getString(R.string.bd_run))) {
                                        countCarrera.updateAndGet(v -> v + Float.valueOf(training.distance));
                                    } else if (training.type.equals(getString(R.string.bd_cycling))) {
                                        countCiclismo.updateAndGet(v -> v + Float.valueOf(training.distance));
                                    } else if (training.type.equals(getString(R.string.bd_indoor_run))) {
                                        countCinta.updateAndGet(v -> v + Float.valueOf(training.distance));
                                    } else if (training.type.equals(getString(R.string.bd_elliptical))) {
                                        countEliptica.updateAndGet(v -> v + Float.valueOf(training.distance));
                                    } else if (training.type.equals(getString(R.string.bd_indoor_cycling))) {
                                        countCiclismoSala.updateAndGet(v -> v + Float.valueOf(training.distance));
                                    }
                                }
                            }
                        }
                        chipTotals.setText(getString(R.string.totals) + " " + String.format("%.02f", count.get()));
                        chipCarrera.setText(getString(R.string.type_run) + " " + String.format("%.02f", countCarrera.get()));
                        chipCiclismo.setText(getString(R.string.type_cycling) + " " + String.format("%.02f", countCiclismo.get()));
                        chipCinta.setText(getString(R.string.type_indoor_run) + " " + String.format("%.02f", countCinta.get()));
                        chipCiclismoSala.setText(getString(R.string.type_indoor_cycling) + " " + String.format("%.02f", countCiclismoSala.get()));
                        chipEliptica.setText(getString(R.string.type_elliptical) + " " + String.format("%.02f", countEliptica.get()));
                    } else {
                        progressDialog.dismiss();
                    }
                });
        return count.get();
    }
}