package com.steingolditay.app.buxassignment.views

import android.os.Bundle
import android.util.Log
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
import com.steingolditay.app.buxassignment.viewmodel.ProductViewModel
import com.steingolditay.app.buxassignment.viewmodel.ProductViewModelFactory
import com.steingolditay.app.buxassignment.viewmodel.QuoteViewModel
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.*


class ProductActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProductBinding
    private lateinit var viewModel: ProductViewModel
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


        val bundle = intent.extras
        val productIdentifier = bundle?.getString(Constants.product_identifier)

        val repository = Repository()
        val viewModelFactory = ProductViewModelFactory(repository)



        networkConnectionMonitor.registerNetworkCallback()
        networkConnectionMonitor.liveData.observe(this, Observer { isConnected ->
            connectionStatus = isConnected
            // if connection available, load product list
            // if not, load connection lost image and un subscribe
            when (connectionStatus) {
                true -> {
                    viewModel = ViewModelProvider(this, viewModelFactory).get(ProductViewModel::class.java)
                    viewModel.getProduct("products/$productIdentifier")
                    viewModel.productData.observe(this, Observer { data ->
                        product = data
                        if (product.productMarketStatus == "OPEN") {
                            marketStatus = true
                        }
                        format.currency = Currency.getInstance(product.quoteCurrency)
                        format.maximumFractionDigits = product.displayDecimals

                        updateUi(product)
                    })

                    binding.tradingStatus.visibility = View.VISIBLE
                    binding.connectionLost.visibility = View.GONE
                }
                false -> {
                    binding.tradingStatus.visibility = View.GONE
                    binding.connectionLost.visibility = View.VISIBLE
                    if (subscribed) {
                        unSubscribeWebSocket()
                    }
                }
            }
        })


        binding.button.setOnClickListener {
            if (this::product.isInitialized && !subscribed && connectionStatus) {
                initWebSocket(product)
            } else if (this::product.isInitialized && subscribed) {
                unSubscribeWebSocket()
            }
        }
    }

    private fun updateUi(product: Product) {
        binding.productName.text = product.displayName
        binding.productId.text = product.securityId
        binding.productSymbol.text = product.symbol

        val formattedClosing = format.format(product.closingPrice["amount"].toString().toBigDecimal())
        val formattedCurrent = format.format(product.currentPrice["amount"].toString().toBigDecimal())
        binding.productClosingPrice.text = formattedClosing
        binding.liveData.text = formattedCurrent

        when (marketStatus) {
            true -> {
                binding.tradingStatus.text = "Tradings are open"
                binding.tradingStatus.setTextColor(getColor(R.color.green))
            }
            false -> {
                binding.tradingStatus.text = "Tradings are closed"
                binding.tradingStatus.setTextColor(getColor(R.color.red))
            }
        }


        calculatePercentage(product.currentPrice["amount"].toString())
    }


    private fun initWebSocket(product: Product) {
        when (marketStatus) {
            true -> {
                binding.progressBar.visibility = View.VISIBLE
                quoteViewModel = ViewModelProvider(this).get(QuoteViewModel::class.java)
                quoteViewModel.getLiveData(product).observe(this, Observer { text ->
                    binding.progressBar.visibility = View.GONE

                    if (text == "Error") {
                        Toast.makeText(this, "Unable to connect to service", Toast.LENGTH_SHORT).show()
                    }
                    else {
                        val json = JsonParser.parseString(text).asJsonObject
                        val currentPrice = json["body"].asJsonObject["currentPrice"].toString().replace("\"", "")

                        val formatted = format.format(currentPrice.toBigDecimal())

                        binding.liveData.text = formatted
                        calculatePercentage(currentPrice)
                        subscribed = true
                        binding.button.text = getString(R.string.unsubscribe)
                    }
                })
            }
            false -> {
                Toast.makeText(this, "Tradings are closed right now", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun unSubscribeWebSocket() {
        quoteViewModel.stopListener()
        binding.button.text = getString(R.string.subscribe)
        subscribed = false
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

}