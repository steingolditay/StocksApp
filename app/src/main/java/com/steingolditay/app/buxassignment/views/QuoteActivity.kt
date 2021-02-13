package com.steingolditay.app.buxassignment.views

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.gson.JsonParser
import com.steingolditay.app.buxassignment.Utils.Constants
import com.steingolditay.app.buxassignment.Utils.NetworkConnectionMonitor
import com.steingolditay.app.buxassignment.R
import com.steingolditay.app.buxassignment.databinding.ActivityProductBinding
import com.steingolditay.app.buxassignment.model.Product
import com.steingolditay.app.buxassignment.repository.Repository
import com.steingolditay.app.buxassignment.viewmodel.ProductsListViewModel
import com.steingolditay.app.buxassignment.viewmodel.ProductsListViewModelFactory
import com.steingolditay.app.buxassignment.viewmodel.QuoteViewModel
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.*


class QuoteActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProductBinding
    private lateinit var viewModel: ProductsListViewModel
    private lateinit var quoteViewModel: QuoteViewModel
    private lateinit var product: Product
    private var subscribed = false
    private var marketStatus = false
    private var connectionStatus = false
    private val format: NumberFormat = DecimalFormat.getCurrencyInstance(Locale.getDefault())
    private val networkConnectionMonitor = NetworkConnectionMonitor(this)

    override fun onDestroy() {
        super.onDestroy()
        if (this::product.isInitialized && subscribed) {
            unSubscribeWebSocket()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProductBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)


        // get product id from bundle
        val bundle = intent.extras
        val productIdentifier = bundle?.getString(Constants.product_identifier)

        val repository = Repository()
        val viewModelFactory = ProductsListViewModelFactory(repository)

        // init an internet connection monitor
        networkConnectionMonitor.registerNetworkCallback()
        networkConnectionMonitor.liveData.observe(this, Observer { isConnected ->
            connectionStatus = isConnected
            // if connection available, load product list
            // if not, load connection lost image and un subscribe
            when (connectionStatus) {
                true -> {
                    viewModel = ViewModelProvider(this, viewModelFactory).get(ProductsListViewModel::class.java)
                    viewModel.getProduct("products/$productIdentifier")
                    viewModel.productData.observe(this, Observer { data ->
                        if (data != null){
                            product = data
                            updateUi(product)
                        }
                    })
                    updateUIConnected()
                }
                false -> {
                    updateUIDisconnected()
                    if (subscribed) {
                        unSubscribeWebSocket()
                    }
                }
            }
        })

        binding.button.setOnClickListener {
            subscribeFunction()
        }
    }

    private fun updateUi(product: Product) {
        // format currency and decimals according to the product
        format.currency = Currency.getInstance(product.quoteCurrency)
        format.maximumFractionDigits = product.displayDecimals

        // update product information
        binding.productName.text = product.displayName
        binding.productId.text = product.securityId
        binding.productSymbol.text = product.symbol

        val formattedClosing = format.format(product.closingPrice["amount"].toString().toBigDecimal())
        val formattedCurrent = format.format(product.currentPrice["amount"].toString().toBigDecimal())
        binding.productClosingPrice.text = formattedClosing
        binding.liveData.text = formattedCurrent
        calculatePercentage(product.currentPrice["amount"].toString())


        // update market status title
        marketStatus = (product.productMarketStatus == Constants.open)
        when (marketStatus) {
            true -> {
                binding.tradingStatus.text = getString(R.string.tradings_open)
                binding.tradingStatus.setTextColor(getColor(R.color.green))
            }
            false -> {
                binding.tradingStatus.text = getString(R.string.tradings_closed)
                binding.tradingStatus.setTextColor(getColor(R.color.red))
            }
        }

        // (?) probably should hide the subscribe button if market is closed
    }


    private fun initWebSocket(product: Product) {
        when (marketStatus) {
            true -> {
                binding.progressBar.visibility = View.VISIBLE
                quoteViewModel = ViewModelProvider(this).get(QuoteViewModel::class.java)
                quoteViewModel.getLiveData(product).observe(this, Observer { text ->
                    binding.progressBar.visibility = View.GONE

                    if (text == "Error") {
                        Toast.makeText(this, getString(R.string.service_unavailable), Toast.LENGTH_SHORT).show()
                    }
                    else {

                        val json = JsonParser.parseString(text).asJsonObject
                        val currentPrice = json["body"].asJsonObject["currentPrice"].toString().replace("\"", "")
                        val formatted = format.format(currentPrice.toBigDecimal())

                        binding.liveData.text = formatted
                        binding.button.text = getString(R.string.unsubscribe)
                        binding.animation.visibility = View.VISIBLE
                        calculatePercentage(currentPrice)
                        subscribed = true
                    }
                })
            }
            false -> {
                Toast.makeText(this, getString(R.string.tradings_closed), Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun unSubscribeWebSocket() {
        quoteViewModel.stopListener()
        binding.button.text = getString(R.string.subscribe)
        subscribed = false
        binding.animation.visibility = View.GONE

    }

    private fun calculatePercentage(currentPrice: String) {
        val lastClosedPrice = BigDecimal(product.closingPrice["amount"].toString())
        val updatedPrice = currentPrice.toBigDecimal()
        val difference = ((updatedPrice - lastClosedPrice) * BigDecimal(100)).divide(lastClosedPrice, 2, RoundingMode.HALF_UP)

        if (difference >= "0".toBigDecimal()) {
            binding.percentage.setTextColor(getColor(R.color.green))
            binding.percentage.text = String.format("%s%%", difference)

        } else {
            binding.percentage.setTextColor(getColor(R.color.red))
            binding.percentage.text = String.format("-%s%%", difference)

        }


    }

    private fun updateUIConnected(){
        binding.tradingStatus.visibility = View.VISIBLE
        binding.connectionLost.visibility = View.GONE
    }

    private fun updateUIDisconnected(){
        binding.tradingStatus.visibility = View.GONE
        binding.connectionLost.visibility = View.VISIBLE
        binding.animation.visibility = View.GONE
    }

    private fun subscribeFunction(){
        if (this::product.isInitialized && !subscribed && connectionStatus) {
            initWebSocket(product)
        } else if (this::product.isInitialized && subscribed) {
            unSubscribeWebSocket()
        }
        else if (!connectionStatus){
            Toast.makeText(this, "Unable to connect without internet", Toast.LENGTH_SHORT).show()
        }
    }
}