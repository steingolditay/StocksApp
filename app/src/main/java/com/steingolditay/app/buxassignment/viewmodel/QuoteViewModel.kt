package com.steingolditay.app.buxassignment.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.steingolditay.app.buxassignment.Utils.Constants
import com.steingolditay.app.buxassignment.repository.WebSocketListener
import com.steingolditay.app.buxassignment.model.Product
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response

class QuoteViewModel: ViewModel() {

    lateinit var webSocketListener: WebSocketListener

    // launce weboscket via coroutines
    fun getLiveData(product: Product): LiveData<String>{
        webSocketListener = WebSocketListener(product)

        viewModelScope.launch {
            val request = Request.Builder().url(Constants.websocket_url).build()
            withContext(Dispatchers.IO){
                val httpClient = OkHttpClient.Builder()
                        .addInterceptor(object : Interceptor {
                            override fun intercept(chain: Interceptor.Chain): Response {
                                val builder = chain.request().newBuilder()
                                builder.addHeader("Authorization", Constants.token)
                                builder.addHeader("Accept-Language", Constants.accept_language)

                                return chain.proceed(builder.build())
                            }
                        })
                        .build()
                httpClient.newWebSocket(request, webSocketListener)
            }
        }
        return webSocketListener.liveData
    }

    fun stopListener(){
        webSocketListener.unSubscribeFromChannel()
    }




}