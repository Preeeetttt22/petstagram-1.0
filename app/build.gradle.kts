plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.gms.google-services")
    id("kotlin-kapt")
    id("androidx.navigation.safeargs.kotlin")
}

android {
    namespace = "com.example.petstagram_1"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.petstagram_1"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    // Add this block to enable ViewBinding
    buildFeatures {
        viewBinding = true
    }
}

dependencies {

    // Core Android & Navigation Dependencies
    implementation("androidx.core:core-ktx:1.12.0") // <-- CHANGE THIS LINE
    implementation("androidx.appcompat:appcompat:1.7.1")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.2.1")
    implementation("androidx.navigation:navigation-fragment-ktx:2.7.7")
    implementation("androidx.navigation:navigation-ui-ktx:2.7.7")

    // Testing Dependencies
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.3.0")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.7.0")

    // --- ADDED DEPENDENCIES FOR YOUR PROJECT ---

    // Firebase Bill of Materials (BoM) - Manages versions for other Firebase libs
    implementation(platform("com.google.firebase:firebase-bom:32.7.0"))

    // Firebase Authentication & Google Sign-In
    implementation("com.google.firebase:firebase-auth-ktx")
    implementation("com.google.android.gms:play-services-auth:21.2.0")

    // Firebase Firestore Database
    implementation("com.google.firebase:firebase-firestore-ktx")

    // --- ADD THIS LINE FOR FIREBASE STORAGE ---
    implementation("com.google.firebase:firebase-storage-ktx")

    // Google Maps for Vet Appointments
    implementation("com.google.android.gms:play-services-maps:18.2.0")
    implementation("com.google.android.gms:play-services-location:21.3.0")

    // Gemini AI for AI Assistant
    // (Note: Check for the latest version of the Gemini SDK when implementing)
    implementation("com.google.ai.client.generativeai:generativeai:0.6.0")

    //Image Loading
    implementation("com.github.bumptech.glide:glide:4.16.0")
    kapt("com.github.bumptech.glide:compiler:4.16.0")

    implementation("com.cloudinary:cloudinary-android:2.4.0")
}
