package com.steingolditay.app.buxassignment.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.steingolditay.app.buxassignment.repository.Repository

class ProductsListViewModelFactory(private val repository: Repository): ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return ProductsListViewModel(repository) as T
    }
}