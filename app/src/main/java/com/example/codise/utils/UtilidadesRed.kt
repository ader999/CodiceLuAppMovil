package com.example.codise.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

class UtilidadesRed(contexto: Context) {
    private val administradorConectividad =
        contexto.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    val estaConectado: Flow<Boolean> = callbackFlow {
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(red: Network) {
                launch { send(true) }
            }

            override fun onLost(red: Network) {
                launch { send(false) }
            }
        }

        val peticion = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()
        
        administradorConectividad.registerNetworkCallback(peticion, callback)

        // Estado inicial
        val redActual = administradorConectividad.activeNetwork
        val capacidades = administradorConectividad.getNetworkCapabilities(redActual)
        val tieneInternet = capacidades?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
        send(tieneInternet)

        awaitClose {
            administradorConectividad.unregisterNetworkCallback(callback)
        }
    }.distinctUntilChanged()

    fun tieneInternet(): Boolean {
        val red = administradorConectividad.activeNetwork
        val capacidades = administradorConectividad.getNetworkCapabilities(red)
        return capacidades?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
    }
}
