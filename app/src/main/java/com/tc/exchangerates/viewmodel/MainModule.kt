package com.tc.exchangerates.viewmodel

import com.tc.exchangerates.data.DataMapper
import com.tc.exchangerates.model.ExchangeRate
import com.tc.exchangerates.model.ExchangeRateResponse
import com.tc.exchangerates.mvi.MVIActor
import com.tc.exchangerates.mvi.mvi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import retrofit2.Response
import java.io.IOException
import javax.inject.Named

@Module
@InstallIn(ViewModelComponent::class)
object MainModule {
    @Provides
    @Named("MainMVIActor")
    fun provideMainMVIActor(): MVIActor<MainUiState, MainEvent, UIEffect> = mvi(MainUiState())

    @Provides
    fun provideExchangeRateDataMapper() = ExchangeRateDataMapper { response ->
        runCatching {
            if (response.isSuccessful) checkNotNull(response.body()?.data) { "Response is null" }
            else with(response) {
                throw IOException("API Error ${code()}: ${errorBody()?.string() ?: message()}")
            }
        }
    }
}

fun interface ExchangeRateDataMapper : DataMapper<Response<ExchangeRateResponse>, Result<List<ExchangeRate>>>
