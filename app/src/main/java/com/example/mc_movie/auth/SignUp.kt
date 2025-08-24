package com.example.mc_movie.auth

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.mc_movie.R
import com.example.mc_movie.R.layout.activity_sign_up
import com.example.mc_movie.databinding.ActivitySignUpBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.database.FirebaseDatabase
import android.net.Uri

class SignUp : AppCompatActivity() {
    private lateinit var binding: ActivitySignUpBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivitySignUpBinding.inflate(layoutInflater)

        binding.btnLogin.setOnClickListener {
            var mail = binding.tiMail.text.toString()
            var pin = binding.tiPin.text.toString()
            val name= binding.tiName.text.toString()
            //Error controlling
            if (TextUtils.isEmpty(mail)) {
                binding.tiMail.error = "Email is required"
                binding.tiMail.requestFocus()

            } else if (TextUtils.isEmpty(pin)) {
                binding.tiPin.error = "Pin is Required"
                binding.tiPin.requestFocus()
            } else {
                registerAccount(name,mail,pin)
            }
        }

        setContentView(binding.root)

    }

    fun registerAccount(name: String, mail: String, pin: String) {
        val auth = FirebaseAuth.getInstance()

        auth.createUserWithEmailAndPassword(mail, pin)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser

                    // Set display name
                    val profileUpdates = UserProfileChangeRequest.Builder()
                        .setDisplayName(name)
                        .build()

                    user?.updateProfile(profileUpdates)
                        ?.addOnCompleteListener { updateTask ->
                            if (updateTask.isSuccessful) {
                                // Save username to Realtime Database
                                user?.uid?.let { uid ->
                                    val databaseRef = FirebaseDatabase.getInstance().reference
                                    databaseRef.child("users").child(uid).child("username").setValue(name)
                                    databaseRef.child("users").child(uid).child("email").setValue(mail)
                                    user.photoUrl?.let { photoUri ->
                                        databaseRef.child("users").child(uid).child("profileImageUrl").setValue(photoUri.toString())
                                    }
                                }

                                Toast.makeText(
                                    this,
                                    "User Created. Please login again.",
                                    Toast.LENGTH_SHORT
                                ).show()
                                finish()
                                onBackPressed()
                            } else {
                                Toast.makeText(
                                    this,
                                    "Failed to update profile name",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }

                } else {
                    Toast.makeText(
                        this,
                        "User creation failed: ${task.exception?.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }

}
