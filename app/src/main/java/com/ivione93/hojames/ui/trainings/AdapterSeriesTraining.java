package com.ivione93.hojames.ui.trainings;

import android.app.AlertDialog;
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
import com.ivione93.hojames.model.Series;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class AdapterSeriesTraining extends FirestoreRecyclerAdapter<Series, AdapterSeriesTraining.SeriesViewHolder> {

    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    public AdapterSeriesTraining(@NonNull FirestoreRecyclerOptions<Series> options) {
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
        if(model.drags) {
            holder.dragsSerie.setVisibility(View.VISIBLE);
        } else {
            holder.dragsSerie.setVisibility(View.INVISIBLE);
        }
        holder.showTimeSerie.setText(Utils.getFormattedResult(model.time));
        holder.shoeSerie.setText(model.shoes);
    }

    private void updateSerie(AlertDialog dialog, Series model, View view) {
        EditText distanceSeries, timeSeries;
        distanceSeries = ((AlertDialog) dialog).findViewById(R.id.distance_series);
        timeSeries = ((AlertDialog) dialog).findViewById(R.id.time_series);
        Switch hurdles = ((AlertDialog) dialog).findViewById(R.id.swHurdles);
        Switch drags = ((AlertDialog) dialog).findViewById(R.id.swDrags);

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
                serie.put("drags", drags.isChecked());

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
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_series_training, parent, false);
        return new AdapterSeriesTraining.SeriesViewHolder(view);
    }

    class SeriesViewHolder extends RecyclerView.ViewHolder {

        TextView showDistanceSerie, showTimeSerie, shoeSerie, hurdlesSerie, dragsSerie;

        public SeriesViewHolder(@NonNull View itemView) {
            super(itemView);
            showDistanceSerie = itemView.findViewById(R.id.showDistanceSerie);
            showTimeSerie = itemView.findViewById(R.id.showTimeSerie);
            shoeSerie = itemView.findViewById(R.id.shoeSerie);
            hurdlesSerie = itemView.findViewById(R.id.hurdlesSerie);
            dragsSerie = itemView.findViewById(R.id.dragsSerie);
        }
    }
}
