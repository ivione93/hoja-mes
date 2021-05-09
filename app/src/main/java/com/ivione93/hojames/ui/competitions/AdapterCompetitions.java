package com.ivione93.hojames.ui.competitions;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.FirebaseFirestore;
import com.ivione93.hojames.R;
import com.ivione93.hojames.Utils;
import com.ivione93.hojames.model.Competition;

public class AdapterCompetitions extends FirestoreRecyclerAdapter<Competition, AdapterCompetitions.CompetitionViewHolder> {

    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    public AdapterCompetitions(@NonNull FirestoreRecyclerOptions<Competition> options) {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull CompetitionViewHolder holder, int position, @NonNull Competition model) {
        holder.name.setText(model.name);
        holder.place.setText(model.place);
        holder.track.setText(model.track);
        holder.result.setText(model.result);
        holder.date.setText(Utils.toString(model.date));

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
                        newCompetition.putExtra("email", model.email);
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

    @NonNull
    @Override
    public CompetitionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_competition, parent, false);
        return new CompetitionViewHolder(view);
    }

    class CompetitionViewHolder extends RecyclerView.ViewHolder {

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
}
