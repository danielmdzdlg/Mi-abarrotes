package com.example.ing_software_abarrotezperez.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.ing_software_abarrotezperez.R
import com.example.ing_software_abarrotezperez.data.DatabaseHelper
import com.google.android.material.textfield.TextInputEditText

class DetalleFiadoActivity : AppCompatActivity() {

    private lateinit var db: DatabaseHelper
    private var idCliente: Int = -1
    private var nombreCliente: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detalle_fiado)

        db            = DatabaseHelper(this)
        idCliente     = intent.getIntExtra("id_cliente", -1)
        nombreCliente = intent.getStringExtra("nombre_cliente") ?: "Cliente"

        val tvTitulo         = findViewById<TextView>(R.id.tvTituloCliente)
        val tvSaldoTotal     = findViewById<TextView>(R.id.tvSaldoTotal)
        val btnRegresar      = findViewById<Button>(R.id.btnRegresar)
        val btnNuevoFiado    = findViewById<Button>(R.id.btnNuevoFiado)
        val btnRegistrarPago = findViewById<Button>(R.id.btnRegistrarPago)
        val rvFiados         = findViewById<RecyclerView>(R.id.rvFiados)
        val tvSinDeudas      = findViewById<TextView>(R.id.tvSinDeudas)

        tvTitulo.text = "👤 $nombreCliente"
        rvFiados.layoutManager = LinearLayoutManager(this)

        cargarDetalle(tvSaldoTotal, rvFiados, tvSinDeudas)

        btnRegresar.setOnClickListener { finish() }
        btnNuevoFiado.setOnClickListener { mostrarDialogoNuevoFiado(tvSaldoTotal, rvFiados, tvSinDeudas) }
        btnRegistrarPago.setOnClickListener { mostrarDialogoPago(tvSaldoTotal, rvFiados, tvSinDeudas) }
    }

    private fun cargarDetalle(tvSaldoTotal: TextView, rvFiados: RecyclerView, tvSinDeudas: TextView) {
        val saldoTotal = db.getSaldoPendienteCliente(idCliente)
        tvSaldoTotal.text = "$${"%.2f".format(saldoTotal)}"

        val fiados = db.getFiadosActivosPorCliente(idCliente)

        if (fiados.isEmpty()) {
            rvFiados.visibility    = View.GONE
            tvSinDeudas.visibility = View.VISIBLE
        } else {
            rvFiados.visibility    = View.VISIBLE
            tvSinDeudas.visibility = View.GONE

            val textos = fiados.mapIndexed { index, fiado ->
                "Deuda #${index + 1} — Saldo: $${"%.2f".format(fiado.saldoPendiente)}"
            }

            rvFiados.adapter = object : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
                override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): RecyclerView.ViewHolder {
                    val tv = TextView(parent.context).apply {
                        setPadding(32, 24, 32, 24)
                        textSize = 15f
                        setTextColor(android.graphics.Color.parseColor("#212121"))
                    }
                    return object : RecyclerView.ViewHolder(tv) {}
                }
                override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
                    (holder.itemView as TextView).text = textos[position]
                }
                override fun getItemCount() = textos.size
            }
        }
    }

    private fun mostrarDialogoNuevoFiado(tvSaldoTotal: TextView, rvFiados: RecyclerView, tvSinDeudas: TextView) {
        val dialogView  = LayoutInflater.from(this).inflate(R.layout.dialog_nuevo_fiado, null)
        val etDesc      = dialogView.findViewById<TextInputEditText>(R.id.etDescripcion)
        val etMonto     = dialogView.findViewById<TextInputEditText>(R.id.etMonto)
        val tvError     = dialogView.findViewById<TextView>(R.id.tvErrorFiado)
        val btnCancelar = dialogView.findViewById<Button>(R.id.btnCancelarFiado)
        val btnGuardar  = dialogView.findViewById<Button>(R.id.btnGuardarFiado)

        // FIX Problema 1: hint claro para no confundir con productos
        etDesc.hint = "Concepto del adeudo (ej: compra del dia, mandado...)"

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(false)
            .create()

        btnCancelar.setOnClickListener { dialog.dismiss() }

        btnGuardar.setOnClickListener {
            val desc  = etDesc.text.toString().trim()
            val monto = etMonto.text.toString().toDoubleOrNull()

            if (desc.isEmpty() || monto == null || monto <= 0) {
                tvError.visibility = View.VISIBLE
                tvError.text = "Por favor completa todos los campos"
                return@setOnClickListener
            }

            val ok = db.registrarFiado(idCliente, monto)
            if (ok) {
                Toast.makeText(this, "Deuda registrada ✅", Toast.LENGTH_SHORT).show()
                cargarDetalle(tvSaldoTotal, rvFiados, tvSinDeudas)
                dialog.dismiss()
            } else {
                tvError.visibility = View.VISIBLE
                tvError.text = "Error al guardar, intenta de nuevo"
            }
        }

        dialog.show()
    }

    private fun mostrarDialogoPago(tvSaldoTotal: TextView, rvFiados: RecyclerView, tvSinDeudas: TextView) {
        val fiados = db.getFiadosActivosPorCliente(idCliente)
        if (fiados.isEmpty()) {
            Toast.makeText(this, "Este cliente no tiene deudas activas", Toast.LENGTH_SHORT).show()
            return
        }

        val fiadoActivo = fiados.first()

        val dialogView  = LayoutInflater.from(this).inflate(R.layout.dialog_nuevo_fiado, null)
        val etDesc      = dialogView.findViewById<TextInputEditText>(R.id.etDescripcion)
        val etMonto     = dialogView.findViewById<TextInputEditText>(R.id.etMonto)
        val tvError     = dialogView.findViewById<TextView>(R.id.tvErrorFiado)
        val btnCancelar = dialogView.findViewById<Button>(R.id.btnCancelarFiado)
        val btnGuardar  = dialogView.findViewById<Button>(R.id.btnGuardarFiado)

        etDesc.visibility = View.GONE
        etMonto.hint = "Monto a pagar (máx: $${"%.2f".format(fiadoActivo.saldoPendiente)})"

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(false)
            .create()

        btnCancelar.setOnClickListener { dialog.dismiss() }

        btnGuardar.setOnClickListener {
            val monto = etMonto.text.toString().toDoubleOrNull()

            if (monto == null || monto <= 0) {
                tvError.visibility = View.VISIBLE
                tvError.text = "Ingresa un monto válido"
                return@setOnClickListener
            }

            // FIX Problema 2: redondear para evitar error de precisión con decimales
            val montoPago   = "%.2f".format(monto).toDouble()
            val saldoMaximo = "%.2f".format(fiadoActivo.saldoPendiente).toDouble()

            if (montoPago > saldoMaximo) {
                tvError.visibility = View.VISIBLE
                tvError.text = "El monto no puede ser mayor al saldo ($${"%.2f".format(saldoMaximo)})"
                return@setOnClickListener
            }

            val ok = db.registrarPagoFiado(fiadoActivo.idFiado, montoPago)
            if (ok) {
                Toast.makeText(this, "Pago registrado ✅", Toast.LENGTH_SHORT).show()
                cargarDetalle(tvSaldoTotal, rvFiados, tvSinDeudas)
                dialog.dismiss()
            } else {
                tvError.visibility = View.VISIBLE
                tvError.text = "Error al guardar, intenta de nuevo"
            }
        }

        dialog.show()
    }
}