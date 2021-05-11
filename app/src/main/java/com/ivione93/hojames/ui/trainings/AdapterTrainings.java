package com.ivione93.hojames.ui.trainings;

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
import com.ivione93.hojames.model.Training;
import com.ivione93.hojames.ui.competitions.NewCompetitionActivity;

public class AdapterTrainings extends FirestoreRecyclerAdapter<Training, AdapterTrainings.TrainingViewHolder> {

    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    public AdapterTrainings(@NonNull FirestoreRecyclerOptions<Training> options) {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull AdapterTrainings.TrainingViewHolder holder, int position, @NonNull Training model) {
        holder.itemTrainingDate.setText(Utils.toString(model.date));
        holder.itemTrainingTime.setText(model.time + " min");
        holder.itemTrainingDistance.setText(model.distance + " km");
        holder.itemTrainingPartial.setText(model.partial + " /km");

        holder.ibOptionsTraining.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(holder.itemView.getContext(), holder.ibOptionsTraining);
            popup.inflate(R.menu.item_training_menu);
            popup.setOnMenuItemClickListener(item -> {
                switch (item.getItemId()) {
                    case R.id.menu_edit_training:
                        Intent newTraining = new Intent(holder.itemView.getContext(), ViewTrainingActivity.class);
                        newTraining.putExtra("isNew", false);
                        newTraining.putExtra("idTraining", model.id);
                        newTraining.putExtra("license", model.license);
                        newTraining.putExtra("email", model.email);
                        holder.itemView.getContext().startActivity(newTraining);
                        return true;
                    case R.id.menu_delete_training:
                        db.collection("trainings").document(model.id).delete();
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
    public TrainingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_training, parent, false);
        return new AdapterTrainings.TrainingViewHolder(view);
    }

    class TrainingViewHolder extends RecyclerView.ViewHolder {

        TextView itemTrainingDate, itemTrainingTime, itemTrainingDistance, itemTrainingPartial;
        ImageButton ibOptionsTraining;

        public TrainingViewHolder(@NonNull View itemView) {
            super(itemView);
            itemTrainingDate = itemView.findViewById(R.id.itemTrainingDate);
            itemTrainingTime = itemView.findViewById(R.id.itemTrainingTime);
            itemTrainingDistance = itemView.findViewById(R.id.itemTrainingDistance);
            itemTrainingPartial = itemView.findViewById(R.id.itemTrainingPartial);
            ibOptionsTraining = itemView.findViewById(R.id.ibOptionsTraining);
        }
    }
}
