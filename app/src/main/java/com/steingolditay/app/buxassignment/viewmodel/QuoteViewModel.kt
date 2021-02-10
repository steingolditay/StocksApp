package com.steingolditay.app.buxassignment.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.steingolditay.app.buxassignment.Constants
import com.steingolditay.app.buxassignment.api.WebSocketListener
import com.steingolditay.app.buxassignment.model.Product
import com.steingolditay.app.buxassignment.repository.Repository
import kotlinx.coroutines.launch
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response

class QuoteViewModel(private val repository: Repository): ViewModel() {


    fun getLiveData(product: Product): LiveData<String>{
        val listener = WebSocketListener(product)

        viewModelScope.launch {
            val request = Request.Builder().url(Constants.websocket_url).build()
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

            val webSocket = httpClient.newWebSocket(request, listener)

        }
        return listener.liveData
    }


}