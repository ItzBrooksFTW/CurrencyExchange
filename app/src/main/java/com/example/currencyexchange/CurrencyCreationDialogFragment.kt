package com.example.currencyexchange

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.DialogFragment

class CurrencyCreationDialogFragment : DialogFragment() {

    interface OnCurrencyCreatedListener {
        fun onCurrencyCreated(code: String, name: String, customRate: Double?)
    }

    private var listener: OnCurrencyCreatedListener? = null
    private var onDismissListener: (() -> Unit)? = null

    fun setOnCurrencyCreatedListener(listener: OnCurrencyCreatedListener) {
        this.listener = listener
    }

    fun setOnDismissListener(onDismiss: () -> Unit) {
        this.onDismissListener = onDismiss
    }

    override fun onDismiss(dialog: android.content.DialogInterface) {
        super.onDismiss(dialog)
        onDismissListener?.invoke()
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.dialog_currency_creation, container, false)

        val inputCode = view.findViewById<EditText>(R.id.inputCurrencyCode)
        val inputName = view.findViewById<EditText>(R.id.inputCurrencyName)

        val inputCustomRate = view.findViewById<EditText>(R.id.inputCustomRate)
        val buttonCreate = view.findViewById<Button>(R.id.buttonCreateCurrency)
        val buttonCancel = view.findViewById<Button>(R.id.buttonCancel)




        buttonCreate.setOnClickListener {
            val code = inputCode.text.toString().trim()
            val name = inputName.text.toString().trim()
            val customRate = inputCustomRate.text.toString().toDoubleOrNull()

            if (code.isNotEmpty() && name.isNotEmpty()) {
                listener?.onCurrencyCreated(code, name, customRate)
                dismiss()
            }
        }

        buttonCancel.setOnClickListener {
            dismiss()
        }

        return view
    }
}
