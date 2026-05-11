package com.example.ing_software_abarrotezperez.ui

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.ing_software_abarrotezperez.R
import com.example.ing_software_abarrotezperez.data.DatabaseHelper

class ReportesActivity : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reportes)

        dbHelper = DatabaseHelper(this)

        val tvGanancia = findViewById<TextView>(R.id.tvGananciaTotal)
        val ganancia = dbHelper.getReporteGanancias()
        tvGanancia.text = "$${String.format("%.2f", ganancia)}"

        val tvTop = findViewById<TextView>(R.id.tvTopVendidos)
        val topList = dbHelper.getTopVendidos()
        if (topList.isEmpty()) {
            tvTop.text = "Aún no hay ventas registradas."
        } else {
            tvTop.text = topList.joinToString("\n") { "${it.first}: ${it.second} unidades" }
        }

        val tvMargen = findViewById<TextView>(R.id.tvComparativaMargen)
        val margenList = dbHelper.getComparativaMargen()
        if (margenList.isEmpty()) {
            tvMargen.text = "No hay datos de costos disponibles."
        } else {
            tvMargen.text = margenList.joinToString("\n")
        }

        val tvMermaTotal = findViewById<TextView>(R.id.tvMermaTotal)
        val tvTopMerma = findViewById<TextView>(R.id.tvTopMerma)

        val totalMerma = dbHelper.getTotalMermaUnidades()
        tvMermaTotal.text = totalMerma.toString()

        val topMerma = dbHelper.getTopProductosMermados()
        tvTopMerma.text = if (topMerma.isEmpty()) {
            "No hay registros de merma"
        } else {
            topMerma.joinToString("\n") { "${it.first}: ${it.second} unidades" }
        }

        val tvTotalFiado = findViewById<TextView>(R.id.tvTotalFiado)
        val tvClientesDeudores = findViewById<TextView>(R.id.tvClientesDeudores)

        val deudaTotal = dbHelper.getTotalDeudaPendiente()
        tvTotalFiado.text = "$${String.format("%.2f", deudaTotal)}"

        val clientesDeuda = dbHelper.getClientesConDeuda()
        tvClientesDeudores.text = if (clientesDeuda.isEmpty()) {
            "No hay clientes con deuda"
        } else {
            clientesDeuda.joinToString("\n") { "${it.second}: $${String.format("%.2f", it.third)}" }
        }
    }
}