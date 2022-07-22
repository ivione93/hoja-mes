package com.ivione93.hojames.ui.competitions;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.FirebaseFirestore;
import com.ivione93.hojames.R;
import com.ivione93.hojames.Utils;
import com.ivione93.hojames.model.Competition;

public class AdapterCompetitions extends FirestoreRecyclerAdapter<Competition, AdapterCompetitions.CompetitionViewHolder> {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    @RequiresApi(api = Build.VERSION_CODES.N)
    public AdapterCompetitions(@NonNull FirestoreRecyclerOptions<Competition> options) {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull CompetitionViewHolder holder, int position, @NonNull Competition model) {
        holder.name.setText(model.name);
        holder.place.setText(model.place);
        holder.track.setText(model.track);
        holder.result.setText(Utils.getFormattedResult(model.result));
        holder.date.setText(Utils.toString(model.date, holder.itemView.getResources().getString(R.string.format_date)));
        if (model.type != null) {
            if(model.type.equals(holder.itemView.getContext().getString(R.string.bd_pc))) {
                holder.type.setText(R.string.type_pc);
                holder.imageTypeCompetition.setBackground(holder.itemView.getContext().getResources().getDrawable(R.drawable.bg_pista));
            }
            if(model.type.equals(holder.itemView.getContext().getString(R.string.bd_al))) {
                holder.type.setText(R.string.type_al);
                holder.imageTypeCompetition.setBackground(holder.itemView.getContext().getResources().getDrawable(R.drawable.bg_pista));
            }
            if(model.type.equals(holder.itemView.getContext().getString(R.string.bd_cross))) {
                holder.type.setText(R.string.type_cross);
                holder.imageTypeCompetition.setBackground(holder.itemView.getContext().getResources().getDrawable(R.drawable.bg_cross));
            }
            if(model.type.equals(holder.itemView.getContext().getString(R.string.bd_road))) {
                holder.type.setText(R.string.type_road);
                holder.imageTypeCompetition.setBackground(holder.itemView.getContext().getResources().getDrawable(R.drawable.bg_road));
            }
        }

        holder.competitionLayout.setOnClickListener(v -> {
            Intent competition = new Intent(holder.itemView.getContext(), CompetitionActivity.class);
            competition.putExtra("isNew", false);
            competition.putExtra("idCompetition", model.id);
            competition.putExtra("email", model.email);
            holder.itemView.getContext().startActivity(competition);
        });

        holder.ibOptionsCompetition.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(holder.itemView.getContext(), holder.ibOptionsCompetition);
            popup.inflate(R.menu.item_competition_menu);
            popup.setOnMenuItemClickListener(item -> {
                switch (item.getItemId()) {
                    case R.id.menu_delete_competition:
                        AlertDialog.Builder deleteConfirm = new AlertDialog.Builder(v.getContext());
                        deleteConfirm.setTitle(R.string.delete_competition);
                        deleteConfirm.setMessage(R.string.delete_competition_confirm);
                        deleteConfirm.setCancelable(false);
                        deleteConfirm.setPositiveButton(R.string.accept, (dialog, which) -> {
                            db.collection("competitions").document(model.id).delete();
                            notifyItemRangeChanged(position, getItemCount());
                            notifyItemRemoved(position);
                        });
                        deleteConfirm.setNegativeButton(R.string.cancel, (dialog, which) -> dialog.cancel());
                        deleteConfirm.show();
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

    @Override
    public void updateOptions(@NonNull FirestoreRecyclerOptions<Competition> options) {
        super.updateOptions(options);
    }

    class CompetitionViewHolder extends RecyclerView.ViewHolder {

        TextView name, place, track, result, date, type;
        ImageView imageTypeCompetition;
        ImageButton ibOptionsCompetition;

        ConstraintLayout competitionLayout;

        public CompetitionViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.competitionNameText);
            place = itemView.findViewById(R.id.placeText);
            track = itemView.findViewById(R.id.cTrackText);
            result = itemView.findViewById(R.id.resultText);
            date = itemView.findViewById(R.id.dateText);
            type = itemView.findViewById(R.id.typeText);
            ibOptionsCompetition = itemView.findViewById(R.id.ibOptionsCompetition);
            imageTypeCompetition = itemView.findViewById(R.id.imageTypeCompetition);

            competitionLayout = itemView.findViewById(R.id.competitionLayout);
        }
    }
}
