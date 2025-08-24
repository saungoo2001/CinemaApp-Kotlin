package com.example.mc_movie

import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.ImageView
import com.bumptech.glide.Glide

class ImagePreviewActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_preview)

        val imageView = findViewById<ImageView>(R.id.imagePreview)

        // Get image path/URL from intent
        val imageUri = intent.getStringExtra("image_uri")

        if (!imageUri.isNullOrEmpty()) {
            // Load with Glide (works for both URLs and file paths)
            Glide.with(this)
                .load(imageUri)
                .into(imageView)
        }
    }
}