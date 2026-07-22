package com.payrollpro.app.network

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    var baseUrl: String = "http://10.0.2.2/payroll/"
        set(value) {
            if (field != value) {
                field = value
                _api = null // force rebuild with new base URL
            }
        }

    private var _api: PayrollApiService? = null

    val api: PayrollApiService
        get() {
            if (_api == null) {
                val logging = HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }
                val client = OkHttpClient.Builder().addInterceptor(logging).build()
                _api = Retrofit.Builder()
                    .baseUrl(baseUrl)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
                    .create(PayrollApiService::class.java)
            }
            return _api!!
        }
}