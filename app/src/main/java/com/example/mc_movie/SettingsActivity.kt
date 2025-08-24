// SettingsActivity.kt
package com.example.mc_movie

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.mc_movie.adapter.SettingsAdapter
import com.example.mc_movie.auth.login
import com.example.mc_movie.model.SettingOption
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.CollapsingToolbarLayout
import com.google.firebase.auth.FirebaseAuth

class SettingsActivity : AppCompatActivity() {

    private lateinit var profileImage: ImageView
    private lateinit var stretchImage: ImageView
    private lateinit var profileName: TextView
    private lateinit var headerContent: View
    private lateinit var headerContainer: View
    private lateinit var collapsingToolbarLayout: CollapsingToolbarLayout
    private lateinit var appBarLayout: AppBarLayout
    private lateinit var toolbar: Toolbar
    private lateinit var recyclerView: RecyclerView
    private lateinit var rootLayout: View

    private var initialY = 0f
    private var isPulling = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        loadTheme() // Load theme before layout
        setContentView(R.layout.activity_setting)

        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            val intent = Intent(this, login::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            return
        }

        rootLayout = findViewById(R.id.rootLayout)
        appBarLayout = findViewById(R.id.appBarLayout)
        collapsingToolbarLayout = findViewById(R.id.collapsingToolbarLayout)
        toolbar = findViewById(R.id.toolbar)
        headerContainer = findViewById(R.id.headerContainer)
        headerContent = findViewById(R.id.headerContent)
        stretchImage = findViewById(R.id.stretchImage)
        profileImage = findViewById(R.id.profileImage)
        profileName = findViewById(R.id.profileName)
        recyclerView = findViewById(R.id.settingsRecyclerView)

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Settings"

        collapsingToolbarLayout.title = " "
        collapsingToolbarLayout.isTitleEnabled = false
        collapsingToolbarLayout.contentScrim = null

        recyclerView.layoutManager = LinearLayoutManager(this)
        loadUserInfo()

        val settingItems = mutableListOf(
            SettingOption("Change Mode", getInitialThemeIcon()),
            SettingOption("Log Out", R.drawable.logout_icon)
        )


        lateinit var adapter: SettingsAdapter  // declare it above

        adapter = SettingsAdapter(settingItems) { selected: SettingOption ->
            when (selected.title) {
                "Change Mode" -> toggleTheme(selected, adapter)
                "Log Out" -> showLogoutDialog()
            }
        }
        recyclerView.adapter = adapter

        headerContainer.scaleX = 1f
        headerContainer.scaleY = 1f

        rootLayout.setOnTouchListener { _, event ->
            if (recyclerView.canScrollVertically(-1)) return@setOnTouchListener false

            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialY = event.y
                    isPulling = true
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    val deltaY = event.y - initialY
                    if (deltaY > 0 && isPulling) {
                        val scale = 1f + (deltaY / 1000f)
                        stretchImage.scaleX = scale
                        stretchImage.scaleY = scale

                        stretchImage.visibility = View.VISIBLE
                        stretchImage.alpha = (deltaY - 100).coerceAtLeast(0f) / 200f

                        val alphaValue = 1 - ((deltaY - 100).coerceAtLeast(0f) / 200f)
                        headerContent.alpha = alphaValue
                        headerContent.visibility = if (alphaValue <= 0f) View.GONE else View.VISIBLE
                        true
                    } else false
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    stretchImage.animate().scaleX(1f).scaleY(1f).alpha(0.5f).setDuration(500).start()
                    stretchImage.visibility = View.VISIBLE
                    headerContent.alpha = 1f
                    headerContent.visibility = View.VISIBLE
                    isPulling = false
                    true
                }
                else -> false
            }
        }

        appBarLayout.addOnOffsetChangedListener { _, _ -> }
    }

    private fun loadUserInfo() {
        val user = FirebaseAuth.getInstance().currentUser
        val displayName = user?.displayName ?: user?.email ?: "Guest"
        val photoUrl = user?.photoUrl

        profileName.text = displayName
        val defaultImage = R.drawable.profile_one

        Glide.with(this)
            .load(photoUrl ?: defaultImage)
            .circleCrop()
            .into(profileImage)

        Glide.with(this)
            .load(photoUrl ?: defaultImage)
            .centerCrop()
            .into(stretchImage)
    }

    private fun toggleTheme(settingItem: SettingOption, adapter: SettingsAdapter) {
        val nightMode = AppCompatDelegate.getDefaultNightMode()
        val newMode = if (nightMode == AppCompatDelegate.MODE_NIGHT_YES) {
            AppCompatDelegate.MODE_NIGHT_NO
        } else {
            AppCompatDelegate.MODE_NIGHT_YES
        }
        saveTheme(newMode)
        AppCompatDelegate.setDefaultNightMode(newMode)
        settingItem.iconResId = getInitialThemeIcon()
        adapter.notifyDataSetChanged()
    }

    private fun getInitialThemeIcon(): Int {
        return if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES) {
            R.drawable.day_mode_icon
        } else {
            R.drawable.changemode_icon
        }
    }

    private fun saveTheme(mode: Int) {
        getSharedPreferences("ThemePrefs", MODE_PRIVATE).edit().putInt("theme_mode", mode).apply()
    }

    private fun loadTheme() {
        val mode = getSharedPreferences("ThemePrefs", MODE_PRIVATE)
            .getInt("theme_mode", AppCompatDelegate.MODE_NIGHT_NO)
        AppCompatDelegate.setDefaultNightMode(mode)
    }

    private fun showLogoutDialog() {
        AlertDialog.Builder(this)
            .setTitle("Confirm Log Out")
            .setMessage("Are you sure you want to log out?")
            .setPositiveButton("Yes") { _, _ ->
                FirebaseAuth.getInstance().signOut()
                val intent = Intent(this, login::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}
