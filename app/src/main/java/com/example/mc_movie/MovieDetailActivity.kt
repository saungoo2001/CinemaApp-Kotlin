package com.example.mc_movie

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide

class MovieDetailActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_movie_detail)

        val imgPoster = findViewById<ImageView>(R.id.posterImage)
        val txtTitle = findViewById<TextView>(R.id.titleText)
        val txtOverview = findViewById<TextView>(R.id.overviewText)
        val txtReleaseDate = findViewById<TextView>(R.id.releaseDateText)
        val txtRating = findViewById<TextView>(R.id.ratingText)

        val title = intent.getStringExtra("title")
        val posterPath = intent.getStringExtra("poster_path")
        val overview = intent.getStringExtra("overview")
        val releaseDate = intent.getStringExtra("release_date")
        val rating = intent.getStringExtra("vote_average")

        txtTitle.text = title
        txtOverview.text = overview
        txtReleaseDate.text = "Release Date: $releaseDate"
        txtRating.text = "Rating: $rating/10"

        Glide.with(this)
            .load("https://image.tmdb.org/t/p/w500$posterPath")
            .into(imgPoster)
    }
}
