package com.example.ing_software_abarrotezperez.ui

import android.app.AlertDialog
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.ing_software_abarrotezperez.R
import com.google.android.material.floatingactionbutton.FloatingActionButton

class PerfilActivity : AppCompatActivity() {

    // Variable global para nuestra imagen
    private lateinit var ivFotoPerfil: ImageView

    // 1. Lanzador para abrir la GALERÍA
    private val abrirGaleria = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            // Si el usuario seleccionó una imagen, la colocamos en el ImageView
            ivFotoPerfil.setImageURI(uri)
        }
    }

    // 2. Lanzador para abrir la CÁMARA (Devuelve un Bitmap ideal para fotos de perfil)
    private val abrirCamara = registerForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap: Bitmap? ->
        if (bitmap != null) {
            // Si el usuario tomó la foto, la colocamos en el ImageView
            ivFotoPerfil.setImageBitmap(bitmap)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_perfil) // Conecta con el XML que hicimos antes

        // Enlazamos las vistas del XML con Kotlin
        ivFotoPerfil = findViewById(R.id.ivFotoPerfil)
        val btnCambiarFoto = findViewById<FloatingActionButton>(R.id.btnCambiarFoto)
        val btnRegresar = findViewById<Button>(R.id.btnRegresarConfig)

        // Qué pasa al hacer clic en el botón flotante morado
        btnCambiarFoto.setOnClickListener {
            mostrarDialogoOpciones()
        }

        // Qué pasa al hacer clic en Regresar
        btnRegresar.setOnClickListener {
            finish() // Cierra esta pantalla y te devuelve a Configuración
        }
    }

    // Función que muestra el menú para elegir Cámara o Galería
    private fun mostrarDialogoOpciones() {
        val opciones = arrayOf("Tomar foto con la cámara", "Elegir de la galería")

        AlertDialog.Builder(this)
            .setTitle("Actualizar foto de perfil")
            .setItems(opciones) { _, opcionSeleccionada ->
                when (opcionSeleccionada) {
                    0 -> {
                        // El usuario eligió la posición 0 (Cámara)
                        abrirCamara.launch(null)
                    }
                    1 -> {
                        // El usuario eligió la posición 1 (Galería)
                        // Le decimos que solo nos muestre imágenes
                        abrirGaleria.launch("image/*")
                    }
                }
            }
            .show()
    }
}