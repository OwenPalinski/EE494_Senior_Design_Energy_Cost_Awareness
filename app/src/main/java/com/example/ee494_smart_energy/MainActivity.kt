package com.example.ee494_smart_energy

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import android.util.Log
import androidx.lifecycle.lifecycleScope
import com.example.ee494_smart_energy.api.RetrofitClient
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val navView: BottomNavigationView = findViewById(R.id.nav_view)
        val navController = findNavController(R.id.nav_host_fragment_activity_main)

        navView.setupWithNavController(navController)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.deviceDetailFragment -> navView.visibility = View.GONE
                else -> navView.visibility = View.VISIBLE
            }
        }
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.getLbmpData()
                Log.d("NYISO_API", "Market Date: ${response.marketDate}")
                Log.d("NYISO_API", "Rows: ${response.totalRows}")

                if (response.data.isNotEmpty()) {
                    Log.d("NYISO_API", "First row: ${response.data[0]}")
                }
            } catch (e: Exception) {
                Log.e("NYISO_API", "Error: ${e.message}")
            }
        }
    }
}