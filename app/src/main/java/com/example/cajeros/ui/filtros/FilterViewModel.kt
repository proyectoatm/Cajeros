package com.example.cajeros.ui.filtros

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class FilterViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "Aqui van los filtros"
    }
    val text: LiveData<String> = _text
}