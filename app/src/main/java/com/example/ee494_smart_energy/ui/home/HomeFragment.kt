package com.example.ee494_smart_energy.ui.home

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.*
import com.example.ee494_smart_energy.R
import com.example.ee494_smart_energy.model.Device
import com.example.ee494_smart_energy.ui.device.DeviceStorage
import com.google.android.material.appbar.MaterialToolbar

class HomeFragment : Fragment(R.layout.fragment_home) {

    private lateinit var recyclerView: RecyclerView
    private lateinit var deviceList: MutableList<Device>
    private lateinit var adapter: DeviceAdapter

    override fun onResume() {
        super.onResume()
        loadDevices()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupToolbar(view)
        setupRecycler(view)
        setupSwipeToDelete()
        loadDevices()
    }

    private fun setupRecycler(view: View) {
        recyclerView = view.findViewById(R.id.deviceRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        val divider = DividerItemDecoration(
            requireContext(),
            DividerItemDecoration.VERTICAL
        )
        recyclerView.addItemDecoration(divider)
    }

    private fun loadDevices() {
        val savedDevices = DeviceStorage.getDevices(requireContext())
        deviceList = savedDevices.toMutableList()

        // Add Device placeholder with safe unique ID
        deviceList.add(Device("ADD_BUTTON", "Add Device", ""))

        adapter = DeviceAdapter(
            deviceList,
            onClick = { selectedDevice ->
                if (selectedDevice.id == "ADD_BUTTON") {
                    findNavController().navigate(R.id.action_home_to_addDevice)                } else {
                    val bundle = Bundle()
                    bundle.putString("deviceName", selectedDevice.name)
                    bundle.putString("deviceIp", selectedDevice.ipAddress)
                    findNavController().navigate(R.id.action_home_to_deviceDetail, bundle)                }
            },
            onLongClick = { device ->
                if (device.id != "ADD_BUTTON") {
                    showRenameDialog(device)
                }
            }
        )

        recyclerView.adapter = adapter
    }

    private fun showRenameDialog(device: Device) {
        val editText = EditText(requireContext())
        editText.setText(device.name)

        android.app.AlertDialog.Builder(requireContext())
            .setTitle("Rename Device")
            .setView(editText)
            .setPositiveButton("Save") { _, _ ->
                val newName = editText.text.toString()
                if (newName.isNotBlank()) {
                    DeviceStorage.renameDevice(requireContext(), device, newName)
                    loadDevices()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun setupSwipeToDelete() {

        val itemTouchHelper = ItemTouchHelper(
            object : ItemTouchHelper.SimpleCallback(
                0,
                ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
            ) {

                override fun onMove(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder,
                    target: RecyclerView.ViewHolder
                ) = false

                override fun onSwiped(
                    viewHolder: RecyclerView.ViewHolder,
                    direction: Int
                ) {

                    val position = viewHolder.adapterPosition
                    val device = deviceList[position]

                    if (device.id == "ADD_BUTTON") {
                        adapter.notifyItemChanged(position)
                        return
                    }

                    showDeleteConfirmation(device)
                }
            }
        )

        itemTouchHelper.attachToRecyclerView(recyclerView)
    }

    private fun showDeleteConfirmation(device: Device) {

        android.app.AlertDialog.Builder(requireContext())
            .setTitle("Remove Device")
            .setMessage("Are you sure you want to remove ${device.name}?")
            .setPositiveButton("Remove") { _, _ ->
                DeviceStorage.removeDevice(requireContext(), device)
                loadDevices()
            }
            .setNegativeButton("Cancel") { _, _ ->
                loadDevices()
            }
            .show()
    }

    private fun setupToolbar(view: View) {
        val toolbar = view.findViewById<MaterialToolbar>(R.id.topToolbar)
        toolbar.title = "Devices"
        toolbar.isTitleCentered = true
        toolbar.setTitleTextColor(Color.WHITE)
    }
}