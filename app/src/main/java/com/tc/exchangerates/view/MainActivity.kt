package com.tc.exchangerates.view

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView.ItemDecoration
import com.tc.exchangerates.databinding.ActivityMainBinding
import com.tc.exchangerates.viewmodel.MainEvent
import com.tc.exchangerates.viewmodel.MainViewModel
import com.tc.exchangerates.viewmodel.UIEffect
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels()

    private val ratesAdapter: ExchangeRatesAdapter by lazy {
        ExchangeRatesAdapter()
    }

    private val layoutManager: LinearLayoutManager by lazy {
        LinearLayoutManager(this)
    }

    private val itemDecoration: ItemDecoration by lazy {
        DividerItemDecoration(this, layoutManager.orientation)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater, null, false)
        setContentView(binding.root)

        with(binding.ratesList) {
            this.layoutManager = this@MainActivity.layoutManager
            addItemDecoration(itemDecoration)
            adapter = ratesAdapter
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.state.collect { state ->
                        binding.loadingProgress.visibility = if (state.loading) View.VISIBLE else View.GONE
                        ratesAdapter.ratesList = state.rates
                    }
                }
                launch {
                    viewModel.effects.collect { effect ->
                        when (effect) {
                            is UIEffect.CompleteMessage -> showMessage("Loading Complete")
                            is UIEffect.ErrorMessage -> showMessage("Error: ${effect.message}")
                        }
                    }
                }
            }
        }

        binding.getRates.setOnClickListener {
            viewModel.postEvent(MainEvent.GetRates)
        }
    }

    private fun showMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
