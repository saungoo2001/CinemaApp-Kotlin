package com.example.mc_movie.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.mc_movie.R

import com.example.mc_movie.model.ShowtimeItem

class ShowtimeAdapter(
    private val showtimeItems: List<ShowtimeItem>,
    private val onClick: (ShowtimeItem) -> Unit
) : RecyclerView.Adapter<ShowtimeAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val txtDate: TextView = view.findViewById(R.id.txtDate)
        val txtTime: TextView = view.findViewById(R.id.txtTime)
        val txtSeats: TextView = view.findViewById(R.id.txtSeats)

        fun bind(item: ShowtimeItem) {
            txtDate.text = item.date
            txtTime.text = "${item.slot.start} - ${item.slot.end}"
            txtSeats.text = "Seats: ${item.slot.availableSeats}"
            itemView.setOnClickListener { onClick(item) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_showtime, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(showtimeItems[position])
    }

    override fun getItemCount(): Int = showtimeItems.size
}

