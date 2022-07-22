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

public class AdapterCuestas extends FirestoreRecyclerAdapter<Cuestas, AdapterCuestas.CuestasViewHolder> {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public AdapterCuestas(@NonNull FirestoreRecyclerOptions<Cuestas> options) {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull CuestasViewHolder holder, int position, @NonNull Cuestas model) {
        holder.showTypeCuestas.setText(model.type);
        holder.showTimesCuestas.setText(String.valueOf(model.times));

        holder.ibDeleteCuesta.setOnClickListener(v -> {
            AlertDialog.Builder deleteConfirm = new AlertDialog.Builder(v.getContext());
            deleteConfirm.setTitle(R.string.delete_cuesta);
            deleteConfirm.setMessage(R.string.delete_cuesta_confirm);
            deleteConfirm.setCancelable(false);
            deleteConfirm.setPositiveButton(R.string.accept, (dialog, which) -> {
                db.collection("cuestas").document(model.id).delete();
                notifyItemRangeChanged(position, getItemCount());
                notifyItemRemoved(position);
            });
            deleteConfirm.setNegativeButton(R.string.cancel, (dialog, which) -> dialog.cancel());
            deleteConfirm.show();
        });
    }

    @NonNull
    @Override
    public CuestasViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_cuestas, parent, false);
        return new CuestasViewHolder(view);
    }

    static class CuestasViewHolder extends RecyclerView.ViewHolder {

        TextView showTypeCuestas, showTimesCuestas;
        ImageButton ibDeleteCuesta;

        public CuestasViewHolder(@NonNull View itemView) {
            super(itemView);
            showTypeCuestas = itemView.findViewById(R.id.showTypeCuestas);
            showTimesCuestas = itemView.findViewById(R.id.showTimesCuestas);
            ibDeleteCuesta = itemView.findViewById(R.id.ibDeleteCuesta);
        }
    }
}
