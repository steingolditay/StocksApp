package com.steingolditay.app.buxassignment.Utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import java.lang.Exception

class NetworkConnectionMonitor(val context: Context) {

    private val _liveData = MutableLiveData<Boolean>()
    val liveData: LiveData<Boolean> get() = _liveData

    fun registerNetworkCallback(){
        _liveData.postValue(false)

        try {
            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

            connectivityManager.registerDefaultNetworkCallback(object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    super.onAvailable(network)
                    _liveData.postValue(true)
                }
                override fun onLost(network: Network) {
                    super.onLost(network)
                    _liveData.postValue(false)
                }
            })

        }
        catch (e: Exception){

        }
    }

}