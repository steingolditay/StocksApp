package com.steingolditay.app.buxassignment.api

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.gson.JsonElement
import com.google.gson.JsonParser
import com.steingolditay.app.buxassignment.model.Product
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

class WebSocketListener(private val product: Product) : WebSocketListener() {

    private val _liveData = MutableLiveData<String>()
    val liveData: LiveData<String> get() = _liveData


    override fun onMessage(webSocket: WebSocket, text: String) {
        super.onMessage(webSocket, text)
        Log.d("TAG", "onMessage: $text")
        when {
            text.contains("connect.connected") -> {
                subscribeToChannel(webSocket)
            }
            text.contains("connect.failed") -> {
                Log.d("TAG", "onMessage: Error in connection to Web Socket")
            }
            text.contains("trading.quote") -> {
                outputData(text)
            }
        }

    }

    private fun subscribeToChannel(webSocket : WebSocket) {
        val subscribeObject = JSONObject()
        val subscribeArray = JSONArray()
        subscribeArray.put("trading.product.${product.securityId}")

        val unSubscribeObject = JSONObject()
        val unSubscribeArray = JSONArray()
        unSubscribeArray.put("trading.product.me")

        try {
            subscribeObject.put("subscribeTo", subscribeArray)
            unSubscribeObject.put("unsubscribeFrom", unSubscribeArray)

            webSocket.send(subscribeObject.toString())


        } catch (e: JSONException) {
            Log.d("TAG", "createJsonObject: $e")
            e.printStackTrace();
        }
    }

    fun outputData(data: String){
        _liveData.postValue(data)
    }

}