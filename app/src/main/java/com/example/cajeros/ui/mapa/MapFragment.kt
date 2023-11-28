package com.example.cajeros.ui.mapa

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.cajeros.R
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapFragment
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class MapFragment : Fragment(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var context: Context
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    companion object {
        var mapFragment : SupportMapFragment?=null
        val TAG: String = MapFragment::class.java.simpleName
        fun newInstance() = MapFragment()
        const val REQUEST_CODE_LOCATION = 0
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        this.context = context
    }

    /*override fun onDetach() {
        super.onDetach()
        context = null
    }*/

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        var rootView = inflater.inflate(R.layout.fragment_map, container, false)
        mapFragment = childFragmentManager.findFragmentById(R.id.map) as? SupportMapFragment
        mapFragment?.getMapAsync(this)
        return rootView
    }

    //AGREGAR FUNCIONES AL MAPA DENTRO DE ESTE METODO
    override fun onMapReady(p0: GoogleMap) {
        mMap = p0
        mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN)//testeo de tipo de mapa
        enableLocation()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this.requireActivity().applicationContext)
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location : Location? ->
                Log.d("testeando", location.toString())
                if (location != null) {
                    mMap.animateCamera(CameraUpdateFactory.newLatLng(LatLng(location.latitude, location.longitude)))
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(location.latitude, location.longitude), 15f))
                }
            }
        var marker1 = mMap.addMarker(MarkerOptions()
            .position(LatLng(37.423106, -122.081365))
            .title("primer marcador")
            .icon(BitmapDescriptorFactory.fromResource(R.drawable.atmgreen)))
        mMap.setOnCameraIdleListener {
            Log.d("testeo", mMap.cameraPosition.zoom.toString())
            if (mMap.cameraPosition.zoom < 12) {
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

    private fun refreshCurrentFragment(){
        val fragmentId = findNavController().currentDestination?.id
        findNavController().popBackStack(fragmentId!!,true)
        findNavController().navigate(fragmentId)
    }

    private fun isLocationPermissionGranted() = ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED

    private fun enableLocation(){
        if(!::mMap.isInitialized) return
        if(isLocationPermissionGranted()){
            if (ActivityCompat.checkSelfPermission(
                    this.requireActivity(),
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    this.requireActivity(),
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestLocationPermission()
                return
            }
            mMap.isMyLocationEnabled = true
        }else{
            requestLocationPermission()
        }
    }

    private fun requestLocationPermission() {
        if(ActivityCompat.shouldShowRequestPermissionRationale(this.requireActivity(), Manifest.permission.ACCESS_FINE_LOCATION)){
            Toast.makeText(this.requireActivity(), "DEBES ACEPTAR PERMISOS DE UBICACION", Toast.LENGTH_SHORT).show()
        }else{
            ActivityCompat.requestPermissions(this.requireActivity(), arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_CODE_LOCATION)
            refreshCurrentFragment()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when(requestCode){
            REQUEST_CODE_LOCATION -> if(grantResults.isNotEmpty() && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                mMap.isMyLocationEnabled = true

            }else{
                Toast.makeText(this.requireActivity(), "DEBES ACEPTAR PERMISOS DE UBICACION", Toast.LENGTH_SHORT).show()
            }
            else -> {}
        }
    }

    override fun onResume() {
        super.onResume()
        if(!::mMap.isInitialized) return
        if(!isLocationPermissionGranted()){
            mMap.isMyLocationEnabled = false
            Toast.makeText(this.requireActivity(), "DEBES ACEPTAR PERMISOS DE UBICACION", Toast.LENGTH_SHORT).show()
        }
    }
}