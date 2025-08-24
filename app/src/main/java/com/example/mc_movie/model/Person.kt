package com.example.mc_movie.model

data class Person(
    val page: Int,
    val results: List<PersonResult>,
    val total_pages: Int,
    val total_results: Int
)