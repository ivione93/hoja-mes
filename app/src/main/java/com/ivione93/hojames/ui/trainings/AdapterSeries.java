package com.ivione93.hojames.ui.trainings;

import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.ivione93.hojames.model.Series;

public class AdapterSeries extends FirestoreRecyclerAdapter<Series, AdapterSeries.SeriesViewHlder> {

    public AdapterSeries(@NonNull FirestoreRecyclerOptions<Series> options) {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull AdapterSeries.SeriesViewHlder holder, int position, @NonNull Series model) {

    }

    @NonNull
    @Override
    public AdapterSeries.SeriesViewHlder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return null;
    }

    class SeriesViewHlder extends RecyclerView.ViewHolder {

        public SeriesViewHlder(@NonNull View itemView) {
            super(itemView);
        }
    }
}
