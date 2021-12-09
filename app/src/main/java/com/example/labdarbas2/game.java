package com.example.labdarbas2;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;

import java.util.Random;

public class game extends AppCompatActivity {

    private Button buttonPlay;
    private Random rand;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        rand = new Random();

        buttonPlay = findViewById(R.id.buttonPlay);
        final MediaPlayer mediaPlayer = MediaPlayer.create(this, R.raw.wickedwitchlaugh);

        buttonPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mediaPlayer.start();
                buttonPlay.setLayoutParams(buttonRandPos());
            }
        });
    }

    public RelativeLayout.LayoutParams buttonRandPos() {
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) buttonPlay.getLayoutParams();
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);

        params.leftMargin = rand.nextInt(metrics.widthPixels - buttonPlay.getWidth());
        params.topMargin = rand.nextInt(metrics.heightPixels - 2 * buttonPlay.getHeight());

        return params;
    }

    public void backToMain(View view) {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }
}