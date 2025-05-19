package com.example.logintarea;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.graphics.PointF;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Vocales extends AppCompatActivity implements VocalTraceView.OnTraceCompleteListener {

    private VocalTraceView vocalTraceView;
    private MediaPlayer mediaPlayer;

    private List<Character> listaVocales = Arrays.asList('A', 'E', 'I', 'O', 'U');
    private int indiceVocalActual = 0;

    //Guardar los datos de cada vocal
    private static class DatosVocal {
        final int imgPunteadaResId;
        final int imgFormadaResId;
        final int sonidoResId;
        final List<PointF> puntosGuia;

        DatosVocal(int imgPunteadaResId, int imgFormadaResId, int sonidoResId, List<PointF> puntosGuia) {
            this.imgPunteadaResId = imgPunteadaResId;
            this.imgFormadaResId = imgFormadaResId;
            this.sonidoResId = sonidoResId;
            this.puntosGuia = puntosGuia;
        }
    }

    private Map<Character, DatosVocal> mapaDatosVocales;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_vocales); // Asegúrate que es tu layout con VocalTraceView

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        vocalTraceView = findViewById(R.id.vocalTraceView);
        vocalTraceView.setOnTraceCompleteListener(this);

        inicializarDatosVocales();
        cargarVocalActual();
    }

    private void inicializarDatosVocales() {
        mapaDatosVocales = new HashMap<>();

        List<PointF> puntosA = Arrays.asList(
                new PointF(50f, 500f),
                new PointF(250f, 50f), //Pico de la A
                new PointF(450f, 500f),
                new PointF(150f, 300f),
                new PointF(350f, 300f)
        );
        mapaDatosVocales.put('A', new DatosVocal(
                R.drawable.vocal_a_punteada,
                R.drawable.vocal_a_formada,
                R.raw.sonido_a,
                puntosA
        ));

        List<PointF> puntosE = Arrays.asList(
                new PointF(50f, 50f),// Raya Vertical
                new PointF(50f, 250f),
                new PointF(50f, 450f),

                new PointF(50f, 50f),
                new PointF(250f, 50f),
                new PointF(450f, 50f),

                new PointF(50f, 250f),
                new PointF(250f, 250f),
                new PointF(450f, 250f),

                new PointF(50f, 450f),
                new PointF(250f, 450f),
                new PointF(450f, 450f)
        );
        mapaDatosVocales.put('E', new DatosVocal(
                R.drawable.vocal_e_punteada,
                R.drawable.vocal_e_formada,
                R.raw.sonido_e,
                puntosE
        ));

        List<PointF> puntosI = Arrays.asList(
                new PointF(250f, 50f),//Raya Vertical
                new PointF(250f, 450f),

                new PointF(50f, 50f),
                new PointF(250f, 50f),
                new PointF(450f, 50f),

                new PointF(50f, 450f),
                new PointF(250f, 450f),
                new PointF(450f, 450f)
        );
        mapaDatosVocales.put('I', new DatosVocal(
                R.drawable.vocal_i_punteada,
                R.drawable.vocal_i_formada,
                R.raw.sonido_i,
                puntosI
        ));

        List<PointF> puntosO = Arrays.asList(
                new PointF(250f, 50f),
                new PointF(100f, 100f),
                new PointF(100f, 400f),
                new PointF(250f, 450f),
                new PointF(400f, 400f),
                new PointF(400f, 100f),
                new PointF(250f, 50f)
        );
        mapaDatosVocales.put('O', new DatosVocal(
                R.drawable.vocal_o_punteada,
                R.drawable.vocal_o_formada,
                R.raw.sonido_o,
                puntosO
        ));

        List<PointF> puntosU = Arrays.asList(
                new PointF(50f, 100f),
                new PointF(100f, 400f),
                new PointF(250f, 450f),
                new PointF(400f, 400f),
                new PointF(450f, 100f)

        );
        mapaDatosVocales.put('U', new DatosVocal(
                R.drawable.vocal_u_punteada,
                R.drawable.vocal_u_formada,
                R.raw.sonido_u,
                puntosU
        ));


    }

    private void cargarVocalActual() {
        if (indiceVocalActual < listaVocales.size()) {
            char vocalChar = listaVocales.get(indiceVocalActual);
            DatosVocal datos = mapaDatosVocales.get(vocalChar);

            if (datos != null) {
                vocalTraceView.setVocal(datos.imgPunteadaResId, datos.imgFormadaResId, datos.puntosGuia);
                vocalTraceView.setVisibility(View.VISIBLE); // Asegurarse que está visible
                vocalTraceView.setAlpha(1f); // Resetear alpha por si hubo animación previa
            } else {
                Toast.makeText(this, "Faltan datos para la vocal: " + vocalChar, Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "¡Felicidades! Has trazado todas las vocales.", Toast.LENGTH_LONG).show();
            new Handler(Looper.getMainLooper()).postDelayed(this::finish, 4000);
        }
    }

    @Override
    public void onComplete() {
        DatosVocal datosActuales = mapaDatosVocales.get(listaVocales.get(indiceVocalActual));
        if (datosActuales == null) return;

        //Reproducir la voz
        if (mediaPlayer != null) {
            mediaPlayer.release();
        }
        mediaPlayer = MediaPlayer.create(this, datosActuales.sonidoResId);
        mediaPlayer.setOnCompletionListener(mp -> {
            // Delay para pasar a la siguiente
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                indiceVocalActual++;
                cargarVocalActual();
            }, 1000);
        });
        mediaPlayer.start();

        vocalTraceView.animateFormedVowel();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
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