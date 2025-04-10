package com.example.currencyexchange.utils

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.example.currencyexchange.ExchangeRateApi
import com.example.currencyexchange.data.ExchangeRates
import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.Callback
import retrofit2.converter.gson.GsonConverterFactory
import androidx.core.content.edit
import com.google.gson.reflect.TypeToken
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


    fun fetchCurrencies(callback: (Map<String, String>?) -> Unit) {
        val cachedCurrenciesJson = sharedPreferences.getString("cached_currencies", null)
        if (cachedCurrenciesJson != null) {
            val type = object : TypeToken<Map<String, String>>() {}.type
            val cachedCurrencies: Map<String, String> = gson.fromJson(cachedCurrenciesJson, type)
            callback(cachedCurrencies)
            return
        }

        api.getCurrencies().enqueue(object : Callback<Map<String, String>> {
            override fun onResponse(call: Call<Map<String, String>>, response: Response<Map<String, String>>) {
                if (response.isSuccessful) {
                    val currencies = response.body()
                    sharedPreferences.edit {
                        putString("cached_currencies", gson.toJson(currencies))
                    }
                    Log.d("nista", "nista")
                    callback(currencies)
                } else {
                    callback(null)
                }
            }

            override fun onFailure(call: Call<Map<String, String>>, t: Throwable) {
                callback(null)
            }
        })
    }

    fun convertCurrency(base: String, target: String, amount: Double, callback: (Double?, Double?) -> Unit) {
        val currentTime = System.currentTimeMillis()
        val cachedRatesJson = sharedPreferences.getString("cached_rates", null)
        val lastFetchedTime = sharedPreferences.getLong("last_fetched_time", 0)

        if (cachedRatesJson != null && currentTime-lastFetchedTime < TimeUnit.HOURS.toMillis(24)) {
            val cachedRates = gson.fromJson(cachedRatesJson, ExchangeRates::class.java)
            if (cachedRates.base == base) {
                val rate = cachedRates.rates[target]
                callback(rate?.times(amount), rate)
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
                        callback(rate?.times(amount), rate)
                    } else {
                        callback(null, null)
                    }
                }

                override fun onFailure(call: Call<ExchangeRates>, t: Throwable) {
                    callback(null, null)
                }
            })
        }
    }

}