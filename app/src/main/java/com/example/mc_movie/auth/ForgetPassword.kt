package com.example.mc_movie.auth

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.mc_movie.R
import com.example.mc_movie.databinding.ActivityForgetPasswordBinding

class ForgetPassword : AppCompatActivity() {
    private lateinit var binding : ActivityForgetPasswordBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding=ActivityForgetPasswordBinding.inflate(layoutInflater)
        binding.btnlogin.setOnClickListener {
            startActivity(Intent(this,login::class.java))
        }

            setContentView(binding.root)

    }
}