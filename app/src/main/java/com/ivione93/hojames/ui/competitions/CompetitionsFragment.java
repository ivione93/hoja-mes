package com.ivione93.hojames.ui.competitions;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.ivione93.hojames.R;
import com.ivione93.hojames.model.Competition;

public class CompetitionsFragment extends Fragment {

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference competitions = db.collection("competitions");
    private AdapterCompetitions adapterCompetitions;

    String email, license;

    RecyclerView rvCompetitions;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_competitions, container, false);

        setHasOptionsMenu(true);

        // Setup
        Bundle bundle = getActivity().getIntent().getExtras();
        email = bundle.getString("email");
        license = bundle.getString("license");

        setupRecyclerView(root);

        return root;
    }

    private void setupRecyclerView(View root) {
        // Query
        Query query = db.collection("competitions")
                .whereEqualTo("license", license);
                //.orderBy("date", Query.Direction.DESCENDING);

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
            newCompetition.putExtra("license", license);
            getContext().startActivity(newCompetition);
        }
        return super.onOptionsItemSelected(item);
    }
}