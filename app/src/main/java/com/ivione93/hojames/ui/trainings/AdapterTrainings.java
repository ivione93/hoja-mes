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

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.ivione93.hojames.R;
import com.ivione93.hojames.Utils;
import com.ivione93.hojames.model.Training;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class AdapterTrainings extends RecyclerView.Adapter<AdapterTrainings.ViewHolderTraining> implements Filterable {

    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    List<Training> listTrainings;
    List<Training> listTrainingsFull;

    public AdapterTrainings(List<Training> listTrainings) {
        this.listTrainings = listTrainings;
        this.listTrainingsFull = new ArrayList<>(listTrainings);
    }

    @NonNull
    @Override
    public AdapterTrainings.ViewHolderTraining onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_training_series, parent, false);
        return new ViewHolderTraining(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AdapterTrainings.ViewHolderTraining holder, int position) {
        holder.itemTrainingDate.setText(Utils.toString(listTrainings.get(position).date));
        holder.itemTrainingTime.setText(Utils.getFormattedTime(listTrainings.get(position).time) + " min");
        holder.itemTrainingDistance.setText(listTrainings.get(position).distance + " km");
        holder.itemTrainingPartial.setText(listTrainings.get(position).partial + " /km");

        // check series
        db.collection("series").whereEqualTo("idTraining", listTrainings.get(position).id).get().addOnCompleteListener(task -> {
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

        // check cuestas
        db.collection("cuestas").whereEqualTo("idTraining", listTrainings.get(position).id).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                QuerySnapshot document = task.getResult();
                if (document.isEmpty()) {
                    holder.tvIndicadorCuestas.setVisibility(View.INVISIBLE);
                    holder.ivIndicadorCuestas.setVisibility(View.INVISIBLE);
                } else {
                    holder.tvIndicadorCuestas.setVisibility(View.VISIBLE);
                    holder.ivIndicadorCuestas.setVisibility(View.VISIBLE);
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
                        newTraining.putExtra("idTraining", listTrainings.get(position).id);
                        newTraining.putExtra("license", listTrainings.get(position).license);
                        newTraining.putExtra("email", listTrainings.get(position).email);
                        holder.itemView.getContext().startActivity(newTraining);
                        return true;
                    case R.id.menu_delete_training:
                        db.collection("trainings").document(listTrainings.get(position).id).delete();
                        db.collection("series").whereEqualTo("idTraining", listTrainings.get(position).id).get().addOnCompleteListener(task -> {
                            if (task.isSuccessful()){
                                for (DocumentSnapshot doc : task.getResult().getDocuments()) {
                                    db.collection("series").document(doc.getId()).delete();
                                }
                            }
                        });
                        db.collection("cuestas").whereEqualTo("idTraining", listTrainings.get(position).id).get().addOnCompleteListener(task -> {
                            if (task.isSuccessful()){
                                for (DocumentSnapshot doc : task.getResult().getDocuments()) {
                                    db.collection("cuestas").document(doc.getId()).delete();
                                }
                            }
                        });
                        notifyItemRemoved(position);
                        return true;
                    default:
                        return false;
                }
            });
            popup.show();
        });
    }

    @Override
    public int getItemCount() {
        return listTrainings.size();
    }

    @Override
    public Filter getFilter() {
        return filter;
    }

    private Filter filter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            List<Training> filteredTrainings = new ArrayList<>();
            if (constraint == null || constraint.length() == 0) {
                filteredTrainings.addAll(filteredTrainings);
            } else {
                String filterPattern = constraint.toString();
                for (Training training : listTrainingsFull) {
                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
                    String actualTrainingDate = sdf.format(training.date.toDate());
                    if (actualTrainingDate.equals(filterPattern)) {
                        filteredTrainings.add(training);
                    }
                }
            }
            FilterResults results = new FilterResults();
            results.values = filteredTrainings;

            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            listTrainings.clear();
            listTrainings.addAll((List) results.values);
            notifyDataSetChanged();
        }
    };

    class ViewHolderTraining extends RecyclerView.ViewHolder {

        TextView itemTrainingDate, itemTrainingTime, itemTrainingDistance, itemTrainingPartial;
        ImageButton ibOptionsTraining;

        TextView tvIndicadorSeries, tvIndicadorCuestas;
        ImageView ivIndicadorSeries, ivIndicadorCuestas;

        public ViewHolderTraining(@NonNull View itemView) {
            super(itemView);
            itemTrainingDate = itemView.findViewById(R.id.itemTrainingDate);
            itemTrainingTime = itemView.findViewById(R.id.itemTrainingTime);
            itemTrainingDistance = itemView.findViewById(R.id.itemTrainingDistance);
            itemTrainingPartial = itemView.findViewById(R.id.itemTrainingPartial);
            ibOptionsTraining = itemView.findViewById(R.id.ibOptionsTraining);

            tvIndicadorSeries = itemView.findViewById(R.id.tvIndicadorSeries);
            ivIndicadorSeries = itemView.findViewById(R.id.ivIndicadorSeries);

            tvIndicadorCuestas = itemView.findViewById(R.id.tvIndicadorCuestas);
            ivIndicadorCuestas = itemView.findViewById(R.id.ivIndicadorCuestas);
        }
    }
}
