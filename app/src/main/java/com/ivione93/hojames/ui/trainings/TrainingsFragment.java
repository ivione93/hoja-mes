package com.ivione93.hojames.ui.trainings;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CalendarView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.ivione93.hojames.R;
import com.ivione93.hojames.Utils;
import com.ivione93.hojames.model.Training;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class TrainingsFragment extends Fragment {

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private AdapterTrainings adapterTrainings;

    private List<Training> listTrainings = new ArrayList<>();

    CalendarView calendarTrainings;
    RecyclerView rvTrainings;
    TextView monthlyKms;

    String email;
    String dateSelected = Utils.toString(new Date());

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
    public void onStop() {
        super.onStop();
    }

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
            newTraining.putExtra("dateSelected", dateSelected);
            getContext().startActivity(newTraining);
        }
        return super.onOptionsItemSelected(item);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void setup(View root) {
        monthlyKms = root.findViewById(R.id.monthly_kms);
        monthlyKms.setText(getKms(dateSelected).toString());
        calendarTrainings = root.findViewById(R.id.calendar_trainings);
        calendarTrainings.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            month += 1;
            if (month < 10) {
                if (dayOfMonth < 10) {
                    dateSelected = "0" + dayOfMonth + "/0" + month + "/" + year;
                } else {
                    dateSelected = dayOfMonth + "/0" + month + "/" + year;
                }
            } else {
                if (dayOfMonth < 10) {
                    dateSelected = "0" + dayOfMonth + "/" + month + "/" + year;
                } else {
                    dateSelected = dayOfMonth + "/" + month + "/" + year;
                }
            }
            monthlyKms.setText(String.format("%.02f", getKms(dateSelected)));
            adapterTrainings.getFilter().filter(dateSelected);
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public Float getKms(String dateSelected) {
        String month = dateSelected.substring(3,5);
        AtomicReference<Float> count = new AtomicReference<>(0.0f);
        db.collection("trainings").whereEqualTo("email", email).get()
                .addOnCompleteListener(task -> {
                    count.set(0f);
                    if (task.isSuccessful()) {
                        for (DocumentSnapshot snap : task.getResult()) {
                            Training training = snap.toObject(Training.class);
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                if (training.date.toDate().toInstant().toString().substring(5,7).equals(month)) {
                                    count.updateAndGet(v -> v + Float.valueOf(training.distance));
                                }
                            }
                        }
                        monthlyKms.setText(String.format("%.02f", count.get()));
                    }
                });
        return count.get();
    }
}