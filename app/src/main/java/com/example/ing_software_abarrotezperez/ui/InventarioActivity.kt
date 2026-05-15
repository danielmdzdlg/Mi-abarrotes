package com.example.ing_software_abarrotezperez.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.ing_software_abarrotezperez.R
import com.example.ing_software_abarrotezperez.data.DatabaseHelper
import com.example.ing_software_abarrotezperez.viewmodel.ScannerViewModel
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter

class InventarioActivity : AppCompatActivity() {

    private lateinit var vm: ScannerViewModel

    // Vistas
    private lateinit var tvEstadoBt: TextView
    private lateinit var etCodigoProducto: EditText
    private lateinit var actvNombre: AutoCompleteTextView
    private lateinit var etDescripcion: EditText
    private lateinit var etPrecioVenta: EditText
    private lateinit var etStock: EditText
    private lateinit var etCaducidad: EditText
    private lateinit var spTipo: Spinner
    private lateinit var btnGuardar: Button
    private lateinit var btnLimpiar: Button
    private lateinit var ivBarcode: ImageView // Nueva vista para la imagen del código

    private var codigoActual: String = ""

    // Variables para el interceptor HID
    private val barcodeBuffer = StringBuilder()
    private var lastKeyTime = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.inventario)

        vm = ViewModelProvider(this)[ScannerViewModel::class.java]

        // Bind vistas
        tvEstadoBt       = findViewById(R.id.tvEstadoBtInv)
        etCodigoProducto = findViewById(R.id.etCodigoProducto)
        ivBarcode        = findViewById(R.id.ivBarcode)
        actvNombre       = findViewById(R.id.actvNombre)
        etDescripcion    = findViewById(R.id.etDescripcion)
        etPrecioVenta    = findViewById(R.id.etPrecioVenta)
        etStock          = findViewById(R.id.etStock)
        etCaducidad      = findViewById(R.id.etCaducidad)
        spTipo           = findViewById(R.id.spTipo)
        btnGuardar       = findViewById(R.id.btnGuardarProducto)
        btnLimpiar       = findViewById(R.id.btnLimpiar)

        configurarSugerenciasNombres()
        configurarFormatoFecha()
        configurarSpinnerTipo()

        btnGuardar.setOnClickListener { guardarProducto() }
        btnLimpiar.setOnClickListener { limpiarFormulario() }

        // Bloquear stock por defecto hasta que se elija "No Perecedero"
        etStock.isEnabled = false
    }

    private fun configurarSpinnerTipo() {
        // Opciones: Índice 0 = Perecedero, Índice 1 = No Perecedero
        val opciones = arrayOf("Perecedero", "No Perecedero")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, opciones)
        spTipo.adapter = adapter

        spTipo.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (position == 1) {
                    // Es NO PERECEDERO
                    etCaducidad.isEnabled = false
                    etCaducidad.setText("")
                    etStock.isEnabled = true // Liberamos el stock manual
                    Toast.makeText(this@InventarioActivity, "Stock manual habilitado", Toast.LENGTH_SHORT).show()
                } else {
                    // Es PERECEDERO
                    etCaducidad.isEnabled = true
                    etStock.isEnabled = false // Bloqueamos el stock (solo aumenta por escáner)
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    // --- GENERAR IMAGEN DEL CÓDIGO DE BARRAS ---
    private fun generarCodigoBarrasImagen(codigo: String) {
        try {
            val bitMatrix = MultiFormatWriter().encode(codigo, BarcodeFormat.CODE_128, 600, 200)
            val width = bitMatrix.width
            val height = bitMatrix.height
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
            for (x in 0 until width) {
                for (y in 0 until height) {
                    bitmap.setPixel(x, y, if (bitMatrix.get(x, y)) Color.BLACK else Color.WHITE)
                }
            }
            ivBarcode.setImageBitmap(bitmap)
        } catch (e: Exception) {
            e.printStackTrace()
            // Si falla, limpiar la imagen
            ivBarcode.setImageDrawable(null)
        }
    }

    private fun configurarFormatoFecha() {
        etCaducidad.addTextChangedListener(object : TextWatcher {
            private var isUpdating = false
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                if (isUpdating) return
                isUpdating = true

                val cleanString = s.toString().replace(Regex("[^\\d]"), "")
                var formattedDate = ""
                var isValidDate = true

                if (cleanString.isNotEmpty()) {
                    val dayStr = cleanString.take(2)
                    formattedDate += dayStr
                    if (dayStr.length == 2) {
                        val day = dayStr.toInt()
                        if (day < 1 || day > 31) isValidDate = false
                    }

                    if (cleanString.length > 2) {
                        formattedDate += "/"
                        val monthStr = cleanString.substring(2, minOf(4, cleanString.length))
                        formattedDate += monthStr
                        if (monthStr.length == 2) {
                            val month = monthStr.toInt()
                            if (month < 1 || month > 12) isValidDate = false
                        }

                        if (cleanString.length > 4) {
                            formattedDate += "/"
                            formattedDate += cleanString.substring(4, minOf(6, cleanString.length))
                        }
                    }
                }

                etCaducidad.setText(formattedDate)
                etCaducidad.setSelection(formattedDate.length)

                if (!isValidDate) {
                    etCaducidad.setTextColor(Color.RED)
                    vibrarError()
                } else {
                    etCaducidad.setTextColor(Color.WHITE) // El texto original es blanco
                }

                isUpdating = false
            }
        })
    }

    private fun vibrarError() {
        val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(300, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            vibrator.vibrate(300)
        }
    }

    private fun configurarSugerenciasNombres() {
        val productos = vm.db.getAllProductos()
        val nombresExistentes = productos.map { it.nombre }.distinct()

        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_dropdown_item_1line,
            nombresExistentes
        )
        actvNombre.setAdapter(adapter)
    }

    private fun habilitarCamposModoNuevo(esNuevo: Boolean) {
        actvNombre.isEnabled = esNuevo
        etPrecioVenta.isEnabled = esNuevo
        spTipo.isEnabled = esNuevo

        etDescripcion.isEnabled = true
        // El stock y caducidad se gestionan en el OnItemSelected del Spinner
    }

    private fun procesarEscaneoInventario(codigo: String) {
        codigoActual = codigo
        etCodigoProducto.setText(codigo)
        generarCodigoBarrasImagen(codigo) // Dibuja la barra

        val existente = vm.db.getProductoPorCodigo(codigo)
        if (existente != null) {
            // EL PRODUCTO YA EXISTE
            val stockNuevo = existente.stock + 1
            etStock.setText(stockNuevo.toString())

            actvNombre.setText(existente.nombre)
            etDescripcion.setText(existente.descripcion)
            etPrecioVenta.setText(existente.precioVenta.toString())

            if (existente.fechaCaducidad.isNullOrEmpty()) {
                spTipo.setSelection(1) // No Perecedero
            } else {
                spTipo.setSelection(0) // Perecedero
                etCaducidad.setText(existente.fechaCaducidad)
            }

            habilitarCamposModoNuevo(false)
            Toast.makeText(this, "Producto conocido. Agregando lote (Stock: $stockNuevo)", Toast.LENGTH_SHORT).show()
        } else {
            // ES UN PRODUCTO NUEVO
            limpiarSoloCampos()
            etStock.setText("1")

            habilitarCamposModoNuevo(true)
            Toast.makeText(this, "Producto nuevo. Registre los datos.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun guardarProducto() {
        if (codigoActual.isEmpty()) {
            Toast.makeText(this, "Escanea un código primero", Toast.LENGTH_SHORT).show()
            return
        }

        val nombre = actvNombre.text.toString().trim()
        if (nombre.isEmpty()) {
            actvNombre.error = "Nombre requerido"
            return
        }

        if (spTipo.selectedItemPosition == 0 && etCaducidad.currentTextColor == Color.RED) {
            Toast.makeText(this, "Por favor, ingresa una fecha válida", Toast.LENGTH_SHORT).show()
            vibrarError()
            return
        }

        val precio = etPrecioVenta.text.toString().toDoubleOrNull() ?: 0.0
        val caducidad = if (spTipo.selectedItemPosition == 1) null else etCaducidad.text.toString().trim().ifEmpty { null }

        // Lee el stock actual (sea por autoincremento o manual)
        val stockGuardar = etStock.text.toString().toIntOrNull() ?: 0

        val producto = DatabaseHelper.Producto(
            codigoBarras   = codigoActual,
            nombre         = nombre,
            descripcion    = etDescripcion.text.toString().trim(),
            precioVenta    = precio,
            stock          = stockGuardar,
            fechaCaducidad = caducidad
        )

        val result = vm.db.upsertProducto(producto)
        if (result >= 0) {
            Toast.makeText(this, "✓ Guardado exitosamente", Toast.LENGTH_SHORT).show()
            configurarSugerenciasNombres()
            limpiarFormulario()
        } else {
            Toast.makeText(this, "Error al guardar", Toast.LENGTH_SHORT).show()
        }
    }

    private fun limpiarFormulario() {
        codigoActual = ""
        etCodigoProducto.setText("")
        ivBarcode.setImageDrawable(null) // Borrar la imagen de las barras
        limpiarSoloCampos()
        habilitarCamposModoNuevo(true)
    }

    private fun limpiarSoloCampos() {
        etStock.setText("")
        actvNombre.setText("")
        etDescripcion.setText("")
        etPrecioVenta.setText("")
        etCaducidad.setText("")
        spTipo.setSelection(0)
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  INTERCEPTOR MODO HID (Teclado del Escáner Bluetooth)
    // ─────────────────────────────────────────────────────────────────────────
    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        val isHardwareDevice = event.device != null && !event.device.isVirtual

        if (event.action == KeyEvent.ACTION_DOWN) {
            val timeNow = System.currentTimeMillis()

            if (event.keyCode == KeyEvent.KEYCODE_ENTER) {
                val codigoLeido = barcodeBuffer.toString().trim()
                if (codigoLeido.isNotEmpty()) {
                    procesarEscaneoInventario(codigoLeido)
                    barcodeBuffer.clear()
                    return true
                }
            }

            val pressedChar = event.unicodeChar.toChar()
            if (pressedChar.isDefined() && event.unicodeChar > 31) {
                if (timeNow - lastKeyTime > 200) {
                    barcodeBuffer.clear()
                }
                barcodeBuffer.append(pressedChar)
                lastKeyTime = timeNow

                if (isHardwareDevice) {
                    return true
                }
            }
        } else if (event.action == KeyEvent.ACTION_UP) {
            if (isHardwareDevice && (event.unicodeChar > 31 || event.keyCode == KeyEvent.KEYCODE_ENTER)) {
                return true
            }
        }
        return super.dispatchKeyEvent(event)
    }
}