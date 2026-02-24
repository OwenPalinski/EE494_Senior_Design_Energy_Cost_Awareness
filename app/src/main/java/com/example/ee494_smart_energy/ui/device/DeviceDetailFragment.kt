package com.example.ee494_smart_energy.ui.device

import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.ee494_smart_energy.R
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.components.XAxis
import kotlin.random.Random

class DeviceDetailFragment : Fragment(R.layout.fragment_device_detail) {

    private val handler = Handler(Looper.getMainLooper())

    private lateinit var lineChart: LineChart
    private lateinit var powerSet: LineDataSet

    private lateinit var connectionDot: View

    private var timeIndex = 0f
    private var totalEnergyKWh = 0.0
    private var isConnected = true   // simulate connection state

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // ===== Custom Top Bar Setup =====
        val backButton = view.findViewById<ImageView>(R.id.backButton)
        val titleText = view.findViewById<TextView>(R.id.deviceTitleText)
        connectionDot = view.findViewById(R.id.connectionDot)

        val deviceName = arguments?.getString("deviceName") ?: "Device"
        titleText.text = deviceName

        backButton.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        updateConnectionDot(isConnected)

        // ===== Chart Setup =====
        lineChart = view.findViewById(R.id.lineChart)

        powerSet = LineDataSet(ArrayList(), "Power (W)")
        powerSet.color = Color.BLACK
        powerSet.setDrawCircles(false)
        powerSet.lineWidth = 2f

        val lineData = LineData(powerSet)
        lineChart.data = lineData

        lineChart.description.isEnabled = false
        lineChart.axisRight.isEnabled = false
        lineChart.xAxis.position = XAxis.XAxisPosition.BOTTOM
        lineChart.legend.textColor = Color.DKGRAY

        lineChart.invalidate()

        // ===== Start Fake Data Updates =====
        val runnable = object : Runnable {
            override fun run() {
                generateFakePzemData()
                handler.postDelayed(this, 2000)
            }
        }

        handler.post(runnable)
    }

    private fun updateConnectionDot(connected: Boolean) {
        if (connected) {
            connectionDot.setBackgroundResource(R.drawable.status_connected_circle)
        } else {
            connectionDot.setBackgroundResource(R.drawable.status_disconnected_circle)
        }
    }

    private fun generateFakePzemData() {

        val voltage = 120 + Random.nextDouble(-1.5, 1.5)
        val current = 1.8 + Random.nextDouble(-0.4, 0.4)
        val powerFactor = 0.90 + Random.nextDouble(-0.05, 0.05)

        val power = voltage * current * powerFactor

        totalEnergyKWh += (power / 1000.0) * (2.0 / 3600.0)

        view?.findViewById<TextView>(R.id.voltageText)?.text =
            "Voltage: %.2f V".format(voltage)

        view?.findViewById<TextView>(R.id.currentText)?.text =
            "Current: %.2f A".format(current)

        view?.findViewById<TextView>(R.id.powerText)?.text =
            "Power: %.2f W".format(power)

        view?.findViewById<TextView>(R.id.energyText)?.text =
            "Energy: %.4f kWh".format(totalEnergyKWh)

        view?.findViewById<TextView>(R.id.pfText)?.text =
            "Power Factor: %.2f".format(powerFactor)

        addPowerEntry(power.toFloat())
    }

    private fun addPowerEntry(value: Float) {

        val data = lineChart.data ?: return

        data.addEntry(Entry(timeIndex++, value), 0)
        data.notifyDataChanged()

        lineChart.notifyDataSetChanged()
        lineChart.setVisibleXRangeMaximum(20f)
        lineChart.moveViewToX(timeIndex)

        lineChart.invalidate()
    }
}