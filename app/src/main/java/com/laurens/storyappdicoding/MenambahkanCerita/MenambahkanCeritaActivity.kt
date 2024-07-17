package com.laurens.storyappdicoding.MenambahkanCerita

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Toast
import com.laurens.storyappdicoding.data.pref.Result
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.laurens.storyappdicoding.R
import com.laurens.storyappdicoding.databinding.ActivityMenambahkanCeritaBinding
import com.laurens.storyappdicoding.view.ModelFacotry.ViewModelFactory
import com.laurens.storyappdicoding.view.main.MainActivity
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody

class MenambahkanCeritaActivity : AppCompatActivity() {
    private val viewModel by viewModels<MenambahkanCeritaViewModel> {
        ViewModelFactory.getInstance(this)
    }
    private lateinit var locationProviderClient: FusedLocationProviderClient
    private lateinit var ceritaBinding: ActivityMenambahkanCeritaBinding
    private lateinit var uriGambarSaatIni: Uri
    private var latitude = 0f
    private var longitude = 0f
    private var isLocationEnabled = false
    private val launcherPermintaanIzin =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                displayToast(getString(R.string.permission_s))
            } else {
                displayToast(getString(R.string.permission_e))
            }
        }

    private val locationPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            when {
                permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false -> {
                    showToast(getString(R.string.permission_s))
                    fetchLastLocation()
                }

                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false -> {
                    showToast(getString(R.string.permission_s))
                    fetchLastLocation()
                }

                else -> {
                    showToast(getString(R.string.permission_e))
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (allPermissionsGranted()) {
            launcherPermintaanIzin.launch(REQUIRED_PERMISSION)
        }
        ceritaBinding = ActivityMenambahkanCeritaBinding.inflate(layoutInflater)
        setContentView(ceritaBinding.root)
        locationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        fetchLastLocation()
        setupCeritaAction()

        ceritaBinding.edStoryDesc.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                setButtonState()
            }

            override fun afterTextChanged(s: Editable) {
            }
        })
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun fetchLastLocation() {
        if (checkPermission(Manifest.permission.ACCESS_FINE_LOCATION) &&
            checkPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
        ) {
            locationProviderClient.lastLocation.addOnSuccessListener { location: Location? ->
                if (location != null) {
                    Log.d("addstoryact", "laslocation lat: ${location.latitude.toFloat()}")
                    latitude = location.latitude.toFloat()
                    longitude = location.longitude.toFloat()
                } else {
                    showToast("Location is not found. Try Again")
                }
            }
        } else {
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }


    private fun allPermissionsGranted() =
        ContextCompat.checkSelfPermission(
            this,
            REQUIRED_PERMISSION
        ) == PackageManager.PERMISSION_GRANTED

    private fun checkPermission(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            permission
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun setupCeritaAction() {
        ceritaBinding.btnGallery.setOnClickListener {
            openGallery()
        }
        ceritaBinding.btnCamera.setOnClickListener {
            openCamera()
        }
        ceritaBinding.btnUpload.setOnClickListener {
            uploadImage(isLocationEnabled)
        }
        ceritaBinding.cbAddLocation.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                isLocationEnabled = true
            } else {
                isLocationEnabled = false
            }

        }
    }

    private fun setButtonState() {
        val isImageValid = ceritaBinding.ivPreview != null
        val isDescValid =
            ceritaBinding.edStoryDesc.text != null && ceritaBinding.edStoryDesc.text.toString()
                .isNotEmpty()
        ceritaBinding.btnUpload.isEnabled = isDescValid && isImageValid
    }


    private fun openGallery() {
        pengelolaGaleri.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }

    private val pengelolaGaleri = registerForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            uriGambarSaatIni = uri
            displayImage()
        } else {
            displayToast(getString(R.string.no_media))
        }
    }

    private fun openCamera() {
        uriGambarSaatIni = getImageUri(this)
        launcherIntentCamera.launch(uriGambarSaatIni)
    }

    private val launcherIntentCamera = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { isSuccess ->
        if (isSuccess) {
            displayImage()
        }
    }

    private fun uploadImage(isLocationEnabled: Boolean) {
        uriGambarSaatIni.let { uri ->
            val imageFile = copyUriContentToFile(uri, this).compressImageFile()
            val description = ceritaBinding.edStoryDesc.text.toString()
            val latString = latitude.toString()
            val lonString = longitude.toString()
            Log.d("MenambahkanCeritaActivity", "uploadImage lat: $latitude, lon: $longitude, description: $description")

            viewModel.observeUserToken().observe(this) { token ->
                val requestBody = description.toRequestBody("text/plain".toMediaType())
                val requestImageFile = imageFile.asRequestBody("image/jpeg".toMediaType())
                val requestLat = latString.toRequestBody("text/plain".toMediaType())
                val requestLon = lonString.toRequestBody("text/plain".toMediaType())
                val multipartBody = MultipartBody.Part.createFormData(
                    "photo",
                    imageFile.name,
                    requestImageFile
                )
                if (token != null) {
                    if (isLocationEnabled) {
                        Log.d("MenambahkanCeritaActivity", "Uploading story with location")
                        viewModel.uploadStoryWithLocation(
                            token,
                            multipartBody,
                            requestBody,
                            requestLat,
                            requestLon
                        ).observe(this) { result ->
                            if (result != null) {
                                when (result) {
                                    is Result.Loading -> {
                                        showLoading(true)
                                    }
                                    is Result.Success -> {
                                        showLoading(false)
                                        Log.d("MenambahkanCeritaActivity", "Upload successful")
                                        val intent = Intent(this, MainActivity::class.java)
                                        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                                        startActivity(intent)
                                    }
                                    is Result.Error -> {
                                        showLoading(false)
                                        val errorMessage = result.error
                                        showToast(errorMessage)
                                        Log.e("MenambahkanCeritaActivity", "Upload error: $errorMessage")
                                    }
                                }
                            }
                        }
                    } else {
                        Log.d("MenambahkanCeritaActivity", "Uploading story without location")
                        viewModel.uploadCerita(token, multipartBody, requestBody).observe(this) { result ->
                            if (result != null) {
                                when (result) {
                                    is Result.Loading -> {
                                        showLoading(true)
                                    }
                                    is Result.Success -> {
                                        showLoading(false)
                                        Log.d("MenambahkanCeritaActivity", "Upload successful")
                                        val intent = Intent(this, MainActivity::class.java)
                                        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                                        startActivity(intent)
                                    }
                                    is Result.Error -> {
                                        showLoading(false)
                                        val errorMessage = result.error
                                        showToast(errorMessage)
                                        Log.e("MenambahkanCeritaActivity", "Upload error: $errorMessage")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun displayImage() {
        uriGambarSaatIni.let {
            ceritaBinding.ivPreview.setImageURI(it)
        }
    }

    private fun displayToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun showLoading(isLoading: Boolean) {
        if (isLoading) {
            ceritaBinding.progressBar.visibility = View.VISIBLE

        } else {
            ceritaBinding.progressBar.visibility = View.GONE
        }
    }


    companion object {
        private const val REQUIRED_PERMISSION = Manifest.permission.CAMERA
    }
}