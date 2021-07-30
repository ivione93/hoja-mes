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
import com.ivione93.hojames.model.Gym;

public class AdapterGym extends FirestoreRecyclerAdapter<Gym, AdapterGym.GymViewHolder> {

    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    public AdapterGym(@NonNull FirestoreRecyclerOptions<Gym> options) {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull GymViewHolder holder, int position, @NonNull Gym model) {
        holder.showExerciseGym.setText(model.exercise);
        if (!model.times.equals("")) {
            holder.showTimesGym.setText(model.times);
        }
        if (!model.kilos.equals("")) {
            holder.showKilosGym.setText(model.kilos + " kgs");
        }

        holder.ibDeleteGym.setOnClickListener(v -> {
            AlertDialog.Builder deleteConfirm = new AlertDialog.Builder(v.getContext());
            deleteConfirm.setTitle("Eliminar gimnasio");
            deleteConfirm.setMessage("¿Está seguro que quiere eliminar el ejercicio?\n\nATENCIÓN: Se elimina sin necesidad de guardar el entrenamiento");
            deleteConfirm.setCancelable(false);
            deleteConfirm.setPositiveButton("Aceptar", (dialog, which) -> {
                db.collection("gym").document(model.id).delete();
                notifyItemRangeChanged(position, getItemCount());
                notifyItemRemoved(position);
            });
            deleteConfirm.setNegativeButton("Cancelar", (dialog, which) -> dialog.cancel());
            deleteConfirm.show();
        });
    }

    @NonNull
    @Override
    public GymViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_gym, parent, false);
        return new AdapterGym.GymViewHolder(view);
    }

    class GymViewHolder extends RecyclerView.ViewHolder {

        TextView showExerciseGym, showTimesGym, showKilosGym;
        ImageButton ibDeleteGym;

        public GymViewHolder(@NonNull View itemView) {
            super(itemView);
            showExerciseGym = itemView.findViewById(R.id.showExerciseGym);
            showTimesGym = itemView.findViewById(R.id.showTimesGym);
            showKilosGym = itemView.findViewById(R.id.showKilosGym);
            ibDeleteGym = itemView.findViewById(R.id.ibDeleteGym);
        }
    }
}
