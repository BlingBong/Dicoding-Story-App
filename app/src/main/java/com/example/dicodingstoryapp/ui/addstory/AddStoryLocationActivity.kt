package com.example.dicodingstoryapp.ui.addstory

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.location.Location
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.dicodingstoryapp.R
import com.example.dicodingstoryapp.databinding.ActivityAddStoryLocationBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*

class AddStoryLocationActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityAddStoryLocationBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAddStoryLocationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        setupUI()
        getMyLastLocation()
        setMapStyle()
    }

    private fun setupUI() {
        mMap.uiSettings.isZoomControlsEnabled = true
        mMap.uiSettings.isIndoorLevelPickerEnabled = true
        mMap.uiSettings.isCompassEnabled = true
        mMap.uiSettings.isMapToolbarEnabled = true
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.map_options, menu)

        menu.findItem(R.id.menu_story_list).isVisible = false
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_translate -> {
                startActivity(Intent(Settings.ACTION_LOCALE_SETTINGS))

                true
            }
            R.id.normal_type -> {
                mMap.mapType = GoogleMap.MAP_TYPE_NORMAL
                true
            }
            R.id.satellite_type -> {
                mMap.mapType = GoogleMap.MAP_TYPE_SATELLITE
                true
            }
            R.id.terrain_type -> {
                mMap.mapType = GoogleMap.MAP_TYPE_TERRAIN
                true
            }
            R.id.hybrid_type -> {
                mMap.mapType = GoogleMap.MAP_TYPE_HYBRID
                true
            }
            else -> {
                super.onOptionsItemSelected(item)
            }
        }
    }

    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            when {
                permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false -> {
                    // Precise location access granted.
                    getMyLastLocation()
                }
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false -> {
                    // Only approximate location access granted.
                    getMyLastLocation()
                }
                else -> {
                    // No location access granted.
                    Toast.makeText(
                        this,
                        getString(R.string.no_permission),
                        Toast.LENGTH_SHORT
                    ).show()
                    finish()
                }
            }
        }

    private fun checkPermission(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            permission
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun getMyLastLocation() {
        if (checkPermission(Manifest.permission.ACCESS_FINE_LOCATION) &&
            checkPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
        ) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                if (location != null) {
                    showMarker(location)
                } else {
                    Toast.makeText(
                        this,
                        getString(R.string.loc_not_found),
                        Toast.LENGTH_SHORT
                    ).show()

                }
            }
        } else {
            requestPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    private fun showMarker(location: Location) {
        var marker: Marker?
        var latitude: Double
        var longitude: Double

        // current location
        val currentLocation = LatLng(location.latitude, location.longitude)

        latitude = location.latitude
        longitude = location.longitude

        marker = mMap.addMarker(
            MarkerOptions()
                .position(currentLocation)
                .title(getString(R.string.current_location))
                .snippet("Lat: ${location.latitude} Lon: ${location.longitude}")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
        )
        marker?.showInfoWindow()
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15f))

        // clicked location
        mMap.setOnMapClickListener { latLng ->
            marker?.remove()

            latitude = latLng.latitude
            longitude = latLng.longitude

            marker = mMap.addMarker(
                MarkerOptions()
                    .position(latLng)
                    .title(getString(R.string.custom_location))
                    .snippet("Lat: ${latLng.latitude} Lon: ${latLng.longitude}")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
            )
            marker?.showInfoWindow()
        }

        // clicked poi
        mMap.setOnPoiClickListener { pointOfInterest ->
            marker?.remove()

            latitude = pointOfInterest.latLng.latitude
            longitude = pointOfInterest.latLng.longitude

            marker = mMap.addMarker(
                MarkerOptions()
                    .position(pointOfInterest.latLng)
                    .title(pointOfInterest.name)
                    .snippet("Lat: ${pointOfInterest.latLng.latitude} Lon: ${pointOfInterest.latLng.longitude}")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
            )
            marker?.showInfoWindow()
        }

        binding.civDetect.setOnClickListener {
            latitude = location.latitude
            longitude = location.longitude

            marker?.remove()

            marker = mMap.addMarker(
                MarkerOptions()
                    .position(currentLocation)
                    .title(getString(R.string.current_location))
                    .snippet("Lat: ${location.latitude} Lon: ${location.longitude}")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
            )
            marker?.showInfoWindow()
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15f))
        }

        binding.btnChoose.setOnClickListener {
            val intent = Intent()
            intent.putExtra("lat", latitude)
            intent.putExtra("lon", longitude)
            setResult(AddStoryActivity.LOCATION_RESULT, intent)
            finish()
        }
    }

    private fun setMapStyle() {
        try {
            val success =
                mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.map_style))
            if (!success) {
                Log.e(TAG, getString(R.string.style_failed))
            }
        } catch (exception: Resources.NotFoundException) {
            Log.e(TAG, getString(R.string.style_not_found), exception)
        }
    }

    companion object {
        private const val TAG = "AddStoryLocActivity"
    }
}