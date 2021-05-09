package com.ivione93.hojames.ui.competitions;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.ivione93.hojames.R;
import com.ivione93.hojames.model.Competition;

public class CompetitionsFragment extends Fragment {

    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    String email, license;

    RecyclerView rvCompetitions;
    FirestoreRecyclerAdapter adapter;

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

        rvCompetitions = root.findViewById(R.id.rvCompetitions);

        // Query
        Query query = db.collection("competitions").whereEqualTo("license", license);

        // Recycler options
        FirestoreRecyclerOptions<Competition> options = new FirestoreRecyclerOptions.Builder<Competition>()
                .setQuery(query, Competition.class)
                .build();

        adapter = new FirestoreRecyclerAdapter<Competition, CompetitionViewHolder>(options) {
            @NonNull
            @Override
            public CompetitionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_competition, parent, false);
                return new CompetitionViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull CompetitionViewHolder holder, int position, @NonNull Competition model) {
                holder.name.setText(model.name);
                holder.place.setText(model.place);
                holder.track.setText(model.track);
                holder.result.setText(model.result);
                holder.date.setText(model.date);

                holder.ibOptionsCompetition.setOnClickListener(v -> {
                    PopupMenu popup = new PopupMenu(holder.itemView.getContext(), holder.ibOptionsCompetition);
                    popup.inflate(R.menu.item_competition_menu);
                    popup.setOnMenuItemClickListener(item -> {
                        switch (item.getItemId()) {
                            case R.id.menu_edit_competition:
                                Intent newCompetition = new Intent(holder.itemView.getContext(), NewCompetitionActivity.class);
                                newCompetition.putExtra("isNew", false);
                                newCompetition.putExtra("idCompetition", model.id);
                                newCompetition.putExtra("license", model.license);
                                newCompetition.putExtra("email", email);
                                holder.itemView.getContext().startActivity(newCompetition);
                                return true;
                            case R.id.menu_delete_competition:
                                db.collection("competitions").document(model.id).delete();
                                notifyItemRemoved(position);
                                return true;
                            default:
                                return false;
                        }
                    });
                    popup.show();
                });
            }
        };

        rvCompetitions.setHasFixedSize(true);
        rvCompetitions.setLayoutManager(new LinearLayoutManager(root.getContext()));
        rvCompetitions.setAdapter(adapter);

        return root;
    }

    private class CompetitionViewHolder extends RecyclerView.ViewHolder {

        TextView name, place, track, result, date;
        ImageButton ibOptionsCompetition;

        public CompetitionViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.competitionNameText);
            place = itemView.findViewById(R.id.placeText);
            track = itemView.findViewById(R.id.surnameText);
            result = itemView.findViewById(R.id.resultText);
            date = itemView.findViewById(R.id.dateText);
            ibOptionsCompetition = itemView.findViewById(R.id.ibOptionsCompetition);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        adapter.startListening();
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
        adapter.stopListening();
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