package com.dicoding.storyapp.view.newstory

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.transition.Fade
import android.transition.TransitionInflater
import android.util.Log
import android.view.View
import android.view.ViewTreeObserver
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.dicoding.storyapp.R
import com.dicoding.storyapp.ViewModelFactory
import com.dicoding.storyapp.data.ResultState
import com.dicoding.storyapp.data.pref.UserModel
import com.dicoding.storyapp.databinding.ActivityNewStoryBinding
import com.dicoding.storyapp.getImageUri
import com.dicoding.storyapp.reduceFileImage
import com.dicoding.storyapp.uriToFile
import com.dicoding.storyapp.view.helper.LocaleHelper
import com.dicoding.storyapp.view.main.MainActivity
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody

class NewStoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNewStoryBinding
    private lateinit var viewModel: NewStoryViewModel
    private var currentImageUri: Uri? = null
    private var token: String? = null

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var currentLat: Double? = null
    private var currentLon: Double? = null

    private val requestLocationPermission = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        val granted = result[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                result[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) getCurrentLocation()
        else {
            binding.switchLocation.isChecked = false
            showToast(getString(R.string.location_permission_denied))
        }
    }

    override fun attachBaseContext(newBase: Context?) {
        val langCode = LocaleHelper.getSavedLanguage(newBase ?: return)
        val contextWithLocale = LocaleHelper.applyLanguage(newBase, langCode)
        super.attachBaseContext(contextWithLocale)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        // Animasi
        window.sharedElementEnterTransition = TransitionInflater.from(this)
            .inflateTransition(android.R.transition.move)
        window.sharedElementReturnTransition = TransitionInflater.from(this)
            .inflateTransition(android.R.transition.move)
        window.enterTransition = Fade()
        window.exitTransition = Fade()

        super.onCreate(savedInstanceState)
        binding = ActivityNewStoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Animasi postpone
        postponeEnterTransition()
        binding.root.viewTreeObserver.addOnPreDrawListener(object :
            ViewTreeObserver.OnPreDrawListener {
            override fun onPreDraw(): Boolean {
                binding.root.viewTreeObserver.removeOnPreDrawListener(this)
                startPostponedEnterTransition()
                return true
            }
        })

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        setupViewModel()

        // Button
        binding.galleryButton.setOnClickListener { startGallery() }
        binding.cameraButton.setOnClickListener { startCamera() }
        binding.uploadButton.setOnClickListener { uploadImage() }

        // Switch lokasi
        binding.switchLocation.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) ensureLocationThenFetch()
            else clearLocation()
        }
    }

    private fun setupViewModel() {
        val factory = ViewModelFactory.getInstance(this)
        viewModel = ViewModelProvider(this, factory)[NewStoryViewModel::class.java]

        viewModel.getSession().observe(this) { user: UserModel ->
            token = user.token
        }
    }

    private fun uploadImage() {
        val userToken = token
        if (userToken.isNullOrEmpty()) {
            showToast(getString(R.string.empty_image_warning))
            return
        }

        currentImageUri?.let { uri ->
            val imageFile = uriToFile(uri, this).reduceFileImage()
            Log.d("Image File", "showImage: ${imageFile.path}")
            val description = binding.descriptionEditText.text.toString()

            if (description.isEmpty()) {
                showToast(getString(R.string.empty_description_warning))
                return
            }

            showLoading(true)

            // Gambar → MultipartBody
            val requestImage = imageFile.asRequestBody("image/jpeg".toMediaType())
            val multipartImage = MultipartBody.Part.createFormData(
                "photo",
                imageFile.name,
                requestImage
            )

            // Deskripsi → RequestBody
            val descRequestBody = description.toRequestBody("text/plain".toMediaType())

            // Lat/Lon → RequestBody opsional
            val latRequestBody = currentLat?.toString()?.toRequestBody("text/plain".toMediaType())
            val lonRequestBody = currentLon?.toString()?.toRequestBody("text/plain".toMediaType())

            // Panggil ViewModel
            viewModel.uploadStory(
                userToken,
                multipartImage,
                descRequestBody,
                latRequestBody,
                lonRequestBody
            ).observe(this) { result ->
                when (result) {
                    is ResultState.Loading -> binding.loadingOverlay.visibility = View.VISIBLE
                    is ResultState.Success -> {
                        showToast(getString(R.string.upload_success))
                        binding.loadingOverlay.visibility = View.GONE
                        val intent = Intent(this, MainActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                        startActivity(intent)
                        finish()
                    }
                    is ResultState.Error -> {
                        showToast(result.error)
                        showLoading(false)
                    }
                }
            }
        } ?: showToast(getString(R.string.empty_image_warning))
    }

    private fun hasLocationPermission(): Boolean {
        val fine = ActivityCompat.checkSelfPermission(
            this, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val coarse = ActivityCompat.checkSelfPermission(
            this, Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        return fine || coarse
    }

    @SuppressLint("MissingPermission")
    private fun getCurrentLocation() {
        val lm = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val gpsOn = lm.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        if (!gpsOn) {
            Toast.makeText(this, getString(R.string.gps_off), Toast.LENGTH_LONG).show()
            binding.switchLocation.isChecked = false
            return
        }

        val cts = CancellationTokenSource()
        fusedLocationClient.getCurrentLocation(
            Priority.PRIORITY_HIGH_ACCURACY,
            cts.token
        ).addOnSuccessListener { location ->
            if (location != null) {
                currentLat = location.latitude
                currentLon = location.longitude
                binding.tvLocationPreview.text =
                    "Lat: %.6f, Lon: %.6f".format(currentLat, currentLon)
            } else {
                binding.switchLocation.isChecked = false
                showToast(getString(R.string.location_failed))
            }
        }.addOnFailureListener {
            binding.switchLocation.isChecked = false
            showToast("Error: ${it.message}")
        }
    }

    private fun ensureLocationThenFetch() {
        if (hasLocationPermission()) getCurrentLocation()
        else {
            requestLocationPermission.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    private fun clearLocation() {
        currentLat = null
        currentLon = null
        binding.tvLocationPreview.text = getString(R.string.location_off)
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressLoadingIndicator.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    private fun startGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        galleryLauncher.launch(intent)
    }

    private val galleryLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val selectedImage = result.data?.data
                selectedImage?.let {
                    currentImageUri = it
                    binding.storyImageView.setImageURI(it)
                }
            }
        }

    private fun startCamera() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CAMERA),
                REQUEST_CAMERA_PERMISSION
            )
        } else {
            cameraLauncher.launch(null)
        }
    }

    private val cameraLauncher =
        registerForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
            bitmap?.let {
                val uri = getImageUri(this, it)
                currentImageUri = uri
                binding.storyImageView.setImageBitmap(it)
            }
        }

    companion object {
        const val REQUEST_CAMERA_PERMISSION = 100
    }
}
