package com.practice.currencyconverter

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path

// Retrofit service interface for ExchangeRate API
interface ExchangeRateApiService {
    @GET("v6/{api_key}/latest/USD") // This endpoint fetches the latest rates with USD as the base
    suspend fun getRates(@Path("api_key") apiKey: String): ExchangeRatesResponse
}

// Data class for the API response
data class ExchangeRatesResponse(
    val base_code: String, // Base currency code, e.g., "USD"
    val conversion_rates: Map<String, Double> // Conversion rates mapped by currency codes
)

// Singleton object to create and provide Retrofit instance
object ApiClient {
    private const val BASE_URL = "https://v6.exchangerate-api.com/"

    val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val apiService: ExchangeRateApiService by lazy {
        retrofit.create(ExchangeRateApiService::class.java)
    }
}
