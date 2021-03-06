package com.ivione93.hojames.ui.competitions;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.chip.Chip;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.ivione93.hojames.MainActivity;
import com.ivione93.hojames.R;
import com.ivione93.hojames.Utils;

public class CompetitionActivity extends AppCompatActivity {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

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
        } else {
            menu.findItem(R.id.menu_new_competition).setVisible(false);
        }
        menu.findItem(R.id.menu_share_competition).setVisible(false);
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
        if (item.getItemId() == R.id.menu_options_competition) {
            showBottomSheetDialog();
        }
        if (item.getItemId() == R.id.menu_edit_competition) {
            editCompetition();
        }
        if (item.getItemId() == R.id.menu_share_competition) {
            shareCompetition();
        }
        return super.onOptionsItemSelected(item);
    }

    private void showBottomSheetDialog() {
        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        bottomSheetDialog.setContentView(R.layout.bottom_sheet_competition);

        LinearLayout editCompetitionL = bottomSheetDialog.findViewById(R.id.editCompetitionL);
        LinearLayout shareCompetitionL = bottomSheetDialog.findViewById(R.id.shareCompetitionL);
        LinearLayout deleteCompetitionL = bottomSheetDialog.findViewById(R.id.deleteCompetitionL);
        LinearLayout cancelL = bottomSheetDialog.findViewById(R.id.cancelL);

        bottomSheetDialog.show();

        if (editCompetitionL != null) {
            editCompetitionL.setOnClickListener(v -> {
                bottomSheetDialog.dismiss();
                editCompetition();
            });
        }

        if (shareCompetitionL != null) {
            shareCompetitionL.setOnClickListener(v -> {
                bottomSheetDialog.dismiss();
                shareCompetition();
            });
        }

        if (deleteCompetitionL != null) {
            deleteCompetitionL.setOnClickListener(v -> {
                bottomSheetDialog.dismiss();
                deleteCompetition();
            });
        }

        if (cancelL != null) {
            cancelL.setOnClickListener(v -> bottomSheetDialog.dismiss());
        }
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

    private void editCompetition() {
        Intent newCompetition = new Intent(this, NewCompetitionActivity.class);
        newCompetition.putExtra("isNew", false);
        newCompetition.putExtra("idCompetition", id);
        newCompetition.putExtra("email", email);
        startActivity(newCompetition);
    }

    private void shareCompetition() {
        try {
            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            String msg = "*Hoja del mes*\n" +
                    "_Mira mi competici??n del " + cDate.getText().toString() + ":_\n\n" +
                    "*" + cName.getText() + "*\n" +
                    "- Lugar: " + cPlace.getText().toString() + "\n" +
                    "- Prueba: " + cTrack.getText().toString() + "\n" +
                    "- Marca: " + cResult.getText().toString();
            sendIntent.putExtra(Intent.EXTRA_TEXT, msg);
            sendIntent.setType("text/plain");

            Intent shareIntent = Intent.createChooser(sendIntent, "Compartir competici??n");
            startActivity(shareIntent);
        } catch (Exception e) {
            Toast.makeText(this, "Algo ha ido mal, prueba con otra aplicaci??n", Toast.LENGTH_SHORT)
                    .show();
        }
    }

    private void deleteCompetition() {
        AlertDialog.Builder deleteConfirm = new AlertDialog.Builder(this);
        deleteConfirm.setTitle(R.string.delete_competition);
        deleteConfirm.setMessage(R.string.delete_competition_confirm);
        deleteConfirm.setCancelable(false);
        deleteConfirm.setPositiveButton(R.string.accept, (dialog, which) -> {
            db.collection("competitions").document(id).delete();
            goProfile(email);
        });
        deleteConfirm.setNegativeButton(R.string.cancel, (dialog, which) -> dialog.cancel());
        deleteConfirm.show();
    }

    private void goProfile(String email) {
        Intent profileIntent = new Intent(this, MainActivity.class);
        profileIntent.putExtra("email", email);
        startActivity(profileIntent);
        finish();
    }

}