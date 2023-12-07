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
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.cajeros.R
import com.example.cajeros.data.model.Cajero
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
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
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
    private lateinit var client: FusedLocationProviderClient
    private lateinit var cajero: Cajero
    private var markerList = mutableListOf<Marker>()
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

    fun mapLogicHere(map: GoogleMap, fusedLocationClient:FusedLocationProviderClient, activity: Activity, bancoF:String){
        map.setMapType(GoogleMap.MAP_TYPE_TERRAIN)//tipo de mapa
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
                    if(bancoF=="Todos"){
                        map.clear()
                        for (document in snapshot) {
                            Log.d("testeo", "Valor actual de banco: "+bancoF)
                            val list = document.data.get("reportes").toString()
                                .replace("[", "")
                                .replace("]", "")
                                .split(",")
                                .map { it.trim() }
                                .toMutableList()
                            var icon = BitmapDescriptorFactory.fromResource(R.drawable.atmgreen)
                            if (document.data.get("reportes")!=null){
                                if (list.size==1 && list[0]!=""){
                                    icon = BitmapDescriptorFactory.fromResource(R.drawable.atmyellow)
                                }
                                if (list.size>=2){
                                    icon = BitmapDescriptorFactory.fromResource(R.drawable.atmred)
                                }
                            }
                            val marker = map.addMarker(
                                MarkerOptions()
                                    .position(LatLng(document.data.get("latitud").toString().toDouble(), document.data?.get("longitud").toString().toDouble()))
                                    .title("Banco: "+document.data.get("banco").toString())
                                    .icon(icon))
                            if (marker != null) {
                                marker.tag=document.id
                                markerList.add(marker)
                            }
                        }
                    }else{
                        for (document in snapshot) {
                            Log.d("testeo", "Valor actual de banco: "+bancoF)
                            if(document.data.get("banco").toString() != bancoF){
                                continue
                            }else{
                                map.clear()
                                val list = document.data.get("reportes").toString()
                                    .replace("[", "")
                                    .replace("]", "")
                                    .split(",")
                                    .map { it.trim() }
                                    .toMutableList()
                                var icon = BitmapDescriptorFactory.fromResource(R.drawable.atmgreen)
                                if (document.data.get("reportes")!=null){
                                    if (list.size==1 && list[0]!=""){
                                        icon = BitmapDescriptorFactory.fromResource(R.drawable.atmyellow)
                                    }
                                    if (list.size>=2){
                                        icon = BitmapDescriptorFactory.fromResource(R.drawable.atmred)
                                    }
                                }
                                val marker = map.addMarker(
                                    MarkerOptions()
                                        .position(LatLng(document.data.get("latitud").toString().toDouble(), document.data?.get("longitud").toString().toDouble()))
                                        .title("Banco: "+document.data.get("banco").toString())
                                        .icon(icon))
                                if (marker != null) {
                                    marker.tag=document.id
                                    markerList.add(marker)
                                }
                            }
                        }
                    }

                    map.setOnCameraIdleListener {
                        Log.d("testeo", map.cameraPosition.zoom.toString())
                        if (map.cameraPosition.zoom < 11) {
                            Log.d("testeo", "zoom menor a 12")
                            for (marker in markerList) {
                                marker.isVisible=false
                            }
                        } else {
                            for (marker in markerList) {
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
                                var botonesReport: LinearLayout? = dialog.findViewById(R.id.reportes)
                                if (botonesReport != null) {
                                    botonesReport.isVisible = distance.roundToInt()<20 // ESTE ES EL LIMITE DE DISTANCIA EN METROS PARA PODER VER BOTONES DE REPORTE
                                }
                            }
                        }
                }
                //ESTADO
                var estado: TextView? = dialog.findViewById<TextView>(R.id.longitudCajero)
                Log.d("testeo", "TAG MARKER (ID): "+marker.tag)
                if (estado != null) {
                    val docRef = db.collection("cajeros").document(marker.tag.toString())
                    docRef.get()
                    docRef.addSnapshotListener { snapshot, e ->
                        if (e != null) {
                            Log.w("testeo", "Listen failed.", e)
                            return@addSnapshotListener
                        }
                        if (snapshot != null) {
                            val list = snapshot.data?.get("reportes").toString()
                                .replace("[", "")
                                .replace("]", "")
                                .split(",")
                                .map { it.trim() }
                                .toMutableList()
                            if (snapshot.data?.get("reportes") != null) {
                                estado.text = "Disponible"
                                if (list.size==1 && list[0]!=""){
                                    estado.text = "Reporte reciente"
                                }
                                if (list.size>=2){
                                    estado.text = "No disponible"
                                }
                            }else{
                                estado.text = "Disponible"
                            }
                        } else {
                            Log.d("testeo", "Current data: null")
                        }
                    }
                }
                //REPORTES
                var scrollReports:LinearLayout? = dialog.findViewById(R.id.scrollReports)
                if (scrollReports!=null){
                    val docRef = db.collection("cajeros").document(marker.tag.toString())
                    docRef.get()
                    docRef.addSnapshotListener { snapshot, e ->
                        if (e != null) {
                            Log.w("testeo", "Listen failed.", e)
                            return@addSnapshotListener
                        }
                        if (snapshot != null) {
                            scrollReports.removeAllViews()
                            val list = snapshot.data?.get("historial").toString()
                                .replace("[", "")
                                .replace("]", "")
                                .split(",")
                                .map { it.trim() }
                                .toMutableList()
                            list.reverse()
                            if (snapshot.data?.get("historial") != null) {
                                val docUsers = db.collection("users")
                                docUsers.get()
                                docUsers.addSnapshotListener { snapshot2, e2 ->
                                    if (e2 != null) {
                                        Log.w("testeo", "Listen failed.", e2)
                                        return@addSnapshotListener
                                    }
                                    if (snapshot2 != null) {
                                        scrollReports.removeAllViews()
                                        for (id in list){
                                            if (list.indexOf(id)==20){
                                                break
                                            }
                                            var avatarid=""
                                            for (document in snapshot2){
                                                if (document.id==id){
                                                    avatarid = document.data["avatar"].toString()
                                                    val field = R.drawable::class.java.getField(avatarid)
                                                    val idav = field.getInt(null)
                                                    var imageView = ImageView(activity)
                                                    imageView.setImageResource(idav)
                                                    imageView.layoutParams = LinearLayout.LayoutParams(80,80)
                                                    scrollReports!!.addView(imageView)
                                                }
                                            }
                                        }
                                    }else{
                                        Log.d("testeo", "Current data: null")
                                    }
                                }
                            }else{
                                Log.d("testeo", "Current data: null")
                            }
                        } else {
                            Log.d("testeo", "Current data: null")
                        }
                    }
                }
                //IR
                var p2 = LatLng(marker.position.latitude, marker.position.longitude)
                var boton_ir:Button? = dialog.findViewById<Button>(R.id.botonDialogIr)
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
                                    //Handle location change here
                                    Log.d("testeo","Actual POS: Lat=${location.latitude}, Long=${location.longitude}, Destino: ${p2.toString()}")
                                    createRoute(mMap,p2,fusedLocationClient)
                                    dialog.dismiss()
                                }
                            }
                        }
                        client = LocationServices.getFusedLocationProviderClient(activity)
                        client.requestLocationUpdates(locationRequest, locationCallback, null)
                    }
                }
                //REALIZAR REPORTES
                var botonDisponible:Button? = dialog.findViewById(R.id.reportDisponible)
                if (botonDisponible != null) {
                    botonDisponible.setOnClickListener{
                        val docRef = db.collection("cajeros").document(marker.tag.toString())
                        val userid = currentUser?.uid.toString()
                        docRef.update("historial", FieldValue.arrayRemove(userid))
                        docRef.update("reportes", FieldValue.delete())
                        docRef.update("historial", FieldValue.arrayUnion(userid))
                        Toast.makeText(activity, "Reporte realizado", Toast.LENGTH_SHORT).show()
                        dialog.dismiss()
                    }
                }
                var botonNoDisponible:Button? = dialog.findViewById(R.id.reportNoDisponible)
                if (botonNoDisponible != null) {
                    botonNoDisponible.setOnClickListener{
                        val docRef = db.collection("cajeros").document(marker.tag.toString())
                        val userid = currentUser?.uid.toString()
                        docRef.update("historial", FieldValue.arrayRemove(userid))
                        docRef.update("reportes", FieldValue.arrayUnion(userid))
                        docRef.update("historial", FieldValue.arrayUnion(userid))
                        Toast.makeText(activity, "Reporte realizado", Toast.LENGTH_SHORT).show()
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