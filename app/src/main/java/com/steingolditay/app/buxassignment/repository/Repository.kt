package com.steingolditay.app.buxassignment.repository


import com.steingolditay.app.buxassignment.api.RetrofitInstance
import com.steingolditay.app.buxassignment.model.Product


class Repository {


    suspend fun getAllProducts(): ArrayList<Product>{
        return RetrofitInstance.api.getAllProducts()
    }

    suspend fun getProduct(product: String): Product{
        return RetrofitInstance.api.getProduct(product)
    }

    




}