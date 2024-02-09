package com.tc.exchangerates.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tc.exchangerates.data.DataMapper
import com.tc.exchangerates.data.RatesApi
import com.tc.exchangerates.model.ExchangeRate
import com.tc.exchangerates.model.ExchangeRateResponse
import com.tc.exchangerates.mvi.MVI
import com.tc.exchangerates.mvi.MVIActor
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import retrofit2.Response
import java.io.IOException
import javax.inject.Inject
import javax.inject.Named

@HiltViewModel
class MainViewModel @Inject constructor(
    private val ratesApi: RatesApi,
    private val dataMapper: ExchangeRateDataMapper,
    @Named("MainMVIActor") private val mvi: MVIActor<MainUiState, MainEvent, UIEffect>
) : ViewModel(), MVI<MainUiState, MainEvent, UIEffect> by mvi {

    private fun eventHandler(event: MainEvent) {
        when (event) {
            is MainEvent.GetRates -> getRates()
        }
    }

    private fun subscribeEvents() {
        viewModelScope.launch {
            mvi.events.collect(this@MainViewModel::eventHandler)
        }
    }

    init {
        subscribeEvents()
        getRates()
    }

    // This method is the basic idea of the Intent in Model-View-Intent architecture.
    // We access it using the events passed from the view, ensuring single point-of-entry
    // into the view model.
    private fun getRates() {
        viewModelScope.launch {
            mvi.setState { copy(loading = true, rates = emptyList()) }
            ratesApi.getAllRates().mapToResult().run {
                onSuccess { data ->
                    mvi.setState {
                        copy(
                            loading = false,
                            rates = data.sortedWith(compareByDescending<ExchangeRate> { it.type }.thenBy { it.symbol })
                        )
                    }
                    mvi.setEffect { UIEffect.CompleteMessage }
                }
                onFailure { e ->
                    mvi.setState { copy(loading = false) }
                    mvi.setEffect { UIEffect.ErrorMessage(e.message ?: "Unknown Error") }
                }
            }
        }
    }

    private fun Response<ExchangeRateResponse>.mapToResult() = dataMapper(this)
}

data class MainUiState(
    val loading: Boolean = false,
    val rates: List<ExchangeRate> = emptyList()
)

sealed class MainEvent {
    data object GetRates : MainEvent()
}

sealed class UIEffect {
    data object CompleteMessage : UIEffect()
    data class ErrorMessage(val message: String) : UIEffect()
}
