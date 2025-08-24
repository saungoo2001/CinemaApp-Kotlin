package com.example.mc_movie.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.mc_movie.R
import com.example.mc_movie.model.Cinema

class CinemaAdapter(
    private val cinemas: List<Cinema>,
    private val onItemClick: (Cinema) -> Unit
) : RecyclerView.Adapter<CinemaAdapter.CinemaViewHolder>() {

    inner class CinemaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameText = itemView.findViewById<TextView>(R.id.cinemaName)
        val locationText = itemView.findViewById<TextView>(R.id.cinemaLocation)

        fun bind(cinema: Cinema) {
            nameText.text = cinema.name
            locationText.text = cinema.location
            itemView.setOnClickListener { onItemClick(cinema) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CinemaViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_cinema, parent, false)
        return CinemaViewHolder(view)
    }

    override fun onBindViewHolder(holder: CinemaViewHolder, position: Int) {
        holder.bind(cinemas[position])
    }

    override fun getItemCount() = cinemas.size
}