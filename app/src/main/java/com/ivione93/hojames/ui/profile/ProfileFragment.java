package com.ivione93.hojames.ui.profile;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.Timestamp;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.ivione93.hojames.R;
import com.ivione93.hojames.Utils;
import com.ivione93.hojames.model.Competition;
import com.ivione93.hojames.model.Training;
import com.ivione93.hojames.ui.competitions.CompetitionActivity;
import com.ivione93.hojames.ui.competitions.NewCompetitionActivity;
import com.ivione93.hojames.ui.login.AuthActivity;
import com.ivione93.hojames.ui.trainings.TrainingActivity;
import com.ivione93.hojames.ui.trainings.ViewTrainingActivity;

import java.util.Date;
import java.util.concurrent.atomic.AtomicReference;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileFragment extends Fragment {

    SharedPreferences.Editor prefs;

    private FirebaseAnalytics mFirebaseAnalytics;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final CollectionReference competitions = db.collection("competitions");

    GoogleSignInClient mGoogleSignInClient;

    CircleImageView photoProfile;
    TextView emailTextView, nombreEditText, birthEditText;
    TextView statTotalTrainings, statTotalKms, statTotalCompetitions;
    TextView last_competition_name, last_competition_place, last_competition_date, last_competition_track, last_competition_result, last_competition_type;
    TextView title_type, last_training_date, title_time, title_distance, title_partial, last_training_time, last_training_distance, last_training_partial;
    ImageView imageTypeCompetition;
    CardView lastTrainingCV, lastCompetitionCV;

    String email;
    String dateSelected;
    Uri photoUrl;

    private ProgressDialog progressDialog;

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_profile, container, false);
        setHasOptionsMenu(true);

        // Setup
        Bundle bundle = getActivity().getIntent().getExtras();
        email = bundle.getString("email");
        setup(root, email);

        loadProfile();

        // Inflate the layout for this fragment
        return root;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void loadProfile() {
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(getContext());
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(getContext(), gso);

        if (account != null) {
            if (account.getPhotoUrl() != null) {
                photoUrl = account.getPhotoUrl();
                Glide.with(getActivity().getApplicationContext()).load(account.getPhotoUrl()).into(photoProfile);
            }
        }
        progressDialog.setMessage(getString(R.string.loading_data));
        progressDialog.setCancelable(false);
        progressDialog.show();
        db.collection("athlete").document(email).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    nombreEditText.setText(task.getResult().get("name").toString() + " " + task.getResult().get("surname").toString());
                    birthEditText.setText(task.getResult().get("birth").toString());

                    statTotalTrainings.setText(String.valueOf(getTotalTrainings()));
                    statTotalKms.setText(String.valueOf(getTotalTrainings()));
                    statTotalCompetitions.setText(String.valueOf(getTotalCompetitions()));

                    getLastCompetition(email);
                    getLastTraining(email);

                    // Guardar datos
                    prefs = getActivity().getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE).edit();
                    prefs.putString("email", email);
                    prefs.apply();

                    getActivity().getIntent().putExtra("email", email);
                } else {
                    progressDialog.dismiss();
                }
            } else {
                progressDialog.dismiss();
            }
        });
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.profile_menu, menu);
        super.onCreateOptionsMenu(menu,inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.menu_profile_options) {
            showBottomSheetDialog();
        }
        if (item.getItemId() == R.id.menu_log_out) {
            // Borrado datos inicio de sesion
            new AlertDialog.Builder(getActivity())
                    .setTitle(R.string.exit)
                    .setMessage(getString(R.string.log_out))
                    .setCancelable(false)
                    .setPositiveButton(getString(R.string.exit), (dialogInterface, i) -> {
                        SharedPreferences.Editor prefs = getActivity().getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE).edit();
                        prefs.remove("email");
                        prefs.apply();

                        FirebaseAuth.getInstance().signOut();
                        mGoogleSignInClient.signOut();

                        //Evento cerrar sesion Analytics
                        Bundle bundle = new Bundle();
                        bundle.putString("message", "Cerrar de sesion");
                        bundle.putString("user", email);
                        mFirebaseAnalytics.logEvent("logout", bundle);

                        Intent mainIntent = new Intent(getActivity().getApplicationContext(), AuthActivity.class);
                        startActivity(mainIntent);
                        getActivity().finish();
                    })
                    .setNegativeButton(getString(R.string.cancel), (dialogInterface, i) -> dialogInterface.dismiss())
                    .show();
        }
        return super.onOptionsItemSelected(item);
    }

    private void showBottomSheetDialog() {
        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(getActivity());
        bottomSheetDialog.setContentView(R.layout.bottom_sheet_profile);

        LinearLayout editProfileL = bottomSheetDialog.findViewById(R.id.editProfileL);
        LinearLayout addTrainingL = bottomSheetDialog.findViewById(R.id.addTrainingL);
        LinearLayout addCompetitionL = bottomSheetDialog.findViewById(R.id.addCompetitionL);
        LinearLayout cancelL = bottomSheetDialog.findViewById(R.id.cancelL);

        bottomSheetDialog.show();

        if (editProfileL != null) {
            editProfileL.setOnClickListener(v -> {
                Intent editProfile = new Intent(getActivity(), EditProfileActivity.class);
                editProfile.putExtra("email", email);
                if (photoUrl != null) {
                    editProfile.putExtra("photoUrl", photoUrl.toString());
                }
                bottomSheetDialog.dismiss();
                getContext().startActivity(editProfile);
            });
        }

        if (addTrainingL != null) {
            addTrainingL.setOnClickListener(v -> {
                Intent viewTraining = new Intent(getActivity(), ViewTrainingActivity.class);
                viewTraining.putExtra("email", email);
                viewTraining.putExtra("dateSelected", dateSelected);
                bottomSheetDialog.dismiss();
                getContext().startActivity(viewTraining);
            });
        }

        if (addCompetitionL != null) {
            addCompetitionL.setOnClickListener(v -> {
                Intent newCompetition = new Intent(getActivity(), NewCompetitionActivity.class);
                newCompetition.putExtra("isNew", true);
                newCompetition.putExtra("email", email);
                bottomSheetDialog.dismiss();
                getContext().startActivity(newCompetition);
            });
        }

        if (cancelL != null) {
            cancelL.setOnClickListener(v -> bottomSheetDialog.dismiss());
        }
    }

    private void setup(View root, String email) {
        Resources res = root.getResources();
        String formatDate = res.getString(R.string.format_date);
        dateSelected = Utils.toString(new Date(), formatDate);
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(getActivity());

        progressDialog = new ProgressDialog(getContext());
        photoProfile = root.findViewById(R.id.photoProfile);
        emailTextView = root.findViewById(R.id.emailTextView);
        emailTextView.setText(email);
        nombreEditText = root.findViewById(R.id.nombreEditText);
        birthEditText = root.findViewById(R.id.birthEditText);

        statTotalTrainings = root.findViewById(R.id.statistics_total_trainings_value);
        statTotalKms = root.findViewById(R.id.statistics_total_kms_value);
        statTotalCompetitions = root.findViewById(R.id.statistics_total_competitions_value);

        last_competition_name = root.findViewById(R.id.lcName);
        last_competition_place = root.findViewById(R.id.lcPlace);
        last_competition_date = root.findViewById(R.id.lcDate);
        last_competition_track = root.findViewById(R.id.lcTrack);
        last_competition_result = root.findViewById(R.id.lcResult);
        last_competition_type = root.findViewById(R.id.lcTrackTV);
        imageTypeCompetition = root.findViewById(R.id.lcBackground);

        title_type = root.findViewById(R.id.ltType);
        last_training_date = root.findViewById(R.id.ltDate);
        title_time = root.findViewById(R.id.ltTimeTV);
        title_distance = root.findViewById(R.id.ltDistanceTV);
        title_partial = root.findViewById(R.id.ltPartialTV);
        last_training_time = root.findViewById(R.id.ltTime);
        last_training_distance = root.findViewById(R.id.ltDistance);
        last_training_partial = root.findViewById(R.id.ltPartial);

        lastTrainingCV = root.findViewById(R.id.cardLastTraining);
        lastCompetitionCV = root.findViewById(R.id.cardLastCompetition);

        photoProfile.setOnClickListener(v -> {
            Intent editProfile = new Intent(getActivity(), EditProfileActivity.class);
            editProfile.putExtra("email", email);
            if (photoUrl != null) {
                editProfile.putExtra("photoUrl", photoUrl.toString());
            }
            getContext().startActivity(editProfile);
        });
    }

    private void getLastCompetition(String email) {
        competitions.whereEqualTo("email", email)
                .orderBy("date", Query.Direction.DESCENDING)
                .limit(1)
                .get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                progressDialog.dismiss();
                if (task.getResult().size() > 0) {
                    for (QueryDocumentSnapshot documentSnapshot : task.getResult()) {
                        if (documentSnapshot.exists()) {
                            lastCompetitionCV.setOnClickListener(v -> {
                                Intent competition = new Intent(getContext(), CompetitionActivity.class);
                                competition.putExtra("isNew", false);
                                competition.putExtra("idCompetition", documentSnapshot.get("id").toString());
                                competition.putExtra("email", email);
                                startActivity(competition);
                            });
                            last_competition_name.setText(documentSnapshot.get("name").toString());
                            last_competition_place.setText(documentSnapshot.get("place").toString());
                            last_competition_date.setText(Utils.toString((Timestamp) documentSnapshot.get("date"), getString(R.string.format_date)));
                            last_competition_track.setText(documentSnapshot.get("track").toString());
                            last_competition_result.setText(Utils.getFormattedResult(documentSnapshot.get("result").toString()));
                            if(documentSnapshot.get("type") != null) {
                                if (documentSnapshot.get("type").toString().equals(getString(R.string.bd_pc))) {
                                    last_competition_type.setText(getString(R.string.type_pc));
                                    imageTypeCompetition.setBackground(getResources().getDrawable(R.drawable.bg_pista));
                                }
                                if (documentSnapshot.get("type").toString().equals(getString(R.string.bd_al))) {
                                    last_competition_type.setText(getString(R.string.type_al));
                                    imageTypeCompetition.setBackground(getResources().getDrawable(R.drawable.bg_pista));
                                }
                                if (documentSnapshot.get("type").toString().equals(getString(R.string.bd_cross))) {
                                    last_competition_type.setText(getString(R.string.type_cross));
                                    imageTypeCompetition.setBackground(getResources().getDrawable(R.drawable.bg_cross));
                                }
                                if (documentSnapshot.get("type").toString().equals(getString(R.string.bd_road))) {
                                    last_competition_type.setText(getString(R.string.type_road));
                                    imageTypeCompetition.setBackground(getResources().getDrawable(R.drawable.bg_road));
                                }
                            }
                        } else {
                            last_competition_track.setText(R.string.no_results);
                        }
                    }
                } else {
                    last_competition_track.setText(R.string.no_results);
                }
            } else {
                progressDialog.dismiss();
                last_competition_track.setText(R.string.no_results);
            }
        });
    }

    private void getLastTraining(String email) {
        db.collection("trainings")
                .whereEqualTo("email", email)
                .orderBy("date", Query.Direction.DESCENDING)
                .limit(1)
                .get().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        progressDialog.dismiss();
                        if (task.getResult().size() > 0) {
                            for (QueryDocumentSnapshot documentSnapshot : task.getResult()) {
                                if (documentSnapshot.exists()) {
                                    lastTrainingCV.setOnClickListener(v -> {
                                        Intent training = new Intent(getContext(), TrainingActivity.class);
                                        training.putExtra("isNew", false);
                                        training.putExtra("idTraining", documentSnapshot.get("id").toString());
                                        training.putExtra("email", email);
                                        startActivity(training);
                                    });

                                    String partialFormat = " /km";
                                    if (documentSnapshot.get("type") != null) {
                                        if (documentSnapshot.get("type").equals(getString(R.string.bd_run))) {
                                            title_type.setText(getString(R.string.type_run));
                                        }
                                        if (documentSnapshot.get("type").equals(getString(R.string.bd_indoor_run))) {
                                            title_type.setText(getString(R.string.type_indoor_run));
                                        }
                                        if (documentSnapshot.get("type").equals(getString(R.string.bd_cycling))) {
                                            title_type.setText(getString(R.string.type_cycling));
                                        }
                                        if (documentSnapshot.get("type").equals(getString(R.string.bd_indoor_cycling))) {
                                            title_type.setText(getString(R.string.type_indoor_cycling));
                                        }
                                        if (documentSnapshot.get("type").equals(getString(R.string.bd_elliptical))) {
                                            title_type.setText(getString(R.string.type_elliptical));
                                        }

                                        if (documentSnapshot.get("type").equals(getString(R.string.bd_run))
                                                || documentSnapshot.get("type").equals(getString(R.string.bd_indoor_run))
                                                || documentSnapshot.get("type").equals(getString(R.string.bd_elliptical))) {
                                            partialFormat = " /km";
                                        } else {
                                            partialFormat = " km/h";
                                        }
                                    }

                                    last_training_distance.setText(documentSnapshot.get("distance").toString() + " kms");
                                    last_training_time.setText(Utils.getFormattedTime(documentSnapshot.get("time").toString()) + " min");
                                    last_training_date.setText(Utils.toString((Timestamp) documentSnapshot.get("date"), getString(R.string.format_date)));
                                    last_training_partial.setText(documentSnapshot.get("partial").toString() + partialFormat);
                                }
                            }
                        } else {
                            title_time.setVisibility(View.INVISIBLE);
                            title_distance.setVisibility(View.INVISIBLE);
                            title_partial.setVisibility(View.INVISIBLE);
                            title_type.setText(R.string.no_results);
                        }
                    }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public Integer getTotalTrainings() {
        AtomicReference<Integer> count = new AtomicReference<>(0);
        AtomicReference<Float> countKm = new AtomicReference<>(0.0f);
        db.collection("trainings").whereEqualTo("email", email).get()
                .addOnCompleteListener(task -> {
                    count.set(0);
                    countKm.set(0f);
                    if (task.isSuccessful()) {
                        for (DocumentSnapshot snap : task.getResult()) {
                            Training training = snap.toObject(Training.class);
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                if (training != null && training.email.equals(email)) {
                                    count.updateAndGet(v -> v + 1);
                                    countKm.updateAndGet(v -> v + Float.parseFloat(training.distance));
                                }
                            }
                        }
                        statTotalTrainings.setText(String.valueOf(count.get()));
                        statTotalKms.setText(String.format("%.02f", countKm.get()));
                    }
                });
        return count.get();
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public Integer getTotalCompetitions() {
        AtomicReference<Integer> count = new AtomicReference<>(0);
        db.collection("competitions").whereEqualTo("email", email).get()
                .addOnCompleteListener(task -> {
                    count.set(0);
                    if (task.isSuccessful()) {
                        for (DocumentSnapshot snap : task.getResult()) {
                            Competition competition = snap.toObject(Competition.class);
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                if (competition != null && competition.email.equals(email)) {
                                    count.updateAndGet(v -> v + 1);
                                }
                            }
                        }
                        statTotalCompetitions.setText(String.valueOf(count.get()));
                    }
                });
        return count.get();
    }
}