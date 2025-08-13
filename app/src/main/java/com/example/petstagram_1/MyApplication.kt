package com.example.petstagram_1

import android.app.Application
import com.cloudinary.android.MediaManager

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // IMPORTANT: Replace these with your actual Cloudinary credentials
        val config = mapOf(
            "cloud_name" to "ddbzqh2vw",
            "api_key" to "799637424972498",
            "api_secret" to "lavGiN-fNbR1XZ9qx4dDejCurLs"
        )
        MediaManager.init(this, config)
    }
}
