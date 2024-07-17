package com.laurens.storyappdicoding.view.maps

import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.DrawableCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.laurens.storyappdicoding.data.pref.Result
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.MarkerOptions
import com.laurens.storyappdicoding.R
import com.laurens.storyappdicoding.data.pref.ListStoryItem
import com.laurens.storyappdicoding.databinding.ActivityMapsBinding
import com.laurens.storyappdicoding.view.ModelFacotry.ViewModelFactory
import com.laurens.storyappdicoding.view.main.MainActivity

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {
    private val mapsViewModel by viewModels<MapsViewModel> {
        ViewModelFactory.getInstance(this)
    }

    private lateinit var googleMapInstance: GoogleMap
    private lateinit var mapsBinding: ActivityMapsBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mapsBinding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(mapsBinding.root)

        // Setup toolbar
        val toolbar: Toolbar = findViewById(R.id.topAppBar)
        setSupportActionBar(toolbar)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

    }

    override fun onMapReady(googleMap: GoogleMap) {
        googleMapInstance = googleMap

        // UI settings
        googleMapInstance.uiSettings.isZoomControlsEnabled = true
        googleMapInstance.uiSettings.isIndoorLevelPickerEnabled = true
        googleMapInstance.uiSettings.isCompassEnabled = true
        googleMapInstance.uiSettings.isMapToolbarEnabled = true

        // Get user location
        fetchUserLocation()

        // Set map style
        applyMapStyle()

        // Fetch stories with location
        val extraToken = intent.getStringExtra(MainActivity.EXTRA_TOKEN).toString()
        mapsViewModel.getSession().observe(this) { user ->
            var token = user.token
            if (token == null) {
                token = extraToken
            }
            mapsViewModel.getStoriesWithLocation(token).observe(this@MapsActivity) { result ->
                when (result) {
                    is Result.Success -> {
                        val stories: List<ListStoryItem> = result.data.listStory
                        Log.d("MapsActivity", "Stories with location: $stories")
                        stories.forEach { data ->
                            val latLng = LatLng(data.lat, data.lon)
                            googleMapInstance.addMarker(
                                MarkerOptions()
                                    .position(latLng)
                                    .title(data.name)
                                    .snippet(data.description)
                            )
                        }
                    }
                    is Result.Error -> {
                        val errorMessage: String = result.error
                        Log.d("MapsActivity", "Error fetching stories: $errorMessage")
                        showToast(errorMessage)
                    }
                    else -> {}
                }
            }
        }
    }

    private fun drawableToBitmapDescriptor(@DrawableRes id: Int, @ColorInt color: Int): BitmapDescriptor {
        val vectorDrawable = ResourcesCompat.getDrawable(resources, id, null)
        if (vectorDrawable == null) {
            Log.e("BitmapHelper", "Resource not found")
            return BitmapDescriptorFactory.defaultMarker()
        }
        val bitmap = Bitmap.createBitmap(
            vectorDrawable.intrinsicWidth,
            vectorDrawable.intrinsicHeight,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        vectorDrawable.setBounds(0, 0, canvas.width, canvas.height)
        DrawableCompat.setTint(vectorDrawable, color)
        vectorDrawable.draw(canvas)
        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }

    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                fetchUserLocation()
            }
        }

    private fun fetchUserLocation() {
        if (ContextCompat.checkSelfPermission(
                this.applicationContext,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            googleMapInstance.isMyLocationEnabled = true

            // Get the last known location and move the camera to it
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    val currentLocation = LatLng(location.latitude, location.longitude)
                    googleMapInstance.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15f))
                    googleMapInstance.addMarker(
                        MarkerOptions()
                            .position(currentLocation)
                            .title("You are here")
                    )
                } else {
                    showToast("Location not found. Please try again.")
                }
            }
        } else {
            requestPermissionLauncher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    private fun applyMapStyle() {
        try {
            val success =
                googleMapInstance.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.map_style))
            if (!success) {
                Log.e(TAG, "Style parsing failed.")
            }
        } catch (exception: Resources.NotFoundException) {
            Log.e(TAG, "Can't find style. Error: ", exception)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.normal_type -> {
                googleMapInstance.mapType = GoogleMap.MAP_TYPE_NORMAL
                true
            }
            R.id.satellite_type -> {
                googleMapInstance.mapType = GoogleMap.MAP_TYPE_SATELLITE
                true
            }
            R.id.terrain_type -> {
                googleMapInstance.mapType = GoogleMap.MAP_TYPE_TERRAIN
                true
            }
            R.id.hybrid_type -> {
                googleMapInstance.mapType = GoogleMap.MAP_TYPE_HYBRID
                true
            }
            else -> {
                super.onOptionsItemSelected(item)
            }
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    companion object {
        private const val TAG = "MapsActivity"
    }
}
