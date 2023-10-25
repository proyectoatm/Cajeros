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

class Login : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        lateinit var auth: FirebaseAuth
        // Initialize Firebase Auth
        auth = Firebase.auth
        setup(auth)


    }


    public override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = Firebase.auth.currentUser
        if (currentUser != null) {
            Toast.makeText(this@Login, "si se ha accedido, con: "+currentUser.email.toString(), Toast.LENGTH_SHORT).show()
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
            //reload()
        }else{
            Toast.makeText(this@Login, "Debes acceder", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setup(auth: FirebaseAuth) {
        title="Autenticacion"

        val boton_registro = findViewById<View>(R.id.boton_registro) as Button
        val email = findViewById<View>(R.id.email) as EditText
        val clave = findViewById<View>(R.id.clave) as EditText
        val claverep = findViewById<View>(R.id.repetir_clave) as EditText
        boton_registro.setOnClickListener {
            if (email.text.isNotEmpty() && clave.text.isNotEmpty() && claverep.text.isNotEmpty()){
                Toast.makeText(this@Login, "primer if pasado", Toast.LENGTH_SHORT).show()
                if (clave.text.toString() == claverep.text.toString()){
                    Toast.makeText(this@Login, "segundo if pasado", Toast.LENGTH_SHORT).show()
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
        val alertbuilder = AlertDialog.Builder(this@Login)
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