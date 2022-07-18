package com.ivione93.hojames.ui.competitions;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.chip.Chip;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.ivione93.hojames.R;
import com.ivione93.hojames.Utils;

public class CompetitionActivity extends AppCompatActivity {

    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    TextView cDate, cTrack, cResult, cName, cPlace;
    Chip cType;

    String email, id;
    Boolean isNew;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_competition);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        email = getIntent().getStringExtra("email");
        isNew = getIntent().getBooleanExtra("isNew", true);

        setup(isNew);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.new_competition_menu, menu);
        if (isNew) {
            menu.findItem(R.id.menu_edit_competition).setVisible(false);
            menu.findItem(R.id.menu_share_competition).setVisible(false);
        } else {
            menu.findItem(R.id.menu_new_competition).setVisible(false);
        }
        return true;
    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            getIntent().putExtra("email", email);
            onBackPressed();
            return true;
        }
        if (item.getItemId() == R.id.menu_edit_competition) {
            Intent newCompetition = new Intent(this, NewCompetitionActivity.class);
            newCompetition.putExtra("isNew", false);
            newCompetition.putExtra("idCompetition", id);
            newCompetition.putExtra("email", email);
            startActivity(newCompetition);
        }
        if (item.getItemId() == R.id.menu_share_competition) {
            try {
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                String msg = "*Hoja del mes*\n" +
                        "_Mira mi competición del " + cDate.getText().toString() + ":_\n\n" +
                        "*" + cName.getText() + "*\n" +
                        "- Lugar: " + cPlace.getText().toString() + "\n" +
                        "- Prueba: " + cTrack.getText().toString() + "\n" +
                        "- Marca: " + cResult.getText().toString();
                sendIntent.putExtra(Intent.EXTRA_TEXT, msg);
                sendIntent.setType("text/plain");

                Intent shareIntent = Intent.createChooser(sendIntent, "Compartir competición");
                startActivity(shareIntent);
            } catch (Exception e) {
                Toast.makeText(this, "Algo ha ido mal, prueba con otra aplicación", Toast.LENGTH_SHORT)
                        .show();
            }

        }
        return super.onOptionsItemSelected(item);
    }

    public void setup(boolean isNew) {
        cDate = findViewById(R.id.cDate);
        cType = findViewById(R.id.cType);
        cTrack = findViewById(R.id.cTrack);
        cResult = findViewById(R.id.cResult);
        cName = findViewById(R.id.cName);
        cPlace = findViewById(R.id.cPlace);

        if (!isNew) {
            getSupportActionBar().setTitle(R.string.title_competition);
            id = getIntent().getStringExtra("idCompetition");
            loadCompetition(id);
        }
    }

    private void loadCompetition(String id) {
        db.collection("competitions").whereEqualTo("id", id).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                QuerySnapshot document = task.getResult();
                if (!document.isEmpty()) {
                    cDate.setText(Utils.toString((Timestamp) task.getResult().getDocuments().get(0).get("date"), getString(R.string.format_date)));
                    cTrack.setText(task.getResult().getDocuments().get(0).get("track").toString());
                    cResult.setText(Utils.getFormattedResult(task.getResult().getDocuments().get(0).get("result").toString()));
                    cName.setText(task.getResult().getDocuments().get(0).get("name").toString());
                    cPlace.setText(task.getResult().getDocuments().get(0).get("place").toString());
                    if(task.getResult().getDocuments().get(0).get("type") != null) {
                        if(task.getResult().getDocuments().get(0).get("type").equals(getString(R.string.bd_pc))) {
                            cType.setText(getString(R.string.type_pc));
                        }
                        if(task.getResult().getDocuments().get(0).get("type").equals(getString(R.string.bd_al))) {
                            cType.setText(getString(R.string.type_al));
                        }
                        if(task.getResult().getDocuments().get(0).get("type").equals(getString(R.string.bd_cross))) {
                            cType.setText(getString(R.string.type_cross));
                        }
                        if(task.getResult().getDocuments().get(0).get("type").equals(getString(R.string.bd_road))) {
                            cType.setText(getString(R.string.type_road));
                        }
                    }
                }
            }
        });
    }

}