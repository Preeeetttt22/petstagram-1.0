package com.example.petstagram_1.ui

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.petstagram_1.R
import com.example.petstagram_1.auth.LoginActivity
import com.example.petstagram_1.databinding.ActivityMainBinding
import com.example.petstagram_1.models.User
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    // Added a variable to hold the NavController
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        setSupportActionBar(binding.toolbar)

        // --- THE FIX IS HERE ---
        // 1. Get the NavHostFragment from the layout.
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment_content_main) as NavHostFragment
        // 2. Get the NavController from the NavHostFragment.
        navController = navHostFragment.navController

        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView

        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_dashboard, R.id.nav_profile, R.id.nav_settings, R.id.nav_admin_panel
            ), drawerLayout
        )

        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        checkUserRoleAndUpdateUI()
    }

    private fun checkUserRoleAndUpdateUI() {
        val uid = firebaseAuth.currentUser?.uid ?: return

        firestore.collection("users").document(uid).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val user = document.toObject(User::class.java)
                    val navView: NavigationView = binding.navView
                    val menu: Menu = navView.menu
                    val adminMenuItem = menu.findItem(R.id.nav_admin_panel)

                    adminMenuItem?.isVisible = user?.role == "Admin"
                }
            }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_logout -> {
                firebaseAuth.signOut()
                val intent = Intent(this, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        // Use the navController variable we defined earlier
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}
