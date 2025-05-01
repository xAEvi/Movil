package com.example.logintarea;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
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

    private TextInputEditText editTextUsername;
    private TextInputEditText editTextPassword;
    private Button buttonLogin;
    private Button buttonCreateAccount;
    private CheckBox checkboxKeepLoggedIn;

    private final String CORRECT_USERNAME = "user";
    private final String CORRECT_PASSWORD = "pass";

    private static final String PREFS_NAME = "LoginPrefs";
    private static final String PREF_USERNAME = "username";
    private static final String PREF_PASSWORD = "password";
    private static final String PREF_KEEP_LOGGED_IN = "keepLoggedIn";

    private SharedPreferences sharedPreferences;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        editTextUsername = findViewById(R.id.editTextUsername);
        editTextPassword = findViewById(R.id.editTextPassword);
        buttonLogin = findViewById(R.id.buttonLogin);
        buttonCreateAccount = findViewById(R.id.buttonCreateAccount);
        checkboxKeepLoggedIn = findViewById(R.id.mantenerSesion);

        checkSavedLogin();

        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateLogin();
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


    private void checkSavedLogin() {
        boolean keepLoggedIn = sharedPreferences.getBoolean(PREF_KEEP_LOGGED_IN, false);
        String savedUsername = sharedPreferences.getString(PREF_USERNAME, null);

        if (keepLoggedIn && savedUsername != null) {
            Toast.makeText(MainActivity.this, "Bienvenido de vuelta, " + savedUsername + "!", Toast.LENGTH_SHORT).show();
            navigateToHome();
        }
    }


    private void validateLogin() {
        String enteredUsername = editTextUsername.getText() != null ? editTextUsername.getText().toString().trim() : "";
        String enteredPassword = editTextPassword.getText() != null ? editTextPassword.getText().toString() : "";
        boolean keepLoggedIn = checkboxKeepLoggedIn.isChecked();

        if (enteredUsername.equals(CORRECT_USERNAME) && enteredPassword.equals(CORRECT_PASSWORD)) {
            Toast.makeText(MainActivity.this, R.string.acceso_concedido, Toast.LENGTH_SHORT).show();

            saveOrClearCredentials(enteredUsername, enteredPassword, keepLoggedIn);

            navigateToHome();

        } else {
            Toast.makeText(MainActivity.this, R.string.datos_incorrectos, Toast.LENGTH_SHORT).show();
        }
    }


    private void saveOrClearCredentials(String username, String password, boolean keepLoggedIn) {
        SharedPreferences.Editor editor = sharedPreferences.edit();

        if (keepLoggedIn && username != null && password != null) {
            editor.putBoolean(PREF_KEEP_LOGGED_IN, true);
            editor.putString(PREF_USERNAME, username);
            editor.putString(PREF_PASSWORD, password);
        } else {
            editor.remove(PREF_KEEP_LOGGED_IN);
            editor.remove(PREF_USERNAME);
            editor.remove(PREF_PASSWORD);
        }

        editor.apply();
    }


    private void navigateToHome() {
        Intent intent = new Intent(MainActivity.this, Home.class);
        startActivity(intent);
        finish();
    }
}