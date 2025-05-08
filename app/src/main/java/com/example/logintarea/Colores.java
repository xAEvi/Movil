package com.example.logintarea;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

public class Colores extends AppCompatActivity {

    private MediaPlayer mediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_colores);

        setupColorCards();
    }

    private void setupColorCards() {
        int[] cardIds = {
                R.id.card_rojo, R.id.card_azul, R.id.card_amarillo,
                R.id.card_verde, R.id.card_naranja, R.id.card_morado,
                R.id.card_rosa, R.id.card_marron, R.id.card_negro,
                R.id.card_blanco, R.id.card_gris, R.id.card_celeste
        };

        int[] soundIds = {
                R.raw.rojo, R.raw.azul, R.raw.amarillo,
                R.raw.verde, R.raw.naranja, R.raw.morado,
                R.raw.rosa, R.raw.marron, R.raw.negro,
                R.raw.blanco, R.raw.gris, R.raw.celeste
        };

        for (int i = 0; i < cardIds.length; i++) {
            CardView cardView = findViewById(cardIds[i]);
            final int soundId = soundIds[i];

            cardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    playSound(soundId);
                    animateCard(v);
                }
            });
        }
    }

    private void playSound(int soundId) {
        // Liberar recursos del MediaPlayer anterior si existe
        if (mediaPlayer != null) {
            mediaPlayer.release();
        }

        mediaPlayer = MediaPlayer.create(this, soundId);
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                mp.release();
                mediaPlayer = null;
            }
        });
        mediaPlayer.start();
    }

    private void animateCard(View view) {
        ScaleAnimation scaleAnimation = new ScaleAnimation(
                1.0f, 1.1f, // Escala X
                1.0f, 1.1f, // Escala Y
                Animation.RELATIVE_TO_SELF, 0.5f, // Pivote X
                Animation.RELATIVE_TO_SELF, 0.5f); // Pivote Y

        scaleAnimation.setDuration(100);
        scaleAnimation.setRepeatCount(1);
        scaleAnimation.setRepeatMode(Animation.REVERSE);

        view.startAnimation(scaleAnimation);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
}