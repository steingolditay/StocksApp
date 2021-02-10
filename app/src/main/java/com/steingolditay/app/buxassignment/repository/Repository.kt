package com.steingolditay.app.buxassignment.repository

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.google.gson.JsonElement
import com.google.gson.JsonParser
import com.steingolditay.app.buxassignment.Constants
import com.steingolditay.app.buxassignment.api.RetrofitInstance
import com.steingolditay.app.buxassignment.api.WebSocketListener
import com.steingolditay.app.buxassignment.model.Product
import okhttp3.*
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

class Repository {

    private val tradingQuote = MutableLiveData<JsonElement>()


    suspend fun getAllProducts(): ArrayList<Product>{
        return RetrofitInstance.api.getAllProducts()
    }

    suspend fun getProduct(product: String): Product{
        return RetrofitInstance.api.getProduct(product)
    }




}