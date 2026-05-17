package com.example.ing_software_abarrotezperez.ui

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.ing_software_abarrotezperez.R
import com.google.android.material.card.MaterialCardView
import com.google.android.material.progressindicator.CircularProgressIndicator
import java.io.File

class AlmacenamientoActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_almacenamiento)

        val btnRespaldo = findViewById<MaterialCardView>(R.id.btnRealizarRespaldo)
        val btnRegresar = findViewById<Button>(R.id.btnRegresarAlmacenamiento)
        val progressApp = findViewById<CircularProgressIndicator>(R.id.progressAppUsage)
        val tvPorcentaje = findViewById<TextView>(R.id.tvPorcentajeUso)

        // Simulación de cálculo de espacio
        // En una app real usaríamos: context.dbPath("nombre_db").length()
        calcularEspacio()

        btnRespaldo.setOnClickListener {
            // Aquí iría la lógica de Google Drive API
            Toast.makeText(this, "Iniciando respaldo en Google Drive...", Toast.LENGTH_LONG).show()
        }

        btnRegresar.setOnClickListener {
            finish()
        }
    }

    private fun calcularEspacio() {
        // Lógica para obtener el tamaño de la base de datos y archivos internos
        // Por ahora lo dejamos visual, pero aquí es donde actualizarías el progressApp.progress
    }
}