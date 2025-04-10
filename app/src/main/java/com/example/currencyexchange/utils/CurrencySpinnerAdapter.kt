package com.example.currencyexchange.utils

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.example.currencyexchange.R

data class CurrencyItem(
    val code: String,
    val name: String,
    val countryCode: String

)

class CurrencySpinnerAdapter(
    context: Context,
    private val items: List<CurrencyItem>
) : ArrayAdapter<CurrencyItem>(context, R.layout.item_currency_spinner, items) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        return createView(position, convertView, parent)
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        return createView(position, convertView, parent)
    }

    private fun createView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.item_currency_spinner, parent, false)
        val item = items[position]

        val flagImageView = view.findViewById<ImageView>(R.id.flagImageView)
        val currencyTextView = view.findViewById<TextView>(R.id.currencyTextView)

        // Load flag icon using Glide
        val flagUrl = "https://flagcdn.com/w320/${item.countryCode}.png"
        Glide.with(context).load(flagUrl).into(flagImageView)

        // Set currency name
        currencyTextView.text = "${item.code} - ${item.name}"

        return view
    }
}