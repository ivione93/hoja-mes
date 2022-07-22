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
import com.ivione93.hojames.model.Gym;

public class AdapterGymTraining extends FirestoreRecyclerAdapter<Gym, AdapterGymTraining.GymViewHolder> {

    public AdapterGymTraining(@NonNull FirestoreRecyclerOptions<Gym> options) {
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
    }

    @NonNull
    @Override
    public GymViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_gym_training, parent, false);
        return new GymViewHolder(view);
    }

    static class GymViewHolder extends RecyclerView.ViewHolder {

        TextView showExerciseGym, showTimesGym, showKilosGym;

        public GymViewHolder(@NonNull View itemView) {
            super(itemView);
            showExerciseGym = itemView.findViewById(R.id.showExerciseGym);
            showTimesGym = itemView.findViewById(R.id.showTimesGym);
            showKilosGym = itemView.findViewById(R.id.showKilosGym);
        }
    }
}
