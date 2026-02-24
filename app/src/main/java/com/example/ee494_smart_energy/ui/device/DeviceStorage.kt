package com.example.ee494_smart_energy.ui.device

import android.content.Context
import com.example.ee494_smart_energy.model.Device
import org.json.JSONArray
import org.json.JSONObject

object DeviceStorage {

    private const val PREF_NAME = "device_prefs"
    private const val KEY_DEVICES = "devices"

    fun getDevices(context: Context): MutableList<Device> {
        val jsonString = context
            .getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .getString(KEY_DEVICES, null)
            ?: return mutableListOf()

        val jsonArray = JSONArray(jsonString)
        val list = mutableListOf<Device>()

        for (i in 0 until jsonArray.length()) {
            val obj = jsonArray.getJSONObject(i)
            list.add(
                Device(
                    id = obj.getString("id"),
                    name = obj.getString("name"),
                    ipAddress = obj.getString("ip")
                )
            )
        }

        return list
    }

    private fun saveAll(context: Context, devices: List<Device>) {
        val jsonArray = JSONArray()

        devices.forEach {
            val obj = JSONObject()
            obj.put("id", it.id)
            obj.put("name", it.name)
            obj.put("ip", it.ipAddress)
            jsonArray.put(obj)
        }

        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_DEVICES, jsonArray.toString())
            .apply()
    }

    fun saveDevice(context: Context, device: Device) {
        val devices = getDevices(context)
        devices.add(device)
        saveAll(context, devices)
    }

    fun removeDevice(context: Context, device: Device) {
        val devices = getDevices(context)
        devices.removeAll { it.id == device.id }
        saveAll(context, devices)
    }

    fun renameDevice(context: Context, device: Device, newName: String) {
        val devices = getDevices(context)
        devices.find { it.id == device.id }?.name = newName
        saveAll(context, devices)
    }
}