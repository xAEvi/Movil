package com.example.logintarea;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;
import java.util.List;

public class Rutinas extends AppCompatActivity {

    private LinearLayout linearLayoutGalleryItems;
    private LinearLayout linearLayoutChecklistItems;
    private ScrollView galleryScrollView;
    private LinearLayout checklistContainer;
    private Button buttonToggleView;
    private Button buttonResetRoutine;
    private Button buttonCreateNewRoutine;

    private DatabaseHelper dbHelper;
    private long currentUserId = 1; // Placeholder. Debes obtener el ID del usuario logueado.

    private boolean isChecklistVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // EdgeToEdge.enable(this); // Comenta o elimina si causa problemas o no es necesario
        setContentView(R.layout.activity_rutinas);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        dbHelper = new DatabaseHelper(this);


        if (dbHelper.getUserId("defaultUser") == -1) {
            long id = dbHelper.addUser("defaultUser", "123", "01/01/1990", "Test");
            if (id != -1) currentUserId = id;
            else {
                Toast.makeText(this, "No se pudo crear usuario por defecto", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
        } else {
            currentUserId = dbHelper.getUserId("defaultUser");
        }

        linearLayoutGalleryItems = findViewById(R.id.linearLayoutGalleryItems);
        linearLayoutChecklistItems = findViewById(R.id.linearLayoutChecklistItems);
        galleryScrollView = findViewById(R.id.galleryScrollView);
        checklistContainer = findViewById(R.id.checklistContainer);
        buttonToggleView = findViewById(R.id.buttonToggleView);
        buttonResetRoutine = findViewById(R.id.buttonResetRoutine);
        buttonCreateNewRoutine = findViewById(R.id.buttonCreateNewRoutine);

        setupGallery();
        setupToggleViewButton();
        setupResetButton();
        setupCreateNewRoutineButton();

        showGalleryView(); // Inicia en la vista de galería
    }

    private void setupCreateNewRoutineButton() {
        buttonCreateNewRoutine.setOnClickListener(v -> showCreateOrEditRoutineDialog(null));
    }

    private void showCreateOrEditRoutineDialog(final Routine routineToEdit) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(routineToEdit == null ? "Crear Nueva Rutina" : "Editar Rutina");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        if (routineToEdit != null) {
            input.setText(routineToEdit.getName());
        }
        input.setHint("Nombre de la rutina");
        builder.setView(input);

        builder.setPositiveButton(routineToEdit == null ? "Crear" : "Guardar", (dialog, which) -> {
            String routineName = input.getText().toString().trim();
            if (routineName.isEmpty()) {
                Toast.makeText(Rutinas.this, "El nombre no puede estar vacío", Toast.LENGTH_SHORT).show();
                return;
            }
            if (routineToEdit == null) { // Crear
                long newId = dbHelper.addRoutine(routineName);
                if (newId == -1) { // -1 podría ser error, o si usas insertWithOnConflict y ya existe, podría ser el ID existente o un error de unique constraint
                    // Re-consulta por nombre para ver si ya existía
                    boolean exists = false;
                    for(Routine r : dbHelper.getAllRoutines()){
                        if(r.getName().equalsIgnoreCase(routineName)){
                            exists = true;
                            break;
                        }
                    }
                    if(exists){
                        Toast.makeText(Rutinas.this, "La rutina '" + routineName + "' ya existe.", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(Rutinas.this, "Error al crear la rutina.", Toast.LENGTH_SHORT).show();
                    }
                } else if (newId == 0) { // Conflicto y se ignoró
                    Toast.makeText(Rutinas.this, "La rutina '" + routineName + "' ya existe.", Toast.LENGTH_SHORT).show();
                }
                else {
                    Toast.makeText(Rutinas.this, "Rutina '" + routineName + "' creada.", Toast.LENGTH_SHORT).show();
                }
            } else { // Editar
                dbHelper.updateRoutine(routineToEdit.getId(), routineName);
                Toast.makeText(Rutinas.this, "Rutina actualizada.", Toast.LENGTH_SHORT).show();
            }
            setupGallery(); // Refrescar galería
            if (isChecklistVisible) setupChecklist(); // Refrescar checklist si está visible
        });
        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    public int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round((float) dp * density);
    }

    private void setupGallery() {
        linearLayoutGalleryItems.removeAllViews();
        List<Routine> allRoutines = dbHelper.getAllRoutines();

        if (allRoutines.isEmpty()) {
            TextView emptyView = new TextView(this);
            emptyView.setText("No hay rutinas disponibles. ¡Crea algunas!");
            emptyView.setGravity(Gravity.CENTER);
            emptyView.setPadding(16, 16, 16, 16);
            linearLayoutGalleryItems.addView(emptyView);
            return;
        }

        List<UserSelectedRoutine> userSelected = dbHelper.getUserSelectedRoutines(currentUserId);
        List<Long> userSelectedRoutineIds = new ArrayList<>();
        for(UserSelectedRoutine usr : userSelected) {
            userSelectedRoutineIds.add(usr.getRoutineId());
        }


        for (Routine routine : allRoutines) {
            LinearLayout itemLayout = new LinearLayout(this);
            itemLayout.setOrientation(LinearLayout.HORIZONTAL);
            itemLayout.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            itemLayout.setPadding(dpToPx(8), dpToPx(10), dpToPx(8), dpToPx(10)); // Ajustado padding para dp, y un poco menos vertical
            itemLayout.setGravity(Gravity.CENTER_VERTICAL);

            TextView textView = new TextView(this);
            textView.setText(routine.getName());
            textView.setTextSize(18f); // Esto está en sp por defecto para TextView, lo cual es bueno
            LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f);
            textParams.setMarginEnd(dpToPx(8)); // Añadir un margen para separar del botón
            textView.setLayoutParams(textParams);

            // --- MODIFICACIÓN AQUÍ ---
            // Botón Añadir/Quitar de "Mi Rutina" - AHORA ES UN BUTTON
            Button toggleSelectionButton = new Button(this); // CAMBIADO A Button
            boolean isSelected = userSelectedRoutineIds.contains(routine.getId());

            toggleSelectionButton.setText(isSelected ? "-" : "+");
            toggleSelectionButton.setTextSize(20f); // Tamaño del texto para el "+" o "-" (en sp)
            // toggleSelectionButton.setTypeface(null, Typeface.BOLD); // Opcional: hacerlo negrita

            // Definir tamaño del botón
            int buttonSizeInDp = 36; // Tamaño deseado en dp (ej: 36dp x 36dp)
            int buttonSizeInPx = dpToPx(buttonSizeInDp);

            LinearLayout.LayoutParams toggleBtnParams = new LinearLayout.LayoutParams(
                    buttonSizeInPx, // Ancho
                    buttonSizeInPx  // Alto
            );
            // No necesitamos setMarginEnd aquí si el textView ya tiene un marginEnd,
            // o si quieres un margen entre este botón y el siguiente (editButton)
            toggleBtnParams.setMarginEnd(dpToPx(8)); // Margen entre este botón y el de editar
            toggleSelectionButton.setLayoutParams(toggleBtnParams);

            // Asegurar que el texto esté centrado si el botón es más grande que el texto
            toggleSelectionButton.setGravity(Gravity.CENTER);
            // Quitar el padding interno por defecto del botón para que el "+" o "-"
            // pueda usar más espacio dentro del tamaño definido (buttonSizeInPx)
            toggleSelectionButton.setPadding(0,0,0,0);


            toggleSelectionButton.setOnClickListener(v -> {
                // La lógica interna para añadir/quitar de la BD sigue igual
                if (userSelectedRoutineIds.contains(routine.getId())) {
                    dbHelper.removeUserSelectedRoutine(currentUserId, routine.getId());
                    Toast.makeText(Rutinas.this, "'" + routine.getName() + "' quitada de tu rutina.", Toast.LENGTH_SHORT).show();
                } else {
                    dbHelper.addUserSelectedRoutine(currentUserId, routine.getId());
                    Toast.makeText(Rutinas.this, "'" + routine.getName() + "' añadida a tu rutina.", Toast.LENGTH_SHORT).show();
                }
                // Refrescar la galería para actualizar el texto del botón ("+" a "-") y la lista de userSelectedRoutineIds
                setupGallery();
                if (isChecklistVisible) setupChecklist();
            });

            // LOS BOTONES DE EDITAR Y ELIMINAR SIGUEN SIENDO IMAGEBUTTON (o cámbialos si también lo deseas)
            // Botón Editar
            ImageButton editButton = new ImageButton(this);
            editButton.setImageResource(android.R.drawable.ic_menu_edit);
            editButton.setBackground(null); // Mantener estilo limpio
            LinearLayout.LayoutParams editBtnParams = new LinearLayout.LayoutParams(
                    buttonSizeInPx, // Usar el mismo tamaño para consistencia
                    buttonSizeInPx
            );
            editBtnParams.setMarginEnd(dpToPx(8)); // Margen entre editar y eliminar
            editButton.setLayoutParams(editBtnParams);
            editButton.setOnClickListener(v -> showCreateOrEditRoutineDialog(routine));

            // Botón Eliminar
            ImageButton deleteButton = new ImageButton(this);
            deleteButton.setImageResource(android.R.drawable.ic_menu_delete);
            deleteButton.setBackground(null); // Mantener estilo limpio
            LinearLayout.LayoutParams deleteBtnParams = new LinearLayout.LayoutParams(
                    buttonSizeInPx, // Usar el mismo tamaño para consistencia
                    buttonSizeInPx
            );
            // No necesita marginEnd si es el último
            deleteButton.setLayoutParams(deleteBtnParams);
            deleteButton.setOnClickListener(v -> confirmDeleteRoutine(routine));

            // Añadir vistas al itemLayout
            itemLayout.addView(textView);
            itemLayout.addView(toggleSelectionButton); // Ahora es un Button
            itemLayout.addView(editButton);
            itemLayout.addView(deleteButton);

            linearLayoutGalleryItems.addView(itemLayout);
        }
    }
    // Crear iconos vectoriales:
    // En Android Studio: File > New > Vector Asset
    // 1. ic_add_to_list: Busca "add" o "playlist_add"
    // 2. ic_remove_from_list: Busca "remove" o "playlist_remove" o "clear"

    private void confirmDeleteRoutine(final Routine routine) {
        new AlertDialog.Builder(this)
                .setTitle("Eliminar Rutina")
                .setMessage("¿Estás seguro de que quieres eliminar la rutina '" + routine.getName())
                .setPositiveButton("Eliminar", (dialog, which) -> {
                    dbHelper.deleteRoutine(routine.getId());
                    Toast.makeText(Rutinas.this, "Rutina '" + routine.getName() + "' eliminada.", Toast.LENGTH_SHORT).show();
                    setupGallery();
                    if (isChecklistVisible) setupChecklist();
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }


    private void setupChecklist() {
        linearLayoutChecklistItems.removeAllViews();
        List<UserSelectedRoutine> selectedRoutines = dbHelper.getUserSelectedRoutines(currentUserId);

        if (selectedRoutines.isEmpty()) {
            TextView emptyView = new TextView(this);
            emptyView.setText("Aún no has añadido tareas a tu rutina. ¡Vuelve a la galería y añade algunas!");
            emptyView.setGravity(Gravity.CENTER);
            emptyView.setPadding(16, 16, 16, 16);
            linearLayoutChecklistItems.addView(emptyView);
            return;
        }

        for (UserSelectedRoutine userRoutine : selectedRoutines) {
            LinearLayout itemLayout = new LinearLayout(this);
            itemLayout.setOrientation(LinearLayout.HORIZONTAL);
            itemLayout.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            itemLayout.setPadding(0, 8, 0, 8);
            itemLayout.setGravity(Gravity.CENTER_VERTICAL);

            CheckBox checkBox = new CheckBox(this);
            checkBox.setChecked(userRoutine.isCompleted());
            checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                dbHelper.updateUserRoutineCompletion(userRoutine.getId(), isChecked);
                checkAllTasksCompleted(); // Verificar si todas están completadas
            });

            TextView textView = new TextView(this);
            textView.setText(userRoutine.getRoutineName());
            textView.setTextSize(18f);
            LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f);
            textParams.setMarginStart(8);
            textView.setLayoutParams(textParams);

            // Botón para quitar de "Mi Rutina" (desde la vista checklist)
            ImageButton removeFromMyRoutineButton = new ImageButton(this);
            removeFromMyRoutineButton.setImageResource(android.R.drawable.ic_menu_delete); // O tu ic_remove_from_list
            removeFromMyRoutineButton.setBackground(null);
            LinearLayout.LayoutParams removeBtnParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            removeFromMyRoutineButton.setLayoutParams(removeBtnParams);
            removeFromMyRoutineButton.setOnClickListener(v -> {
                dbHelper.removeUserSelectedRoutine(currentUserId, userRoutine.getRoutineId());
                Toast.makeText(Rutinas.this, "'" + userRoutine.getRoutineName() + "' eliminada de tu rutina.", Toast.LENGTH_SHORT).show();
                setupChecklist(); // Recargar la checklist
                setupGallery(); // Recargar la galería para actualizar el icono de añadir/quitar
            });


            itemLayout.addView(checkBox);
            itemLayout.addView(textView);
            itemLayout.addView(removeFromMyRoutineButton); // Añadir el botón
            linearLayoutChecklistItems.addView(itemLayout);
        }
        checkAllTasksCompleted(); // Comprobar estado inicial
    }

    private void checkAllTasksCompleted() {
        List<UserSelectedRoutine> selectedRoutines = dbHelper.getUserSelectedRoutines(currentUserId);
        if (selectedRoutines.isEmpty()) {
            return; // No hay tareas, no hay nada que completar
        }

        boolean allChecked = true;
        for (UserSelectedRoutine routine : selectedRoutines) {
            if (!routine.isCompleted()) {
                allChecked = false;
                break;
            }
        }

        if (allChecked) {
            Toast.makeText(this, "¡Felicidades! Has completado tu rutina.", Toast.LENGTH_LONG).show();
        }
    }

    private void setupToggleViewButton() {
        buttonToggleView.setOnClickListener(v -> {
            if (isChecklistVisible) {
                showGalleryView();
            } else {
                showChecklistView();
            }
        });
    }

    private void showGalleryView() {
        galleryScrollView.setVisibility(View.VISIBLE);
        buttonCreateNewRoutine.setVisibility(View.VISIBLE); // Mostrar botón de crear con la galería
        checklistContainer.setVisibility(View.GONE);
        buttonToggleView.setText("Ver Mi Rutina");
        isChecklistVisible = false;
        setupGallery(); // Siempre refrescar la galería al mostrarla
    }

    private void showChecklistView() {
        galleryScrollView.setVisibility(View.GONE);
        buttonCreateNewRoutine.setVisibility(View.GONE); // Ocultar botón de crear con la checklist
        checklistContainer.setVisibility(View.VISIBLE);
        buttonToggleView.setText("Ver Galería de Rutinas");
        isChecklistVisible = true;
        setupChecklist(); // Siempre refrescar la checklist al mostrarla
    }

    private void setupResetButton() {
        buttonResetRoutine.setOnClickListener(v -> {
            dbHelper.resetUserRoutineProgress(currentUserId);
            Toast.makeText(Rutinas.this, "Rutina reiniciada.", Toast.LENGTH_SHORT).show();
            setupChecklist(); // Refrescar para mostrar checkboxes desmarcados
        });
    }

    @Override
    protected void onDestroy() {
        dbHelper.close(); // Buena práctica cerrar la BD cuando la actividad se destruye
        super.onDestroy();
    }
}