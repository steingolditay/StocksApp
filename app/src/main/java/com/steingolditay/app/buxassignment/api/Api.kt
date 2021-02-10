package com.steingolditay.app.buxassignment.api

import com.steingolditay.app.buxassignment.model.Product
import retrofit2.http.GET
import retrofit2.http.Url

interface Api {

    @GET("products/")
    suspend fun getAllProducts(): ArrayList<Product>

    @GET
    suspend fun getProduct(@Url url: String): Product

}