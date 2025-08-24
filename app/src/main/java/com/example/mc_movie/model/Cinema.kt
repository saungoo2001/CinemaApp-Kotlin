package com.example.mc_movie.model

data class Cinema(
    var id: String = "",
    val name:String?=null,
    val location:String?=null,
    val movies:Map<String,CinemaMovieResponse>?=null
)
