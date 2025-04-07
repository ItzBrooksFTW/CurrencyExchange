package com.example.currencyexchange

import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.currencyexchange.utils.CurrencyConverter



class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val currencyConverter = CurrencyConverter(this)

        currencyConverter.convertCurrency("USD", "GBP", 100.0) { result ->
            if (result != null) {
                // Use the converted amount
                Log.d("CurrencyConverter", "Converted amount: $result")
            } else {
                // Handle error
                Log.d("CurrencyConverter", "Error converting currency")
            }
        }
    }
}

//TODO: add ui, show currency rate, if unchecked it canot be changed and it will use the api for conversion, if custom is checked the rate can be changed