package com.example.mc_movie.model

data class MovieCreditsResponse(
    val cast: List<Result>,
    val crew: List<Result>
)
