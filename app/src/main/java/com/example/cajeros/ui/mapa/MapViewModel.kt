package com.example.cajeros.ui.mapa

import android.location.Location
import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.cajeros.R
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class MapViewModel : ViewModel() {

    private val db = Firebase.firestore
    fun mapLogicHere(map: GoogleMap, fusedLocationClient:FusedLocationProviderClient){
        map.setMapType(GoogleMap.MAP_TYPE_TERRAIN)//testeo de tipo de mapa
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location : Location? ->
                Log.d("testeando", location.toString())
                if (location != null) {
                    map.animateCamera(CameraUpdateFactory.newLatLng(LatLng(location.latitude, location.longitude)))
                    map.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(location.latitude, location.longitude), 15f))
                }
            }
        //se crea un marcador
        var marker1 = map.addMarker(
            MarkerOptions()
            .position(LatLng(37.423106, -122.081365))
            .title("primer marcador")
            .icon(BitmapDescriptorFactory.fromResource(R.drawable.atmgreen)))
        //listener que detecta zoom del mapa
        map.setOnCameraIdleListener {
            Log.d("testeo", map.cameraPosition.zoom.toString())
            if (map.cameraPosition.zoom < 12) {
                Log.d("testeo", "zoom menor a 12")
                if (marker1 != null) {
                    marker1.isVisible=false
                }
            } else {
                Log.d("testeo", "zoom mayor a 12")
                if (marker1 != null) {
                    marker1.isVisible=true
                }
            }
        }
    }
}