package com.example.ee494_smart_energy.ui.notifications

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.ee494_smart_energy.api.RetrofitClient
import com.example.ee494_smart_energy.databinding.FragmentNotificationsBinding
import kotlinx.coroutines.launch

class NotificationsFragment : Fragment() {

    private var _binding: FragmentNotificationsBinding? = null
    private val binding get() = _binding!!

    private val zoneOptions = listOf("MILLWD", "N.Y.C.", "DUNWOD")
    private var deviceOptions = listOf<String>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNotificationsBinding.inflate(inflater, container, false)
        val root: View = binding.root

        setupZoneSpinner()
        loadDeviceSpinner()
        loadSavedSettings()
        setupSaveButton()

        return root
    }

    private fun setupZoneSpinner() {
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            zoneOptions
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerZone.adapter = adapter
    }

    private fun loadDeviceSpinner() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val devices = RetrofitClient.apiService.getDeviceIds()

                deviceOptions = if (devices.isNotEmpty()) {
                    devices
                } else {
                    listOf("No devices found")
                }

                val adapter = ArrayAdapter(
                    requireContext(),
                    android.R.layout.simple_spinner_item,
                    deviceOptions
                )
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                binding.spinnerDevice.adapter = adapter

                loadSavedDeviceSelection()

            } catch (e: Exception) {
                deviceOptions = listOf("Error loading devices")

                val adapter = ArrayAdapter(
                    requireContext(),
                    android.R.layout.simple_spinner_item,
                    deviceOptions
                )
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                binding.spinnerDevice.adapter = adapter
            }
        }
    }

    private fun loadSavedSettings() {
        val sharedPrefs = requireActivity().getSharedPreferences("app_settings", Context.MODE_PRIVATE)

        val savedZone = sharedPrefs.getString("selected_zone", "MILLWD")
        val savedRuntime = sharedPrefs.getFloat("runtime_hours", 1.0f)

        val zoneIndex = zoneOptions.indexOf(savedZone)
        if (zoneIndex >= 0) {
            binding.spinnerZone.setSelection(zoneIndex)
        }

        binding.etRuntimeHours.setText(savedRuntime.toString())
    }

    private fun loadSavedDeviceSelection() {
        val sharedPrefs = requireActivity().getSharedPreferences("app_settings", Context.MODE_PRIVATE)
        val savedDevice = sharedPrefs.getString("selected_device", null)

        if (savedDevice != null) {
            val deviceIndex = deviceOptions.indexOf(savedDevice)
            if (deviceIndex >= 0) {
                binding.spinnerDevice.setSelection(deviceIndex)
            }
        }
    }

    private fun setupSaveButton() {
        binding.btnSaveSettings.setOnClickListener {
            val selectedZone = binding.spinnerZone.selectedItem.toString()
            val selectedDevice = binding.spinnerDevice.selectedItem?.toString() ?: ""
            val runtimeText = binding.etRuntimeHours.text.toString().trim()

            if (runtimeText.isEmpty()) {
                binding.tvSavedMessage.text = "Please enter a run time."
                return@setOnClickListener
            }

            val runtimeHours = runtimeText.toFloatOrNull()
            if (runtimeHours == null || runtimeHours <= 0f) {
                binding.tvSavedMessage.text = "Enter a valid number greater than 0."
                return@setOnClickListener
            }

            if (selectedDevice == "No devices found" || selectedDevice == "Error loading devices") {
                binding.tvSavedMessage.text = "Please load a valid device."
                return@setOnClickListener
            }

            val sharedPrefs = requireActivity().getSharedPreferences("app_settings", Context.MODE_PRIVATE)
            with(sharedPrefs.edit()) {
                putString("selected_zone", selectedZone)
                putString("selected_device", selectedDevice)
                putFloat("runtime_hours", runtimeHours)
                apply()
            }

            binding.tvSavedMessage.text = "Settings saved."
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}