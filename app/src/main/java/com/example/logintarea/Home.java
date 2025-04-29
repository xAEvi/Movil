package com.example.logintarea;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class Home extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);
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
            Toast.makeText(this, "Opción 2 seleccionada", Toast.LENGTH_SHORT).show();
            return true;
        } else if (id == R.id.menuAcerca) {
            showAcercaDeDialog();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void showAcercaDeDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        // Inflar el layout personalizado
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_acerca_de, null);

        // Establecer la vista personalizada en el constructor del diálogo
        builder.setView(dialogView);

        // Establecer el título (opcional, ya que el layout podría tenerlo)
        builder.setTitle(R.string.dialog_title_acerca_de);

        // Añadir un botón para cerrar
        builder.setPositiveButton(R.string.dialog_cerrar, (dialog, which) -> {
            // Simplemente cierra el diálogo
            dialog.dismiss();
        });

        // Crear y mostrar el AlertDialog
        AlertDialog dialog = builder.create();
        dialog.show();
    }
}