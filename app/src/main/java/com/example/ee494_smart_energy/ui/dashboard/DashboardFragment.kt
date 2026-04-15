package com.example.ee494_smart_energy.ui.dashboard

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.ee494_smart_energy.R
import com.example.ee494_smart_energy.api.LbmpRow
import com.example.ee494_smart_energy.api.RetrofitClient
import com.example.ee494_smart_energy.databinding.FragmentDashboardBinding
import kotlinx.coroutines.launch
import java.util.Calendar

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        val root: View = binding.root

        binding.btnOpenPricing.setOnClickListener {
            findNavController().navigate(R.id.navigation_pricing)
        }

        loadDashboardData()

        return root
    }

    private fun loadDashboardData() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val sharedPrefs = requireActivity().getSharedPreferences("app_settings", Context.MODE_PRIVATE)
                val selectedZone = sharedPrefs.getString("selected_zone", "MILLWD") ?: "MILLWD"
                val runtimeHours = sharedPrefs.getFloat("runtime_hours", 1.0f).toDouble()
                val selectedDevice = sharedPrefs.getString("selected_device", "") ?: ""

                val powerReadings = RetrofitClient.apiService.getPowerReadings()
                val lbmpResponse = RetrofitClient.apiService.getLbmpData()

                if (powerReadings.isEmpty()) {
                    binding.tvCurrentPower.text = "Current Power: no data"
                    binding.tvCurrentCost.text = "Current Cost: no data"
                    binding.tvTotalRunCost.text = "Total Run Cost: no data"
                    binding.tvRecommendations.text = "No recommendations available"
                    return@launch
                }

                val selectedDeviceReadings = powerReadings.filter { it.device_id == selectedDevice }

                if (selectedDeviceReadings.isEmpty()) {
                    binding.tvCurrentPower.text = "Current Power: no data for selected device"
                    binding.tvCurrentCost.text = "Current Cost: unavailable"
                    binding.tvTotalRunCost.text = "Total Run Cost: unavailable"
                    binding.tvRecommendations.text = "No recommendations available"
                    return@launch
                }

                val latestReading = selectedDeviceReadings.first()
                val zoneRows = lbmpResponse.data.filter { it.name == selectedZone }

                binding.tvCurrentPower.text =
                    "Current Power: ${String.format("%.1f", latestReading.power)} W"

                if (zoneRows.isEmpty()) {
                    binding.tvCurrentCost.text = "Current Cost: zone data not found"
                    binding.tvTotalRunCost.text = "Total Run Cost: zone data not found"
                    binding.tvRecommendations.text = "No recommendations available for $selectedZone"
                    return@launch
                }

                val currentHourRow = findMatchingHourRow(zoneRows)

                if (currentHourRow != null && currentHourRow.lbmp != null) {
                    val currentCostPerHour = calculateHourlyRunCost(
                        latestReading.power,
                        currentHourRow.lbmp
                    )

                    binding.tvCurrentCost.text =
                        "Current Cost: $${String.format("%.4f", currentCostPerHour)}/hour"

                    val totalRunCost = calculateTotalRunCost(
                        latestReading.power,
                        currentHourRow.lbmp,
                        runtimeHours
                    )

                    binding.tvTotalRunCost.text =
                        "Total Run Cost: $${String.format("%.4f", totalRunCost)}"
                } else {
                    binding.tvCurrentCost.text = "Current Cost: unavailable"
                    binding.tvTotalRunCost.text = "Total Run Cost: unavailable"
                }

                binding.tvRecommendations.text = buildRecommendations(
                    latestReading.power,
                    runtimeHours,
                    zoneRows
                )

            } catch (e: Exception) {
                binding.tvCurrentPower.text = "Current Power: error"
                binding.tvCurrentCost.text = "Current Cost: error"
                binding.tvTotalRunCost.text = "Total Run Cost: error"
                binding.tvRecommendations.text = "Error loading recommendations: ${e.message}"
            }
        }
    }

    private fun calculateHourlyRunCost(powerWatts: Double, lbmpPerMwh: Double): Double {
        val powerKw = powerWatts / 1000.0
        val pricePerKwh = lbmpPerMwh / 1000.0
        return powerKw * pricePerKwh
    }

    private fun calculateTotalRunCost(
        powerWatts: Double,
        lbmpPerMwh: Double,
        runtimeHours: Double
    ): Double {
        val powerKw = powerWatts / 1000.0
        val pricePerKwh = lbmpPerMwh / 1000.0
        return powerKw * pricePerKwh * runtimeHours
    }

    private fun buildRecommendations(
        powerWatts: Double,
        runtimeHours: Double,
        zoneRows: List<LbmpRow>
    ): String {
        val validRows = zoneRows.filter { it.lbmp != null }

        if (validRows.isEmpty()) {
            return "No pricing data available"
        }

        val recommendationList = validRows.map { row ->
            val totalCost = calculateTotalRunCost(
                powerWatts,
                row.lbmp!!,
                runtimeHours
            )
            Pair(row, totalCost)
        }.sortedBy { it.second }

        val top3 = recommendationList.take(3)

        val builder = StringBuilder()
        builder.append("Best start times for ${String.format("%.2f", runtimeHours)} hour(s):\n\n")

        top3.forEachIndexed { index, pair ->
            val row = pair.first
            val totalCost = pair.second

            builder.append(
                "${index + 1}. ${formatHour(row.hour)} - $${String.format("%.4f", totalCost)}\n"
            )
        }

        return builder.toString()
    }

    private fun findMatchingHourRow(zoneRows: List<LbmpRow>): LbmpRow? {
        val currentHour24 = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)

        return zoneRows.find { row ->
            extractHour24(row.hour) == currentHour24
        }
    }

    private fun extractHour24(timeValue: String?): Int? {
        if (timeValue == null) return null

        val cleanedTime = timeValue.trim()
        val parts = cleanedTime.split(" ")
        if (parts.size < 2) return cleanedTime.toIntOrNull()

        val timePart = parts[1]
        val hourMinute = timePart.split(":")
        if (hourMinute.isEmpty()) return null

        return hourMinute[0].toIntOrNull()
    }

    private fun formatHour(timeValue: String?): String {
        val hour24 = extractHour24(timeValue) ?: return "N/A"

        val period = if (hour24 < 12) "a.m." else "p.m."
        val hour12 = when {
            hour24 == 0 -> 12
            hour24 == 12 -> 12
            hour24 > 12 -> hour24 - 12
            else -> hour24
        }

        return "$hour12:00 $period"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}