package com.example.mc_movie.apiservice


import com.example.mc_movie.model.CinemaMovieResponse
import com.example.mc_movie.model.Movie
import com.example.mc_movie.model.MovieCreditsResponse
import com.example.mc_movie.model.Person
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path
import retrofit2.http.Query

interface TMDBServiceAPI {
    @GET("movie/popular")
    fun getPopularMovies(
        @Header("Authorization") authToken: String,

    @Header("accept") acceptHeader: String = "application/json",
    @Query("language") language: String = "en-US",
    @Query("page") page: Int = 1
    ): Call<Movie>

    @GET("movie/upcoming")
    fun getUpcomingMovies(
        @Header("Authorization") authToken: String,

        @Header("accept") acceptHeader: String = "application/json",
        @Query("language") language: String = "en-US",
        @Query("page") page: Int = 1
    ): Call<Movie>

    @GET("movie/top_rated")
    fun getTopRelatedMovies(
        @Header("Authorization") authToken: String,

        @Header("accept") acceptHeader: String = "application/json",
        @Query("language") language: String = "en-US",
        @Query("page") page: Int = 1
    ): Call<Movie>

    @GET("search/movie")
    fun getSearchMovies(
        @Header("Authorization") authToken: String,
        @Query("query") query: String,
        @Header("accept") acceptHeader: String = "application/json",
        @Query("language") language: String = "en-US",
        @Query("page") page: Int = 1
    ): Call<Movie>

    @GET("search/person")
    fun getSearchPerson(
        @Header("Authorization") authToken: String,
        @Query("query") query: String,
        @Header("accept") acceptHeader: String = "application/json",
        @Query("language") language: String = "en-US",
        @Query("page") page: Int = 1
    ): Call<Person>

    @GET("person/{person_id}/movie_credits")
    fun getPersonMovieCredits(
        @Header("Authorization") authToken: String,
        @Path("person_id") personId: Int
    ): Call<MovieCreditsResponse>

    @GET("movie/{movie_id}")
    fun getMovieDetails(
        @Path("movie_id") movieId: String,
        @Query("api_key") apiKey: String
    ): Call<CinemaMovieResponse>


}