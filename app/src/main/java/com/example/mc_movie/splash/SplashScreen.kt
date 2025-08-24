package com.example.mc_movie.splash

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.mc_movie.MainActivity
import com.example.mc_movie.R
import com.example.mc_movie.auth.login

class SplashScreen : AppCompatActivity() {
    private lateinit var splashImg :ImageView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_splash_screen)
        splashImg=findViewById(R.id.splashImg)
        splashImg.alpha =0f
        splashImg.animate().setDuration(2000).alpha(1f).withEndAction{
            overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out)
            finish()
            val shp = getSharedPreferences("LoginInfo", MODE_PRIVATE)
            if(shp.getBoolean("Login",false)){
                startActivity(Intent(this,MainActivity::class.java))

            }else{
                startActivity(Intent(this,login::class.java))
            }

        }
    }
}