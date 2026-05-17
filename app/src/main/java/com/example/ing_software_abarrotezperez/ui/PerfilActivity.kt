package com.example.ing_software_abarrotezperez.ui

import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.ing_software_abarrotezperez.R
import java.io.File
import java.io.FileOutputStream

class PerfilActivity : AppCompatActivity() {

    private lateinit var ivFotoPerfil: ImageView
    private lateinit var btnCambiarFoto: Button

    // Selector moderno que no requiere permisos extra en el Manifest
    private val pickMedia = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            ivFotoPerfil.setImageURI(uri)
            guardarFotoInternamente(uri)
        } else {
            Toast.makeText(this, "No seleccionaste ninguna imagen", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_perfil)

        ivFotoPerfil = findViewById(R.id.ivFotoPerfil)
        btnCambiarFoto = findViewById(R.id.btnCambiarFoto)

        cargarFotoGuardada()

        btnCambiarFoto.setOnClickListener {
            pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }
    }

    private fun guardarFotoInternamente(uri: Uri) {
        try {
            val inputStream = contentResolver.openInputStream(uri)
            val file = File(filesDir, "foto_perfil.jpg")
            val outputStream = FileOutputStream(file)

            inputStream?.use { input ->
                outputStream.use { output ->
                    input.copyTo(output)
                }
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Error al guardar imagen", Toast.LENGTH_SHORT).show()
        }
    }

    private fun cargarFotoGuardada() {
        val file = File(filesDir, "foto_perfil.jpg")
        if (file.exists()) {
            ivFotoPerfil.setImageURI(Uri.fromFile(file))
        }
    }
}