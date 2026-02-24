package com.example.ee494_smart_energy.ui.device

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.ee494_smart_energy.R
import com.example.ee494_smart_energy.model.Device
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException
import java.util.UUID

class AddDeviceFragment : Fragment(R.layout.fragment_add_device) {

    private val client = OkHttpClient()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val toolbar = view.findViewById<MaterialToolbar>(R.id.addDeviceToolbar)
        val ssidEditText = view.findViewById<TextInputEditText>(R.id.ssidEditText)
        val passwordEditText = view.findViewById<TextInputEditText>(R.id.passwordEditText)
        val provisionButton = view.findViewById<MaterialButton>(R.id.provisionButton)

        toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        provisionButton.setOnClickListener {

            val ssid = ssidEditText.text.toString()
            val password = passwordEditText.text.toString()

            if (ssid.isBlank() || password.isBlank()) {
                Toast.makeText(requireContext(), "Please enter SSID and password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            sendProvisionRequest(ssid, password)
        }
    }

    private fun sendProvisionRequest(ssid: String, password: String) {

        val json = """
            {
                "ssid": "$ssid",
                "password": "$password"
            }
        """.trimIndent()

        val body = json.toRequestBody("application/json".toMediaType())

        val request = Request.Builder()
            .url("http://192.168.4.1/provision")
            .post(body)
            .build()

        client.newCall(request).enqueue(object : Callback {

            override fun onFailure(call: Call, e: IOException) {
                requireActivity().runOnUiThread {
                    Toast.makeText(requireContext(), "Connection failed", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {

                val responseBody = response.body?.string()

                requireActivity().runOnUiThread {

                    if (response.isSuccessful && responseBody != null) {

                        val jsonResponse = JSONObject(responseBody)
                        val status = jsonResponse.getString("status")

                        if (status == "connected") {

                            val ip = jsonResponse.getString("ip")

                            val device = Device(
                                id = UUID.randomUUID().toString(),
                                name = "Smart Plug ${System.currentTimeMillis() % 1000}",
                                ipAddress = ip
                            )

                            DeviceStorage.saveDevice(requireContext(), device)

                            Toast.makeText(requireContext(), "Device Connected!", Toast.LENGTH_SHORT).show()

                            findNavController().navigateUp()

                        } else {
                            Toast.makeText(requireContext(), "Provisioning failed", Toast.LENGTH_SHORT).show()
                        }

                    } else {
                        Toast.makeText(requireContext(), "Provisioning error", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }
}