package com.example.cajeros.ui.perfil


import android.app.Activity
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.GridLayout
import android.widget.ImageButton
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class ProfileViewModel : ViewModel() {

    private val db = Firebase.firestore
    private val currentUser = Firebase.auth.currentUser

    private val _background = MutableLiveData<String>().apply {
        val docRef = db.collection("users").document(currentUser?.uid.toString())
        docRef.get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    Log.d("testeo", "DocumentSnapshot data: ${document.data}")
                    value = document.data?.get("avatar").toString()
                } else {
                    Log.d("testeo", "No such document")
                }
            }
            .addOnFailureListener { exception ->
                Log.d("testeo", "get failed with ", exception)
            }
        docRef.addSnapshotListener { snapshot, e ->
            if (e != null) {
                Log.w("testeo", "Listen failed.", e)
                return@addSnapshotListener
            }

            if (snapshot != null && snapshot.exists()) {
                Log.d("testeo", "Current data: ${snapshot.data}")
                value = snapshot.data?.get("avatar").toString()
            } else {
                Log.d("testeo", "Current data: null")
            }
        }
        Log.d("testeo", docRef.toString())
    }
    val background: LiveData<String> = _background

    private val _tvemail = MutableLiveData<String>().apply {
        val email = currentUser?.email.toString()
        value = "Email: $email"
    }

    val tvemail: LiveData<String> = _tvemail

    fun abrirDialogAvatares(activity:Activity, resource:Int, inflater:LayoutInflater, resource2:Int, exitButton: Int, grid:Int){
        val alertbuilder = AlertDialog.Builder(activity,resource)
        alertbuilder.setView(inflater.inflate(resource2, null))
        val dialog = alertbuilder.show()
        dialog.setCancelable(false)
        dialog.setCanceledOnTouchOutside(false)
        val ext = dialog.findViewById<ImageButton>(exitButton)!!
        var checked = ""
        val gridlayout = dialog.findViewById<GridLayout>(grid)!!
        for (i in 0 until gridlayout.childCount){
            val imageB: View? =gridlayout.getChildAt(i) as ImageButton
            if (imageB != null) {
                imageB.setOnClickListener{
                    Log.d("testeo", "boton "+(i+1).toString()+" presionado")
                }
            }
        }
        /*av24.setOnClickListener {
            Log.d("testeo", "boton 24 presionado")
            checked = "a24"
            if (gridlayout != null) {
                for (i in 0 until gridlayout.childCount){
                    Log.d("testeo", i.toString())
                }
            }
        }

         */
        ext.setOnClickListener {
            Log.d("testeo", checked)
            dialog.dismiss()
        }
    }

    /*
    fun avatarSeleccionado(activity: Activity, radioGroupID: Int){
        val radioGroup = RadioGroup(activity)
        radioGroup.findViewById<RadioGroup>(radioGroupID)
        val radioButtonIds = mutableListOf<Int>()
        for (i in 0 until radioGroup.childCount){
            val radioButton = radioGroup.getChildAt(i) as RadioButton
            radioButtonIds.add(radioButton.id)
        }
    }

     */


}