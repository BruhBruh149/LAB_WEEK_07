package com.example.lab_week_07

import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import android.Manifest
import androidx.activity.result.ActivityResultLauncher

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

        // Initialize the map fragment
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // Register for activity result
        requestPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            if (isGranted) {
                // If granted by the user, execute the necessary function
                getLastLocation()
            } else {
                // If not granted, show a rationale dialog
                showPermissionRationale {
                    requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                }
            }
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // Add a default marker (Sydney) untuk testing
        val sydney = LatLng(-34.0, 151.0)
        mMap.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney))

        // OnMapReady is called when the map is ready to be used
        // Check for location permission
        when {
            hasLocationPermission() -> {
                getLastLocation()
            }
            shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION) -> {
                showPermissionRationale {
                    requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                }
            }
            else -> {
                requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }
    }
    private fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }
    private fun showPermissionRationale(positiveAction: () -> Unit) {
        AlertDialog.Builder(this)
            .setTitle("Location Permission Required")
            .setMessage("This app needs location permission to show your current location on the map.")
            .setPositiveButton("OK") { _, _ ->
                positiveAction()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .create()
            .show()
    }

    private fun getLastLocation() {
        Log.d("MapsActivity", "getLastLocation() called - Permission granted!")
        val exampleLocation = LatLng(-33.8688, 151.2093) // Sydney coordinates
        mMap.addMarker(
            MarkerOptions()
                .position(exampleLocation)
                .title("Location Permission Granted!")
                .snippet("Your location would be shown here")
        )
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(exampleLocation, 12f))
        if (hasLocationPermission()) {
            try {
                mMap.isMyLocationEnabled = true
            } catch (e: SecurityException) {
                Log.e("MapsActivity", "Error enabling location layer: ${e.message}")
            }
        }
    }
}