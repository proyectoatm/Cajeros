package com.example.cajeros

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class InicioSesion : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_inicio_sesion)

        val intentRegistro = Intent(this, Registro::class.java)
        lateinit var auth: FirebaseAuth
        // Initialize Firebase Auth
        auth = Firebase.auth

        val boton_inicio_sesion = findViewById<View>(R.id.boton_iniciar_sesion) as Button
        val email = findViewById<View>(R.id.email_inicio_sesion) as EditText
        val clave = findViewById<View>(R.id.clave_inicio_sesion) as EditText
        val boton_registro = findViewById<View>(R.id.txt_signup) as TextView


        boton_inicio_sesion.setOnClickListener {
            if (email.text.isNotEmpty() && clave.text.isNotEmpty()){
                auth.signInWithEmailAndPassword(email.text.toString(), clave.text.toString()).addOnCompleteListener(this){
                    if (it.isSuccessful){
                        val intent = Intent(this, MainActivity::class.java)
                        startActivity(intent)
                        finish()
                    }else{
                        showAlert()
                    }
                }
            }
        }

        boton_registro.setOnClickListener{
            startActivity(intentRegistro)
        }

    }
    private fun showAlert() {
        val alertbuilder = AlertDialog.Builder(this@InicioSesion)
        alertbuilder.setTitle("Error")
        alertbuilder.setMessage("Error al autenticar usuario")
        alertbuilder.setPositiveButton(
            "OK"
        ) { dialogInterface, i -> finish() }
        val dialog = alertbuilder.show()
        dialog.setCancelable(false)
        dialog.setCanceledOnTouchOutside(false)
    }
}