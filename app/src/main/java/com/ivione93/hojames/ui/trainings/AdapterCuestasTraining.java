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
import com.ivione93.hojames.model.Cuestas;

public class AdapterCuestasTraining extends FirestoreRecyclerAdapter<Cuestas, AdapterCuestasTraining.CuestasViewHolder> {

    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    public AdapterCuestasTraining(@NonNull FirestoreRecyclerOptions<Cuestas> options) {
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
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_cuestas_training, parent, false);
        return new AdapterCuestasTraining.CuestasViewHolder(view);
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
