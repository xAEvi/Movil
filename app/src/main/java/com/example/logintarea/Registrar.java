package com.example.logintarea; // Make sure this matches your package name

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;

import java.text.DateFormat;
import java.util.Calendar;

public class Registrar extends AppCompatActivity implements DatePickerDialog.OnDateSetListener {

    private static final String TAG = "RegistrarActivity";

    private TextInputEditText editTextUsername;
    private TextInputEditText editTextPassword;
    private Button buttonPickDate;
    private TextView textViewSelectedDate;
    private RadioGroup radioGroupGender;
    private Button buttonRegistrar;
    private Button buttonBorrar;
    private Button buttonCancelar;

    private Calendar selectedDateCalendar;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registrar); // Use the new layout file name

        dbHelper = new DatabaseHelper(this);
        initializeViews();
        setupButtonClickListeners();
    }

    private void initializeViews() {
        editTextUsername = findViewById(R.id.editTextUsername);
        editTextPassword = findViewById(R.id.editTextPassword);
        buttonPickDate = findViewById(R.id.buttonPickDate);
        textViewSelectedDate = findViewById(R.id.textViewSelectedDate);
        radioGroupGender = findViewById(R.id.radioGroupGender);
        buttonRegistrar = findViewById(R.id.buttonRegistrarSimplified);
        buttonBorrar = findViewById(R.id.buttonBorrarSimplified);
        buttonCancelar = findViewById(R.id.buttonCancelarSimplified);

        selectedDateCalendar = Calendar.getInstance();
        textViewSelectedDate.setText(getString(R.string.fecha_no_seleccionada));
    }

    private void setupButtonClickListeners() {
        buttonPickDate.setOnClickListener(v -> showDatePickerDialog());
        buttonRegistrar.setOnClickListener(v -> registerUser());
        buttonBorrar.setOnClickListener(v -> clearForm());
        buttonCancelar.setOnClickListener(v -> finish());
    }

    private void showDatePickerDialog() {
        int year = selectedDateCalendar.get(Calendar.YEAR);
        int month = selectedDateCalendar.get(Calendar.MONTH);
        int day = selectedDateCalendar.get(Calendar.DAY_OF_MONTH);

        // If no date was previously selected, default to today
        if (textViewSelectedDate.getText().toString().equals(getString(R.string.fecha_no_seleccionada))) {
            Calendar now = Calendar.getInstance();
            year = now.get(Calendar.YEAR);
            month = now.get(Calendar.MONTH);
            day = now.get(Calendar.DAY_OF_MONTH);
        }

        DatePickerDialog datePickerDialog = new DatePickerDialog(this, this, year, month, day);
        datePickerDialog.show();
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        selectedDateCalendar.set(Calendar.YEAR, year);
        selectedDateCalendar.set(Calendar.MONTH, month);
        selectedDateCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

        String selectedDateString = DateFormat.getDateInstance(DateFormat.MEDIUM).format(selectedDateCalendar.getTime());
        textViewSelectedDate.setText(selectedDateString);
    }

    private void registerUser() {
        String username = editTextUsername.getText() != null ? editTextUsername.getText().toString().trim() : "";
        String password = editTextPassword.getText() != null ? editTextPassword.getText().toString().trim() : ""; // NEVER store plain text passwords in a real app! Hash them.
        String dob = textViewSelectedDate.getText().toString();

        String gender = "";
        int selectedGenderId = radioGroupGender.getCheckedRadioButtonId();
        if (selectedGenderId != -1) {
            RadioButton selectedRadioButton = findViewById(selectedGenderId);
            gender = selectedRadioButton.getText().toString();
        } else {
            Toast.makeText(this, R.string.error_seleccionar_genero, Toast.LENGTH_SHORT).show();
            radioGroupGender.requestFocus();
            return;
        }

        if (username.isEmpty()) {
            editTextUsername.setError(getString(R.string.error_campo_requerido));
            editTextUsername.requestFocus();
            return;
        }
        if (password.isEmpty()) {
            editTextPassword.setError(getString(R.string.error_campo_requerido));
            editTextPassword.requestFocus();
            return;
        }
        if (dob.equals(getString(R.string.fecha_no_seleccionada))) {
            Toast.makeText(this, R.string.error_fecha_nacimiento, Toast.LENGTH_SHORT).show();
            buttonPickDate.requestFocus();
            return;
        }

        // Basic password validation (example: at least 6 characters)
        if (password.length() < 6) {
            editTextPassword.setError(getString(R.string.error_password_corta));
            editTextPassword.requestFocus();
            return;
        }


        long result = dbHelper.addUser(username, password, dob, gender);

        if (result != -1) {
            Toast.makeText(this, getString(R.string.registro_exitoso_db, username), Toast.LENGTH_LONG).show();
            Log.i(TAG, "User registered successfully: " + username);
            clearForm(); // Optionally clear form on success
        } else {
            // Check logs for specific SQLiteConstraintException if username is not unique
            Toast.makeText(this, getString(R.string.error_registro_db), Toast.LENGTH_LONG).show();
            Log.e(TAG, "Failed to register user: " + username);
        }
    }

    private void clearForm() {
        editTextUsername.setText("");
        editTextPassword.setText("");
        textViewSelectedDate.setText(R.string.fecha_no_seleccionada);
        selectedDateCalendar = Calendar.getInstance(); // Reset calendar
        radioGroupGender.clearCheck();
        editTextUsername.requestFocus();
    }

    @Override
    protected void onDestroy() {
        dbHelper.close(); // Close the database connection when the activity is destroyed
        super.onDestroy();
    }
}