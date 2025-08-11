package com.example.petstagram_1.auth

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.example.petstagram_1.R
import com.example.petstagram_1.ui.MainActivity
import com.google.firebase.auth.FirebaseAuth

// THE FIX IS HERE: It must extend AppCompatActivity
class SplashActivity : AppCompatActivity() {

    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        firebaseAuth = FirebaseAuth.getInstance()

        // Use a Handler to delay the screen transition
        Handler(Looper.getMainLooper()).postDelayed({
            checkUserStatus()
        }, 2000) // 2-second delay
    }

    private fun checkUserStatus() {
        val currentUser = firebaseAuth.currentUser
        if (currentUser != null) {
            // User is logged in, go to MainActivity
            startActivity(Intent(this, MainActivity::class.java))
        } else {
            // User is not logged in, go to LoginActivity
            startActivity(Intent(this, LoginActivity::class.java))
        }
        // Finish SplashActivity so the user can't go back to it
        finish()
    }
}
