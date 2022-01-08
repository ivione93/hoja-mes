package com.ivione93.hojames.ui.trainings;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.FirebaseFirestore;
import com.ivione93.hojames.R;
import com.ivione93.hojames.Utils;
import com.ivione93.hojames.dto.SeriesDto;
import com.ivione93.hojames.model.Series;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class AdapterSeries extends FirestoreRecyclerAdapter<Series, AdapterSeries.SeriesViewHolder> {

    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    public AdapterSeries(@NonNull FirestoreRecyclerOptions<Series> options) {
        super(options);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onBindViewHolder(@NonNull SeriesViewHolder holder, int position, @NonNull Series model) {
        holder.showDistanceSerie.setText(model.distance + " m");
        if(model.hurdles) {
            holder.hurdlesSerie.setVisibility(View.VISIBLE);
        } else {
            holder.hurdlesSerie.setVisibility(View.INVISIBLE);
        }
        holder.showTimeSerie.setText(Utils.getFormattedResult(model.time));
        holder.shoeSerie.setText(model.shoes);

        holder.cvSeries.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
            if (model.hurdles) {
                if (model.shoes != null) {
                    builder.setTitle("Editar serie: \n" + model.distance + " mV - " + model.time + " - " + model.shoes);
                } else {
                    builder.setTitle("Editar serie: \n" + model.distance + " mV - " + model.time);
                }
            } else {
                if (model.shoes != null) {
                    builder.setTitle("Editar serie: \n" + model.distance + " m - " + model.time + " - " + model.shoes);
                } else {
                    builder.setTitle("Editar serie: \n" + model.distance + " m - " + model.time);
                }
            }
            builder.setView(R.layout.dialog_add_series);
            builder.setPositiveButton(R.string.save, (dialog, which) -> updateSerie((AlertDialog) dialog, model, v));
            builder.setNegativeButton(R.string.cancel, (dialog, which) -> dialog.dismiss());
            builder.create().show();
        });

        holder.ibDeleteSerie.setOnClickListener(v -> {
            AlertDialog.Builder deleteConfirm = new AlertDialog.Builder(v.getContext());
            deleteConfirm.setTitle(R.string.delete_serie);
            deleteConfirm.setMessage(R.string.delete_serie_confirm);
            deleteConfirm.setCancelable(false);
            deleteConfirm.setPositiveButton(R.string.accept, (dialog, which) -> {
                db.collection("series").document(model.id).delete();
                notifyItemRangeChanged(position, getItemCount());
                notifyItemRemoved(position);
            });
            deleteConfirm.setNegativeButton(R.string.cancel, (dialog, which) -> dialog.cancel());
            deleteConfirm.show();
        });
    }

    private void updateSerie(AlertDialog dialog, Series model, View view) {
        EditText distanceSeries, timeSeries;
        distanceSeries = ((AlertDialog) dialog).findViewById(R.id.distance_series);
        timeSeries = ((AlertDialog) dialog).findViewById(R.id.time_series);
        Switch hurdles = ((AlertDialog) dialog).findViewById(R.id.swHurdles);

        if (distanceSeries.getText().toString().equals("") || timeSeries.getText().toString().equals("")) {
            Toast.makeText(view.getContext(), R.string.all_fields_mandatories, Toast.LENGTH_LONG).show();
        } else {
            if (validateTimeSeries(timeSeries.getText().toString())) {
                Map<String, Object> serie = new HashMap<>();
                serie.put("id", model.id);
                serie.put("idTraining", model.idTraining);
                serie.put("distance", distanceSeries.getText().toString());
                serie.put("time", timeSeries.getText().toString());
                serie.put("date", model.date);
                serie.put("hurdles", hurdles.isChecked());

                String shoes = null;
                RadioGroup radioGroupShoes = ((AlertDialog) dialog).findViewById(R.id.radioGroupShoes);
                RadioButton radioNormal, radioFlying, radioSpikes;
                radioNormal = ((AlertDialog) dialog).findViewById(R.id.radio_normal);
                radioFlying = ((AlertDialog) dialog).findViewById(R.id.radio_flying);
                radioSpikes = ((AlertDialog) dialog).findViewById(R.id.radio_spikes);
                if (radioGroupShoes.getCheckedRadioButtonId() == radioNormal.getId()) {
                    shoes = view.getContext().getString(R.string.normal_shoe);
                } else if (radioGroupShoes.getCheckedRadioButtonId() == radioFlying.getId()) {
                    shoes = view.getContext().getString(R.string.flying_shoe);
                } else if (radioGroupShoes.getCheckedRadioButtonId() == radioSpikes.getId()) {
                    shoes = view.getContext().getString(R.string.spike_shoe);
                }
                serie.put("shoes", shoes);

                db.collection("series").document(model.id).set(serie);
                Toast.makeText(view.getContext(), view.getContext().getString(R.string.serie_updated), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(view.getContext(), view.getContext().getString(R.string.wrong_time_format), Toast.LENGTH_LONG).show();
            }
        }
    }

    public boolean validateTimeSeries(String time) {
        String formatoHora = "\\d{1,2}h \\d{2}:\\d{2}\\.\\d{2}";
        return Pattern.matches(formatoHora, time);
    }

    @NonNull
    @Override
    public SeriesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_series, parent, false);
        return new AdapterSeries.SeriesViewHolder(view);
    }

    class SeriesViewHolder extends RecyclerView.ViewHolder {

        CardView cvSeries;
        TextView showDistanceSerie, showTimeSerie, shoeSerie, hurdlesSerie;
        ImageButton ibDeleteSerie;

        public SeriesViewHolder(@NonNull View itemView) {
            super(itemView);
            cvSeries = itemView.findViewById(R.id.cvSeries);
            showDistanceSerie = itemView.findViewById(R.id.showDistanceSerie);
            showTimeSerie = itemView.findViewById(R.id.showTimeSerie);
            shoeSerie = itemView.findViewById(R.id.shoeSerie);
            hurdlesSerie = itemView.findViewById(R.id.hurdlesSerie);
            ibDeleteSerie = itemView.findViewById(R.id.ibDeleteSerie);
        }
    }
}
