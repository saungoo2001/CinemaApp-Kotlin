package com.example.mc_movie.apiservice

import ImgBBService
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ImgBBRetrofitClient {
    private const val BASE_URL = "https://api.imgbb.com/1/"

    val apiService: ImgBBService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ImgBBService::class.java)
    }
}
