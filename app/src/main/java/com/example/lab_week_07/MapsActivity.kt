package com.example.lab_week_07
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import android.Manifest

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private val LOCATION_PERMISSION_REQUEST_CODE = 1001
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        val sydney = LatLng(-34.0, 151.0)
        mMap.addMarker(MarkerOptions().position(sydney).title("Default Location - Sydney"))
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sydney, 10f))
        checkLocationPermission()
    }

    private fun checkLocationPermission() {
        when {
            hasLocationPermission() -> {
                getLastLocation()
            }
            ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) -> {
                showPermissionRationale()
            }
            else -> {
                requestLocationPermission()
            }
        }
    }

    private fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun showPermissionRationale() {
        AlertDialog.Builder(this)
            .setTitle("Location Permission Required")
            .setMessage("This app needs location permission to show your current location on the map.")
            .setPositiveButton("OK") { _, _ ->
                requestLocationPermission()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
                showLimitedFunctionalityMessage()
            }
            .create()
            .show()
    }

    private fun showLimitedFunctionalityMessage() {
        AlertDialog.Builder(this)
            .setTitle("Limited Functionality")
            .setMessage("The app will work with limited functionality without location permission.")
            .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
            .create()
            .show()
    }

    private fun requestLocationPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            LOCATION_PERMISSION_REQUEST_CODE
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            LOCATION_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getLastLocation()
                } else {
                    Log.d("MapsActivity", "Location permission denied")
                    showLimitedFunctionalityMessage()
                }
            }
        }
    }
    private fun getLastLocation() {
        if (hasLocationPermission()) {
            try {
                fusedLocationClient.lastLocation
                    .addOnSuccessListener { location: Location? ->
                        location?.let {
                            val userLocation = LatLng(location.latitude, location.longitude)
                            updateMapLocation(userLocation)
                            addMarkerAtLocation(userLocation, "You are here!")

                            Log.d("MapsActivity", "Location found: ${location.latitude}, ${location.longitude}")
                        } ?: run {
                            // Location is null, show error
                            Log.d("MapsActivity", "Location is null")
                            showLocationUnavailableMessage()
                        }
                    }
                    .addOnFailureListener { e ->
                        Log.e("MapsActivity", "Error getting location: ${e.message}")
                        showLocationErrorMessage()
                    }
            } catch (e: SecurityException) {
                Log.e("MapsActivity", "SecurityException: ${e.message}")
            }
        } else {
            Log.d("MapsActivity", "Location permission not granted")
        }
    }

    private fun updateMapLocation(location: LatLng) {
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 15f))
        if (hasLocationPermission()) {
            try {
                mMap.isMyLocationEnabled = true
                mMap.uiSettings.isMyLocationButtonEnabled = true
                mMap.uiSettings.isZoomControlsEnabled = true
            } catch (e: SecurityException) {
                Log.e("MapsActivity", "Error enabling location layer: ${e.message}")
            }
        }
    }
    private fun addMarkerAtLocation(location: LatLng, title: String) {
        mMap.clear() // Clear previous markers
        mMap.addMarker(
            MarkerOptions()
                .position(location)
                .title(title)
                .snippet("Lat: ${location.latitude}, Lng: ${location.longitude}")
        )
    }

    private fun showLocationUnavailableMessage() {
        AlertDialog.Builder(this)
            .setTitle("Location Unavailable")
            .setMessage("Unable to get your current location. Please make sure location services are enabled.")
            .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
            .create()
            .show()
    }

    private fun showLocationErrorMessage() {
        AlertDialog.Builder(this)
            .setTitle("Location Error")
            .setMessage("There was an error getting your location. Please try again.")
            .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
            .create()
            .show()
    }

    override fun onResume() {
        super.onResume()
        if (hasLocationPermission()) {
            getLastLocation()
        }
    }
}