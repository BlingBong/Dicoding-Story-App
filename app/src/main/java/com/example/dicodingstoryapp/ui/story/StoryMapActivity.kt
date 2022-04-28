package com.example.dicodingstoryapp.ui.story

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.dicodingstoryapp.R
import com.example.dicodingstoryapp.databinding.ActivityStoryMapBinding
import com.example.dicodingstoryapp.utils.ApiCallbackString
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*


class StoryMapActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var storyMapBinding: ActivityStoryMapBinding
    private val storyMapViewModel: StoryMapViewModel by viewModels()
    private var boundsBuilder = LatLngBounds.Builder()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        storyMapBinding = ActivityStoryMapBinding.inflate(layoutInflater)
        setContentView(storyMapBinding.root)

        val actionBar = supportActionBar
        if (actionBar != null) {
            title = getString(R.string.story_map)
        }

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        setupUI()
        setupViewModel()
        getMyLocation()
        setMapStyle()
    }

    private fun setupUI() {
        mMap.uiSettings.isZoomControlsEnabled = true
        mMap.uiSettings.isIndoorLevelPickerEnabled = true
        mMap.uiSettings.isCompassEnabled = true
        mMap.uiSettings.isMapToolbarEnabled = true
    }

    private fun setupViewModel() {
        storyMapViewModel.getUser().observe(this) {
            noData(true)
            storyMapViewModel.getAllStoriesWithMap(
                it.token,
                object : ApiCallbackString {
                    override fun responseState(success: Boolean, message: String) {
                        if (!success) {
                            AlertDialog.Builder(this@StoryMapActivity).apply {
                                setTitle(getString(R.string.failed))
                                setMessage(getString(R.string.fail_fetch))
                                setPositiveButton(getString(R.string.cont)) { _, _ ->
                                    val intent = Intent(context, StoryListActivity::class.java)
                                    intent.flags =
                                        Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                                    startActivity(intent)
                                    finish()
                                }
                                create()
                                show()
                            }
                        }
                    }
                }
            )

            storyMapViewModel.itemStory.observe(this) { itemStory ->
                for (stories in itemStory) {
                    val latLng = LatLng(stories.lat!!, stories.lon!!)

                    mMap.addMarker(
                        MarkerOptions()
                            .position(latLng)
                            .title(stories.name)
                            .snippet(stories.description)
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
                    )

                    //set boundaries
                    boundsBuilder.include(latLng)
                    val bounds: LatLngBounds = boundsBuilder.build()
                    mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 64))
                }
                noData(false)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.map_options, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_story_list -> {
                startActivity(Intent(this@StoryMapActivity, StoryListActivity::class.java))

                true
            }
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
                    getMyLocation()
                }
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false -> {
                    // Only approximate location access granted.
                    getMyLocation()
                }
                else -> {
                    // No location access granted.
                    AlertDialog.Builder(this).apply {
                        setTitle(getString(R.string.attention))
                        setMessage(getString(R.string.current_loc_wont_work))
                        setPositiveButton(getString(R.string.show_permission_dialog)) { _, _ ->
                            getMyLocation()
                        }
                        setNegativeButton(getString(R.string.cont), null)
                        create()
                        show()
                    }
                }
            }
        }

    private fun checkPermission(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            permission
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun getMyLocation() {
        if (checkPermission(Manifest.permission.ACCESS_FINE_LOCATION) &&
            checkPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
        ) {
            mMap.isMyLocationEnabled = true
        } else {
            requestPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
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

    private fun noData(state: Boolean) {
        if (state)
            storyMapBinding.root.visibility = View.GONE
        else
            storyMapBinding.root.visibility = View.VISIBLE
    }

    companion object {
        private const val TAG = "StoryMapActivity"
    }
}