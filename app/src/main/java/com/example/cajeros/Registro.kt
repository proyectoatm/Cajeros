package com.example.cajeros

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class Registro : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registro)

        lateinit var auth: FirebaseAuth
        // Initialize Firebase Auth
        auth = Firebase.auth
        setup(auth)

    }


    private fun setup(auth: FirebaseAuth) {
        val boton_registro = findViewById<View>(R.id.boton_registro) as Button
        val email = findViewById<View>(R.id.email) as EditText
        val clave = findViewById<View>(R.id.clave) as EditText
        val claverep = findViewById<View>(R.id.repetir_clave) as EditText
        boton_registro.setOnClickListener {
            if (email.text.isNotEmpty() && clave.text.isNotEmpty() && claverep.text.isNotEmpty()){
                Toast.makeText(this@Registro, "primer if pasado", Toast.LENGTH_SHORT).show()
                if (clave.text.toString() == claverep.text.toString()){
                    Toast.makeText(this@Registro, "segundo if pasado", Toast.LENGTH_SHORT).show()
                    auth.createUserWithEmailAndPassword(email.text.toString(), clave.text.toString()).addOnCompleteListener(this){
                        if (it.isSuccessful){
                            val intent = Intent(this, MainActivity::class.java)
                            startActivity(intent)
                        }else{
                            showAlert()
                        }
                    }
                }
            }
        }
    }
    private fun showAlert() {
        val alertbuilder = AlertDialog.Builder(this@Registro)
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