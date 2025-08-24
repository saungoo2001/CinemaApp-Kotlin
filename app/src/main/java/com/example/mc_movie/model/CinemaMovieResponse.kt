package com.example.mc_movie.model

import com.google.gson.annotations.SerializedName

data class CinemaMovieResponse(
    val id: Int = 0,
    val title: String = "",
    val overview: String = "",
    @SerializedName("poster_path") val poster_path: String? = null,
    val genres: List<Genre> = emptyList(),

    val showtimes: ShowtimeMap = emptyMap()
) {
    // Explicit no-arg constructor for Firebase (Java compatibility)
    constructor() : this(0, "", "", null, emptyList())
}

data class Genre(
    val id: Int = 0,
    val name: String = ""
){
    constructor() : this(0, "")
}


data class ShowtimeSlot(
    val start: String = "",
    val end: String = "",
    val availableSeats: Int = 0,
    val bookedSeats: Map<String, List<String>> = emptyMap()
)
typealias ShowtimeMap = Map<String, Map<String, ShowtimeSlot>>



