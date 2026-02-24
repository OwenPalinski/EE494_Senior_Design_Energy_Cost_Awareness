package com.example.ee494_smart_energy.ui.home

import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.ee494_smart_energy.R
import com.example.ee494_smart_energy.model.Device

class DeviceAdapter(
    private val devices: List<Device>,
    private val onClick: (Device) -> Unit
) : RecyclerView.Adapter<DeviceAdapter.DeviceViewHolder>() {

    inner class DeviceViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {

        val nameText: TextView = itemView.findViewById(R.id.deviceNameText)
        val ipText: TextView = itemView.findViewById(R.id.deviceIpText)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeviceViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_device, parent, false)
        return DeviceViewHolder(view)
    }

    override fun onBindViewHolder(holder: DeviceViewHolder, position: Int) {
        val device = devices[position]

        if (device.name == "Add Device") {

            // Style for Add Device row
            holder.nameText.text = "+ Add Device"
            holder.nameText.gravity = Gravity.CENTER
            holder.nameText.textSize = 18f
            holder.nameText.setPadding(0, 32, 0, 32)

            holder.ipText.visibility = View.GONE

        } else {

            // Normal device row
            holder.nameText.text = device.name
            holder.nameText.gravity = Gravity.START
            holder.nameText.textSize = 16f
            holder.nameText.setPadding(0, 0, 0, 0)

            holder.ipText.visibility = View.VISIBLE
            holder.ipText.text = device.ipAddress
        }

        holder.itemView.setOnClickListener {
            onClick(device)
        }
    }

    override fun getItemCount(): Int = devices.size
}