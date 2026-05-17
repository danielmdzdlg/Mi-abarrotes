package com.example.ing_software_abarrotezperez

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView // <-- Importamos CardView (puedes borrar el import de Button)
import com.example.ing_software_abarrotezperez.ui.FiadoActivity
import com.example.ing_software_abarrotezperez.ui.InventarioActivity
import com.example.ing_software_abarrotezperez.ui.VentaActivity
import com.example.ing_software_abarrotezperez.ui.ReportesActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Referencias a los botones (ahora son CardView) del XML
        val btnInventario = findViewById<CardView>(R.id.btnIrInventario)
        val btnVentas = findViewById<CardView>(R.id.btnIrVentas)
        val btnReportes = findViewById<CardView>(R.id.btnVerReportes)
        val btnFiados = findViewById<CardView>(R.id.btnIrFiados)

        // Acción: Abrir Inventario
        btnInventario.setOnClickListener {
            val intent = Intent(this, InventarioActivity::class.java)
            startActivity(intent)
        }

        // Acción: Abrir Punto de Venta
        btnVentas.setOnClickListener {
            val intent = Intent(this, VentaActivity::class.java)
            startActivity(intent)
        }

        // Acción: Abrir Reportes de Negocio
        btnReportes.setOnClickListener {
            val intent = Intent(this, ReportesActivity::class.java)
            startActivity(intent)
        }

        // Acción: Abrir Fiados
        btnFiados.setOnClickListener {
            val intent = Intent(this, FiadoActivity::class.java)
            startActivity(intent)
        }
    }
}