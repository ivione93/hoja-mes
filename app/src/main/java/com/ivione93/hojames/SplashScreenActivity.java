package com.ivione93.hojames;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Objects;

public class SplashScreenActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        Objects.requireNonNull(getSupportActionBar()).hide();
        setContentView(R.layout.activity_splash_screen);

        // Animaciones
        Animation animation = AnimationUtils.loadAnimation(this, R.anim.move_up);
        animation.setDuration(2000);
        Animation animation2 = AnimationUtils.loadAnimation(this, R.anim.move_right);
        animation2.setDuration(2000);

        ImageView splashTitle = findViewById(R.id.splashTitle);
        TextView splashAuthor = findViewById(R.id.splashAuthor);
        TextView splashBy = findViewById(R.id.splashBy);
        splashTitle.setAnimation(animation2);
        splashBy.setAnimation(animation);
        splashAuthor.setAnimation(animation);

        new Handler().postDelayed(() -> {
            Intent intent = new Intent(getApplicationContext(), InformationActivity.class);
            startActivity(intent);
            finish();
        }, 2500);
    }
}