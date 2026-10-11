package com.example.storyapp.ui.maps

import android.content.res.Resources
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.example.storyapp.R
import com.example.storyapp.data.ResultState
import com.example.storyapp.data.remote.response.ListStoryItem
import com.example.storyapp.databinding.ActivityMapsBinding
import com.example.storyapp.ui.ViewModelFactory
import com.example.storyapp.ui.detail.DetailActivity
import com.example.storyapp.utils.applySystemBarsPadding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var binding: ActivityMapsBinding
    private lateinit var googleMap: GoogleMap
    private val viewModel: MapsViewModel by viewModels { ViewModelFactory.getInstance(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT))
        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.root.applySystemBarsPadding(topInsetTarget = binding.appBar)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        googleMap.uiSettings.isZoomControlsEnabled = true
        googleMap.uiSettings.isMapToolbarEnabled = false
        applyMapStyle()
        googleMap.setOnInfoWindowClickListener { marker ->
            (marker.tag as? ListStoryItem)?.let { story ->
                startActivity(DetailActivity.createIntent(this, story))
            }
        }
        viewModel.stories.observe(this) { result -> renderStories(result) }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    private fun renderStories(result: ResultState<List<ListStoryItem>>) {
        when (result) {
            is ResultState.Loading -> binding.progressBar.isVisible = true
            is ResultState.Success -> {
                binding.progressBar.isVisible = false
                showMarkers(result.data)
            }
            is ResultState.Error -> {
                binding.progressBar.isVisible = false
                showErrorDialog(result.message ?: getString(R.string.error_connection))
            }
        }
    }

    private fun showMarkers(stories: List<ListStoryItem>) {
        googleMap.clear()
        val locatedStories = stories.mapNotNull { story ->
            val lat = story.lat ?: return@mapNotNull null
            val lon = story.lon ?: return@mapNotNull null
            story to LatLng(lat, lon)
        }
        if (locatedStories.isEmpty()) {
            Toast.makeText(this, R.string.no_story_location, Toast.LENGTH_SHORT).show()
            return
        }

        val boundsBuilder = LatLngBounds.Builder()
        locatedStories.forEach { (story, position) ->
            val marker = googleMap.addMarker(
                MarkerOptions()
                    .position(position)
                    .title(story.name)
                    .snippet(story.description)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET))
            )
            marker?.tag = story
            boundsBuilder.include(position)
        }

        val width = resources.displayMetrics.widthPixels
        val height = resources.displayMetrics.heightPixels
        val padding = (width * BOUNDS_PADDING_RATIO).toInt()
        googleMap.animateCamera(
            CameraUpdateFactory.newLatLngBounds(boundsBuilder.build(), width, height, padding)
        )
    }

    private fun applyMapStyle() {
        try {
            val success = googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.map_style))
            if (!success) Log.e(TAG, "Style parsing failed.")
        } catch (e: Resources.NotFoundException) {
            Log.e(TAG, "Can't find style. Error: ", e)
        }
    }

    private fun showErrorDialog(message: String) {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.dialog_failed_title)
            .setMessage(message)
            .setPositiveButton(R.string.button_retry) { _, _ -> viewModel.getStoriesWithLocation() }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    companion object {
        private const val TAG = "MapsActivity"
        private const val BOUNDS_PADDING_RATIO = 0.15
    }
}
