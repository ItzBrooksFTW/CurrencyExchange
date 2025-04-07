package com.example.currencyexchange.data

data class ExchangeRates(

    val base: String,
    val date: String,
    val rates: Map<String, Double>
)
