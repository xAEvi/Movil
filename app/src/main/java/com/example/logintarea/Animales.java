package com.example.logintarea;

import android.os.Bundle;

import androidx.annotation.DrawableRes;
import androidx.annotation.RawRes;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;


public class Animales extends AppCompatActivity {

    private String id;
    private String nombre;
    private int imagenResId;
    private int sonidoResId;

    public Animales(String id, String nombre, @DrawableRes int imagenResId, @RawRes int sonidoResId) {
        this.id = id;
        this.nombre = nombre;
        this.imagenResId = imagenResId;
        this.sonidoResId = sonidoResId;
    }

    public Animales() {
    }

    public String getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public int getImagenResId() {
        return imagenResId;
    }

    public int getSonidoResId() {
        return sonidoResId;
    }
    private TextView tvInstruccion;
    private TextView tvFeedback;
    private ImageButton btnEscucharSonidoObjetivo;
    private ImageButton imgOpcion1;
    private ImageButton imgOpcion2;
    private ImageButton imgOpcion3;

    private MediaPlayer mediaPlayer;
    private final Handler handler = new Handler(Looper.getMainLooper());

    private List<Animales> todosLosAnimales;
    private Animales animalObjetivo;
    private List<Animales> opcionesActuales;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_animales);

        tvInstruccion = findViewById(R.id.tvInstruccion);
        tvFeedback = findViewById(R.id.tvFeedback);
        btnEscucharSonidoObjetivo = findViewById(R.id.btnEscucharSonidoObjetivo);
        imgOpcion1 = findViewById(R.id.imgOpcion1);
        imgOpcion2 = findViewById(R.id.imgOpcion2);
        imgOpcion3 = findViewById(R.id.imgOpcion3);

        inicializarAnimales();

        if (todosLosAnimales.size() < 3) {
            Toast.makeText(this, "No hay suficientes animales para jugar", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        configurarNuevaRonda();

        btnEscucharSonidoObjetivo.setOnClickListener(v -> {
            if (animalObjetivo != null) {
                reproducirSonido(animalObjetivo.getSonidoResId());
            }
        });

        View.OnClickListener listenerOpciones = view -> {
            Animales animalSeleccionado = (Animales) view.getTag();
            if (animalSeleccionado != null) {
                reproducirSonido(animalSeleccionado.getSonidoResId());
                verificarRespuesta(animalSeleccionado);
            }
        };

        imgOpcion1.setOnClickListener(listenerOpciones);
        imgOpcion2.setOnClickListener(listenerOpciones);
        imgOpcion3.setOnClickListener(listenerOpciones);
    }

    private void inicializarAnimales() {
        todosLosAnimales = new ArrayList<>(Arrays.asList(
                new Animales("perro", "Perro", R.drawable.perro, R.raw.perro_sound),
                new Animales("gato", "Gato", R.drawable.gato, R.raw.gato_sound),
                new Animales("vaca", "Vaca", R.drawable.vaca, R.raw.vaca_sound),
                new Animales("leon", "León", R.drawable.leon, R.raw.leon_sound),
                new Animales("oveja", "Oveja", R.drawable.oveja, R.raw.oveja_sound)

        ));
    }

    private void configurarNuevaRonda() {
        tvFeedback.setText("");
        tvInstruccion.setText("Escucha y encuentra el animal");


        List<Animales> animalesBarajados = new ArrayList<>(todosLosAnimales);
        Collections.shuffle(animalesBarajados);

        animalObjetivo = animalesBarajados.get(0);


        opcionesActuales = new ArrayList<>();
        opcionesActuales.add(animalObjetivo);

        for (int i = 1; i < animalesBarajados.size() && opcionesActuales.size() < 3; i++) {
            if (!animalesBarajados.get(i).getId().equals(animalObjetivo.getId())) {
                opcionesActuales.add(animalesBarajados.get(i));
            }
        }
        Collections.shuffle(opcionesActuales);


        if (opcionesActuales.size() >= 3) {
            imgOpcion1.setImageResource(opcionesActuales.get(0).getImagenResId());
            imgOpcion1.setTag(opcionesActuales.get(0));

            imgOpcion2.setImageResource(opcionesActuales.get(1).getImagenResId());
            imgOpcion2.setTag(opcionesActuales.get(1));

            imgOpcion3.setImageResource(opcionesActuales.get(2).getImagenResId());
            imgOpcion3.setTag(opcionesActuales.get(2));
        }


        handler.postDelayed(() -> {
            if (animalObjetivo != null) {
                reproducirSonido(animalObjetivo.getSonidoResId());
            }
        }, 1000);
    }

    private void reproducirSonido(int sonidoResId) {
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
        mediaPlayer = MediaPlayer.create(this, sonidoResId);
        if (mediaPlayer != null) {
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    mp.release();
                    if (mediaPlayer == mp) {
                        mediaPlayer = null;
                    }
                }
            });
            mediaPlayer.start();
        } else {
            Toast.makeText(this, "Error al reproducir sonido", Toast.LENGTH_SHORT).show();
        }
    }

    private void verificarRespuesta(Animales seleccionado) {
        if (seleccionado.getId().equals(animalObjetivo.getId())) {
            tvFeedback.setText("¡Muy Bien! Era un " + animalObjetivo.getNombre());
            tvFeedback.setTextColor(ContextCompat.getColor(this, android.R.color.holo_green_dark));
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    configurarNuevaRonda();
                }
            }, 2000);
        } else {
            tvFeedback.setText("Ups, ese es un " + seleccionado.getNombre() + ". ¡Intenta de nuevo!");
            tvFeedback.setTextColor(ContextCompat.getColor(this, android.R.color.holo_red_dark));
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
        handler.removeCallbacksAndMessages(null);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
        handler.removeCallbacksAndMessages(null);
    }
}