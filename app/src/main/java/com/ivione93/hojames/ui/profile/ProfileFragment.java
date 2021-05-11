package com.ivione93.hojames.ui.profile;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.ivione93.hojames.ui.login.AuthActivity;
import com.ivione93.hojames.R;
import com.ivione93.hojames.Utils;
import com.ivione93.hojames.ui.competitions.NewCompetitionActivity;
import com.ivione93.hojames.ui.trainings.ViewTrainingActivity;

import java.util.Date;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileFragment extends Fragment {

    SharedPreferences.Editor prefs;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    GoogleSignInClient mGoogleSignInClient;

    CircleImageView photoProfile;
    TextView emailTextView, licenciaEditText, nombreEditText, birthEditText;
    TextView last_competition_name, last_competition_place, last_competition_date, last_competition_track, last_competition_result;
    TextView title_training, last_training_date, title_time, title_distance, title_partial, last_training_time, last_training_distance, last_training_partial;

    String email, license;
    String dateSelected = Utils.toString(new Date());
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
        license = bundle.getString("license");
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
        progressDialog.setMessage("Cargando perfil...");
        progressDialog.show();
        db.collection("athlete").document(email).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    license = task.getResult().get("license").toString();
                    licenciaEditText.setText(task.getResult().get("license").toString());
                    nombreEditText.setText(task.getResult().get("name").toString() + " " + task.getResult().get("surname").toString());
                    birthEditText.setText(task.getResult().get("birth").toString());

                    getLastCompetition(license);
                    getLastTraining(license);

                    // Guardar datos
                    prefs = getActivity().getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE).edit();
                    prefs.putString("email", email);
                    prefs.putString("license", license);
                    prefs.apply();

                    getActivity().getIntent().putExtra("email", email);
                    getActivity().getIntent().putExtra("license", license);
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
            return true;
        }
        if (item.getItemId() == R.id.menu_add_training_profile) {
            Intent viewTraining = new Intent(getActivity(), ViewTrainingActivity.class);
            viewTraining.putExtra("license", license);
            viewTraining.putExtra("email", email);
            viewTraining.putExtra("dateSelected", dateSelected);
            getContext().startActivity(viewTraining);
        }
        if (item.getItemId() == R.id.menu_add_competition_profile) {
            Intent newCompetition = new Intent(getActivity(), NewCompetitionActivity.class);
            newCompetition.putExtra("isNew", true);
            newCompetition.putExtra("email", email);
            newCompetition.putExtra("license", license);
            getContext().startActivity(newCompetition);
        }
        if (item.getItemId() == R.id.menu_log_out) {
            // Borrado datos inicio de sesion
            SharedPreferences.Editor prefs = getActivity().getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE).edit();
            prefs.clear();
            prefs.apply();

            FirebaseAuth.getInstance().signOut();
            mGoogleSignInClient.signOut();

            Intent mainIntent = new Intent(getActivity().getApplicationContext(), AuthActivity.class);
            startActivity(mainIntent);
        }
        return super.onOptionsItemSelected(item);
    }

    private void setup(View root, String email) {
        progressDialog = new ProgressDialog(getContext());
        photoProfile = root.findViewById(R.id.photoProfile);
        emailTextView = root.findViewById(R.id.emailTextView);
        emailTextView.setText(email);
        licenciaEditText = root.findViewById(R.id.licenciaEditText);
        nombreEditText = root.findViewById(R.id.nombreEditText);
        birthEditText = root.findViewById(R.id.birthEditText);

        last_competition_name = root.findViewById(R.id.last_competition_name);
        last_competition_place = root.findViewById(R.id.last_competition_place);
        last_competition_date = root.findViewById(R.id.last_competition_date);
        last_competition_track = root.findViewById(R.id.last_competition_track);
        last_competition_result = root.findViewById(R.id.last_competition_result);

        title_training = root.findViewById(R.id.title_training);
        last_training_date = root.findViewById(R.id.last_training_date);
        title_time = root.findViewById(R.id.title_time);
        title_distance = root.findViewById(R.id.title_distance);
        title_partial = root.findViewById(R.id.title_partial);
        last_training_time = root.findViewById(R.id.last_training_time);
        last_training_distance = root.findViewById(R.id.last_training_distance);
        last_training_partial = root.findViewById(R.id.last_training_partial);
    }

    private void getLastCompetition(String license) {
        db.collection("competitions")
                .whereEqualTo("license", license)
                .get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                progressDialog.dismiss();
                for (QueryDocumentSnapshot documentSnapshot : task.getResult()) {
                    if (documentSnapshot.exists()) {
                        last_competition_name.setText(documentSnapshot.get("name").toString());
                        last_competition_place.setText(documentSnapshot.get("place").toString());
                        last_competition_date.setText(Utils.toString((Timestamp) documentSnapshot.get("date")));
                        last_competition_track.setText(documentSnapshot.get("track").toString());
                        last_competition_result.setText(documentSnapshot.get("result").toString());
                    } else {
                        last_competition_name.setText("No se han encontrado competiciones");
                    }
                }
            } else {
                progressDialog.dismiss();
                last_competition_name.setText("No se han encontrado competiciones");
            }
        });
    }

    private void getLastTraining(String license) {
        db.collection("trainings")
                .whereEqualTo("license", license)
                .get().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        progressDialog.dismiss();
                        for (QueryDocumentSnapshot documentSnapshot : task.getResult()) {
                            if (documentSnapshot.exists()) {
                                last_training_distance.setText(documentSnapshot.get("distance").toString() + " kms");
                                last_training_time.setText(documentSnapshot.get("time").toString() + " min");
                                last_training_date.setText(Utils.toString((Timestamp) documentSnapshot.get("date")));
                                last_training_partial.setText(documentSnapshot.get("partial").toString() + " /km");
                            } else {
                                title_training.setText("No se han encontrado entrenamientos");
                                title_training.setTextSize(20);
                                title_time.setText("");
                                title_distance.setText("");
                                title_partial.setText("");
                            }
                        }
                    } else {
                        progressDialog.dismiss();
                        title_training.setText("No se han encontrado entrenamientos");
                        title_training.setTextSize(20);
                        title_time.setText("");
                        title_distance.setText("");
                        title_partial.setText("");
                    }
        });
    }
}