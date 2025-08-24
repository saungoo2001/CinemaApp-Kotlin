package com.example.mc_movie.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.mc_movie.R
import com.example.mc_movie.model.CinemaMovieResponse

class CinemaMovieAdapter(
    private val movies: List<CinemaMovieResponse>,
    private val onMovieClick: (CinemaMovieResponse) -> Unit
) : RecyclerView.Adapter<CinemaMovieAdapter.MovieViewHolder>() {

    inner class MovieViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgPoster: ImageView = itemView.findViewById(R.id.imgPoster)
        val txtTitle: TextView = itemView.findViewById(R.id.txtTitle)
        val txtOverview: TextView = itemView.findViewById(R.id.txtOverview)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MovieViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_cinema_movie, parent, false)
        return MovieViewHolder(view)
    }

    override fun onBindViewHolder(holder: MovieViewHolder, position: Int) {
        val movie = movies[position]
        val posterPath = movie.poster_path
        val imageUrl = if (!posterPath.isNullOrEmpty()) "https://image.tmdb.org/t/p/w500$posterPath" else null

        Log.d("CinemaMovieAdapter", "Loading image URL: $imageUrl") // Add this log

        Glide.with(holder.imgPoster.context)
            .load(imageUrl)
            .into(holder.imgPoster)

        holder.txtTitle.text = movie.title
        holder.txtOverview.text = movie.overview
        holder.itemView.setOnClickListener {
            onMovieClick(movie)
        }
    }


    override fun getItemCount() = movies.size
}