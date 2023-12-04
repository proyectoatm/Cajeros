package com.example.cajeros.ui.mapa

import android.app.Activity
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.location.Location
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModel
import com.example.cajeros.R
import com.example.cajeros.network.ApiService
import com.example.cajeros.network.RouteResponse
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
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
import kotlin.math.roundToInt

class MapViewModel : ViewModel() {

    private val db = Firebase.firestore
    private var poly : Polyline? = null
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback
    private lateinit var client:FusedLocationProviderClient
    fun mapLogicHere(map: GoogleMap, fusedLocationClient:FusedLocationProviderClient, activity: Activity){
        map.setMapType(GoogleMap.MAP_TYPE_TERRAIN)//testeo de tipo de mapa
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location : Location? ->
                if (location != null) {
                    map.animateCamera(CameraUpdateFactory.newLatLng(LatLng(location.latitude, location.longitude)))
                    map.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(location.latitude, location.longitude), 15f))
                }
            }
        val docRef = db.collection("cajeros")
        docRef.get()
            docRef.addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.w("testeo", "Listen failed.", e)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    Log.d("testeo", "Current data: ${snapshot}")
                    //value = snapshot.data?.get("avatar").toString()

                    val markerList = mutableListOf<Marker>()
                    for (document in snapshot) {
                        Log.d("testeo", document.data.get("reportes").toString())
                        val list = document.data.get("reportes").toString()
                            .replace("[", "")
                            .replace("]", "")
                            .split(",")
                            .map { it.trim() }
                            .toMutableList()
                        var icon = BitmapDescriptorFactory.fromResource(R.drawable.atmgreen)
                        if (document.data.get("reportes")!=null){
                            Log.d("testeo", list.toString())
                            icon = if (list.size<3) {
                                BitmapDescriptorFactory.fromResource(R.drawable.atmgreen)
                            } else if (list.size in 3..5) {
                                BitmapDescriptorFactory.fromResource(R.drawable.atmyellow)
                            } else {
                                BitmapDescriptorFactory.fromResource(R.drawable.atmred)
                            }
                        }
                        val marker = map.addMarker(
                            MarkerOptions()
                                .position(LatLng(document.data.get("latitud").toString().toDouble(), document.data?.get("longitud").toString().toDouble()))
                                .title("Banco: "+document.data.get("banco").toString())
                                .icon(icon))
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
                } else {
                    Log.d("testeo", "Current data: null")
                }
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
                val width = 200 // Width en dp
                val height = 295 // Height en dp
                val scale = activity.resources.displayMetrics.density
                dialog.window?.setLayout((width * scale).toInt(), (height * scale).toInt())
                //NOMBRE BANCO
                var nombre: TextView? = dialog.findViewById<TextView>(R.id.nombreBanco)
                if (nombre != null) {
                    nombre.text=marker.title
                }
                //DISTANCIA
                var dist: TextView? = dialog.findViewById<TextView>(R.id.distanciaCajero)
                if (dist != null) {
                    fusedLocationClient.lastLocation
                        .addOnSuccessListener { location : Location? ->
                            if (location != null) {
                                var start = Location("actualPos")
                                start.latitude=location.latitude
                                start.longitude=location.longitude
                                var end = Location("markerPos")
                                end.latitude=marker.position.latitude
                                end.longitude=marker.position.longitude
                                val distance = start.distanceTo(end)
                                dist.text="Distancia: "+distance.roundToInt().toString()+" metros"
                                //REPORTAR
                                var botonReport: Button? = dialog.findViewById(R.id.botonDialogReport)
                                if (botonReport != null) {
                                    botonReport.isVisible = distance.roundToInt()<20
                                }
                            }
                        }
                }
                //ESTADO
                var lon: TextView? = dialog.findViewById<TextView>(R.id.longitudCajero)
                if (lon != null) {
                    lon.text=marker.position.longitude.toString()
                }
                var scrollReports:LinearLayout? = dialog.findViewById(R.id.scrollReports)
                for (i in 0 until 20) {
                    var imageView = ImageView(activity)
                    imageView.setImageResource(R.drawable.a0)
                    imageView.layoutParams = LinearLayout.LayoutParams(80,80)
                    scrollReports!!.addView(imageView)
                }
                var p2 = LatLng(marker.position.latitude, marker.position.longitude)
                var boton_ir:Button? = dialog.findViewById<Button>(R.id.botonDialogIr)
                //IR
                if (boton_ir != null) {
                    if (::client.isInitialized){
                        client.removeLocationUpdates(locationCallback)
                    }
                    boton_ir.setOnClickListener{
                        locationRequest = LocationRequest.create().apply {
                            interval = 10000 // Update interval in milliseconds
                            fastestInterval = 5000 // Fastest update interval
                            // Set other properties as needed (e.g., priority)
                        }
                        locationCallback = object : LocationCallback() {
                            override fun onLocationResult(p0: LocationResult) {
                                p0?.locations?.forEach { location ->
                                    // Handle location change here
                                    Log.d("testeo","New Location: Lat=${location.latitude}, Long=${location.longitude}, going to: ${p2.toString()}")
                                    // Execute your custom function
                                    // ...
                                    createRoute(mMap,p2,fusedLocationClient)
                                    dialog.dismiss()
                                }
                            }
                        }
                        client = LocationServices.getFusedLocationProviderClient(activity)
                        client.requestLocationUpdates(locationRequest, locationCallback, null)
                    }
                }
                return true
            }
        })
    }

    private fun createRoute(map: GoogleMap, p2:LatLng, fusedLocationClient:FusedLocationProviderClient) {
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location : Location? ->
                if (location != null) {
                    var start = "${location.longitude},${location.latitude}"
                    var end = "${p2.longitude},${p2.latitude}"
                    CoroutineScope(Dispatchers.IO).launch {
                        val call = getRetrofit().create(ApiService::class.java)
                            .getRoute("5b3ce3597851110001cf624807d5e5f2c6fd4b0aaa8ca7d3fced1089", start, end)
                        if (call.isSuccessful) {
                            drawRoute(map,call.body())
                        } else {
                            Log.i("testeo", "KO")
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
            poly?.remove()
            poly = map.addPolyline(polyLineOptions)
        }

    }
    private fun getRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://api.openrouteservice.org/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}