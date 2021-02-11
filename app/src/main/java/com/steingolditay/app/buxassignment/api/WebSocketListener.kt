package com.steingolditay.app.buxassignment.api

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.steingolditay.app.buxassignment.Constants
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
    lateinit var webSocket: WebSocket

    override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
        super.onFailure(webSocket, t, response)
        unSubscribeFromChannel()
    }

    override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
        super.onClosing(webSocket, code, reason)
        Log.d("TAG", "onClosing:\ncode: $code\nreason: $reason ")

    }


    override fun onMessage(webSocket: WebSocket, text: String) {
        super.onMessage(webSocket, text)
        Log.d("TAG", "onMessage: $text")
        when {
            text.contains("connect.connected") -> {
                this.webSocket = webSocket
                subscribeToChannel(webSocket)
            }
            text.contains("connect.failed") -> {
                unSubscribeFromChannel()
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

        try {
            subscribeObject.put("subscribeTo", subscribeArray)

            webSocket.send(subscribeObject.toString())


        } catch (e: JSONException) {
            Log.d("TAG", "createJsonObject: $e")
            e.printStackTrace();
        }
    }

    fun unSubscribeFromChannel() {
        Log.d("TAG", "unSubscribeFromChannel: ")
        val unSubscribeObject = JSONObject()
        val unSubscribeArray = JSONArray()
        unSubscribeArray.put("trading.product.${product.securityId}")

        try {
            unSubscribeObject.put("unsubscribeFrom", unSubscribeArray)

            webSocket.send(unSubscribeObject.toString())
            webSocket.close(Constants.websocket_close, "User Unsubscribed")


        } catch (e: JSONException) {
            Log.d("TAG", "createJsonObject: $e")
            e.printStackTrace();
        }
    }

    private fun outputData(data: String){
        _liveData.postValue(data)
    }


}