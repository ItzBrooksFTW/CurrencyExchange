package com.example.currencyexchange.utils

import android.content.Context
import android.content.SharedPreferences
import com.example.currencyexchange.ExchangeRateApi
import com.example.currencyexchange.data.ExchangeRates
import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.Callback
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.core.content.edit
import java.util.concurrent.TimeUnit

class CurrencyConverter(context: Context) {

    private val baseUrl = "https://api.frankfurter.app/"
    private val retrofit = Retrofit.Builder()
        .baseUrl(baseUrl)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    private val api = retrofit.create(ExchangeRateApi::class.java)
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences("exchange_rates", Context.MODE_PRIVATE)
    private val gson = Gson()

    fun convertCurrency(base: String, target: String, amount: Double, callback: (Double?) -> Unit) {
        val currentTime = System.currentTimeMillis()
        val cachedRatesJson = sharedPreferences.getString("cached_rates", null)
        val lastFetchedTime = sharedPreferences.getLong("last_fetched_time", 0)

        if (cachedRatesJson != null && currentTime-lastFetchedTime < TimeUnit.HOURS.toMillis(24)) {
            val cachedRates = gson.fromJson(cachedRatesJson, ExchangeRates::class.java)
            if (cachedRates.base == base) {
                val rate = cachedRates.rates[target]
                callback(rate?.times(amount))
                return
            }
        } else {
            val call = api.getExchangeRates(base)
            call.enqueue(object : Callback<ExchangeRates> {
                override fun onResponse(call: Call<ExchangeRates>, response: Response<ExchangeRates>) {
                    if (response.isSuccessful) {
                        val rates = response.body()
                        sharedPreferences.edit() {
                            putString("cached_rates", gson.toJson(rates))
                            putLong("last_fetched_date", currentTime)
                        }
                        val rate = rates?.rates?.get(target)
                        callback(rate?.times(amount))
                    } else {
                        callback(null)
                    }
                }

                override fun onFailure(call: Call<ExchangeRates>, t: Throwable) {
                    callback(null)
                }
            })
        }
    }}