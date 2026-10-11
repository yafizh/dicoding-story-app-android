package com.example.storyapp.ui.add

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.widget.Toast
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.storyapp.R
import com.example.storyapp.data.ResultState
import com.example.storyapp.databinding.ActivityAddStoryBinding
import com.example.storyapp.ui.ViewModelFactory
import com.example.storyapp.utils.applySystemBarsPadding
import com.example.storyapp.utils.createCameraImageUri
import com.example.storyapp.utils.reduceImageFile
import com.example.storyapp.widget.StoryWidget
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch
import java.io.IOException

class AddStoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddStoryBinding
    private val viewModel: AddStoryViewModel by viewModels { ViewModelFactory.getInstance(this) }
    private val fusedLocationClient by lazy { LocationServices.getFusedLocationProviderClient(this) }

    private val galleryLauncher = registerForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) viewModel.setImageUri(uri)
    }

    private val cameraLauncher = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { isSuccess ->
        viewModel.onCameraResult(isSuccess)
    }

    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.values.any { it }) {
            getCurrentLocation()
        } else {
            binding.switchLocation.isChecked = false
            Toast.makeText(this, R.string.error_location_permission, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT))
        binding = ActivityAddStoryBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.root.applySystemBarsPadding(topInsetTarget = binding.appBar)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.btnCamera.setOnClickListener { startCamera() }
        binding.btnGallery.setOnClickListener { startGallery() }
        binding.buttonAdd.setOnClickListener { uploadStory() }
        binding.switchLocation.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) getCurrentLocation() else viewModel.setLocation(null)
        }

        viewModel.imageUri.observe(this) { uri ->
            Glide.with(this).load(uri).into(binding.ivPreview)
        }
        viewModel.location.observe(this) { location ->
            binding.tvLocation.isVisible = location != null
            if (location != null) {
                binding.tvLocation.text = getString(R.string.location_format, location.latitude, location.longitude)
            }
        }
        viewModel.uploadResult.observe(this) { result ->
            when (result) {
                is ResultState.Loading -> showLoading(true)
                is ResultState.Success -> {
                    showLoading(false)
                    Toast.makeText(this, R.string.upload_success, Toast.LENGTH_SHORT).show()
                    StoryWidget.refresh(this)
                    setResult(RESULT_OK)
                    finish()
                }
                is ResultState.Error -> {
                    showLoading(false)
                    showErrorDialog(result.message ?: getString(R.string.error_connection))
                }
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    private fun startGallery() {
        galleryLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }

    private fun startCamera() {
        val uri = createCameraImageUri(this)
        viewModel.prepareCameraUri(uri)
        try {
            cameraLauncher.launch(uri)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(this, R.string.error_no_camera, Toast.LENGTH_SHORT).show()
        }
    }

    private fun getCurrentLocation() {
        val isFineGranted = ContextCompat.checkSelfPermission(
            this, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val isCoarseGranted = ContextCompat.checkSelfPermission(
            this, Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        if (!isFineGranted && !isCoarseGranted) {
            locationPermissionLauncher.launch(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
            )
            return
        }

        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, null)
            .addOnSuccessListener { location ->
                if (location != null) {
                    viewModel.setLocation(location)
                } else {
                    onLocationUnavailable()
                }
            }
            .addOnFailureListener { onLocationUnavailable() }
    }

    private fun onLocationUnavailable() {
        binding.switchLocation.isChecked = false
        Toast.makeText(this, R.string.error_location_unavailable, Toast.LENGTH_SHORT).show()
    }

    private fun uploadStory() {
        val imageUri = viewModel.imageUri.value
        if (imageUri == null) {
            Toast.makeText(this, R.string.error_empty_image, Toast.LENGTH_SHORT).show()
            return
        }
        val description = binding.edAddDescription.text.toString().trim()
        if (description.isEmpty()) {
            binding.edAddDescription.error = getString(R.string.error_empty_description)
            return
        }

        showLoading(true)
        lifecycleScope.launch {
            try {
                val photo = reduceImageFile(this@AddStoryActivity, imageUri)
                viewModel.uploadStory(photo, description)
            } catch (e: IOException) {
                showLoading(false)
                Toast.makeText(this@AddStoryActivity, R.string.error_process_image, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showErrorDialog(message: String) {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.dialog_failed_title)
            .setMessage(message)
            .setPositiveButton(R.string.ok, null)
            .show()
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.isVisible = isLoading
        binding.buttonAdd.isEnabled = !isLoading
        binding.btnCamera.isEnabled = !isLoading
        binding.btnGallery.isEnabled = !isLoading
        binding.switchLocation.isEnabled = !isLoading
    }
}
