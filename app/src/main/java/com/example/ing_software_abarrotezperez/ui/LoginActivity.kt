package com.example.ing_software_abarrotezperez.ui

import android.content.Intent
import android.os.Bundle
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.ing_software_abarrotezperez.MainActivity
import com.example.ing_software_abarrotezperez.R
import com.google.android.material.textfield.TextInputEditText

class LoginActivity : AppCompatActivity() {

    // ─────────────────────────────────────────────
    //  USUARIOS DEFINIDOS
    //  Para agregar más: pon "usuario" to "contraseña"
    // ─────────────────────────────────────────────
    private val usuarios = mapOf(
        "admin"  to "1234",
        "saul"   to "perez123"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val etUsuario  = findViewById<TextInputEditText>(R.id.etUsuario)
        val etPassword = findViewById<TextInputEditText>(R.id.etPassword)
        val btnEntrar  = findViewById<Button>(R.id.btnEntrar)
        val tvError    = findViewById<TextView>(R.id.tvError)

        // Entrar con el botón
        btnEntrar.setOnClickListener {
            validarLogin(
                etUsuario.text.toString().trim(),
                etPassword.text.toString(),
                tvError
            )
        }

        // Entrar presionando "Done" en el teclado
        etPassword.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                validarLogin(
                    etUsuario.text.toString().trim(),
                    etPassword.text.toString(),
                    tvError
                )
                true
            } else false
        }
    }

    private fun validarLogin(usuario: String, password: String, tvError: TextView) {
        val passwordEsperada = usuarios[usuario]

        if (passwordEsperada != null && passwordEsperada == password) {
            // Credenciales correctas → ir al menú principal
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish() // Cierra el login para que no regrese con "atrás"
        } else {
            // Credenciales incorrectas → mostrar error
            tvError.visibility = TextView.VISIBLE
        }
    }
}