package com.example.ing_software_abarrotezperez.ui

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
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

class FiadoActivity : AppCompatActivity() {

    private lateinit var db: DatabaseHelper
    private lateinit var adapter: ClienteAdapter
    private var todosLosClientes = listOf<DatabaseHelper.Cliente>()
    private var saldos = mapOf<Int, Double>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fiado)

        db = DatabaseHelper(this)

        val rvClientes     = findViewById<RecyclerView>(R.id.rvClientes)
        val etBuscar       = findViewById<TextInputEditText>(R.id.etBuscar)
        val btnNuevoCliente = findViewById<Button>(R.id.btnNuevoCliente)
        val tvVacio        = findViewById<TextView>(R.id.tvVacio)

        // Configurar RecyclerView
        adapter = ClienteAdapter(emptyList(), emptyMap()) { cliente ->
            // Al tocar "Ver" → abrir detalle del cliente
            val intent = Intent(this, DetalleFiadoActivity::class.java)
            intent.putExtra("id_cliente", cliente.idCliente)
            intent.putExtra("nombre_cliente", cliente.nombre)
            startActivity(intent)
        }
        rvClientes.layoutManager = LinearLayoutManager(this)
        rvClientes.adapter = adapter

        // Cargar clientes
        cargarClientes(rvClientes, tvVacio)

        // Buscador
        etBuscar.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val query = s.toString().lowercase()
                val filtrados = todosLosClientes.filter {
                    it.nombre.lowercase().contains(query)
                }
                adapter.actualizar(filtrados, saldos)
                tvVacio.visibility  = if (filtrados.isEmpty()) TextView.VISIBLE else TextView.GONE
                rvClientes.visibility = if (filtrados.isEmpty()) RecyclerView.GONE else RecyclerView.VISIBLE
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        // Botón nuevo cliente
        btnNuevoCliente.setOnClickListener {
            mostrarDialogoNuevoCliente(rvClientes, tvVacio)
        }
    }

    override fun onResume() {
        super.onResume()
        // Recargar al regresar del detalle
        val rvClientes = findViewById<RecyclerView>(R.id.rvClientes)
        val tvVacio    = findViewById<TextView>(R.id.tvVacio)
        cargarClientes(rvClientes, tvVacio)
    }

    private fun cargarClientes(rvClientes: RecyclerView, tvVacio: TextView) {
        todosLosClientes = db.getAllClientes()
        saldos = todosLosClientes.associate { it.idCliente to db.getSaldoPendienteCliente(it.idCliente) }

        // Solo mostrar clientes que tengan saldo pendiente
        val clientesConSaldo = todosLosClientes.filter { (saldos[it.idCliente] ?: 0.0) > 0.0 }

        adapter.actualizar(clientesConSaldo, saldos)

        tvVacio.visibility    = if (clientesConSaldo.isEmpty()) TextView.VISIBLE else TextView.GONE
        rvClientes.visibility = if (clientesConSaldo.isEmpty()) RecyclerView.GONE else RecyclerView.VISIBLE
    }

    private fun mostrarDialogoNuevoCliente(rvClientes: RecyclerView, tvVacio: TextView) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_nuevo_fiado, null)

        // Reutilizamos el dialog pero adaptamos los textos
        val tvTitulo   = dialogView.findViewById<TextView>(R.id.tvErrorFiado)
        val etDesc     = dialogView.findViewById<TextInputEditText>(R.id.etDescripcion)
        val etMonto    = dialogView.findViewById<TextInputEditText>(R.id.etMonto)
        val tvError    = dialogView.findViewById<TextView>(R.id.tvErrorFiado)
        val btnCancelar = dialogView.findViewById<Button>(R.id.btnCancelarFiado)
        val btnGuardar  = dialogView.findViewById<Button>(R.id.btnGuardarFiado)

        // Ocultamos el campo monto, aquí solo necesitamos el nombre
        etMonto.visibility = android.view.View.GONE
        etDesc.hint        = "Nombre del cliente"

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(false)
            .create()

        btnCancelar.setOnClickListener { dialog.dismiss() }

        btnGuardar.setOnClickListener {
            val nombre = etDesc.text.toString().trim()
            if (nombre.isEmpty()) {
                tvError.visibility = TextView.VISIBLE
                tvError.text = "Escribe el nombre del cliente"
                return@setOnClickListener
            }
            val resultado = db.registrarCliente(nombre)
            if (resultado != -1L) {
                Toast.makeText(this, "Cliente '$nombre' registrado ✅", Toast.LENGTH_SHORT).show()
                cargarClientes(rvClientes, tvVacio)
                dialog.dismiss()
            } else {
                tvError.visibility = TextView.VISIBLE
                tvError.text = "Error al guardar, intenta de nuevo"
            }
        }

        dialog.show()
    }
}