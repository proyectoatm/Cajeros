package com.example.cajeros.ui.mapa

import android.app.Activity
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.location.Location
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.ViewModel
import com.example.cajeros.R
import com.example.cajeros.network.ApiService
import com.example.cajeros.network.RouteResponse
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MapViewModel : ViewModel() {

    private val db = Firebase.firestore
    private val polyList = mutableListOf<Polyline>()
    fun mapLogicHere(map: GoogleMap, fusedLocationClient:FusedLocationProviderClient, activity: Activity){
        map.setMapType(GoogleMap.MAP_TYPE_TERRAIN)//testeo de tipo de mapa
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location : Location? ->
                Log.d("posiaaa", location.toString())
                if (location != null) {
                    map.animateCamera(CameraUpdateFactory.newLatLng(LatLng(location.latitude, location.longitude)))
                    map.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(location.latitude, location.longitude), 15f))
                }
            }
        val docRef = db.collection("cajeros")
        docRef.get().addOnSuccessListener { documents ->
            val markerList = mutableListOf<Marker>()
            for (document in documents) {
                Log.d("testeo", "${document.id} => ${document.data}")
                val marker = map.addMarker(
                    MarkerOptions()
                        .position(LatLng(document.data.get("latitud").toString().toDouble(), document.data?.get("longitud").toString().toDouble()))
                        .title("Banco: "+document.data.get("banco").toString())
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.atmgreen)))
                if (marker != null) {
                    markerList.add(marker)
                }
            }
            map.setOnCameraIdleListener {
                Log.d("testeo", map.cameraPosition.zoom.toString())
                if (map.cameraPosition.zoom < 11) {
                    Log.d("testeo", "zoom menor a 12")
                    for (marker in markerList) {
                        Log.d("testeo", markerList.toString())
                        marker.isVisible=false
                    }
                } else {
                    for (marker in markerList) {
                        Log.d("testeo", markerList.toString())
                        marker.isVisible=true
                    }
                }
            }
        }.addOnFailureListener { exception ->
            Log.w("testeo", "Error getting documents: ", exception)
        }
        dialogMarkers(map, activity, fusedLocationClient)
    }
    fun dialogMarkers(mMap:GoogleMap, activity: Activity, fusedLocationClient: FusedLocationProviderClient){
        mMap.setOnMarkerClickListener(object : GoogleMap.OnMarkerClickListener {
            override fun onMarkerClick(marker: Marker): Boolean {
                val builder = AlertDialog.Builder(activity)
                builder.setView(R.layout.dialog_cajero)
                val dialog = builder.show()
                dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                val width = 200 // Width in dp
                val height = 295 // Height in dp
                val scale = activity.resources.displayMetrics.density
                dialog.window?.setLayout((width * scale).toInt(), (height * scale).toInt())
                var nombre: TextView? = dialog.findViewById<TextView>(R.id.nombreBanco)
                if (nombre != null) {
                    nombre.text=marker.title
                }
                var lat: TextView? = dialog.findViewById<TextView>(R.id.latitudCajero)
                if (lat != null) {
                    lat.text=marker.position.latitude.toString()
                }
                var lon: TextView? = dialog.findViewById<TextView>(R.id.longitudCajero)
                if (lon != null) {
                    lon.text=marker.position.longitude.toString()
                }
                var p2 = LatLng(marker.position.latitude, marker.position.longitude)
                var boton_ir:Button? = dialog.findViewById<Button>(R.id.botonDialogIr)

                if (boton_ir != null) {
                    boton_ir.setOnClickListener{
                        createRoute(mMap,p2,fusedLocationClient)
                        dialog.dismiss()
                    }
                }
                return true

            }
        })
    }
    private fun createRoute(map: GoogleMap, p2:LatLng, fusedLocationClient:FusedLocationProviderClient) {
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location : Location? ->
                Log.d("posiaaa", location.toString())
                if (location != null) {
                    var start = "${location.longitude},${location.latitude}"
                    var end = "${p2.longitude},${p2.latitude}"
                    Log.d("posiaaa", "start: "+start.toString())
                    Log.d("posiaaa", "end: "+end.toString())
                    CoroutineScope(Dispatchers.IO).launch {
                        val call = getRetrofit().create(ApiService::class.java)
                            .getRoute("5b3ce3597851110001cf624807d5e5f2c6fd4b0aaa8ca7d3fced1089", start, end)
                        if (call.isSuccessful) {
                            drawRoute(map,call.body())
                        } else {
                            Log.i("testeo", "KO")
                            Log.i("testeo", call.toString())
                        }
                    }
                }
            }
    }
    private fun drawRoute(map:GoogleMap,routeResponse: RouteResponse?) {
        val polyLineOptions = PolylineOptions()
        routeResponse?.features?.first()?.geometry?.coordinates?.forEach {
            polyLineOptions.add(LatLng(it[1], it[0]))
        }
        val handler = Handler(Looper.getMainLooper())
        handler.post {
            polyList.clear()
            val poly = map.addPolyline(polyLineOptions)
            polyList.add(poly)
        }

    }
    private fun getRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://api.openrouteservice.org/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}