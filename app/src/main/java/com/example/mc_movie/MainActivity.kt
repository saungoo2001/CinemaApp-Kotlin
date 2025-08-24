package com.example.mc_movie

import com.example.mc_movie.model.ImgBBResponse
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit
import ImgBBService
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Base64
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.SearchView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.mc_movie.adapter.CinemaAdapter
import com.example.mc_movie.adapter.CinemaMovieAdapter
import com.example.mc_movie.adapter.MovieAdapter
import com.example.mc_movie.adapter.ShowtimeAdapter
import com.example.mc_movie.apiservice.TMDBServiceAPI
import com.example.mc_movie.auth.login
import com.example.mc_movie.databinding.ActivityMainBinding
import com.example.mc_movie.model.*
import com.example.mc_movie.network.NetworkUtils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.google.firebase.ktx.initialize
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import android.Manifest

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var toggle: ActionBarDrawerToggle
    private var dialogProfileImage: ImageView? = null

    // Your TMDB token here (replace with yours)
    private val authToken = "Bearer eyJhbGciOiJIUzI1NiJ9.eyJhdWQiOiI0ZWQ5MzYzYzdhYzg2NzQwNzJjMjc5YWZlODAzZTg1ZCIsIm5iZiI6MTc1MDk1NTE5NC4yMTgsInN1YiI6IjY4NWQ3NGJhM2E0MThiNmRkZTdlY2FlYyIsInNjb3BlcyI6WyJhcGlfcmVhZCJdLCJ2ZXJzaW9uIjoxfQ.PXfie96BFxkTsbh1J7p4qL19cUEjo6JqEJJgn0w1vFQ"

    // ImgBB API key
    private val imgbbApiKey = "181653835262aa1bb197b3a20a43345b"

    // Image picker launcher
    private val imagePicker = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri == null) {
            Toast.makeText(this, "No image selected", Toast.LENGTH_SHORT).show()
        } else {
            // Internet check before uploading
            checkInternetBefore {
                Toast.makeText(this, "Image picked! Uploading...", Toast.LENGTH_SHORT).show()
                uploadImageToImgBB(uri)
            }
        }
    }//---------------------------------------------------------------------------------------
    // Add Camera Launcher and File Creation
    private var currentPhotoPath: String = ""
    private val cameraLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            // Internet check before uploading
            checkInternetBefore {
                Toast.makeText(this, "Photo taken! Uploading...", Toast.LENGTH_SHORT).show()
                val uri = Uri.fromFile(File(currentPhotoPath))
                uploadImageToImgBB(uri)
            }
        } else {
            Toast.makeText(this, "Failed to capture image", Toast.LENGTH_SHORT).show()
        }
    }
//--------------------------------------------------------------------------------------------------

    //set user default image
companion object {
    private const val CAMERA_PERMISSION_REQUEST_CODE = 1001
    private const val DEFAULT_PROFILE_IMAGE_URL = "https://cdn.pixabay.com/photo/2015/10/05/22/37/blank-profile-picture-973460_1280.png"
}
    //-----------------------------------------------------------------------------------------------------

    // For cinema loading
    private lateinit var database: FirebaseDatabase
    private lateinit var cinemasRef: DatabaseReference
    private lateinit var cinemaRecyclerView: RecyclerView
    private val cinemaList = mutableListOf<Cinema>()
    private var isInCategoryView = false
    private var isViewingCinemaMovies = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Firebase.initialize(this)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupDrawer()
        setupUserInfo()
        setupRecyclerViews()
        setupSearch()

        // Wrap network calls with internet check
        checkInternetBefore { loadAllMovies() }
        checkInternetBefore { loadCinemaList() }

        // Hide cinema list initially
        binding.cinemaRecyclerView.visibility = View.GONE

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                when {
                    binding.dLayout.isDrawerOpen(GravityCompat.START) -> {
                        binding.dLayout.closeDrawer(GravityCompat.START)
                    }
                    binding.rvShowtimes.visibility == View.VISIBLE -> {
                        binding.rvShowtimes.visibility = View.GONE
                        binding.rvCinemaMovies.visibility = View.VISIBLE
                    }
                    isViewingCinemaMovies -> {
                        binding.rvCinemaMovies.visibility = View.GONE
                        binding.cinemaRecyclerView.visibility = View.VISIBLE
                        binding.idSearch.visibility = View.GONE
                        isViewingCinemaMovies = false
                    }
                    isInCategoryView -> {
                        binding.cinemaRecyclerView.visibility = View.GONE
                        binding.idSearch.visibility = View.VISIBLE
                        showOriginalLists()
                        isInCategoryView = false
                    }
                    else -> finish()
                }
            }
        })
    }

    override fun onStart() {
        super.onStart()
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            val intent = Intent(this, login::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
            return
        }

        val databaseRef = FirebaseDatabase.getInstance().reference
        databaseRef.child("users").child(currentUser.uid).child("profileImageUrl")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (!snapshot.exists()) {
                        // Set default image
                        val defaultImageUrl = "https://your-default-image-url.com/default_profile.png"
                        databaseRef.child("users").child(currentUser.uid)
                            .child("profileImageUrl").setValue(defaultImageUrl)
                        val profileUpdates = UserProfileChangeRequest.Builder()
                            .setPhotoUri(Uri.parse(defaultImageUrl))
                            .build()
                        currentUser.updateProfile(profileUpdates)
                        setupUserInfo() // now safe to load
                    } else {
                        setupUserInfo()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    setupUserInfo()
                }
            })
    }

    private fun checkInternetBefore(action: () -> Unit) {
        if (NetworkUtils.isInternetAvailable(this)) {
            action()
        } else {
            Toast.makeText(this, "No internet connection", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupDrawer() {
        toggle = ActionBarDrawerToggle(this, binding.dLayout, R.string.open, R.string.close)
        binding.dLayout.addDrawerListener(toggle)
        toggle.syncState()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.naviView.setNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.home -> {
                    if (!isInCategoryView && !isViewingCinemaMovies) {
                        showToast("Already in Home")
                    } else {
                        showToast("Home clicked")
                        showOriginalLists()
                        binding.cinemaRecyclerView.visibility = View.GONE
                        binding.rvCinemaMovies.visibility = View.GONE
                        binding.rvShowtimes.visibility = View.GONE
                        binding.idSearch.visibility = View.VISIBLE
                        isInCategoryView = false
                        isViewingCinemaMovies = false
                    }
                }
                R.id.category -> {
                    if (isInCategoryView) {
                        showToast("Already in Category")
                    } else {
                        showToast("Category clicked")
                        binding.cinemaRecyclerView.visibility = View.VISIBLE
                        hideOtherLists()
                        binding.rvCinemaMovies.visibility = View.GONE
                        binding.rvShowtimes.visibility = View.GONE
                        binding.idSearch.visibility = View.GONE
                        isInCategoryView = true
                        isViewingCinemaMovies = false
                    }
                }
                R.id.setting -> {
                    showToast("Setting clicked")
                    binding.cinemaRecyclerView.visibility = View.GONE
                    binding.rvCinemaMovies.visibility = View.GONE
                    binding.rvShowtimes.visibility = View.GONE
                    binding.idSearch.visibility = View.VISIBLE
                    isInCategoryView = false
                    isViewingCinemaMovies = false
                    val intent = Intent(this, SettingsActivity::class.java)
                    startActivity(intent)
                }
            }
            binding.dLayout.closeDrawer(GravityCompat.START)
            true
        }
    }

    // Setup user information in the navigation drawer header
    private fun setupUserInfo() {
        val user = FirebaseAuth.getInstance().currentUser
        val navigationView = binding.naviView
        if (navigationView == null) return // Prevent NullPointerException if binding is not ready

        // Make sure the header exists
        val headerView = navigationView.getHeaderView(0) ?: return

        val txtName = headerView.findViewById<TextView>(R.id.txtname)
        val profileImage = headerView.findViewById<ImageView>(R.id.txtimage)

        // Safely set display name
        txtName.text = user?.displayName ?: user?.email ?: "Guest"

        if (user != null) {
            // Load profile image from Firebase Database with a fallback URL
            val databaseRef = FirebaseDatabase.getInstance().reference
            databaseRef.child("users")
                .child(user.uid)
                .child("profileImageUrl")
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val imageUrl = snapshot.getValue(String::class.java)
                        Glide.with(this@MainActivity)
                            .load(imageUrl ?: "https://your-default-image-url.com/default_profile.png")
                            .circleCrop()
                            .into(profileImage)
                    }

                    override fun onCancelled(error: DatabaseError) {
                        // Load default image if database read fails
                        Glide.with(this@MainActivity)
                            .load("https://your-default-image-url.com/default_profile.png")
                            .circleCrop()
                            .into(profileImage)
                    }
                })
        } else {
            // User is null, show default image
            Glide.with(this@MainActivity)
                .load("https://your-default-image-url.com/default_profile.png")
                .circleCrop()
                .into(profileImage)
        }
        profileImage.setOnClickListener { showProfileDialog() }
    }


    private fun setupRecyclerViews() {
        binding.rvPopular.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.rvUpcoming.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.rvTopRelated.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.cinemaRecyclerView.layoutManager = LinearLayoutManager(this)
        cinemaRecyclerView = binding.cinemaRecyclerView
    }

    private fun setupSearch() {
        binding.idSearch.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                val q = query?.trim().orEmpty()
                checkInternetBefore {
                    searchPerson(q) { found ->
                        if (!found) checkInternetBefore { searchMovie(q) }
                    }
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText.isNullOrEmpty()) showOriginalLists()
                return false
            }
        })
    }

    private fun loadAllMovies() {
        loadPopularMovies()
        loadUpcomingMovies()
        loadTopRelatedMovies()
    }
    // Initialize and return a Retrofit instance for TMDB API calls
    private fun getRetrofit(): TMDBServiceAPI {
        // Create logging interceptor (optional, useful for debugging network requests)
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        // Build OkHttpClient with custom timeout values and logging interceptor
        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(logging)               // Add logging interceptor
            .connectTimeout(30, TimeUnit.SECONDS) // Set connection timeout to 30 seconds
            .readTimeout(30, TimeUnit.SECONDS)    // Set read timeout to 30 seconds
            .writeTimeout(30, TimeUnit.SECONDS)   // Set write timeout to 30 seconds
            .build()

        // Build Retrofit instance with TMDB base URL, custom OkHttpClient, and Gson converter
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.themoviedb.org/3/")
            .client(okHttpClient)                  // Use custom OkHttpClient
            .addConverterFactory(GsonConverterFactory.create()) // Use Gson converter
            .build()

        // Return the Retrofit service API interface
        return retrofit.create(TMDBServiceAPI::class.java)
    }

    private fun loadPopularMovies() {
        getRetrofit().getPopularMovies(authToken).enqueue(object : Callback<Movie> {
            override fun onResponse(call: Call<Movie>, response: Response<Movie>) {
                val movies = response.body()?.results ?: return
                binding.rvPopular.adapter = MovieAdapter(movies) { movie ->
                    val intent = Intent(this@MainActivity, MovieDetailActivity::class.java).apply {
                        putExtra("title", movie.title ?: "")
                        putExtra("poster_path", movie.poster_path ?: "")
                        putExtra("overview", movie.overview ?: "")
                        putExtra("release_date", movie.release_date ?: "")
                        putExtra("vote_average", movie.vote_average?.toString() ?: "")
                    }
                    startActivity(intent)
                }
            }

            override fun onFailure(call: Call<Movie>, t: Throwable) {
                showToast("Popular movies load failed: ${t.message}")
            }
        })
    }

    private fun loadUpcomingMovies() {
        getRetrofit().getUpcomingMovies(authToken).enqueue(object : Callback<Movie> {
            override fun onResponse(call: Call<Movie>, response: Response<Movie>) {
                val movies = response.body()?.results ?: return
                binding.rvUpcoming.adapter = MovieAdapter(movies) { movie ->
                    val intent = Intent(this@MainActivity, MovieDetailActivity::class.java).apply {
                        putExtra("title", movie.title)
                        putExtra("poster_path", movie.poster_path)
                        putExtra("overview", movie.overview)
                        putExtra("release_date", movie.release_date)
                        putExtra("vote_average", movie.vote_average.toString())
                    }
                    startActivity(intent)
                }
            }

            override fun onFailure(call: Call<Movie>, t: Throwable) {
                showToast("Upcoming movies load failed: ${t.message}")
            }
        })
    }

    private fun loadTopRelatedMovies() {
        getRetrofit().getTopRelatedMovies(authToken).enqueue(object : Callback<Movie> {
            override fun onResponse(call: Call<Movie>, response: Response<Movie>) {
                val movies = response.body()?.results ?: return
                binding.rvTopRelated.adapter = MovieAdapter(movies) { movie ->
                    val intent = Intent(this@MainActivity, MovieDetailActivity::class.java).apply {
                        putExtra("title", movie.title)
                        putExtra("poster_path", movie.poster_path)
                        putExtra("overview", movie.overview)
                        putExtra("release_date", movie.release_date)
                        putExtra("vote_average", movie.vote_average.toString())
                    }
                    startActivity(intent)
                }
            }

            override fun onFailure(call: Call<Movie>, t: Throwable) {
                showToast("Top related movies load failed: ${t.message}")
            }
        })
    }

    private fun searchPerson(query: String, callback: (Boolean) -> Unit) {
        getRetrofit().getSearchPerson(authToken, query).enqueue(object : Callback<Person> {
            override fun onResponse(call: Call<Person>, response: Response<Person>) {
                val personList = response.body()?.results
                if (!personList.isNullOrEmpty()) {
                    getPersonMovieCredits(personList[0].id)
                    callback(true)
                } else {
                    callback(false)
                }
            }

            override fun onFailure(call: Call<Person>, t: Throwable) {
                callback(false)
            }
        })
    }

    private fun getPersonMovieCredits(personId: Int) {
        getRetrofit().getPersonMovieCredits(authToken, personId)
            .enqueue(object : Callback<MovieCreditsResponse> {
                override fun onResponse(
                    call: Call<MovieCreditsResponse>,
                    response: Response<MovieCreditsResponse>
                ) {
                    val movies = response.body()?.cast ?: return
                    binding.rvSearchResults.visibility = View.VISIBLE
                    binding.rvSearchResults.layoutManager = LinearLayoutManager(
                        this@MainActivity,
                        LinearLayoutManager.HORIZONTAL,
                        false
                    )
                    binding.rvSearchResults.adapter = MovieAdapter(movies) { movie ->
                        val intent = Intent(this@MainActivity, MovieDetailActivity::class.java).apply {
                            putExtra("title", movie.title ?: "")
                            putExtra("poster_path", movie.poster_path ?: "")
                            putExtra("overview", movie.overview ?: "")
                            putExtra("release_date", movie.release_date ?: "")
                            putExtra("vote_average", movie.vote_average?.toString() ?: "")
                        }
                        startActivity(intent)
                    }
                    hideOtherLists()
                }

                override fun onFailure(call: Call<MovieCreditsResponse>, t: Throwable) {
                    showOriginalLists()
                }
            })
    }

    private fun searchMovie(query: String) {
        getRetrofit().getSearchMovies(authToken, query).enqueue(object : Callback<Movie> {
            override fun onResponse(call: Call<Movie>, response: Response<Movie>) {
                val movies = response.body()?.results ?: return
                binding.rvSearchResults.adapter = MovieAdapter(movies) { movie ->
                    val intent = Intent(this@MainActivity, MovieDetailActivity::class.java).apply {
                        putExtra("title", movie.title ?: "")
                        putExtra("poster_path", movie.poster_path ?: "")
                        putExtra("overview", movie.overview ?: "")
                        putExtra("release_date", movie.release_date ?: "")
                        putExtra("vote_average", movie.vote_average?.toString() ?: "")
                    }
                    startActivity(intent)
                }
                binding.rvSearchResults.layoutManager = LinearLayoutManager(this@MainActivity, LinearLayoutManager.HORIZONTAL, false)
                binding.rvSearchResults.visibility = View.VISIBLE
                hideOtherLists()
            }

            override fun onFailure(call: Call<Movie>, t: Throwable) {
                showOriginalLists()
            }
        })
    }

    private fun hideOtherLists() {
        binding.rvPopular.visibility = View.GONE
        binding.rvUpcoming.visibility = View.GONE
        binding.rvTopRelated.visibility = View.GONE
        binding.txtPopular.visibility = View.GONE
        binding.txtUpcoming.visibility = View.GONE
        binding.txtTopRelated.visibility = View.GONE
    }

    private fun showOriginalLists() {
        binding.rvSearchResults.visibility = View.GONE
        binding.rvPopular.visibility = View.VISIBLE
        binding.rvUpcoming.visibility = View.VISIBLE
        binding.rvTopRelated.visibility = View.VISIBLE
        binding.txtPopular.visibility = View.VISIBLE
        binding.txtUpcoming.visibility = View.VISIBLE
        binding.txtTopRelated.visibility = View.VISIBLE
    }
//------------------------------------------------------------------------------------
    //Add Camera Permission Check
    private fun checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CAMERA),
                CAMERA_PERMISSION_REQUEST_CODE
            )
        } else {
            openCamera()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera()
            } else {
                Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }


//--------------------------------------------------------------------------------------------------
    private fun openGallery() {
        imagePicker.launch("image/*")
    }

//Add this function to show a dialog letting the user choose between camera and gallery:
    private fun showImageSourceDialog() {
        val options = arrayOf("Take Photo", "Choose from Gallery", "Cancel")
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Select Image Source")
        builder.setItems(options) { dialog, which ->
            when (which) {
                0 -> checkCameraPermission()
                1 -> openGallery()
                // 2 is Cancel, do nothing
            }
        }
        builder.show()
    }
    //-------------------------------------------------------------------------------------------
    private fun openCamera() {
        val photoFile: File? = try {
            createImageFile()
        } catch (ex: IOException) {
            Toast.makeText(this, "Error creating file", Toast.LENGTH_SHORT).show()
            null
        }

        photoFile?.also {
            val photoURI: Uri = FileProvider.getUriForFile(
                this,
                "${packageName}.fileprovider",
                it
            )
            cameraLauncher.launch(photoURI)
        }
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "JPEG_${timeStamp}_",
            ".jpg",
            storageDir
        ).apply {
            currentPhotoPath = absolutePath
        }
    }


    // Show profile dialog with large profile image and edit options
    private fun showProfileDialog() {
        // Create a fullscreen dialog without title bar
        val dialog = Dialog(this, android.R.style.Theme_Black_NoTitleBar_Fullscreen)

        // Set the custom layout for the dialog
        dialog.setContentView(R.layout.profile_dialog)

        // Find the large ImageView in the dialog layout
        val imgLarge = dialog.findViewById<ImageView>(R.id.imgLarge)

        // Store reference to update later if the image changes
        dialogProfileImage = imgLarge

        // Find the menu icon ImageView for edit options
        val menuIcon = dialog.findViewById<ImageView>(R.id.menuIcon)

        // Get current logged-in Firebase user
        val user = FirebaseAuth.getInstance().currentUser

        // Load user's profile photo into the large ImageView using Glide
        Glide.with(this)
            .load(user?.photoUrl)
            .into(imgLarge)

        // Set click listener for the edit menu
        menuIcon.setOnClickListener {
            showEditMenu(it, dialog) // Show edit options (name or image)
        }

        // Display the dialog
        dialog.show()
    }


    private fun showEditMenu(anchor: View, dialog: Dialog) {
        val popup = PopupMenu(this, anchor)
        popup.menuInflater.inflate(R.menu.edit_menu, popup.menu)
        popup.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.edit_name -> {
                    showEditNameDialog()
                    dialog.dismiss()
                    true
                }
                R.id.edit_image -> {
                    showImageSourceDialog()
                    dialog.dismiss()
                    true
                }
                R.id.delete_image -> {
                    showDeleteConfirmationDialog()
                    dialog.dismiss()
                    true
                }
                else -> false
            }
        }
        popup.show()
    }
    //----------------------------------------
    private fun showDeleteConfirmationDialog() {
        AlertDialog.Builder(this)
            .setTitle("Delete Profile Photo")
            .setMessage("Are you sure you want to delete your profile photo?")
            .setPositiveButton("Delete") { dialog, _ ->
                deleteProfilePhoto()
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }
    //---------------------------------------------------------------
    private fun deleteProfilePhoto() {
        val user = FirebaseAuth.getInstance().currentUser ?: return
        val databaseRef = FirebaseDatabase.getInstance().reference

        // Update Firebase Database
        databaseRef.child("users")
            .child(user.uid)
            .child("profileImageUrl")
            .setValue(DEFAULT_PROFILE_IMAGE_URL)
            .addOnSuccessListener {
                // Update Firebase Auth profile
                val profileUpdates = UserProfileChangeRequest.Builder()
                    .setPhotoUri(Uri.parse(DEFAULT_PROFILE_IMAGE_URL))
                    .build()

                user.updateProfile(profileUpdates)
                    .addOnSuccessListener {
                        // Update UI immediately
                        updateProfileImageUI(DEFAULT_PROFILE_IMAGE_URL)
                        Toast.makeText(this, "Profile photo deleted", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Failed to update profile: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to delete photo: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
    //----------------------------------------------------
    private fun updateProfileImageUI(imageUrl: String) {
        // Update navigation drawer image
        val headerView = binding.naviView.getHeaderView(0)
        val profileImage = headerView.findViewById<ImageView>(R.id.txtimage)
        Glide.with(this)
            .load(imageUrl)
            .circleCrop()
            .into(profileImage)

        // Update profile dialog image if open
        dialogProfileImage?.let { dialogImg ->
            Glide.with(this)
                .load(imageUrl)
                .circleCrop()
                .into(dialogImg)
        }

        // Clear Glide cache to ensure new image is loaded
        Glide.get(this).clearMemory()
        Thread {
            Glide.get(this).clearDiskCache()
        }.start()
    }

    private fun showEditNameDialog() {
        val editText = EditText(this)
        editText.hint = "Enter new name"
        AlertDialog.Builder(this)
            .setTitle("Edit Name")
            .setView(editText)
            .setPositiveButton("Save") { _, _ ->
                val newName = editText.text.toString()
                val user = FirebaseAuth.getInstance().currentUser
                val updates = UserProfileChangeRequest.Builder().setDisplayName(newName).build()
                user?.updateProfile(updates)?.addOnCompleteListener {
                    if (it.isSuccessful) {
                        showToast("Name updated")
                        recreate()
                    } else {
                        showToast("Failed to update name")
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun pickImageFromGallery() { imagePicker.launch("image/*") } // Helper: convert Uri to Base64 String

    private fun uriToBase64(uri: Uri): String? {
        return try {
            val inputStream: InputStream? = contentResolver.openInputStream(uri)
            val bytes = inputStream?.readBytes()
            inputStream?.close()
            bytes?.let { Base64.encodeToString(it, Base64.DEFAULT) }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun uploadImageToImgBB(uri: Uri) {
        // Convert image URI to Base64 string
        val base64Image = uriToBase64(uri)
        if (base64Image == null) {
            showToast("Failed to convert image")
            return
        }

        Log.d("Upload", "Base64 image length: ${base64Image.length}")

        // Setup Retrofit for ImgBB API
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.imgbb.com/1/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val apiService = retrofit.create(ImgBBService::class.java)
        val call = apiService.uploadImage(imgbbApiKey, base64Image)

        // Make API call asynchronously
        call.enqueue(object : Callback<ImgBBResponse> {
            override fun onResponse(call: Call<ImgBBResponse>, response: Response<ImgBBResponse>) {
                Log.d("Upload", "Response received: $response")

                if (response.isSuccessful && response.body()?.success == true) {
                    val imageUrl = response.body()?.data?.display_url ?: ""
                    Log.d("Upload", "Image URL: $imageUrl")

                    // Save the image URL to Firebase
                    saveImageUrlToFirebase(imageUrl)

                    // Update navigation drawer image
                    val headerView = binding.naviView.getHeaderView(0)
                    val profileImage = headerView.findViewById<ImageView>(R.id.txtimage)
                    Glide.with(this@MainActivity)
                        .load(imageUrl)
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .skipMemoryCache(true)
                        .into(profileImage)

                    // Update profile dialog image if open
                    dialogProfileImage?.let { dialogImg ->
                        Glide.with(this@MainActivity)
                            .load(imageUrl)
                            .diskCacheStrategy(DiskCacheStrategy.NONE)
                            .skipMemoryCache(true)
                            .into(dialogImg)
                    }

                } else {
                    val error = response.errorBody()?.string()
                    showToast("ImgBB upload failed: $error")
                    Log.e("Upload", "Error body: $error")
                }
            }

            override fun onFailure(call: Call<ImgBBResponse>, t: Throwable) {
                showToast("Upload error: ${t.localizedMessage}")
                Log.e("Upload", "Upload failed", t)
            }
        })
    }




    private fun saveImageUrlToFirebase(url: String) {
        val user = FirebaseAuth.getInstance().currentUser ?: return
        val databaseRef = FirebaseDatabase.getInstance().reference

        // Save in Realtime Database
        databaseRef.child("users")
            .child(user.uid)
            .child("profileImageUrl")
            .setValue(url)
            .addOnSuccessListener {
                showToast("Profile image updated!")

                // Update Firebase Auth profile
                val profileUpdates = UserProfileChangeRequest.Builder()
                    .setPhotoUri(Uri.parse(url))
                    .build()

                user.updateProfile(profileUpdates)
                    .addOnSuccessListener {
                        // Update navigation drawer image
                        val headerView = binding.naviView.getHeaderView(0)
                        val profileImage = headerView.findViewById<ImageView>(R.id.txtimage)

                        Glide.with(this)
                            .load(url)
                            .circleCrop()
                            .into(profileImage)

                        // Update profile dialog image if open
                        dialogProfileImage?.let { dialogImg ->
                            Glide.with(this)
                                .load(url)
                                .circleCrop()
                                .into(dialogImg)
                        }
                    }
                    .addOnFailureListener { e ->
                        showToast("Failed to update profile image in Firebase Auth: ${e.message}")
                    }
            }
            .addOnFailureListener { e ->
                showToast("Failed to save image URL: ${e.message}")
            }
    }

    // Helper function to show toast messages


    private fun loadCinemaList() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val userRef = FirebaseDatabase.getInstance().getReference("users/$userId/username") // FOR USER NAME HANDLING IN DATABASE

        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(userSnapshot: DataSnapshot) {
                val username = userSnapshot.getValue(String::class.java) ?: "guest" // fallback

                cinemasRef = FirebaseDatabase.getInstance().getReference("cinemas")
                cinemasRef.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        cinemaList.clear()
                        val adapter = CinemaAdapter(cinemaList) { selectedCinema ->
                            Toast.makeText(
                                this@MainActivity,
                                "Cinema selected: ${selectedCinema.name}",
                                Toast.LENGTH_SHORT
                            ).show()

                            val moviesMap = selectedCinema.movies ?: emptyMap()
                            val moviesList = moviesMap.values.toList()

                            if (moviesList.isNotEmpty()) {
                                binding.rvCinemaMovies.visibility = View.VISIBLE
                                binding.cinemaRecyclerView.visibility = View.GONE
                                isViewingCinemaMovies = true

                                val movieAdapter = CinemaMovieAdapter(moviesList) { selectedMovie ->
                                    val showtimeList = mutableListOf<ShowtimeItem>()

                                    selectedMovie.showtimes.forEach { (date, showtimeMap) ->
                                        showtimeMap.forEach { (showtimeId, slot) ->
                                            if (slot.availableSeats > 0 && slot.start.isNotBlank()) {
                                                showtimeList.add(
                                                    ShowtimeItem(
                                                        showtimeId = showtimeId,
                                                        date = date,
                                                        slot = slot
                                                    )
                                                )
                                            }
                                        }
                                    }

                                    if (showtimeList.isNotEmpty()) {
                                        binding.rvShowtimes.visibility = View.VISIBLE
                                        binding.rvCinemaMovies.visibility = View.GONE
                                        binding.rvShowtimes.adapter = null
                                        binding.rvShowtimes.layoutManager =
                                            LinearLayoutManager(this@MainActivity)

                                        val showtimeAdapter = ShowtimeAdapter(showtimeList) { selectedShowtimeItem ->
                                            val intent = Intent(this@MainActivity, SeatSelectionActivity::class.java).apply {
                                                putExtra("cinemaId", selectedCinema.id)
                                                putExtra("movieId", selectedMovie.id.toString())
                                                putExtra("date", selectedShowtimeItem.date)
                                                putExtra("showtimeId", selectedShowtimeItem.showtimeId)
                                                putExtra("availableSeats", selectedShowtimeItem.slot.availableSeats)
                                                putExtra("bookedSeats", HashMap(selectedShowtimeItem.slot.bookedSeats))
                                                putExtra("username", username) // ✅ Now works
                                            }
                                            startActivity(intent)
                                        }

                                        binding.rvShowtimes.adapter = showtimeAdapter
                                    } else {
                                        Toast.makeText(
                                            this@MainActivity,
                                            "No showtimes available for this movie",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }

                                binding.rvCinemaMovies.layoutManager = LinearLayoutManager(this@MainActivity)
                                binding.rvCinemaMovies.adapter = movieAdapter
                            } else {
                                Toast.makeText(
                                    this@MainActivity,
                                    "No movies available for this cinema",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }

                        for (cinemaSnap in snapshot.children) {
                            val cinema = cinemaSnap.getValue(Cinema::class.java)
                            if (cinema != null) {
                                cinema.id = cinemaSnap.key ?: ""
                                cinemaList.add(cinema)
                            }
                        }

                        binding.cinemaRecyclerView.adapter = adapter
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(
                            this@MainActivity,
                            "Failed to load cinemas: ${error.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                })
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@MainActivity, "Failed to load username", Toast.LENGTH_SHORT).show()
            }
        })
    }


    private fun showToast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (toggle.onOptionsItemSelected(item)) return true
        return super.onOptionsItemSelected(item)
    }
}
