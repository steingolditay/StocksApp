package com.steingolditay.app.buxassignment.api

import android.util.Log
import com.steingolditay.app.buxassignment.Constants
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


// Retrofit instance singleton

object RetrofitInstance {
    private val httpClient = OkHttpClient.Builder()
        .addInterceptor(object : Interceptor {
            override fun intercept(chain: Interceptor.Chain): Response {
                val builder = chain.request().newBuilder()
                builder.addHeader("Authorization", Constants.token)
                builder.addHeader("Accept", Constants.accept)
                builder.addHeader("Accept-Language", Constants.accept_language)

                return chain.proceed(builder.build())
            }
        })
        .build()

    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(Constants.base_url)
            .addConverterFactory(GsonConverterFactory.create())
            .client(httpClient)
            .build()
    }

    val api: Api by lazy {
        retrofit.create(Api::class.java)
    }

}