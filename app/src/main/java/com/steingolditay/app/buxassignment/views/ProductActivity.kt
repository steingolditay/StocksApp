package com.steingolditay.app.buxassignment.views

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.gson.JsonObject
import com.steingolditay.app.buxassignment.Constants
import com.steingolditay.app.buxassignment.api.WebSocketListener
import com.steingolditay.app.buxassignment.databinding.ActivityProductBinding
import com.steingolditay.app.buxassignment.model.Product
import com.steingolditay.app.buxassignment.repository.Repository
import com.steingolditay.app.buxassignment.viewmodel.ProductViewModel
import com.steingolditay.app.buxassignment.viewmodel.ProductViewModelFactory
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response


class ProductActivity: AppCompatActivity() {

    private lateinit var binding: ActivityProductBinding
    private lateinit var viewModel: ProductViewModel

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
            initProduct(it)
        })

    }

    private fun initProduct(product: Product){
        binding.productName.text = product.displayName
        binding.productId.text = product.securityId
        binding.productSymbol.text = product.symbol
        binding.productCurrentPrice.text = "${product.currentPrice["amount"]} ${product.quoteCurrency}"
        binding.productClosingPrice.text = "${product.closingPrice["amount"]} ${product.quoteCurrency}"

        initWebSocket(product)
    }


    private fun initWebSocket(product: Product){
        val request = Request.Builder().url(Constants.websocket_url).build()
        val webSocketListener = WebSocketListener(product)
        val httpClient = OkHttpClient.Builder()
            .addInterceptor(object : Interceptor {
                override fun intercept(chain: Interceptor.Chain): Response {
                    val builder = chain.request().newBuilder()
                    builder.addHeader("Authorization", Constants.token)
                    builder.addHeader("Accept-Language", Constants.accept_language)

                    return chain.proceed(builder.build())
                }
            })
            .build()

        val webSocket = httpClient.newWebSocket(request, webSocketListener)



    }

}