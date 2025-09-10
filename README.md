Petstagram - The Social App for Pet Lovers & Vets

- Petstagram is a comprehensive Android application designed to bridge the gap between pet owners and veterinary professionals. It provides a dedicated platform for users to manage their pets' health records, book appointments, and connect with a community, while offering veterinarians the tools to manage their schedules and professional profiles.

✨ Key Features
The app is tailored to two main user roles, providing a unique experience for each.

- For Pet Owners 🐾
Authentication: Secure sign-up and login using Email/Password and Google Sign-In.

1.Pet Profiles: Create and manage detailed profiles for multiple pets.

2.Appointment Booking: Search for veterinarians by clinic name and book appointments directly through the app.

3.Medical Records: View and manage your pet's health records.

4.Community Forum: Engage with other pet owners, share experiences, and ask questions.

5.AI Assistant: Get quick answers to common pet-related questions, powered by the Gemini API.

- For Veterinarians 🩺
1.Role-Based Login: A separate, dedicated experience for veterinary professionals.

2.Vet Dashboard: A dashboard with at-a-glance statistics for today's appointments, patients seen, and completed consultations.

3.Schedule Management: A dedicated screen to view and manage the full daily schedule of appointments.

4.Profile Management: Vets can create and edit their professional profiles, including specialization, clinic name, address, and contact information.

Cloudinary Integration: Profile pictures are seamlessly uploaded and managed using Cloudinary.

🛠️ Technology Stack
Language: Kotlin

Architecture: MVVM (Model-View-ViewModel)

UI: Android XML with Material Design 3

Asynchronous Programming: Coroutines

Backend & Database: Firebase

Authentication: For user sign-up and login.

Firestore: As the primary NoSQL database for storing user data, pet profiles, and appointments.

AI Integration: Google Gemini API for the AI Assistant feature.

Image Storage: Cloudinary for hosting vet profile pictures.

Image Loading: Glide for efficiently loading and caching images.

Navigation: Android Jetpack Navigation Component.

🚀 Getting Started
To get a local copy up and running, follow these simple steps.

- Prerequisites
Android Studio Flamingo or later

A Firebase project

A Cloudinary account

A Google Gemini API Key

- Installation
Clone the repository:

git clone [https://github.com/your-username/petstagram.git](https://github.com/your-username/petstagram.git)

- Open in Android Studio:

Open Android Studio and select "Open an Existing Project".

Navigate to the cloned repository and open it.

- Firebase Configuration:

Go to your Firebase Console and create a new project.

Add an Android app to your Firebase project with the package name com.example.petstagram_1.

Follow the instructions to download the google-services.json file.

Place the downloaded google-services.json file into the app/ directory of your project.

In the Firebase Console, enable Authentication (Email/Password and Google providers) and Firestore Database.

- Cloudinary Configuration:

Open the EditVetProfileFragment.kt file.

Replace the placeholder credentials with your own Cloudinary Cloud Name, API Key, and API Secret.

private val CLOUD_NAME = "YOUR_CLOUD_NAME"
private val API_KEY = "YOUR_API_KEY"
private val API_SECRET = "YOUR_API_SECRET"

- Gemini API Key Configuration:

Go to Google AI Studio to create your API key.

Open the AiAssistantFragment.kt file.

Replace the placeholder constant with your generated Gemini API key.

private const val GEMINI_API_KEY = "YOUR_GEMINI_API_KEY"

- Build and Run:

Sync the project with Gradle files.

Build and run the application on an emulator or a physical device.

✒️ Author

Preet Darji - [preet220704@gmail.com]

Isha V. Solanki - [ishasolanki29@gmail.com]

Jainil Parmar - [prmrjainil04@gmail.com]

Manush Desai - [manushdesai2110@gmail.com]

Feel free to reach out with any questions or feedback!
