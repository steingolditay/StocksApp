package com.steingolditay.app.buxassignment.views

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.gson.JsonParser
import com.steingolditay.app.buxassignment.Constants
import com.steingolditay.app.buxassignment.databinding.ActivityProductBinding
import com.steingolditay.app.buxassignment.model.Product
import com.steingolditay.app.buxassignment.repository.Repository
import com.steingolditay.app.buxassignment.viewmodel.ProductViewModel
import com.steingolditay.app.buxassignment.viewmodel.ProductViewModelFactory
import com.steingolditay.app.buxassignment.viewmodel.QuoteViewModel


class ProductActivity: AppCompatActivity() {

    private lateinit var binding: ActivityProductBinding
    private lateinit var viewModel: ProductViewModel
    private lateinit var quoteViewModel: QuoteViewModel
    private lateinit var product: Product
    private var subscribed = false


    override fun onStop() {
        super.onStop()
        if (this::product.isInitialized && subscribed){
            unSubscribeWebSocket(product)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProductBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        val bundle = intent.extras
        val productIdentifier = bundle?.getString(Constants.product_identifier)

        val repository = Repository()
        val viewModelFactory = ProductViewModelFactory(repository)
        viewModel = ViewModelProvider(this, viewModelFactory).get(ProductViewModel::class.java)
        viewModel.getProduct("products/$productIdentifier")
        viewModel.productData.observe(this, Observer {
            product = it
            initProduct(product)
        })

        binding.button.setOnClickListener {
            if (this::product.isInitialized && !subscribed){
                initWebSocket(product)
            }
            else if (this::product.isInitialized && subscribed){
                unSubscribeWebSocket(product)
            }
        }


    }

    private fun initProduct(product: Product){
        binding.productName.text = product.displayName
        binding.productId.text = product.securityId
        binding.productSymbol.text = product.symbol
        binding.productCurrentPrice.text = "${product.currentPrice["amount"]} ${product.quoteCurrency}"
        binding.productClosingPrice.text = "${product.closingPrice["amount"]} ${product.quoteCurrency}"
    }


    private fun initWebSocket(product: Product){
        Log.d("TAG", "initWebSocket: ")
        quoteViewModel = ViewModelProvider(this).get(QuoteViewModel::class.java)
        quoteViewModel.getLiveData(product).observe(this, Observer {
            val json = JsonParser.parseString(it).asJsonObject
            val currentPrice = json["body"].asJsonObject["currentPrice"].toString().replace("\"", "")
            binding.liveData.text = currentPrice
            calculatePercentage(currentPrice)
            subscribed = true
            binding.button.text = "Unsubscribe"

        })
    }

    private fun unSubscribeWebSocket(product: Product){
        quoteViewModel.stopListener()
        binding.button.text = "Subscribe"
        subscribed = false
    }

    private fun calculatePercentage(currentPrice: String){

    }

}