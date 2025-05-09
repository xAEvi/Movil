package com.example.logintarea;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

public class Colores extends AppCompatActivity {

    private MediaPlayer mediaPlayer;
    private Handler handler = new Handler();
    private int currentColorIndex = 0;
    private boolean isPlayingAll = false;
    private Button btnReproducirTodos;
    private FrameLayout imageOverlay;
    private ImageView colorImage;

    // Arrays de IDs
    private final int[] cardIds = {
            R.id.card_rojo, R.id.card_azul, R.id.card_amarillo,
            R.id.card_verde, R.id.card_naranja, R.id.card_morado,
            R.id.card_rosa, R.id.card_marron, R.id.card_negro,
            R.id.card_blanco, R.id.card_gris, R.id.card_celeste
    };

    private final int[] soundIds = {
            R.raw.rojo, R.raw.azul, R.raw.amarillo,
            R.raw.verde, R.raw.naranja, R.raw.morado,
            R.raw.rosa, R.raw.marron, R.raw.negro,
            R.raw.blanco, R.raw.gris, R.raw.celeste
    };

    private final int[] imageIds = {
            R.drawable.rojo, R.drawable.azul, R.drawable.amarillo,
            R.drawable.hierba, R.drawable.naranja, R.drawable.morado,
            R.drawable.rosa, R.drawable.marron, R.drawable.negro,
            R.drawable.blanco, R.drawable.gris, R.drawable.celeste
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_colores);

        btnReproducirTodos = findViewById(R.id.btnReproducirTodos);
        imageOverlay = findViewById(R.id.imageOverlay);
        colorImage = findViewById(R.id.colorImage);

        // Configurar el overlay para cerrar al tocar
        imageOverlay.setOnClickListener(v -> {
            hideImageOverlay();
            stopCurrentSound();
        });

        setupColorCards();
        setupReproducirTodosButton();
    }

    private void setupColorCards() {
        for (int i = 0; i < cardIds.length; i++) {
            CardView cardView = findViewById(cardIds[i]);
            final int soundId = soundIds[i];
            final int imageId = imageIds[i];

            cardView.setOnClickListener(v -> {
                // Detener la reproducción de todos si está activa
                if (isPlayingAll) {
                    stopPlayingAll();
                }
                showImageForColor(imageId);
                playSound(soundId);
                animateCard(v);
            });
        }
    }

    private void setupReproducirTodosButton() {
        btnReproducirTodos.setOnClickListener(v -> {
            if (!isPlayingAll) {
                isPlayingAll = true;
                btnReproducirTodos.setEnabled(false);
                currentColorIndex = 0;
                playAllColors();
            }
        });
    }

    private void playAllColors() {
        if (!isPlayingAll || currentColorIndex >= cardIds.length) {
            // Terminó la reproducción o fue cancelada
            isPlayingAll = false;
            btnReproducirTodos.setEnabled(true);
            hideImageOverlay();
            return;
        }

        CardView currentCard = findViewById(cardIds[currentColorIndex]);
        showImageForColor(imageIds[currentColorIndex]);
        playSoundForAll(soundIds[currentColorIndex], currentCard);
        animateCard(currentCard);
    }

    private void playSoundForAll(int soundId, CardView card) {
        // Resaltar la tarjeta actual
        card.setCardBackgroundColor(getResources().getColor(R.color.light_blue_A200));

        if (mediaPlayer != null) {
            mediaPlayer.release();
        }

        mediaPlayer = MediaPlayer.create(this, soundId);
        mediaPlayer.setOnCompletionListener(mp -> {
            // Restaurar color original después de medio segundo
            handler.postDelayed(() -> {
                card.setCardBackgroundColor(getResources().getColor(getColorResIdForIndex(currentColorIndex)));

                // Pasar al siguiente color
                currentColorIndex++;
                playAllColors();
            }, 500); // Medio segundo entre colores
        });
        mediaPlayer.start();
    }

    private void stopPlayingAll() {
        isPlayingAll = false;
        handler.removeCallbacksAndMessages(null);
        btnReproducirTodos.setEnabled(true);
        hideImageOverlay();

        // Restaurar color de la tarjeta actual si es necesario
        if (currentColorIndex < cardIds.length) {
            CardView currentCard = findViewById(cardIds[currentColorIndex]);
            currentCard.setCardBackgroundColor(getResources().getColor(getColorResIdForIndex(currentColorIndex)));
        }
    }

    private void showImageForColor(int imageResId) {
        colorImage.setImageResource(imageResId);
        imageOverlay.setVisibility(View.VISIBLE);
        imageOverlay.bringToFront(); // Asegurar que el overlay esté encima de todo
    }

    private void hideImageOverlay() {
        imageOverlay.setVisibility(View.GONE);
    }

    private void stopCurrentSound() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    private int getColorResIdForIndex(int index) {
        switch(index) {
            case 0: return R.color.rojo;
            case 1: return R.color.azul;
            case 2: return R.color.amarillo;
            case 3: return R.color.verde;
            case 4: return R.color.naranja;
            case 5: return R.color.morado;
            case 6: return R.color.rosa;
            case 7: return R.color.marron;
            case 8: return R.color.negro;
            case 9: return R.color.blanco;
            case 10: return R.color.gris;
            case 11: return R.color.celeste;
            default: return R.color.rojo;
        }
    }

    private void playSound(int soundId) {
        if (mediaPlayer != null) {
            mediaPlayer.release();
        }

        mediaPlayer = MediaPlayer.create(this, soundId);
        mediaPlayer.setOnCompletionListener(mp -> {
            mp.release();
            mediaPlayer = null;
            hideImageOverlay();
        });
        mediaPlayer.start();
    }

    private void animateCard(View view) {
        ScaleAnimation scaleAnimation = new ScaleAnimation(
                1.0f, 1.1f, 1.0f, 1.1f,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f);

        scaleAnimation.setDuration(100);
        scaleAnimation.setRepeatCount(1);
        scaleAnimation.setRepeatMode(Animation.REVERSE);
        view.startAnimation(scaleAnimation);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
}