package com.tc.exchangerates.view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.tc.exchangerates.R
import com.tc.exchangerates.databinding.ListItemExchangeRateBinding
import com.tc.exchangerates.model.ExchangeRate

class ExchangeRatesAdapter : RecyclerView.Adapter<ExchangeRatesAdapter.ViewHolder>() {
    var ratesList: List<ExchangeRate> = emptyList()
        set(value) {
            notifyItemRangeRemoved(0, ratesList.size)
            field = value
            notifyItemRangeInserted(0, ratesList.size)
        }

    class ViewHolder(private val binding: ListItemExchangeRateBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(rateValue: ExchangeRate) {
            with(binding) {
                currencySymbol.text = rateValue.currencySymbol
                symbol.text = rateValue.rateUsd.toDouble().let { rate ->
                    if (rate < 1) {
                        itemView.context.getString(R.string.exchange_rate, rateValue.symbol, "USD", 1 / rate)
                    } else {
                        itemView.context.getString(R.string.exchange_rate, "USD", rateValue.symbol, rate)
                    }
                }
                ContextCompat.getColor(
                    itemView.context,
                    if (rateValue.type == "fiat") R.color.teal_200 else R.color.purple_200
                ).run {
                    root.setBackgroundColor(this)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ListItemExchangeRateBinding.inflate(LayoutInflater.from(parent.context), parent, false).run {
            ViewHolder(this)
        }
    }

    override fun getItemCount(): Int = ratesList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(ratesList[position])
    }
}
