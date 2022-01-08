package com.ivione93.hojames.ui.trainings;

import android.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.FirebaseFirestore;
import com.ivione93.hojames.R;
import com.ivione93.hojames.Utils;
import com.ivione93.hojames.model.Series;

public class AdapterSeries extends FirestoreRecyclerAdapter<Series, AdapterSeries.SeriesViewHolder> {

    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    public AdapterSeries(@NonNull FirestoreRecyclerOptions<Series> options) {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull SeriesViewHolder holder, int position, @NonNull Series model) {
        holder.showDistanceSerie.setText(model.distance + " m");
        if(model.hurdles) {
            holder.hurdlesSerie.setVisibility(View.VISIBLE);
        } else {
            holder.hurdlesSerie.setVisibility(View.INVISIBLE);
        }
        holder.showTimeSerie.setText(Utils.getFormattedResult(model.time));
        holder.shoeSerie.setText(model.shoes);

        holder.ibDeleteSerie.setOnClickListener(v -> {
            AlertDialog.Builder deleteConfirm = new AlertDialog.Builder(v.getContext());
            deleteConfirm.setTitle(R.string.delete_serie);
            deleteConfirm.setMessage(R.string.delete_serie_confirm);
            deleteConfirm.setCancelable(false);
            deleteConfirm.setPositiveButton(R.string.accept, (dialog, which) -> {
                db.collection("series").document(model.id).delete();
                notifyItemRangeChanged(position, getItemCount());
                notifyItemRemoved(position);
            });
            deleteConfirm.setNegativeButton(R.string.cancel, (dialog, which) -> dialog.cancel());
            deleteConfirm.show();
        });
    }

    @NonNull
    @Override
    public SeriesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_series, parent, false);
        return new AdapterSeries.SeriesViewHolder(view);
    }

    class SeriesViewHolder extends RecyclerView.ViewHolder {

        TextView showDistanceSerie, showTimeSerie, shoeSerie, hurdlesSerie;
        ImageButton ibDeleteSerie;

        public SeriesViewHolder(@NonNull View itemView) {
            super(itemView);
            showDistanceSerie = itemView.findViewById(R.id.showDistanceSerie);
            showTimeSerie = itemView.findViewById(R.id.showTimeSerie);
            shoeSerie = itemView.findViewById(R.id.shoeSerie);
            hurdlesSerie = itemView.findViewById(R.id.hurdlesSerie);
            ibDeleteSerie = itemView.findViewById(R.id.ibDeleteSerie);
        }
    }
}
