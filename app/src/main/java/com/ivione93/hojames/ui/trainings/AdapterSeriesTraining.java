package com.ivione93.hojames.ui.trainings;

import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.ivione93.hojames.R;
import com.ivione93.hojames.Utils;
import com.ivione93.hojames.model.Series;

public class AdapterSeriesTraining extends FirestoreRecyclerAdapter<Series, AdapterSeriesTraining.SeriesViewHolder> {

    public AdapterSeriesTraining(@NonNull FirestoreRecyclerOptions<Series> options) {
        super(options);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onBindViewHolder(@NonNull SeriesViewHolder holder, int position, @NonNull Series model) {
        holder.showDistanceSerie.setText(model.distance + " m");
        if(model.hurdles) {
            holder.hurdlesSerie.setVisibility(View.VISIBLE);
        } else {
            holder.hurdlesSerie.setVisibility(View.INVISIBLE);
        }
        if(model.drags) {
            holder.dragsSerie.setVisibility(View.VISIBLE);
        } else {
            holder.dragsSerie.setVisibility(View.INVISIBLE);
        }
        holder.showTimeSerie.setText(Utils.getFormattedResult(model.time));
        holder.shoeSerie.setText(model.shoes);
    }

    @NonNull
    @Override
    public SeriesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_series_training, parent, false);
        return new SeriesViewHolder(view);
    }

    static class SeriesViewHolder extends RecyclerView.ViewHolder {

        TextView showDistanceSerie, showTimeSerie, shoeSerie, hurdlesSerie, dragsSerie;

        public SeriesViewHolder(@NonNull View itemView) {
            super(itemView);
            showDistanceSerie = itemView.findViewById(R.id.showDistanceSerie);
            showTimeSerie = itemView.findViewById(R.id.showTimeSerie);
            shoeSerie = itemView.findViewById(R.id.shoeSerie);
            hurdlesSerie = itemView.findViewById(R.id.hurdlesSerie);
            dragsSerie = itemView.findViewById(R.id.dragsSerie);
        }
    }
}
