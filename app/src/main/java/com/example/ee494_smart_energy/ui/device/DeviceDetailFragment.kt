package com.example.ee494_smart_energy.ui.device

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.ee494_smart_energy.R
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class DeviceDetailFragment : Fragment(R.layout.fragment_device_detail) {

    private lateinit var connectionDot: View
    private lateinit var loadingSpinner: ProgressBar
    private lateinit var lastSeenText: TextView

    private lateinit var deviceIp: String

    private val client = OkHttpClient()

    private val handler = Handler(Looper.getMainLooper())

    private val pollRunnable = object : Runnable {
        override fun run() {
            if (deviceIp.isNotBlank()) {
                checkDeviceConnection(deviceIp)
            }
            handler.postDelayed(this, 3000)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val backButton = view.findViewById<ImageView>(R.id.backButton)
        val titleText = view.findViewById<TextView>(R.id.deviceTitleText)

        connectionDot = view.findViewById(R.id.connectionDot)
        loadingSpinner = view.findViewById(R.id.connectionLoading)
        lastSeenText = view.findViewById(R.id.lastSeenText)

        val deviceName = arguments?.getString("deviceName") ?: "Device"
        deviceIp = arguments?.getString("deviceIp") ?: ""

        titleText.text = deviceName

        backButton.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        setDisconnectedUI()

        // Start polling AFTER deviceIp is initialized
        handler.post(pollRunnable)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        handler.removeCallbacks(pollRunnable)
    }

    private fun checkDeviceConnection(ip: String) {

        showLoading(true)

        val request = Request.Builder()
            .url("http://$ip/status")
            .build()

        client.newCall(request).enqueue(object : okhttp3.Callback {

            override fun onFailure(call: okhttp3.Call, e: IOException) {
                requireActivity().runOnUiThread {
                    showLoading(false)
                    setDisconnectedUI()
                }
            }

            override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                requireActivity().runOnUiThread {
                    showLoading(false)
                    if (response.isSuccessful) {
                        setConnectedUI()
                    } else {
                        setDisconnectedUI()
                    }
                }
            }
        })
    }

    private fun setConnectedUI() {
        updateConnectionDot(true)
        updateLastSeen()
    }

    private fun setDisconnectedUI() {
        updateConnectionDot(false)

        view?.findViewById<TextView>(R.id.voltageText)?.text = "Voltage: 0.00 V"
        view?.findViewById<TextView>(R.id.currentText)?.text = "Current: 0.00 A"
        view?.findViewById<TextView>(R.id.powerText)?.text = "Power: 0.00 W"
        view?.findViewById<TextView>(R.id.energyText)?.text = "Energy: 0.0000 kWh"
        view?.findViewById<TextView>(R.id.pfText)?.text = "Power Factor: 0.00"
    }

    private fun updateConnectionDot(connected: Boolean) {
        if (connected) {
            connectionDot.setBackgroundResource(R.drawable.status_connected_circle)
        } else {
            connectionDot.setBackgroundResource(R.drawable.status_disconnected_circle)
        }
    }

    private fun showLoading(show: Boolean) {
        loadingSpinner.visibility = if (show) View.VISIBLE else View.GONE
    }

    private fun updateLastSeen() {
        val sdf = SimpleDateFormat("MMM dd, HH:mm:ss", Locale.getDefault())
        lastSeenText.text = "Last seen: ${sdf.format(Date())}"
    }
}