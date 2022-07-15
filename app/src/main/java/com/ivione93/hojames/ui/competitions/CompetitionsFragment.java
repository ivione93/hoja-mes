package com.ivione93.hojames.ui.competitions;

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

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.material.chip.Chip;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.ivione93.hojames.R;
import com.ivione93.hojames.model.Competition;

import java.util.concurrent.atomic.AtomicReference;

public class CompetitionsFragment extends Fragment {

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference competitions = db.collection("competitions");
    private AdapterCompetitions adapterCompetitions;

    String email;
    Chip chipTotals, chipPC, chipAL, chipCross, chipRuta;

    RecyclerView rvCompetitions;

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_competitions, container, false);

        setHasOptionsMenu(true);

        // Setup
        Bundle bundle = getActivity().getIntent().getExtras();
        email = bundle.getString("email");

        setupRecyclerView(root);

        setup(root);

        return root;
    }

    private void setupRecyclerView(View root) {
        // Query
        Query query = competitions.whereEqualTo("email", email)
                .orderBy("date", Query.Direction.DESCENDING);

        // Recycler options
        FirestoreRecyclerOptions<Competition> options = new FirestoreRecyclerOptions.Builder<Competition>()
                .setQuery(query, Competition.class)
                .build();

        adapterCompetitions = new AdapterCompetitions(options);

        rvCompetitions = root.findViewById(R.id.rvCompetitions);
        rvCompetitions.setHasFixedSize(true);
        rvCompetitions.setLayoutManager(new LinearLayoutManager(root.getContext()));
        rvCompetitions.setAdapter(adapterCompetitions);
    }

    @Override
    public void onStart() {
        super.onStart();
        adapterCompetitions.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        adapterCompetitions.stopListening();
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.list_competitions_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.menu_add_competition) {
            Intent newCompetition = new Intent(getActivity(), NewCompetitionActivity.class);
            newCompetition.putExtra("isNew", true);
            newCompetition.putExtra("email", email);
            getContext().startActivity(newCompetition);
        }
        return super.onOptionsItemSelected(item);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void setup(View root) {
        chipTotals = root.findViewById(R.id.chipTotals);
        chipPC = root.findViewById(R.id.chipPC);
        chipAL = root.findViewById(R.id.chipAL);
        chipCross = root.findViewById(R.id.chipCross);
        chipRuta = root.findViewById(R.id.chipRuta);

        // Actualiza todos los totales
        chipTotals.setText(String.valueOf(getTotalCompetitions()));
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public Integer getTotalCompetitions() {
        AtomicReference<Integer> count = new AtomicReference<>(0);
        AtomicReference<Integer> countPC = new AtomicReference<>(0);
        AtomicReference<Integer> countAL = new AtomicReference<>(0);
        AtomicReference<Integer> countCross = new AtomicReference<>(0);
        AtomicReference<Integer> countRuta = new AtomicReference<>(0);
        db.collection("competitions").whereEqualTo("email", email).get()
                .addOnCompleteListener(task -> {
                    count.set(0);
                    if (task.isSuccessful()) {
                        for (DocumentSnapshot snap : task.getResult()) {
                            Competition competition = snap.toObject(Competition.class);
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                if (competition.email.equals(email)) {
                                    count.updateAndGet(v -> v + 1);
                                    if (competition.type != null && competition.type.equals("PC")) {
                                        countPC.updateAndGet(v -> v + 1);
                                    } else if (competition.type != null && competition.type.equals("AL")) {
                                        countAL.updateAndGet(v -> v + 1);
                                    } else if (competition.type != null && competition.type.equals("Cross")) {
                                        countCross.updateAndGet(v -> v + 1);
                                    } else if (competition.type != null && competition.type.equals("Ruta")) {
                                        countRuta.updateAndGet(v -> v + 1);
                                    }
                                }
                            }
                        }
                        chipTotals.setText("Totales " +  String.valueOf(count.get()));
                        chipPC.setText("Pista cubierta " + String.valueOf(countPC.get()));
                        chipAL.setText("Aire libre " + String.valueOf(countAL.get()));
                        chipCross.setText("Cross " + String.valueOf(countCross.get()));
                        chipRuta.setText("Ruta " + String.valueOf(countRuta.get()));
                    }
                });
        return count.get();
    }
}