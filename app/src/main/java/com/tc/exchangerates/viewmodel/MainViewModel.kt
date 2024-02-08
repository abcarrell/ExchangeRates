package com.tc.exchangerates.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tc.exchangerates.component.RatesApi
import com.tc.exchangerates.model.ExchangeRate
import com.tc.exchangerates.mvi.MVI
import com.tc.exchangerates.mvi.MVIActor
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import javax.inject.Inject
import javax.inject.Named

@HiltViewModel
class MainViewModel @Inject constructor(
    private val ratesApi: RatesApi,
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
            withContext(Dispatchers.IO) {
                try {
                    val response = ratesApi.getAllRates()
                    if (response.isSuccessful) {
                        response.body()?.run {
                            mvi.setState { copy(loading = false, rates = data.sortedBy { it.symbol }) }
                        }
                        mvi.setEffect { UIEffect.CompleteMessage }
                    } else {
                        throw IOException("${response.code()}: ${response.message()}")
                    }
                } catch (e: Throwable) {
                    mvi.setState { copy(loading = false) }
                    mvi.setEffect { UIEffect.ErrorMessage(e.message ?: "Unknown Error") }
                }
            }
        }
    }
}

data class MainUiState(
    val loading: Boolean = false,
    val rates: List<ExchangeRate> = emptyList()
)

sealed class MainEvent {
    object GetRates : MainEvent()
}

sealed class UIEffect {
    object CompleteMessage : UIEffect()
    data class ErrorMessage(val message: String) : UIEffect()
}
