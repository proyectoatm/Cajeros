package com.example.cajeros

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class MenuIngreso : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu_ingreso)

        val currentUser = Firebase.auth.currentUser
        if (currentUser != null) {
            Toast.makeText(this@MenuIngreso, "si se ha accedido, con: "+currentUser.email.toString(), Toast.LENGTH_SHORT).show()
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
            //reload()
        }else{
            Toast.makeText(this@MenuIngreso, "Debes inciar sesion o registrarte", Toast.LENGTH_SHORT).show()
            val boton_inicio_sesion = findViewById<View>(R.id.boton_inicio_sesion) as Button
            val boton_registrarse = findViewById<View>(R.id.boton_registrarse) as Button

            val intent_ingresar = Intent(this, InicioSesion::class.java)
            boton_inicio_sesion.setOnClickListener {
                startActivity(intent_ingresar)
            }

            val intent_registrarse = Intent(this, Registro::class.java)
            boton_registrarse.setOnClickListener {
                startActivity(intent_registrarse)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        val currentUser = Firebase.auth.currentUser
        if (currentUser != null) {
            finish()
        }

    }
}