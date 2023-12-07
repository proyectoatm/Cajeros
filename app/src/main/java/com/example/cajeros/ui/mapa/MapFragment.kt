package com.example.cajeros.ui.mapa

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.cajeros.R
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapFragment
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment

class MapFragment : Fragment(), OnMapReadyCallback {

    private val mapViewModel : MapViewModel by viewModels()
    private lateinit var mMap: GoogleMap
    private lateinit var context: Context
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private lateinit var filtroBanco:String
    private lateinit var filtroDispo:String
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        filtroBanco = mapViewModel.banco.value.toString()
        filtroDispo = mapViewModel.dispo.value.toString()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        var rootView = inflater.inflate(R.layout.fragment_map, container, false)
        filtroBanco = mapViewModel.banco.value.toString()
        filtroDispo = mapViewModel.dispo.value.toString()
        mapFragment = childFragmentManager.findFragmentById(R.id.map) as? SupportMapFragment
        mapFragment?.getMapAsync(this)
        return rootView
    }

    override fun onMapReady(p0: GoogleMap) {
        mMap = p0
        enableLocation()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this.requireActivity().applicationContext)
        filtroBanco = mapViewModel.banco.value.toString()
        filtroDispo = mapViewModel.dispo.value.toString()
        mapViewModel.mapLogicHere(mMap, fusedLocationClient, this.requireActivity(), filtroBanco, filtroDispo)
    }

    private fun refreshCurrentFragment(){
        val fragmentId = findNavController().currentDestination?.id
        findNavController().popBackStack(fragmentId!!,true)
        findNavController().navigate(fragmentId)
    }

    private fun isLocationPermissionGranted() = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED

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
            val alertbuilder = AlertDialog.Builder(this.requireActivity())
            alertbuilder.setTitle("Permiso de ubicacion")
            alertbuilder.setMessage("Para usar esta aplicacion necesitas activar el permiso de ubicacion")
            alertbuilder.setPositiveButton("OK") { dialogInterface, i -> this.requireActivity().finish() }
            val dialog = alertbuilder.show()
            dialog.setCancelable(false)
            dialog.setCanceledOnTouchOutside(false)
        }else{
            ActivityCompat.requestPermissions(this.requireActivity(), arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_CODE_LOCATION)
            refreshCurrentFragment()
        }
    }
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when(requestCode){
            REQUEST_CODE_LOCATION -> if(grantResults.isNotEmpty() && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                mMap.isMyLocationEnabled = true

            }else{
                val alertbuilder = AlertDialog.Builder(this.requireActivity())
                alertbuilder.setTitle("Permiso de ubicacion")
                alertbuilder.setMessage("Para usar esta aplicacion necesitas activar el permiso de ubicacion")
                alertbuilder.setPositiveButton("OK") { dialogInterface, i -> this.requireActivity().finish() }
                val dialog = alertbuilder.show()
                dialog.setCancelable(false)
                dialog.setCanceledOnTouchOutside(false)
            }
            else -> {}
        }
    }
    override fun onResume() {
        super.onResume()
        if(!::mMap.isInitialized) return
        if(!isLocationPermissionGranted()){
            mMap.isMyLocationEnabled = false
            val alertbuilder = AlertDialog.Builder(this.requireActivity())
            alertbuilder.setTitle("Permiso de ubicacion")
            alertbuilder.setMessage("Para usar esta aplicacion necesitas activar el permiso de ubicacion")
            alertbuilder.setPositiveButton("OK") { dialogInterface, i -> this.requireActivity().finish() }
            val dialog = alertbuilder.show()
            dialog.setCancelable(false)
            dialog.setCanceledOnTouchOutside(false)
        }
    }
}