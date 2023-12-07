package com.example.cajeros.ui.auth

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.cajeros.MainActivity
import com.example.cajeros.R
import com.example.cajeros.data.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class Registro : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registro)

        val db = Firebase.firestore
        lateinit var auth: FirebaseAuth
        auth = Firebase.auth
        setup(auth, db)

    }
    private fun setup(auth: FirebaseAuth, db: FirebaseFirestore) {
        val boton_registro = findViewById<View>(R.id.boton_registro) as Button
        val email = findViewById<View>(R.id.etEmail) as EditText
        val clave = findViewById<View>(R.id.etPass) as EditText
        val claverep = findViewById<View>(R.id.etRPass) as EditText
        val error = findViewById<View>(R.id.errorReg) as TextView
        boton_registro.setOnClickListener {
            if (email.text.isNotEmpty() && clave.text.isNotEmpty() && claverep.text.isNotEmpty()){
                if(email.text.toString().count { it == '@'}==1 && email.text.toString().count { it == '.'}==1) {
                    if (clave.text.toString().length >= 6) {
                        if (clave.text.toString() == claverep.text.toString()) {
                            auth.createUserWithEmailAndPassword(
                                email.text.toString(),
                                clave.text.toString()
                            ).addOnCompleteListener(this) {
                                if (it.isSuccessful) {
                                    val intent = Intent(this, MainActivity::class.java)
                                    val currentUser = Firebase.auth.currentUser
                                    Log.d(
                                        "testeo",
                                        "current user actual es : ${currentUser?.email.toString()}"
                                    )
                                    val newUser =
                                        User(email.text.toString(), "a0", "Todos", "Todos")
                                    val user = hashMapOf(
                                        "email" to newUser.email,
                                        "avatar" to newUser.avatar,
                                        "filtrobanco" to newUser.filtrobanco,
                                        "filtrodispo" to newUser.filtrodispo
                                    )
                                    db.collection("users").document(currentUser?.uid.toString())
                                        .set(user)
                                        .addOnSuccessListener { documentReference ->
                                            Log.d(
                                                "testeo",
                                                "DocumentSnapshot added with ID: ${currentUser?.uid.toString()}"
                                            )
                                        }
                                        .addOnFailureListener { e ->
                                            Log.w("testeo", "Error adding document", e)
                                        }

                                    startActivity(intent)
                                    finish()
                                } else {
                                    showAlert()
                                }
                            }
                        } else {
                            error.text = "No coinciden las contraseñas"
                        }
                    } else {
                        error.text = "La contraseña debe tener minimo 6 caracteres"
                    }
                }else{
                    error.text="El email no es valido"
                }
            }else{
                error.text="Debes llenar todos los campos"
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