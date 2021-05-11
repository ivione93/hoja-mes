package com.ivione93.hojames.ui.trainings;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CalendarView;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.ivione93.hojames.R;
import com.ivione93.hojames.Utils;
import com.ivione93.hojames.model.Training;

import java.util.Date;

public class TrainingsFragment extends Fragment {

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference trainings = db.collection("trainings");
    private AdapterTrainings adapterTrainings;

    CalendarView calendarTrainings;

    String email, license;
    String dateSelected = Utils.toString(new Date());

    RecyclerView rvTrainings;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_trainings, container, false);

        setHasOptionsMenu(true);

        // Setup
        Bundle bundle = getActivity().getIntent().getExtras();
        email = bundle.getString("email");
        license = bundle.getString("license");

        setup(root);
        setupRecyclerView(root);

        return root;
    }

    @Override
    public void onStart() {
        super.onStart();
        adapterTrainings.startListening();
        db.collection("athlete").document(email).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    license = task.getResult().get("license").toString();
                }
            }
        });
    }

    @Override
    public void onStop() {
        super.onStop();
        adapterTrainings.stopListening();
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
            newTraining.putExtra("license", license);
            newTraining.putExtra("email", email);
            newTraining.putExtra("dateSelected", dateSelected);
            getContext().startActivity(newTraining);
        }
        return super.onOptionsItemSelected(item);
    }

    private void setup(View root) {
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
            //adapterTraining.getFilter().filter(dateSelected);
        });
    }

    private void setupRecyclerView(View root) {
        // Query
        Query query = trainings.whereEqualTo("license", license);
        //.orderBy("date", Query.Direction.DESCENDING);

        // Recycler options
        FirestoreRecyclerOptions<Training> options = new FirestoreRecyclerOptions.Builder<Training>()
                .setQuery(query, Training.class)
                .build();

        adapterTrainings = new AdapterTrainings(options);

        rvTrainings = root.findViewById(R.id.rvTrainings);
        rvTrainings.setHasFixedSize(true);
        rvTrainings.setLayoutManager(new LinearLayoutManager(root.getContext()));
        rvTrainings.setAdapter(adapterTrainings);
    }
}