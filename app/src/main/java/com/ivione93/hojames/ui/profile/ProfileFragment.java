package com.ivione93.hojames.ui.profile;

import android.app.ProgressDialog;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.Timestamp;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.ivione93.hojames.R;
import com.ivione93.hojames.Utils;
import com.ivione93.hojames.ui.competitions.NewCompetitionActivity;
import com.ivione93.hojames.ui.login.AuthActivity;
import com.ivione93.hojames.ui.trainings.ViewTrainingActivity;

import java.util.Date;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileFragment extends Fragment {

    SharedPreferences.Editor prefs;

    private FirebaseAnalytics mFirebaseAnalytics;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference competitions = db.collection("competitions");

    GoogleSignInClient mGoogleSignInClient;

    CircleImageView photoProfile;
    TextView emailTextView, nombreEditText, birthEditText;
    TextView last_competition_name, last_competition_place, last_competition_date, last_competition_track, last_competition_result, last_competition_type;
    TextView title_training, last_training_date, title_time, title_distance, title_partial, last_training_time, last_training_distance, last_training_partial;
    TextView tvIndicadorSeries, tvIndicadorCuestas, tvIndicadorFartlek, tvIndicadorGym;
    TextView last_competition_track_text;
    ImageView mapImageView, imageTypeCompetition;
    ImageView ivIndicadorSeries, ivIndicadorCuestas, ivIndicadorFartlek, ivIndicadorGym, ivIndicadorObserves;

    CardView lastTrainingCV, lastCompetitionCV;

    String email;
    String dateSelected;
    Uri photoUrl;

    private ProgressDialog progressDialog;

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
        if (item.getItemId() == R.id.menu_edit_profile) {
            Intent editProfile = new Intent(getActivity(), EditProfileActivity.class);
            editProfile.putExtra("email", email);
            if (photoUrl != null) {
                editProfile.putExtra("photoUrl", photoUrl.toString());
            }
            getContext().startActivity(editProfile);
        }
        if (item.getItemId() == R.id.menu_add_training_profile) {
            Intent viewTraining = new Intent(getActivity(), ViewTrainingActivity.class);
            viewTraining.putExtra("email", email);
            viewTraining.putExtra("dateSelected", dateSelected);
            getContext().startActivity(viewTraining);
        }
        if (item.getItemId() == R.id.menu_add_competition_profile) {
            Intent newCompetition = new Intent(getActivity(), NewCompetitionActivity.class);
            newCompetition.putExtra("isNew", true);
            newCompetition.putExtra("email", email);
            getContext().startActivity(newCompetition);
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

        last_competition_name = root.findViewById(R.id.last_competition_name);
        last_competition_place = root.findViewById(R.id.last_competition_place);
        last_competition_date = root.findViewById(R.id.last_competition_date);
        last_competition_track = root.findViewById(R.id.last_competition_track);
        last_competition_result = root.findViewById(R.id.last_competition_result);
        last_competition_type = root.findViewById(R.id.last_competition_type);
        imageTypeCompetition = root.findViewById(R.id.imageTypeCompetition);

        title_training = root.findViewById(R.id.title_training);
        last_training_date = root.findViewById(R.id.last_training_date);
        title_time = root.findViewById(R.id.title_time);
        title_distance = root.findViewById(R.id.title_distance);
        title_partial = root.findViewById(R.id.title_partial);
        last_training_time = root.findViewById(R.id.last_training_time);
        last_training_distance = root.findViewById(R.id.last_training_distance);
        last_training_partial = root.findViewById(R.id.last_training_partial);

        last_competition_track_text = root.findViewById(R.id.last_competition_track_text);
        mapImageView = root.findViewById(R.id.mapImageView);

        tvIndicadorSeries = root.findViewById(R.id.tvIndicadorSeries);
        ivIndicadorSeries = root.findViewById(R.id.ivIndicadorSeries);

        tvIndicadorCuestas = root.findViewById(R.id.tvIndicadorCuestas);
        ivIndicadorCuestas = root.findViewById(R.id.ivIndicadorCuestas);

        tvIndicadorFartlek = root.findViewById(R.id.tvIndicadorFartlek);
        ivIndicadorFartlek = root.findViewById(R.id.ivIndicadorFartlek);

        tvIndicadorGym = root.findViewById(R.id.tvIndicadorGym);
        ivIndicadorGym = root.findViewById(R.id.ivIndicadorGym);

        ivIndicadorObserves = root.findViewById(R.id.ivIndicadorObserves);

        lastTrainingCV = root.findViewById(R.id.last_training);
        lastCompetitionCV = root.findViewById(R.id.last_competition);

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
                                Intent newCompetition = new Intent(getContext(), NewCompetitionActivity.class);
                                newCompetition.putExtra("isNew", false);
                                newCompetition.putExtra("idCompetition", documentSnapshot.get("id").toString());
                                newCompetition.putExtra("email", email);
                                startActivity(newCompetition);
                            });
                            last_competition_name.setText(documentSnapshot.get("name").toString());
                            last_competition_place.setText(documentSnapshot.get("place").toString());
                            last_competition_date.setText(Utils.toString((Timestamp) documentSnapshot.get("date"), getString(R.string.format_date)));
                            last_competition_track.setText(documentSnapshot.get("track").toString());
                            last_competition_result.setText(Utils.getFormattedResult(documentSnapshot.get("result").toString()));
                            if(documentSnapshot.get("type") != null) {
                                last_competition_type.setText(documentSnapshot.get("type").toString());
                                if (documentSnapshot.get("type").toString().equals(getString(R.string.type_pc))) {
                                    imageTypeCompetition.setBackground(getResources().getDrawable(R.drawable.bg_pista));
                                }
                                if (documentSnapshot.get("type").toString().equals(getString(R.string.type_al))) {
                                    imageTypeCompetition.setBackground(getResources().getDrawable(R.drawable.bg_pista));
                                }
                                if (documentSnapshot.get("type").toString().equals(getString(R.string.type_cross))) {
                                    imageTypeCompetition.setBackground(getResources().getDrawable(R.drawable.bg_cross));
                                }
                                if (documentSnapshot.get("type").toString().equals(getString(R.string.type_road))) {
                                    imageTypeCompetition.setBackground(getResources().getDrawable(R.drawable.bg_road));
                                }
                            }
                        } else {
                            last_competition_name.setText(R.string.no_results);
                        }
                    }
                } else {
                    last_competition_name.setText(R.string.no_results);
                    last_competition_track_text.setVisibility(View.INVISIBLE);
                    mapImageView.setVisibility(View.INVISIBLE);
                }
            } else {
                progressDialog.dismiss();
                last_competition_name.setText(R.string.no_results);
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
                                        Intent newTraining = new Intent(getContext(), ViewTrainingActivity.class);
                                        newTraining.putExtra("isNew", false);
                                        newTraining.putExtra("idTraining", documentSnapshot.get("id").toString());
                                        newTraining.putExtra("email", email);
                                        startActivity(newTraining);
                                    });

                                    last_training_distance.setText(documentSnapshot.get("distance").toString() + " kms");
                                    last_training_time.setText(Utils.getFormattedTime(documentSnapshot.get("time").toString()) + " min");
                                    last_training_date.setText(Utils.toString((Timestamp) documentSnapshot.get("date"), getString(R.string.format_date)));
                                    last_training_partial.setText(documentSnapshot.get("partial").toString() + " /km");

                                    if (documentSnapshot.get("observes") == null || documentSnapshot.get("observes").equals("")) {
                                        ivIndicadorObserves.setVisibility(View.INVISIBLE);
                                    } else {
                                        ivIndicadorObserves.setVisibility(View.VISIBLE);
                                    }

                                    // check series
                                    db.collection("series").whereEqualTo("idTraining", documentSnapshot.get("id").toString()).get().addOnCompleteListener(t -> {
                                        if (t.isSuccessful()) {
                                            QuerySnapshot document = t.getResult();
                                            if (!document.isEmpty()) {
                                                tvIndicadorSeries.setVisibility(View.VISIBLE);
                                                ivIndicadorSeries.setVisibility(View.VISIBLE);
                                            } else {
                                                tvIndicadorSeries.setVisibility(View.INVISIBLE);
                                                ivIndicadorSeries.setVisibility(View.INVISIBLE);
                                            }
                                        }
                                    });

                                    // check cuestas
                                    db.collection("cuestas").whereEqualTo("idTraining", documentSnapshot.get("id").toString()).get().addOnCompleteListener(t -> {
                                        if (t.isSuccessful()) {
                                            QuerySnapshot document = t.getResult();
                                            if (!document.isEmpty()) {
                                                tvIndicadorCuestas.setVisibility(View.VISIBLE);
                                                ivIndicadorCuestas.setVisibility(View.VISIBLE);
                                            } else {
                                                tvIndicadorCuestas.setVisibility(View.INVISIBLE);
                                                ivIndicadorCuestas.setVisibility(View.INVISIBLE);
                                            }
                                        }
                                    });

                                    // check fartlek
                                    db.collection("fartlek").whereEqualTo("idTraining", documentSnapshot.get("id").toString()).get().addOnCompleteListener(t -> {
                                        if (t.isSuccessful()) {
                                            QuerySnapshot document = t.getResult();
                                            if (!document.isEmpty()) {
                                                tvIndicadorFartlek.setVisibility(View.VISIBLE);
                                                ivIndicadorFartlek.setVisibility(View.VISIBLE);
                                            } else {
                                                tvIndicadorFartlek.setVisibility(View.INVISIBLE);
                                                ivIndicadorFartlek.setVisibility(View.INVISIBLE);
                                            }
                                        }
                                    });

                                    // check gym
                                    db.collection("gym").whereEqualTo("idTraining", documentSnapshot.get("id").toString()).get().addOnCompleteListener(t -> {
                                        if (t.isSuccessful()) {
                                            QuerySnapshot document = t.getResult();
                                            if (!document.isEmpty()) {
                                                tvIndicadorGym.setVisibility(View.VISIBLE);
                                                ivIndicadorGym.setVisibility(View.VISIBLE);
                                            } else {
                                                tvIndicadorGym.setVisibility(View.INVISIBLE);
                                                ivIndicadorGym.setVisibility(View.INVISIBLE);
                                            }
                                        }
                                    });
                                }
                            }
                        } else {
                            last_training_time.setText(R.string.no_results);
                        }
                    }
        });
    }
}