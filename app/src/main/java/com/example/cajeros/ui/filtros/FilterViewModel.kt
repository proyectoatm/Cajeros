package com.example.cajeros.ui.filtros

import android.app.Activity
import android.util.Log
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.cajeros.R
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class FilterViewModel : ViewModel() {

    private val db = Firebase.firestore
    private val currentUser = Firebase.auth.currentUser

    private val _banco = MutableLiveData<String>().apply {
        val docRef = db.collection("users").document(currentUser?.uid.toString())
        docRef.get()
        docRef.addSnapshotListener { snapshot, e ->
            if (e != null) {
                Log.w("testeo", "Listen failed.", e)
                return@addSnapshotListener
            }
            if (snapshot != null) {
                value=snapshot.data?.get("filtrobanco").toString()
            } else {
                Log.d("testeo", "Current data: null")
            }
        }
    }
    val banco: LiveData<String> = _banco

    fun abrirDialogFilter(activity:Activity, resource:Int, inflater: LayoutInflater, resource2:Int, scroll:Int) {
        val alertbuilder = AlertDialog.Builder(activity, resource)
        alertbuilder.setView(inflater.inflate(resource2, null))
        val dialog = alertbuilder.show()
        val width = 230 // Width en dp
        val height = 305 // Height en dp
        val scale = activity.resources.displayMetrics.density
        dialog.window?.setLayout((width * scale).toInt(), (height * scale).toInt())
        val scrollFilter = dialog.findViewById<LinearLayout>(scroll)!!
        var lista = mutableListOf<String>()
        val docRef = db.collection("cajeros")
        docRef.get()
        docRef.addSnapshotListener { snapshot, e ->
            if (e != null) {
                Log.w("testeo", "Listen failed.", e)
                return@addSnapshotListener
            }
            if (snapshot != null) {
                lista.clear()
                lista.add(banco.value.toString())
                lista.add("Todos")
                for (document in snapshot) {
                    lista.add(document.data.get("banco").toString())
                }
                lista = lista.distinct().toMutableList()
                Log.d("testeo", "lista de cosas: " + lista + ", tamaño: " + lista.size)
                for (i in 0..lista.size - 1) {
                    val inflater = LayoutInflater.from(activity)
                    val view = inflater.inflate(R.layout.custom_textviews, null)
                    val textV = view.findViewById<TextView>(R.id.customTv)
                    textV.text = lista[i]
                    scrollFilter.addView(textV)
                    textV.setOnClickListener {
                        val docRef = db.collection("users").document(currentUser?.uid.toString())
                            .update("filtrobanco", textV.text.toString())
                        dialog.dismiss()
                    }
                }
            } else {
                Log.d("testeo", "Current data: null")
            }
        }
    }

    private val _dispo = MutableLiveData<String>().apply {
        val docRef = db.collection("users").document(currentUser?.uid.toString())
        docRef.get()
        docRef.addSnapshotListener { snapshot, e ->
            if (e != null) {
                Log.w("testeo", "Listen failed.", e)
                return@addSnapshotListener
            }
            if (snapshot != null) {
                value=snapshot.data?.get("filtrodispo").toString()
            } else {
                Log.d("testeo", "Current data: null")
            }
        }
    }
    val dispo: LiveData<String> = _dispo
    fun abrirDialogDispo(activity:Activity, resource:Int, inflater: LayoutInflater, resource2:Int, scroll:Int) {
        val alertbuilder = AlertDialog.Builder(activity, resource)
        alertbuilder.setView(inflater.inflate(resource2, null))
        val dialog = alertbuilder.show()
        val width = 230 // Width en dp
        val height = 305 // Height en dp
        val scale = activity.resources.displayMetrics.density
        dialog.window?.setLayout((width * scale).toInt(), (height * scale).toInt())
        val scrollFilter = dialog.findViewById<LinearLayout>(scroll)!!
        var lista = mutableListOf<String>()
        lista.clear()
        lista.add(dispo.value.toString())
        lista.add("Todos")
        lista.add("Disponibles")
        lista.add("Reportados")
        lista.add("No disponibles")
        lista = lista.distinct().toMutableList()
        for (i in 0..lista.size - 1) {
            val inflater = LayoutInflater.from(activity)
            val view = inflater.inflate(R.layout.custom_textviews, null)
            val textV = view.findViewById<TextView>(R.id.customTv)
            textV.text = lista[i]
            scrollFilter.addView(textV)
            textV.setOnClickListener {
                val docRef = db.collection("users").document(currentUser?.uid.toString())
                    .update("filtrodispo", textV.text.toString())
                dialog.dismiss()
            }
        }
    }
}