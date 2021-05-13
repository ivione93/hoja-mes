package com.ivione93.hojames.ui.trainings;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.firebase.ui.firestore.ObservableSnapshotArray;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.ivione93.hojames.R;
import com.ivione93.hojames.Utils;
import com.ivione93.hojames.model.Training;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class AdapterTrainings extends FirestoreRecyclerAdapter<Training, AdapterTrainings.TrainingViewHolder> implements Filterable {

    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    private ObservableSnapshotArray<Training> mSnapshots;
    private List<Training> list;

    public AdapterTrainings(@NonNull FirestoreRecyclerOptions<Training> options) {
        super(options);
        mSnapshots = options.getSnapshots();
        list = new ArrayList<>(mSnapshots);
    }

    @Override
    protected void onBindViewHolder(@NonNull AdapterTrainings.TrainingViewHolder holder, int position, @NonNull Training model) {
        holder.itemTrainingDate.setText(Utils.toString(model.date));
        holder.itemTrainingTime.setText(model.time + " min");
        holder.itemTrainingDistance.setText(model.distance + " km");
        holder.itemTrainingPartial.setText(model.partial + " /km");

        // check series
        db.collection("series").whereEqualTo("idTraining", model.id).get().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot document = task.getResult();
                        if (document.isEmpty()) {
                            holder.tvIndicadorSeries.setVisibility(View.INVISIBLE);
                            holder.ivIndicadorSeries.setVisibility(View.INVISIBLE);
                        } else {
                            holder.tvIndicadorSeries.setVisibility(View.VISIBLE);
                            holder.ivIndicadorSeries.setVisibility(View.VISIBLE);
                        }
                    }
                });

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
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_training_series, parent, false);
        return new AdapterTrainings.TrainingViewHolder(view);
    }

    @Override
    public Filter getFilter() {
        return filter;
    }

    private Filter filter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults results = new FilterResults();
            if (constraint == null || constraint.length() == 0) {
                results.values = mSnapshots;
                results.count = mSnapshots.size();
            } else {
                List<Training> filteredTrainings = new ArrayList<>();
                String filterPattern = constraint.toString().toLowerCase().trim();
                for (Training training : mSnapshots) {
                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
                    String actualTrainingDate = sdf.format(training.date.toDate());
                    if (actualTrainingDate.equals(filterPattern)) {
                        filteredTrainings.add(training);
                        results.values = filteredTrainings;
                        results.count = filteredTrainings.size();
                    }
                }
            }

            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            list.clear();
            if (results.count > 0) {
                list.addAll((List) results.values);
            }
            notifyDataSetChanged();
        }
    };

    class TrainingViewHolder extends RecyclerView.ViewHolder {

        TextView itemTrainingDate, itemTrainingTime, itemTrainingDistance, itemTrainingPartial;
        ImageButton ibOptionsTraining;

        TextView tvIndicadorSeries;
        ImageView ivIndicadorSeries;

        public TrainingViewHolder(@NonNull View itemView) {
            super(itemView);
            itemTrainingDate = itemView.findViewById(R.id.itemTrainingDate);
            itemTrainingTime = itemView.findViewById(R.id.itemTrainingTime);
            itemTrainingDistance = itemView.findViewById(R.id.itemTrainingDistance);
            itemTrainingPartial = itemView.findViewById(R.id.itemTrainingPartial);
            ibOptionsTraining = itemView.findViewById(R.id.ibOptionsTraining);

            tvIndicadorSeries = itemView.findViewById(R.id.tvIndicadorSeries);
            ivIndicadorSeries = itemView.findViewById(R.id.ivIndicadorSeries);
        }
    }
}
