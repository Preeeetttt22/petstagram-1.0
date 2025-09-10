package com.example.petstagram_1.auth

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.RadioButton
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.petstagram_1.R
import com.example.petstagram_1.databinding.ActivitySignupBinding
import com.example.petstagram_1.models.User
import com.example.petstagram_1.ui.MainActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore

class SignupActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignupBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var googleSignInClient: GoogleSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        binding.textGoToLogin.setOnClickListener {
            finish()
        }

        binding.buttonSignUp.setOnClickListener {
            validateAndCreateUserWithEmail()
        }

        binding.buttonSignUpGoogle.setOnClickListener {
            signUpWithGoogle()
        }
    }

    private fun validateAndCreateUserWithEmail() {
        val username = binding.inputUsername.text.toString().trim()
        val email = binding.inputEmail.text.toString().trim()
        val password = binding.inputPassword.text.toString().trim()
        val confirmPassword = binding.inputConfirmPassword.text.toString().trim()

        if (username.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this, "All fields are required.", Toast.LENGTH_SHORT).show()
            return
        }
        if (password != confirmPassword) {
            Toast.makeText(this, "Passwords do not match.", Toast.LENGTH_SHORT).show()
            return
        }
        if (password.length < 6) {
            Toast.makeText(this, "Password must be at least 6 characters long.", Toast.LENGTH_SHORT).show()
            return
        }

        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val firebaseUser = firebaseAuth.currentUser
                    firebaseUser?.sendEmailVerification()?.addOnCompleteListener {
                        saveUserDataToFirestore(firebaseUser, username, isGoogleSignIn = false)
                    }
                } else {
                    Toast.makeText(this, "Signup failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                }
            }
    }

    private fun signUpWithGoogle() {
        // --- THIS IS THE FIX ---
        // Sign out of the Google client first to force the account picker dialog.
        googleSignInClient.signOut().addOnCompleteListener {
            val signInIntent = googleSignInClient.signInIntent
            googleSignInLauncher.launch(signInIntent)
        }
    }

    private val googleSignInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)!!
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                Toast.makeText(this, "Google Sign-In failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        firebaseAuth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = firebaseAuth.currentUser
                    saveUserDataToFirestore(user, user?.displayName ?: "User", isGoogleSignIn = true)
                } else {
                    Toast.makeText(this, "Firebase authentication failed.", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun saveUserDataToFirestore(firebaseUser: FirebaseUser?, username: String, isGoogleSignIn: Boolean) {
        if (firebaseUser == null) return
        val uid = firebaseUser.uid
        val email = firebaseUser.email

        val userRef = firestore.collection("users").document(uid)
        userRef.get().addOnSuccessListener { document ->
            if (!document.exists()) {
                val selectedRoleId = binding.rolesRadioGroup.checkedRadioButtonId
                val selectedRoleButton = findViewById<RadioButton>(selectedRoleId)
                val selectedRole = selectedRoleButton?.text?.toString() ?: "User"
                val user = User(uid = uid, username = username, email = email, role = selectedRole)

                userRef.set(user)
                    .addOnSuccessListener {
                        if (isGoogleSignIn) navigateToMainActivity() else showEmailVerificationAndRedirect()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Failed to save user data: ${e.message}", Toast.LENGTH_LONG).show()
                    }
            } else {
                // If user already exists, just proceed
                if (isGoogleSignIn) navigateToMainActivity() else showEmailVerificationAndRedirect()
            }
        }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to check user data: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun showEmailVerificationAndRedirect() {
        Toast.makeText(this, "Account created! Please check your inbox to verify your email.", Toast.LENGTH_LONG).show()
        firebaseAuth.signOut()
        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this, LoginActivity::class.java))
            finishAffinity()
        }, 3000)
    }

    private fun navigateToMainActivity() {
        Toast.makeText(this, "Login Successful!", Toast.LENGTH_SHORT).show()
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
