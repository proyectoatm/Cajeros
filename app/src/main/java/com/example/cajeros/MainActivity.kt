package com.example.cajeros

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val boton_cerrar_sesion = findViewById<View>(R.id.boton_cerrar_sesion) as Button

        boton_cerrar_sesion.setOnClickListener {
            Firebase.auth.signOut()
            val currentUser = Firebase.auth.currentUser
            if (currentUser != null) {
                Toast.makeText(this@MainActivity, "No se pudo cerrar sesion", Toast.LENGTH_SHORT).show()
                //reload()
            }else{
                Toast.makeText(this@MainActivity, "Se cerro la sesion exitosamente", Toast.LENGTH_SHORT).show()
            }
            }
    }
}