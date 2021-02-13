package com.steingolditay.app.buxassignment.views

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.steingolditay.app.buxassignment.Utils.Constants
import com.steingolditay.app.buxassignment.Utils.NetworkConnectionMonitor
import com.steingolditay.app.buxassignment.R
import com.steingolditay.app.buxassignment.adapters.ProductsAdapter
import com.steingolditay.app.buxassignment.databinding.ActivityMainBinding
import com.steingolditay.app.buxassignment.model.Product
import com.steingolditay.app.buxassignment.repository.Repository
import com.steingolditay.app.buxassignment.viewmodel.ProductsListViewModel
import com.steingolditay.app.buxassignment.viewmodel.ProductsListViewModelFactory


class ProductsListActivity : AppCompatActivity(), ProductsAdapter.OnItemClickListener {
    private lateinit var binding: ActivityMainBinding

    private lateinit var viewModel: ProductsListViewModel
    private lateinit var adapter: ProductsAdapter
    private val networkConnectionMonitor = NetworkConnectionMonitor(this)
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)


        val repository = Repository()
        val viewModelFactory = ProductsListViewModelFactory(repository)
        viewModel = ViewModelProvider(this, viewModelFactory).get(ProductsListViewModel::class.java)

        // Check if internet connection is available
        // LiveData listener is implemented for real-time response
        networkConnectionMonitor.registerNetworkCallback()
        networkConnectionMonitor.liveData.observe(this, Observer {
            // if connection available, load product list
            // if not, notify user
            when (it) {
                true -> {
                    updateUIConnected()
                    initViewHolder()
                }
                false -> {
                    updateUIDisconnected()
                }
            }
        })
    }

    private fun initViewHolder() {
        viewModel.getAllProducts()
        viewModel.allProductsData.observe(this, Observer { product ->
            if (product != null) {
                initRecyclerView()
            }
        })
    }

    // Implement search function to the toolbar
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.product_search_menu, menu)

        val searchItem = menu?.findItem(R.id.search)
        val searchView = searchItem?.actionView as SearchView

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                adapter.filter.filter(newText)
                return false
            }
        })

        return true
    }

    // Load recyclerView with the product list
    private fun initRecyclerView() {
        adapter = ProductsAdapter(this, (viewModel.allProductsData.value), this)
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter
    }

    // Move to single product view on item selected
    override fun onItemClick(product: Product) {
        val intent = Intent(this, QuoteActivity::class.java)
        intent.putExtra(Constants.product_identifier, product.securityId)
        startActivity(intent)
    }

    private fun updateUIConnected() {
        binding.recyclerView.visibility = View.VISIBLE
        binding.connectionLost.visibility = View.GONE
    }

    private fun updateUIDisconnected() {
        binding.recyclerView.visibility = View.GONE
        binding.connectionLost.visibility = View.VISIBLE
    }


}