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
import com.ivione93.hojames.model.Fartlek;

public class AdapterFartlekTraining extends FirestoreRecyclerAdapter<Fartlek, AdapterFartlekTraining.FartlekViewHolder> {

    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    public AdapterFartlekTraining(@NonNull FirestoreRecyclerOptions<Fartlek> options) {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull FartlekViewHolder holder, int position, @NonNull Fartlek model) {
        holder.showFartlek.setText(model.fartlek);
    }

    @NonNull
    @Override
    public FartlekViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_fartlek_training
                , parent, false);
        return new AdapterFartlekTraining.FartlekViewHolder(view);
    }

    class FartlekViewHolder extends RecyclerView.ViewHolder {

        TextView showFartlek;

        public FartlekViewHolder(@NonNull View itemView) {
            super(itemView);
            showFartlek = itemView.findViewById(R.id.showFartlek);
        }
    }
}
