package com.example.ee494_smart_energy.ui.pricing

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.ee494_smart_energy.R
import com.example.ee494_smart_energy.api.RetrofitClient
import kotlinx.coroutines.launch

class PricingFragment : Fragment(R.layout.fragment_pricing) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val tvPricingDate = view.findViewById<TextView>(R.id.tvPricingDate)
        val tvMillwd = view.findViewById<TextView>(R.id.tvMillwd)
        val tvNyc = view.findViewById<TextView>(R.id.tvNyc)
        val tvDunwod = view.findViewById<TextView>(R.id.tvDunwod)

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.getLbmpData()

                tvPricingDate.text = "Date: ${response.marketDate}"

                val millwdRows = response.data.filter { it.name == "MILLWD" }
                val nycRows = response.data.filter { it.name == "N.Y.C." }
                val dunwodRows = response.data.filter { it.name == "DUNWOD" }

                tvMillwd.text = "MILLWD\n\n" + buildZoneText(millwdRows)
                tvNyc.text = "N.Y.C.\n\n" + buildZoneText(nycRows)
                tvDunwod.text = "DUNWOD\n\n" + buildZoneText(dunwodRows)

            } catch (e: Exception) {
                tvPricingDate.text = "Error loading pricing"
                tvMillwd.text = e.message ?: "Unknown error"
            }
        }
    }

    private fun buildZoneText(
        rows: List<com.example.ee494_smart_energy.api.LbmpRow>
    ): String {
        val builder = StringBuilder()

        for (row in rows) {
            val formattedTime = formatHour(row.hour)
            val pricePerKwh = row.lbmp?.div(1000.0)
            val formattedPrice = pricePerKwh?.let { String.format("$%.4f/kWh", it) } ?: "N/A"

            builder.append("$formattedTime  -  $formattedPrice\n")
        }

        return builder.toString()
    }

    private fun formatHour(timeValue: String?): String {
        if (timeValue == null) return "N/A"

        val cleanedTime = timeValue.trim()

        val parts = cleanedTime.split(" ")
        if (parts.size < 2) return cleanedTime

        val timePart = parts[1]   // example: 23:00
        val hourMinute = timePart.split(":")
        if (hourMinute.size < 2) return cleanedTime

        val hour24 = hourMinute[0].toIntOrNull() ?: return cleanedTime
        val minute = hourMinute[1]

        val period = if (hour24 < 12) "a.m." else "p.m."

        val hour12 = when {
            hour24 == 0 -> 12
            hour24 == 12 -> 12
            hour24 > 12 -> hour24 - 12
            else -> hour24
        }

        return "$hour12:$minute $period"
    }
}