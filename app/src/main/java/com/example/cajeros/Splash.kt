package com.example.cajeros


import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.cajeros.ui.auth.InicioSesion
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase


class Splash : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val currentUser = Firebase.auth.currentUser
        val intentLogin = Intent(this, InicioSesion::class.java)
        val intentMain = Intent(this, MainActivity::class.java)
        Handler(Looper.getMainLooper()).postDelayed({
            if (isOnline(this)){
                if (currentUser != null) {
                    Toast.makeText(this, "Se ha accedido, con: "+currentUser.email.toString(), Toast.LENGTH_SHORT).show()
                    startActivity(intentMain)
                    finish()
                }else{
                    startActivity(intentLogin)
                    finish()
                }
            }else{
                val alertbuilder = AlertDialog.Builder(this@Splash)
                alertbuilder.setTitle("Sin internet")
                alertbuilder.setMessage("Para usar esta aplicacion necesitas internet")
                alertbuilder.setPositiveButton("OK") { dialogInterface, i -> finish() }
                val dialog = alertbuilder.show()
                dialog.setCancelable(false)
                dialog.setCanceledOnTouchOutside(false)
            }
        }, 2000)
    }

    fun isOnline(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (connectivityManager != null) {
            val capabilities =
                connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
            if (capabilities != null) {
                if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                    Log.i("Internet", "NetworkCapabilities.TRANSPORT_CELLULAR")
                    return true
                } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                    Log.i("Internet", "NetworkCapabilities.TRANSPORT_WIFI")
                    return true
                } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) {
                    Log.i("Internet", "NetworkCapabilities.TRANSPORT_ETHERNET")
                    return true
                }
            }
        }
        return false
    }
}