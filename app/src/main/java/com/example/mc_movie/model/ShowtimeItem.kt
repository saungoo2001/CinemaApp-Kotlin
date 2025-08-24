package com.example.mc_movie.model

import java.io.Serializable

data class ShowtimeItem(
    val showtimeId: String,
    val date: String,

    val slot: ShowtimeSlot
): Serializable
