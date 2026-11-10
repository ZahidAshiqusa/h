package com.zaidashiq.ultra.engine

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ConnectionMonitor(context: Context) {

    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager

    private val _isNetworkAvailable = MutableStateFlow(checkCurrentConnectivity())
    val isNetworkAvailable: StateFlow<Boolean> = _isNetworkAvailable.asStateFlow()

    private var onNetworkRestoredCallback: (() -> Unit)? = null
    private var onNetworkLostCallback: (() -> Unit)? = null

    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            val wasOffline = !_isNetworkAvailable.value
            _isNetworkAvailable.value = true
            if (wasOffline) {
                onNetworkRestoredCallback?.invoke()
            }
        }

        override fun onLost(network: Network) {
            _isNetworkAvailable.value = checkCurrentConnectivity()
            if (!_isNetworkAvailable.value) {
                onNetworkLostCallback?.invoke()
            }
        }
    }

    fun startMonitoring(
        onNetworkRestored: () -> Unit,
        onNetworkLost: () -> Unit
    ) {
        onNetworkRestoredCallback = onNetworkRestored
        onNetworkLostCallback = onNetworkLost
        try {
            val request = NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build()
            connectivityManager?.registerNetworkCallback(request, networkCallback)
        } catch (_: Throwable) {
            // Safe fallback if network callback registration fails in unit tests
        }
    }

    fun stopMonitoring() {
        try {
            connectivityManager?.unregisterNetworkCallback(networkCallback)
        } catch (_: Throwable) {
        }
    }

    private fun checkCurrentConnectivity(): Boolean {
        val cm = connectivityManager ?: return true
        return try {
            val activeNetwork = cm.activeNetwork ?: return false
            val caps = cm.getNetworkCapabilities(activeNetwork) ?: return false
            caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        } catch (_: Throwable) {
            true
        }
    }
}
