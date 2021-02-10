package com.steingolditay.app.buxassignment.model
import com.google.gson.annotations.SerializedName


class Product (
    val symbol: String,
    val securityId: String,
    val displayName: String,
    val displayDecimals: String,
    val quoteCurrency: String,
    val currentPrice: HashMap<String, Any>,
    val closingPrice: HashMap<String, Any>
)
