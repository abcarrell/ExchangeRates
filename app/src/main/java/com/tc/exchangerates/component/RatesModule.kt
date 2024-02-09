package com.tc.exchangerates.component

import com.google.gson.Gson
import com.tc.exchangerates.data.RatesApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

@Module
@InstallIn(SingletonComponent::class)
object RatesModule {

    @Provides
    fun providesRatesApi(): RatesApi = Retrofit.Builder()
        .baseUrl("https://api.coincap.io/v2/")
        .addConverterFactory(GsonConverterFactory.create(Gson()))
        .build().create(RatesApi::class.java)
}
