package com.steingolditay.app.buxassignment.repository


import com.steingolditay.app.buxassignment.api.RetrofitInstance
import com.steingolditay.app.buxassignment.model.Product
import java.lang.Exception


class Repository {


    suspend fun getAllProducts(): ArrayList<Product>?{
        return try {
            RetrofitInstance.api.getAllProducts()
        }
        catch (e: Exception){
            null
        }
    }

    suspend fun getProduct(product: String): Product? {
        return try {
            RetrofitInstance.api.getProduct(product)
        }
        catch (e: Exception){
            null
        }
    }




}