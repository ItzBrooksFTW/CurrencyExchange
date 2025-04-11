package com.example.currencyexchange

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ListView
import android.widget.Spinner
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.currencyexchange.utils.CurrencyConverter
import com.example.currencyexchange.utils.CurrencyItem
import com.example.currencyexchange.utils.CurrencySpinnerAdapter
import com.example.currencyexchange.utils.InputUtils
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.Locale
import androidx.core.content.edit
import com.example.currencyexchange.data.HistoryItem
import com.example.currencyexchange.utils.formatAsCurrency


class MainActivity : AppCompatActivity() {

    private var inputNumber1: EditText? = null
    private var inputNumber2: EditText? = null
    private var spinnerCurrency1: Spinner? = null
    private var spinnerCurrency2: Spinner? = null
    private var buttonSwap: ImageButton? = null
    private var buttonSave: Button? = null
    private var buttonClearAll: Button? = null
    private val historyList = mutableListOf<HistoryItem>()
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var listView: ListView
    private lateinit var historyAdapter: ArrayAdapter<String>
    private lateinit var currencyConverter: CurrencyConverter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        enableEdgeToEdge()
        currencyConverter = CurrencyConverter(this)
        sharedPreferences = getSharedPreferences("conversion_history", Context.MODE_PRIVATE)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { _, insets ->
            val isKeyboardVisible = insets.isVisible(WindowInsetsCompat.Type.ime())
            if (!isKeyboardVisible) {
                inputNumber1?.clearFocus()
                inputNumber2?.clearFocus()
            }
            insets
        }

        initializeUI()
        setupSpinners()
        setupListeners()
        loadHistory()
        setupClearAllButton()
        setupSaveButton()
        setupListView()

    }

    private fun initializeUI() {
        inputNumber1 = findViewById(R.id.inputNumber1)
        inputNumber2 = findViewById(R.id.inputNumber2)
        spinnerCurrency1 = findViewById(R.id.spinnerCurrency1)
        spinnerCurrency2 = findViewById(R.id.spinnerCurrency2)
        buttonSwap = findViewById(R.id.buttonSwap)
        listView = findViewById(R.id.listView)
        buttonSave=findViewById(R.id.buttonSave)
        buttonClearAll=findViewById(R.id.buttonClearAll)

        inputNumber1?.setText("0")
        inputNumber2?.setText("0")
    }

    private fun setupSpinners() {
        currencyConverter.fetchCurrencies { currencies ->
            if (currencies != null) {
                val currencyItems = currencies.map { (code, name) ->
                    CurrencyItem(code, name, code.take(2).lowercase())
                }
                val adapter = CurrencySpinnerAdapter(this, currencyItems)
                runOnUiThread {
                    spinnerCurrency1?.adapter = adapter
                    spinnerCurrency2?.adapter = adapter
                    spinnerCurrency1?.setSelection(currencyItems.indexOfFirst { it.code == "EUR" })
                    spinnerCurrency2?.setSelection(currencyItems.indexOfFirst { it.code == "USD" })
                }
                setupSwapButton(currencyItems)
            } else {

            }
        }
    }

    private fun setupSwapButton(currencyItems: List<CurrencyItem>) {
        buttonSwap?.setOnClickListener {
            val selectedCurrency1 = spinnerCurrency1?.selectedItem as CurrencyItem
            val selectedCurrency2 = spinnerCurrency2?.selectedItem as CurrencyItem
            spinnerCurrency1?.setSelection(currencyItems.indexOfFirst { it.code == selectedCurrency2.code })
            spinnerCurrency2?.setSelection(currencyItems.indexOfFirst { it.code == selectedCurrency1.code })
        }
    }

    private fun setupSaveButton() {
        buttonSave?.setOnClickListener {
            val amount = inputNumber1?.text.toString().toDoubleOrNull() ?: 0.0
            val from = (spinnerCurrency1?.selectedItem as CurrencyItem).code
            val to = (spinnerCurrency2?.selectedItem as CurrencyItem).code
            val result = inputNumber2?.text.toString().toDoubleOrNull() ?: 0.0

            if(amount == 0.0 || result == 0.0){
                return@setOnClickListener
            }
            val newHistoryItem = HistoryItem(amount, from, to, result)
            saveHistoryItem(newHistoryItem)
        }

    }
    private fun setupClearAllButton() {
        buttonClearAll?.setOnClickListener {
            historyList.clear()
            saveHistoryToPreferences()
            updateListView()
            if(historyList.isEmpty()){
                buttonClearAll?.visibility = View.GONE
            }
        }
    }
    private fun saveHistoryItem(item: HistoryItem) {

            historyList.add(0, item)
            saveHistoryToPreferences()
            updateListView()

    }
    private fun saveHistoryToPreferences() {
        val json = Gson().toJson(historyList)
        sharedPreferences.edit { putString("history", json) }
    }

    private fun loadHistory() {
        val json = sharedPreferences.getString("history", null)
        if (!json.isNullOrEmpty()) {
            val type = object : TypeToken<MutableList<HistoryItem>>() {}.type
            historyList.clear()
            historyList.addAll(Gson().fromJson(json, type))
        }
    }
    private fun setupListView() {
        historyAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_list_item_1,
            historyList.map { it.toDisplayString() }
        )
        listView.adapter = historyAdapter
    }
    private fun updateListView() {
        historyAdapter.clear()
        historyAdapter.addAll(historyList.map { it.toDisplayString() })
        historyAdapter.notifyDataSetChanged()
        if(historyList.isNotEmpty()){
            buttonClearAll?.visibility = View.VISIBLE
        }
    }
    private fun HistoryItem.toDisplayString(): String {
        return "${amount.formatAsCurrency()} $from -> $to: ${result.formatAsCurrency()}"
    }
    private fun setupListeners() {
        setTextListeners(inputNumber1, inputNumber2)
        setTextListeners(inputNumber2, inputNumber1)

        spinnerCurrency1?.onItemSelectedListener = createSpinnerListener()
        spinnerCurrency2?.onItemSelectedListener = createSpinnerListener()
    }

    private fun setTextListeners(input: EditText?, output: EditText?) {
        val listSpinners = mutableListOf(spinnerCurrency1, spinnerCurrency2)
        if (input == inputNumber2) listSpinners.reverse()

        input?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                if (input.hasFocus()) {
                    showConversion(
                        (listSpinners[0]?.selectedItem as CurrencyItem).code,
                        (listSpinners[1]?.selectedItem as CurrencyItem).code,
                        input,
                        output
                    )
                }
                InputUtils.truncateDecimal(input, 2, this)
            }
        })
        InputUtils.handleFocusChange(input!!)
    }

    private fun createSpinnerListener(): AdapterView.OnItemSelectedListener {
        return object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedCurrency1 = spinnerCurrency1?.selectedItem as CurrencyItem
                val selectedCurrency2 = spinnerCurrency2?.selectedItem as CurrencyItem
                showConversion(
                    selectedCurrency1.code,
                    selectedCurrency2.code,
                    inputNumber1,
                    inputNumber2)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun showConversion(currency1: String, currency2: String, input: EditText?, output: EditText?) {
        val conversionValue = input?.text.toString().toDoubleOrNull() ?: 0.0
        currencyConverter.convertCurrency(currency1, currency2, conversionValue) { result, rate ->
            if (result != null && rate != null) {
                val outputValue = if(result == 0.0){
                    "0"
                }else {
                    String.format(Locale.getDefault(), "%.2f", result)
                }
                output?.setText(outputValue)
                Log.d("CurrencyConverter", "Converted amount: $result, Rate: $rate")
            } else {
                Log.d("CurrencyConverter", "Error converting currency")
            }
        }
    }
}

//TODO: add ui, show currency rate, if unchecked it cannot be changed and it will use the api for conversion, if custom is checked the rate can be changed