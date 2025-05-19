package com.example.logintarea;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log; // Importar Log
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.google.android.material.textfield.TextInputEditText;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity"; // Para logs

    private TextInputEditText editTextUsername;
    private TextInputEditText editTextPassword;
    private Button buttonLogin;
    private Button buttonCreateAccount;
    private CheckBox checkboxKeepLoggedIn;

    private static final String PREFS_NAME = "LoginPrefs";
    private static final String PREF_USERNAME = "username";
    private static final String PREF_PASSWORD = "password"; // Guardar contraseña es inseguro, pero para el ejemplo
    private static final String PREF_KEEP_LOGGED_IN = "keepLoggedIn";

    private SharedPreferences sharedPreferences;
    private DatabaseHelper dbHelper; // Añadir referencia al DatabaseHelper

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        dbHelper = new DatabaseHelper(this); // Inicializar DatabaseHelper

        editTextUsername = findViewById(R.id.editTextUsername);
        editTextPassword = findViewById(R.id.editTextPassword);
        buttonLogin = findViewById(R.id.buttonLogin);
        buttonCreateAccount = findViewById(R.id.buttonCreateAccount);
        checkboxKeepLoggedIn = findViewById(R.id.mantenerSesion); // Asegúrate que este ID exista en activity_main.xml

        // Cargar credenciales guardadas si "mantener sesión" estaba activo
        loadSavedCredentialsIfKept();

        checkSavedLogin(); // Esta función decidirá si ir a Home directamente

        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateLoginWithDatabase();
            }
        });

        buttonCreateAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, Registrar.class);
                startActivity(intent);
            }
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void loadSavedCredentialsIfKept() {
        boolean keepLoggedIn = sharedPreferences.getBoolean(PREF_KEEP_LOGGED_IN, false);
        if (keepLoggedIn) {
            String savedUsername = sharedPreferences.getString(PREF_USERNAME, "");
            String savedPassword = sharedPreferences.getString(PREF_PASSWORD, ""); // Recuperar contraseña guardada
            editTextUsername.setText(savedUsername);
            editTextPassword.setText(savedPassword); // Opcional: no mostrar la contraseña guardada
            checkboxKeepLoggedIn.setChecked(true);
        }
    }

    private void checkSavedLogin() {
        boolean keepLoggedIn = sharedPreferences.getBoolean(PREF_KEEP_LOGGED_IN, false);
        String savedUsername = sharedPreferences.getString(PREF_USERNAME, null);
        String savedPassword = sharedPreferences.getString(PREF_PASSWORD, null);

        if (keepLoggedIn && savedUsername != null && savedPassword != null) {
            // Para "mantener sesión iniciada", podríamos confiar en SharedPreferences
            // o re-validar contra la BD para mayor seguridad (ej. si la contraseña cambió).
            // Por simplicidad, si está guardado y "keepLoggedIn" es true, asumimos que es válido.
            // En un escenario real, podrías querer llamar a dbHelper.checkUser(savedUsername, savedPassword) aquí.
            Log.i(TAG, "Mantener sesión activado para: " + savedUsername);
            Toast.makeText(MainActivity.this, "Bienvenido de vuelta, " + savedUsername + "!", Toast.LENGTH_SHORT).show();
            navigateToHome();
        } else {
            Log.i(TAG, "No hay sesión activa guardada o 'mantener sesión' está desactivado.");
        }
    }

    private void validateLoginWithDatabase() {
        String enteredUsername = editTextUsername.getText() != null ? editTextUsername.getText().toString().trim() : "";
        String enteredPassword = editTextPassword.getText() != null ? editTextPassword.getText().toString() : ""; // No usar trim() en contraseñas
        boolean keepLoggedIn = checkboxKeepLoggedIn.isChecked();

        if (enteredUsername.isEmpty()) {
            editTextUsername.setError("El nombre de usuario es requerido");
            editTextUsername.requestFocus();
            return;
        }
        if (enteredPassword.isEmpty()) {
            editTextPassword.setError("La contraseña es requerida");
            editTextPassword.requestFocus();
            return;
        }

        if (dbHelper.checkUser(enteredUsername, enteredPassword)) {
            Log.i(TAG, "Login exitoso para: " + enteredUsername);
            Toast.makeText(MainActivity.this, getString(R.string.acceso_concedido), Toast.LENGTH_SHORT).show();
            saveOrClearCredentials(enteredUsername, enteredPassword, keepLoggedIn);
            navigateToHome();
        } else {
            Log.w(TAG, "Login fallido para: " + enteredUsername);
            Toast.makeText(MainActivity.this, getString(R.string.datos_incorrectos), Toast.LENGTH_SHORT).show();
            // Opcionalmente, limpiar el campo de contraseña tras un intento fallido
            editTextPassword.setText("");
            // Asegurarse de que no se guarden credenciales incorrectas si "keepLoggedIn" estaba marcado
            saveOrClearCredentials(null, null, false); // Limpiar cualquier credencial guardada previamente
        }
    }

    private void saveOrClearCredentials(String username, String password, boolean keepLoggedIn) {
        SharedPreferences.Editor editor = sharedPreferences.edit();

        if (keepLoggedIn && username != null && password != null) {
            Log.i(TAG, "Guardando credenciales para: " + username + " con 'mantener sesión'.");
            editor.putBoolean(PREF_KEEP_LOGGED_IN, true);
            editor.putString(PREF_USERNAME, username);
            editor.putString(PREF_PASSWORD, password); // ¡Guardar contraseña en SharedPreferences es inseguro!
        } else {
            Log.i(TAG, "Limpiando credenciales guardadas.");
            editor.remove(PREF_KEEP_LOGGED_IN);
            editor.remove(PREF_USERNAME);
            editor.remove(PREF_PASSWORD);
        }
        editor.apply();
    }

    private void navigateToHome() {
        Intent intent = new Intent(MainActivity.this, Home.class); // Asumo que tienes una Home.class
        startActivity(intent);
        finish(); // Finaliza MainActivity para que el usuario no pueda volver con el botón "atrás"
    }

    @Override
    protected void onDestroy() {
        if (dbHelper != null) {
            dbHelper.close(); // Cierra la conexión a la base de datos
        }
        super.onDestroy();
    }
}