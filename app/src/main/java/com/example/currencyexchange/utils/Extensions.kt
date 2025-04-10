package com.example.currencyexchange.utils

import java.util.Locale

fun Double.formatAsCurrency(): String {
    return if (this % 1.0 == 0.0) this.toLong().toString() else String.format(Locale.getDefault(), "%.2f", this)
}