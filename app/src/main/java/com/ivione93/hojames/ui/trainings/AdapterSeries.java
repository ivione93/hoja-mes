package com.ivione93.hojames.ui.trainings;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.ivione93.hojames.R;
import com.ivione93.hojames.model.Series;

public class AdapterSeries extends FirestoreRecyclerAdapter<Series, AdapterSeries.SeriesViewHolder> {

    public AdapterSeries(@NonNull FirestoreRecyclerOptions<Series> options) {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull SeriesViewHolder holder, int position, @NonNull Series model) {
        holder.showDistanceSerie.setText(model.distance + " m");
        holder.showTimeSerie.setText(model.time);
    }

    @NonNull
    @Override
    public SeriesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_series, parent, false);
        return new AdapterSeries.SeriesViewHolder(view);
    }

    class SeriesViewHolder extends RecyclerView.ViewHolder {

        TextView showDistanceSerie, showTimeSerie;

        public SeriesViewHolder(@NonNull View itemView) {
            super(itemView);
            showDistanceSerie = itemView.findViewById(R.id.showDistanceSerie);
            showTimeSerie = itemView.findViewById(R.id.showTimeSerie);
        }
    }
}
