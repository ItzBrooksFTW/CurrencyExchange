package com.example.currencyexchange.utils

import android.text.TextWatcher
import android.widget.EditText

object InputUtils {

    fun truncateDecimal(input: EditText, maxDecimalPlaces: Int, textWatcher: TextWatcher) {
        val inputValue = input.text.toString()
        if (inputValue.contains(".")) {
            val parts = inputValue.split(".")
            if (parts.size > 1 && parts[1].length > maxDecimalPlaces) {
                val truncated = parts[0] + "." + parts[1].substring(0, maxDecimalPlaces)
                input.removeTextChangedListener(textWatcher)
                input.setText(truncated)
                input.setSelection(truncated.length)
                input.addTextChangedListener(textWatcher)
            }
        }
    }

    fun handleFocusChange(input: EditText) {
        input.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus && input.text.toString() == "0") {
                input.text?.clear()
            } else if (input.text.toString().isEmpty()) {
                input.setText("0")
            }
        }
    }
}