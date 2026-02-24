package com.example.ee494_smart_energy.ui.home

import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.ee494_smart_energy.R
import com.example.ee494_smart_energy.model.Device
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.DividerItemDecoration

class HomeFragment : Fragment(R.layout.fragment_home) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recyclerView = view.findViewById<RecyclerView>(R.id.deviceRecyclerView)

        // Fake devices for now
        val deviceList = listOf(
            Device("Kitchen Plug", "192.168.1.10"),
            Device("Bedroom Plug", "192.168.1.11"),
            Device("Office Plug", "192.168.1.12"),
            Device("Add Device", "")
        )

        val adapter = DeviceAdapter(deviceList) { selectedDevice ->

            if (selectedDevice.name != "Add Device") {

                val bundle = Bundle()
                bundle.putString("deviceName", selectedDevice.name)

                findNavController().navigate(
                    R.id.deviceDetailFragment,
                    bundle
                )
            }
        }

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        val divider = DividerItemDecoration(
            requireContext(),
            DividerItemDecoration.VERTICAL
        )
        recyclerView.addItemDecoration(divider)

        val toolbar = view.findViewById<com.google.android.material.appbar.MaterialToolbar>(R.id.topToolbar)
        toolbar.title = "Devices"
        toolbar.isTitleCentered = true
        toolbar.setTitleTextColor(Color.WHITE)

    }
}