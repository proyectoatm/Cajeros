package com.example.cajeros.ui.perfil


import android.util.Log
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
}