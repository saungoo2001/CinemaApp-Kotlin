package com.example.mc_movie.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.mc_movie.R
import com.example.mc_movie.model.PersonResult

class PersonAdapter(
    private val personList: List<PersonResult>,
    private val onPersonClick: (PersonResult) -> Unit
) : RecyclerView.Adapter<PersonAdapter.PersonViewHolder>() {

    inner class PersonViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var imgProfile = itemView.findViewById<ImageView>(R.id.imgMovie)
        val txtName = itemView.findViewById<TextView>(R.id.txtmovie)

        init {
            itemView.setOnClickListener {
                onPersonClick(personList[adapterPosition])
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PersonViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.movie_item, parent, false)
        return PersonViewHolder(view)
    }

    override fun getItemCount(): Int = personList.size

    override fun onBindViewHolder(holder: PersonViewHolder, position: Int) {
        Glide.with(holder.itemView.context)
            .load("https://image.tmdb.org/t/p/w500" + personList[position].profile_path)
            .into(holder.imgProfile)
        holder.txtName.text = personList[position].name
    }
}
