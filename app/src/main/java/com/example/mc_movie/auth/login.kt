package com.example.mc_movie.auth

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.mc_movie.MainActivity
import com.example.mc_movie.R
import com.example.mc_movie.databinding.ActivityLoginBinding
import com.google.firebase.ktx.Firebase
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.initialize
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.auth.FirebaseUser

class login : AppCompatActivity() {
    private lateinit var binding : ActivityLoginBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Firebase.initialize(this)
        enableEdgeToEdge()
        binding= ActivityLoginBinding.inflate(layoutInflater)
        binding.btnLogin.setOnClickListener {
            var mail = binding.tiMail.text.toString()
            var pin = binding.tiPin.text.toString()
            var username=binding.tiName.text.toString()
            if(TextUtils.isEmpty(mail)) {
                binding.tiMail.error = "Email is required"
                binding.tiMail.requestFocus()

            }else if(TextUtils.isEmpty(pin)){
                binding.tiPin.error = "Pin is Required"
                binding.tiPin.requestFocus()
            }else{
                loginAccount(mail,pin,username)
            }
        }

        binding.btnSignUp.setOnClickListener {
            startActivity(Intent(this,SignUp::class.java))
        }
        binding.txtForgot.setOnClickListener{
            var mail = binding.tiMail.text.toString()
            if(TextUtils.isEmpty(mail)) {
                binding.tiMail.error = "Email is required"
                binding.tiMail.requestFocus()
            }else{
                forgetPin(mail)


            }

        }


        setContentView(binding.root)

    }
    override fun onStart() {
        super.onStart()

        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            // User is logged in, send to main app
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }


    fun forgetPin(mail:String){
       FirebaseAuth.getInstance().sendPasswordResetEmail(mail).addOnCompleteListener { task ->
           if(task.isSuccessful){
               startActivity(Intent(this,ForgetPassword::class.java))
           }else{
               Toast.makeText(this,"Reset Failed",Toast.LENGTH_SHORT).show()
           }
       }
    }
    fun loginAccount(mail: String, pin: String, usernameInput: String) {
        FirebaseAuth.getInstance().signInWithEmailAndPassword(mail, pin).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val user = FirebaseAuth.getInstance().currentUser

                // Update username in Realtime Database if provided
                user?.let {
                    val usernameToSave = if (usernameInput.isNotBlank()) usernameInput else (user.displayName ?: user.email ?: "guest")
                    val databaseRef = FirebaseDatabase.getInstance().reference
                    databaseRef.child("users").child(user.uid).child("username").setValue(usernameToSave)
                }

                val shp = getSharedPreferences("LoginInfo", MODE_PRIVATE)
                val editor = shp.edit()
                editor.putBoolean("Login", true)
                editor.commit()
                Toast.makeText(this, "Login Success", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, MainActivity::class.java))

            } else {
                Toast.makeText(this, "Login Failed", Toast.LENGTH_SHORT).show()
            }
        }
    }

}