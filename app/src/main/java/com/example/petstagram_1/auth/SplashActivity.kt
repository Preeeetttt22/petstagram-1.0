package com.example.petstagram_1.auth

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.example.petstagram_1.R
import com.example.petstagram_1.models.User
import com.example.petstagram_1.ui.MainActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class SplashActivity : AppCompatActivity() {

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        firebaseAuth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        Handler(Looper.getMainLooper()).postDelayed({
            checkUserStatus()
        }, 2000) // 2-second delay
    }

    private fun checkUserStatus() {
        val currentUser = firebaseAuth.currentUser
        if (currentUser != null) {
            // User is logged in, check their role
            firestore.collection("users").document(currentUser.uid).get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val user = document.toObject(User::class.java)
                        val intent = Intent(this, MainActivity::class.java)

                        if (user?.role == "Veterinarian") {
                            // It's a vet, tell MainActivity to open the vet dashboard
                            intent.putExtra("userRole", "Veterinarian")
                        }
                        // For any other role, just start MainActivity normally
                        startActivity(intent)
                        finish()
                    } else {
                        // Failsafe in case user document doesn't exist
                        goToLoginActivity()
                    }
                }
                .addOnFailureListener {
                    // Handle error, go to login
                    goToLoginActivity()
                }
        } else {
            // User is not logged in, go to LoginActivity
            goToLoginActivity()
        }
    }

    private fun goToLoginActivity() {
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }
}
