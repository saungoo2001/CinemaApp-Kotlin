package com.example.mc_movie.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.mc_movie.R

import com.example.mc_movie.model.SettingOption

class SettingsAdapter(
    private val items: List<SettingOption>,
    private val onItemClick: (SettingOption) -> Unit
) : RecyclerView.Adapter<SettingsAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val icon: ImageView = itemView.findViewById(R.id.itemIcon)
        val title: TextView = itemView.findViewById(R.id.itemTitle)

        init {
            itemView.setOnClickListener {
                onItemClick(items[adapterPosition])
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_setting_option, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.title.text = item.title
        holder.icon.setImageResource(item.iconResId)
    }
}
