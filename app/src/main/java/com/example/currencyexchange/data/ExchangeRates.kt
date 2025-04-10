package com.example.currencyexchange.data

data class ExchangeRates(


    val date: String,
    val base: String,
    val rates: Map<String, Double>
)
