package com.steingolditay.app.buxassignment.viewmodel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.steingolditay.app.buxassignment.model.Product
import com.steingolditay.app.buxassignment.repository.Repository
import kotlinx.coroutines.launch

class ProductsListViewModel(private val repository: Repository): ViewModel() {

    val productData = MutableLiveData<Product>()
    val allProductsData = MutableLiveData<ArrayList<Product>>()

    fun getProduct(product: String){
        viewModelScope.launch {
            val response = repository.getProduct(product)
            productData.value = response
        }
    }

    fun getAllProducts(){
        viewModelScope.launch {
            val response = repository.getAllProducts()
            allProductsData.value = response

        }
    }
}