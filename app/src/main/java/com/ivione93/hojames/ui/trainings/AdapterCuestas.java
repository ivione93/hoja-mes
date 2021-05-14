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
import com.ivione93.hojames.model.Cuestas;

public class AdapterCuestas extends FirestoreRecyclerAdapter<Cuestas, AdapterCuestas.CuestasViewHolder> {

    public AdapterCuestas(@NonNull FirestoreRecyclerOptions<Cuestas> options) {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull CuestasViewHolder holder, int position, @NonNull Cuestas model) {
        holder.showTypeCuestas.setText(model.type);
        holder.showTimesCuestas.setText(String.valueOf(model.times));
    }

    @NonNull
    @Override
    public CuestasViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_cuestas, parent, false);
        return new AdapterCuestas.CuestasViewHolder(view);
    }

    class CuestasViewHolder extends RecyclerView.ViewHolder {

        TextView showTypeCuestas, showTimesCuestas;

        public CuestasViewHolder(@NonNull View itemView) {
            super(itemView);
            showTypeCuestas = itemView.findViewById(R.id.showTypeCuestas);
            showTimesCuestas = itemView.findViewById(R.id.showTimesCuestas);
        }
    }
}
