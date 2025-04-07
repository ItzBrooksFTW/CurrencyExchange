package com.example.currencyexchange

import com.example.currencyexchange.data.ExchangeRates
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface ExchangeRateApi {
    @GET("latest")
    fun getExchangeRates(
        @Query("from") from: String = "EUR"
    ): Call<ExchangeRates>

}
// TODO: ako postoji vec json s tom valutom i nije starije od 1 dana ne treba ponovno zvati