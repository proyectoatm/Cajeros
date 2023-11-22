package com.example.cajeros.ui.perfil

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ProfileViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "Aqui va el perfil"
    }
    val text: LiveData<String> = _text
}