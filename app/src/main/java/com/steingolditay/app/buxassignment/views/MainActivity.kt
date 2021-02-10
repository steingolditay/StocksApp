package com.steingolditay.app.buxassignment.views

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.steingolditay.app.buxassignment.Constants
import com.steingolditay.app.buxassignment.R
import com.steingolditay.app.buxassignment.adapters.ProductsAdapter
import com.steingolditay.app.buxassignment.databinding.ActivityMainBinding
import com.steingolditay.app.buxassignment.model.Product
import com.steingolditay.app.buxassignment.repository.Repository
import com.steingolditay.app.buxassignment.viewmodel.ProductViewModel
import com.steingolditay.app.buxassignment.viewmodel.ProductViewModelFactory


class MainActivity : AppCompatActivity(), ProductsAdapter.OnItemClickListener {
    private lateinit var binding: ActivityMainBinding

    private lateinit var viewModel: ProductViewModel
    private lateinit var adapter: ProductsAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        val repository = Repository()
        val viewModelFactory = ProductViewModelFactory(repository)
        viewModel = ViewModelProvider(this, viewModelFactory).get(ProductViewModel::class.java)

        viewModel.getAllProducts()

        viewModel.allProductsData.observe(this, Observer {
            initRecyclerView()

        })

//        viewModel.getProduct("products/sb26496")
//        viewModel.productData.observe(this, Observer {
//            binding.text.text = it.closingPrice["amount"].toString()
//        })

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.product_search_menu, menu)

        val searchItem = menu?.findItem(R.id.search)
        val searchView = searchItem?.actionView as SearchView

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener{
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

    private fun initRecyclerView(){
        adapter = ProductsAdapter(this, viewModel.allProductsData.value, this)
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter

    }

    override fun onItemClick(product: Product) {
        val intent = Intent(this, ProductActivity::class.java)
        intent.putExtra(Constants.product_identifier, product.securityId)
        startActivity(intent)
    }


}