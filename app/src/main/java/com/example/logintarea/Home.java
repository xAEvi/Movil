package com.example.logintarea;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class Home extends AppCompatActivity {

    private SharedPreferences sharedPreferences;

    private static final String PREFS_NAME = "LoginPrefs";
    private static final String PREF_USERNAME = "username";
    private static final String PREF_PASSWORD = "password";
    private static final String PREF_KEEP_LOGGED_IN = "keepLoggedIn";

    private Button btnNumeros, btnVocales, btnRutinas, btnAnimales, btnColores;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);

        sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        btnNumeros = findViewById(R.id.btnNumeros);
        btnVocales = findViewById(R.id.btnVocales);
        btnRutinas = findViewById(R.id.btnRutinas);
        btnAnimales = findViewById(R.id.btnAnimales);
        btnColores = findViewById(R.id.btnColores);

        btnNumeros.setOnClickListener(v -> {
            Intent intent = new Intent(Home.this, Numeros.class);
            startActivity(intent);
        });

        btnVocales.setOnClickListener(v -> {
            Intent intent = new Intent(Home.this, Vocales.class);
            startActivity(intent);
        });

        btnRutinas.setOnClickListener(v -> {
            Intent intent = new Intent(Home.this, Rutinas.class);
            startActivity(intent);
        });

        btnAnimales.setOnClickListener(v -> {
            Intent intent = new Intent(Home.this, Animales.class);
            startActivity(intent);
        });

        btnColores.setOnClickListener(v -> {
            Intent intent = new Intent(Home.this, Colores.class);
            startActivity(intent);
        });


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.menu1) {
            Toast.makeText(this, "Opción 1 seleccionada", Toast.LENGTH_SHORT).show();
            return true;
        } else if (id == R.id.menu2) {
            clearLoginData();
            return true;
        } else if (id == R.id.menuAcerca) {
            showAcercaDeDialog();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void showAcercaDeDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_acerca_de, null);

        builder.setView(dialogView);
        builder.setTitle(R.string.dialog_title_acerca_de);

        builder.setPositiveButton(R.string.dialog_cerrar, (dialog, which) -> {
            dialog.dismiss();
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void clearLoginData() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(PREF_KEEP_LOGGED_IN);
        editor.remove(PREF_USERNAME);
        editor.remove(PREF_PASSWORD);
        editor.apply();

        Toast.makeText(this, R.string.login_data_cleared, Toast.LENGTH_LONG).show();

        Intent intent = new Intent(Home.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}