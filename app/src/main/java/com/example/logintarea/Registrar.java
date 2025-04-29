package com.example.logintarea; // Make sure this matches your package name

import android.app.DatePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RatingBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.util.Calendar;

public class Registrar extends AppCompatActivity implements DatePickerDialog.OnDateSetListener {

    private static final String TAG = "RegistroActivity";
    private static final String FILENAME = "registration_data.txt";
    private static final String DELIMITER = ";";
    private static final int EXPECTED_FIELDS = 9;

    private TextInputEditText editTextCedula;
    private TextInputEditText editTextNombres;
    private TextInputEditText editTextApellidos;
    private TextInputEditText editTextEdad;
    private Spinner spinnerNacionalidad;
    private Spinner spinnerGenero;
    private RadioGroup radioGroupEstadoCivil;
    private Button buttonPickDate;
    private TextView textViewSelectedDate;
    private RatingBar ratingBarNivelIngles;
    private Button buttonRegistrar;
    private Button buttonBorrar;
    private Button buttonCargarDatos; // New button
    private Button buttonCancelar;

    private Calendar selectedDateCalendar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registrar);

        initializeViews();
        setupSpinners();
        setupButtonClickListeners();
    }

    private void initializeViews() {
        editTextCedula = findViewById(R.id.editTextCedula);
        editTextNombres = findViewById(R.id.editTextNombres);
        editTextApellidos = findViewById(R.id.editTextApellidos);
        editTextEdad = findViewById(R.id.editTextEdad);
        spinnerNacionalidad = findViewById(R.id.spinnerNacionalidad);
        spinnerGenero = findViewById(R.id.spinnerGenero);
        radioGroupEstadoCivil = findViewById(R.id.radioGroupEstadoCivil);
        buttonPickDate = findViewById(R.id.buttonPickDate);
        textViewSelectedDate = findViewById(R.id.textViewSelectedDate);
        ratingBarNivelIngles = findViewById(R.id.ratingBarNivelIngles);
        buttonRegistrar = findViewById(R.id.buttonRegistrar);
        buttonBorrar = findViewById(R.id.buttonBorrar);
        buttonCargarDatos = findViewById(R.id.buttonCargarDatos);
        buttonCancelar = findViewById(R.id.buttonCancelar);
        selectedDateCalendar = Calendar.getInstance();
        textViewSelectedDate.setText(getString(R.string.fecha_no_seleccionada));
    }

    private void setupSpinners() {
        // Nacionalidad
        ArrayAdapter<CharSequence> nacionalidadAdapter = ArrayAdapter.createFromResource(this,
                R.array.nacionalidades_array, android.R.layout.simple_spinner_item);
        nacionalidadAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerNacionalidad.setAdapter(nacionalidadAdapter);

        // Genero
        ArrayAdapter<CharSequence> generoAdapter = ArrayAdapter.createFromResource(this,
                R.array.generos_array, android.R.layout.simple_spinner_item);
        generoAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerGenero.setAdapter(generoAdapter);
    }

    private void setupButtonClickListeners() {
        // Date Picker
        buttonPickDate.setOnClickListener(v -> showDatePickerDialog());

        // Registrar
        buttonRegistrar.setOnClickListener(v -> saveDataToFile());

        // Borrar
        buttonBorrar.setOnClickListener(v -> clearForm());

        // Cargar Datos
        buttonCargarDatos.setOnClickListener(v -> loadAndShowData());

        // Cancelar
        buttonCancelar.setOnClickListener(v -> {
            finish();
        });
    }

    private void showDatePickerDialog() {
        int year = selectedDateCalendar.get(Calendar.YEAR);
        int month = selectedDateCalendar.get(Calendar.MONTH);
        int day = selectedDateCalendar.get(Calendar.DAY_OF_MONTH);

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

        // Use a consistent format, like MEDIUM
        String selectedDateString = DateFormat.getDateInstance(DateFormat.MEDIUM).format(selectedDateCalendar.getTime());
        textViewSelectedDate.setText(selectedDateString);
    }

    private void saveDataToFile() {
        String cedula = editTextCedula.getText() != null ? editTextCedula.getText().toString().trim() : "";
        String nombres = editTextNombres.getText() != null ? editTextNombres.getText().toString().trim() : "";
        String apellidos = editTextApellidos.getText() != null ? editTextApellidos.getText().toString().trim() : "";
        String edad = editTextEdad.getText() != null ? editTextEdad.getText().toString().trim() : "";
        String nacionalidad = spinnerNacionalidad.getSelectedItem() != null ? spinnerNacionalidad.getSelectedItem().toString() : "";
        String genero = spinnerGenero.getSelectedItem() != null ? spinnerGenero.getSelectedItem().toString() : "";

        int selectedEstadoCivilId = radioGroupEstadoCivil.getCheckedRadioButtonId();
        String estadoCivil = "";
        if (selectedEstadoCivilId != -1) {
            RadioButton selectedRadioButton = findViewById(selectedEstadoCivilId);
            estadoCivil = selectedRadioButton.getText().toString();
        } else {
            Toast.makeText(this, R.string.error_estado_civil, Toast.LENGTH_SHORT).show();
            radioGroupEstadoCivil.requestFocus();
            return;
        }

        String fechaNacimiento = textViewSelectedDate.getText().toString();
        if (fechaNacimiento.equals(getString(R.string.fecha_no_seleccionada))) {
            Toast.makeText(this, R.string.error_fecha_nacimiento, Toast.LENGTH_SHORT).show();
            buttonPickDate.requestFocus();
            return;
        }

        if (cedula.isEmpty() || nombres.isEmpty() || apellidos.isEmpty() || edad.isEmpty()) {
            Toast.makeText(this, "Por favor complete todos los campos de texto.", Toast.LENGTH_SHORT).show();
            if (cedula.isEmpty()) editTextCedula.requestFocus();
            else if (nombres.isEmpty()) editTextNombres.requestFocus();
            else if (apellidos.isEmpty()) editTextApellidos.requestFocus();
            else editTextEdad.requestFocus();
            return;
        }


        String nivelIngles = String.valueOf(ratingBarNivelIngles.getRating()); // Store rating as string

        String dataLine = String.join(DELIMITER,
                cedula,
                nombres,
                apellidos,
                edad,
                nacionalidad,
                genero,
                estadoCivil,
                fechaNacimiento,
                nivelIngles
        );

        FileOutputStream fos = null;
        OutputStreamWriter osw = null;
        BufferedWriter writer = null;
        try {
            fos = openFileOutput(FILENAME, Context.MODE_PRIVATE);
            osw = new OutputStreamWriter(fos, StandardCharsets.UTF_8);
            writer = new BufferedWriter(osw);
            writer.write(dataLine);
            writer.newLine();
            Toast.makeText(this, R.string.datos_guardados_exito, Toast.LENGTH_SHORT).show();
            Log.i(TAG, "Datos guardados: " + dataLine);
        } catch (IOException e) {
            Log.e(TAG, "Error al guardar archivo", e);
            Toast.makeText(this, getString(R.string.error_guardar_datos, e.getMessage()), Toast.LENGTH_LONG).show();
        } finally {
            try {
                if (writer != null) writer.close();
                else if (osw != null) osw.close();
                else if (fos != null) fos.close();
            } catch (IOException e) {
                Log.e(TAG, "Error cerrando writer/streams", e);
            }
        }
    }

    private void loadAndShowData() {
        FileInputStream fis = null;
        InputStreamReader isr = null;
        BufferedReader reader = null;
        String dataLine = null;

        try {
            fis = openFileInput(FILENAME);
            isr = new InputStreamReader(fis, StandardCharsets.UTF_8);
            reader = new BufferedReader(isr);
            dataLine = reader.readLine();

            if (dataLine != null && !dataLine.isEmpty()) {
                Log.i(TAG, "Datos leídos: " + dataLine);
                displayDataInDialog(dataLine);
            } else {
                Toast.makeText(this, R.string.datos_no_encontrados, Toast.LENGTH_SHORT).show();
            }

        } catch (FileNotFoundException e) {
            Log.w(TAG, "Archivo no encontrado: " + FILENAME);
            Toast.makeText(this, R.string.datos_no_encontrados, Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Log.e(TAG, "Error al leer archivo", e);
            Toast.makeText(this, getString(R.string.error_cargar_datos, e.getMessage()), Toast.LENGTH_LONG).show();
        } finally {
            try {
                if (reader != null) reader.close();
                else if (isr != null) isr.close();
                else if (fis != null) fis.close();
            } catch (IOException e) {
                Log.e(TAG, "Error cerrando reader/streams", e);
            }
        }
    }

    private void displayDataInDialog(String dataLine) {
        String[] fields = dataLine.split(DELIMITER, -1);

        if (fields.length != EXPECTED_FIELDS) {
            Log.e(TAG, "Error de formato. Esperados: " + EXPECTED_FIELDS + ", Obtenidos: " + fields.length);
            Toast.makeText(this, R.string.error_formato_datos, Toast.LENGTH_LONG).show();
            return;
        }

        String dialogMessage = getString(R.string.dialogo_datos_mensaje_formato,
                fields[0], // Cedula
                fields[1], // Nombres
                fields[2], // Apellidos
                fields[3], // Edad
                fields[4], // Nacionalidad
                fields[5], // Genero
                fields[6], // Estado Civil
                fields[7], // Fecha Nacimiento
                fields[8]  // Nivel Ingles
        );

        new AlertDialog.Builder(this)
                .setTitle(R.string.dialogo_titulo_datos_registrados)
                .setMessage(dialogMessage)
                .setPositiveButton(R.string.dialogo_ok, (dialog, which) -> dialog.dismiss())
                .setIcon(android.R.drawable.ic_dialog_info)
                .setCancelable(true)
                .show();
    }


    private void clearForm() {
        editTextCedula.setText("");
        editTextNombres.setText("");
        editTextApellidos.setText("");
        editTextEdad.setText("");
        spinnerNacionalidad.setSelection(0);
        spinnerGenero.setSelection(0);
        radioGroupEstadoCivil.clearCheck();
        textViewSelectedDate.setText(R.string.fecha_no_seleccionada);
        selectedDateCalendar = Calendar.getInstance();
        ratingBarNivelIngles.setRating(0);

        editTextCedula.requestFocus();
    }
}