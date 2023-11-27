package com.example.dicodingstoryapp.ui.addstory

import android.Manifest
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.Intent.ACTION_GET_CONTENT
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.dicodingstoryapp.R
import com.example.dicodingstoryapp.databinding.ActivityAddStoryBinding
import com.example.dicodingstoryapp.ui.camera.CameraActivity
import com.example.dicodingstoryapp.ui.story.StoryListActivity
import com.example.dicodingstoryapp.utils.ApiCallbackString
import com.example.dicodingstoryapp.utils.reduceFileImage
import com.example.dicodingstoryapp.utils.rotateBitmap
import com.example.dicodingstoryapp.utils.uriToFile
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.util.*

class AddStoryActivity : AppCompatActivity() {
    private lateinit var addStoryBinding: ActivityAddStoryBinding
    private val addStoryViewModel: AddStoryViewModel by viewModels()

    private var picUploadFile: File? = null

    private var lat: Float? = null
    private var lon: Float? = null

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (!allPermissionsGranted()) {
                Toast.makeText(
                    this,
                    getString(R.string.no_permission),
                    Toast.LENGTH_SHORT
                ).show()
                finish()
            }
        }
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putSerializable("image", picUploadFile)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        addStoryBinding = ActivityAddStoryBinding.inflate(layoutInflater)
        setContentView(addStoryBinding.root)

        // Prevent image file and preview lost when there's config change
        if (savedInstanceState != null) {
            picUploadFile = savedInstanceState.getSerializable("image") as File
            // Make new preview from saved picUploadFile that is converted to bitmap
            addStoryBinding.ivPreview.setImageBitmap(BitmapFactory.decodeFile(picUploadFile!!.path))
        }

        if (!allPermissionsGranted()) {
            ActivityCompat.requestPermissions(
                this,
                REQUIRED_PERMISSIONS,
                REQUEST_CODE_PERMISSIONS
            )
        }

        addStoryBinding.btnCamerax.setOnClickListener { startCameraX() }
        addStoryBinding.btnGallery.setOnClickListener { startGallery() }
        addStoryBinding.btnLocation.setOnClickListener { chooseLocation() }
        addStoryBinding.btnUpload.setOnClickListener { uploadImage() }

        val actionBar = supportActionBar
        if (actionBar != null) {
            title = getString(R.string.add_story)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_home)
            actionBar.setDisplayHomeAsUpEnabled(true)
        }
    }

    private fun chooseLocation() {
        val intent = Intent(this, AddStoryLocationActivity::class.java)
        location.launch(intent)
    }

    private fun startCameraX() {
        val intent = Intent(this, CameraActivity::class.java)
        launcherIntentCameraX.launch(intent)
    }

    private fun startGallery() {
        val intent = Intent()
        intent.action = ACTION_GET_CONTENT
        intent.type = "image/*"
        val chooser = Intent.createChooser(intent, getString(R.string.choose_pic))
        launcherIntentGallery.launch(chooser)
    }

    private fun uploadImage() {
        showLoading(true)
        when {
            lat == null -> {
                showLoading(false)
                Toast.makeText(
                    this@AddStoryActivity,
                    getString(R.string.location_null),
                    Toast.LENGTH_SHORT
                ).show()
            }

            picUploadFile != null -> {
                val file = reduceFileImage(picUploadFile as File)

                val description = addStoryBinding.etDescription.text.toString()
                    .toRequestBody("text/plain".toMediaType())
                Log.d("REQUEST BODY", description.toString())
                val requestImageFile = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
                val imageMultipart: MultipartBody.Part = MultipartBody.Part.createFormData(
                    "photo",
                    file.name,
                    requestImageFile
                )

                addStoryViewModel.getUser().observe(this) {
                    addStoryViewModel.addStory(
                        it.token,
                        imageMultipart,
                        description,
                        lat!!,
                        lon!!,
                        object : ApiCallbackString {
                            override fun responseState(success: Boolean, message: String) {
                                if (!success) {
                                    AlertDialog.Builder(this@AddStoryActivity).apply {
                                        showLoading(false)
                                        setTitle(getString(R.string.failed))
                                        setMessage(getString(R.string.upload_fail))
                                        setPositiveButton(getString(R.string.cont), null)
                                        create()
                                        show()
                                    }
                                } else {
                                    AlertDialog.Builder(this@AddStoryActivity).apply {
                                        setTitle(getString(R.string.success))
                                        setMessage(getString(R.string.upload_success))
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
                        })
                }
            }

            addStoryBinding.etDescription.text.toString().isEmpty() -> {
                showLoading(false)
                Toast.makeText(
                    this@AddStoryActivity,
                    getString(R.string.no_desc),
                    Toast.LENGTH_SHORT
                ).show()
            }
            else -> {
                showLoading(false)
                Toast.makeText(
                    this@AddStoryActivity,
                    getString(R.string.no_image),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private val location = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (it.resultCode == LOCATION_RESULT) {
            val latitude = it.data?.getDoubleExtra("lat", 0.0) as Double
            val longitude = it.data?.getDoubleExtra("lon", 0.0) as Double
            lat = latitude.toFloat()
            lon = longitude.toFloat()

            addStoryBinding.btnLocation.text = getString(R.string.change_location)
        }
    }

    private val launcherIntentCameraX = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (it.resultCode == CAMERA_X_RESULT) {
            val myFile = it.data?.getSerializableExtra("picture") as File
            val isBackCamera = it.data?.getBooleanExtra("isBackCamera", true) as Boolean

            // Preview image
            val bitmapResult = rotateBitmap(
                BitmapFactory.decodeFile(myFile.path),
                isBackCamera
            )

            // Convert bitmap to file (because the cameraX image result is always rotated)
            picUploadFile = bitmapToFile(bitmapResult)

            addStoryBinding.ivPreview.setImageBitmap(bitmapResult)
        }
    }

    private val launcherIntentGallery = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val selectedImgUri: Uri = result.data?.data as Uri

            val myFile = uriToFile(selectedImgUri, this@AddStoryActivity)

            picUploadFile = myFile

            addStoryBinding.ivPreview.setImageURI(selectedImgUri)
        }
    }

    private fun bitmapToFile(bitmap: Bitmap): File {
        val wrapper = ContextWrapper(applicationContext)
        var file = wrapper.getDir("Images", Context.MODE_PRIVATE)
        file = File(file, "${UUID.randomUUID()}.jpg")

        try {
            val stream: OutputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
            stream.flush()
            stream.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return file
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.option_menu, menu)

        menu.findItem(R.id.menu_logout).isVisible = false
        menu.findItem(R.id.menu_story_map).isVisible = false
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_translate -> {
                startActivity(Intent(Settings.ACTION_LOCALE_SETTINGS))

                true
            }
            else -> false
        }
    }

    private fun showLoading(state: Boolean) {
        if (state) {
            addStoryBinding.apply {
                ivPreview.visibility = View.GONE
                btnCamerax.visibility = View.GONE
                btnGallery.visibility = View.GONE
                btnUpload.visibility = View.GONE
                etDescription.visibility = View.GONE
                pbAddStory.visibility = View.VISIBLE
            }
        } else {
            addStoryBinding.apply {
                ivPreview.visibility = View.VISIBLE
                btnCamerax.visibility = View.VISIBLE
                btnGallery.visibility = View.VISIBLE
                btnUpload.visibility = View.VISIBLE
                etDescription.visibility = View.VISIBLE
                pbAddStory.visibility = View.GONE
            }
        }
    }

    companion object {
        const val CAMERA_X_RESULT = 200
        const val LOCATION_RESULT = 123

        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
        private const val REQUEST_CODE_PERMISSIONS = 10
    }
}