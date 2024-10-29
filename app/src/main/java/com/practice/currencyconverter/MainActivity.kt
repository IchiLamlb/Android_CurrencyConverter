package com.practice.currencyconverter

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {
    private val apiKey = "55f677901a3fdbe9a50f63b2"
    private lateinit var ed1: EditText
    private lateinit var ed2: EditText
    private lateinit var spinner1: Spinner
    private lateinit var spinner2: Spinner
    private lateinit var conversionRates: Map<String, Double>

    // Cờ để theo dõi việc cập nhật EditText
    private var isUpdatingEd1 = false
    private var isUpdatingEd2 = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Bind UI elements
        ed1 = findViewById(R.id.ed1)
        ed2 = findViewById(R.id.ed2)
        spinner1 = findViewById(R.id.spinner1)
        spinner2 = findViewById(R.id.spinner2)

        // Set default value for ed1
        ed1.setText("0.0")

        // Fetch currency rates and initialize spinners
        fetchRatesAndSetupSpinners()

        // Clear default value on focus
        ed1.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus && ed1.text.toString() == "0.0") {
                ed1.setText("")
            }
        }
    }

    private fun fetchRatesAndSetupSpinners() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response: ExchangeRatesResponse = ApiClient.apiService.getRates(apiKey)
                conversionRates = response.conversion_rates

                // Update UI on main thread
                withContext(Dispatchers.Main) {
                    setupSpinners()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                // Handle errors (display a Toast or error message in production)
            }
        }
    }

    private fun setupSpinners() {
        val currencyList = conversionRates.keys.toList()
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, currencyList)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner1.adapter = adapter
        spinner2.adapter = adapter

        // Set up listeners to trigger currency conversion on changes
        ed1.addTextChangedListener { convertCurrencyFromEd1() }
        ed2.addTextChangedListener { convertCurrencyFromEd2() }
        spinner1.onItemSelectedListener = createConversionListener()
        spinner2.onItemSelectedListener = createConversionListener()
    }

    private fun convertCurrencyFromEd1() {
        // Kiểm tra xem ed1 có đang được cập nhật không
        if (isUpdatingEd1) return

        val amount = ed1.text.toString().toDoubleOrNull() ?: return
        val fromCurrency = spinner1.selectedItem.toString()
        val toCurrency = spinner2.selectedItem.toString()

        val rate = conversionRates[toCurrency]!! / conversionRates[fromCurrency]!!
        val convertedAmount = amount * rate

        // Cập nhật giá trị và đánh dấu là đang cập nhật
        isUpdatingEd2 = true
        ed2.setText(String.format("%.2f", convertedAmount))
        isUpdatingEd2 = false
    }

    private fun convertCurrencyFromEd2() {
        // Kiểm tra xem ed2 có đang được cập nhật không
        if (isUpdatingEd2) return

        val amount = ed2.text.toString().toDoubleOrNull() ?: return
        val fromCurrency = spinner2.selectedItem.toString()
        val toCurrency = spinner1.selectedItem.toString()

        val rate = conversionRates[toCurrency]!! / conversionRates[fromCurrency]!!
        val convertedAmount = amount * rate

        // Cập nhật giá trị và đánh dấu là đang cập nhật
        isUpdatingEd1 = true
        ed1.setText(String.format("%.2f", convertedAmount))
        isUpdatingEd1 = false
    }

    private fun createConversionListener() = object : AdapterView.OnItemSelectedListener {
        override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
            // Gọi cả hai phương thức chuyển đổi khi spinner thay đổi
            convertCurrencyFromEd1() // Chuyển đổi khi spinner 1 thay đổi
            convertCurrencyFromEd2() // Chuyển đổi khi spinner 2 thay đổi
        }

        override fun onNothingSelected(parent: AdapterView<*>) {}
    }
}
