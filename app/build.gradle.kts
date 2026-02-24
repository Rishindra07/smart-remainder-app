import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.google.services)

}

android {
    namespace = "com.example.smartremainder"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.smartremainder"
        minSdk = 24
        targetSdk = 36
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        viewBinding = true
        buildConfig = true // 3. Enable BuildConfig
    }
    // Corrected aaptOptions block with proper Kotlin DSL syntax
    aaptOptions {
        noCompress.add("tflite")
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation("com.github.bumptech.glide:glide:4.12.0")
    implementation(libs.androidx.material3)
    annotationProcessor("com.github.bumptech.glide:compiler:4.12.0")
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    // Simplified and corrected TensorFlow Lite dependencies
    implementation("org.tensorflow:tensorflow-lite:2.9.0")
    implementation("org.tensorflow:tensorflow-lite-support:0.4.0")
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // Networking
    implementation ("com.squareup.retrofit2:retrofit:2.9.0")
    implementation ("com.squareup.retrofit2:converter-gson:2.9.0")
    //implementation ("com.squareup.okhttp3:logging-interceptor:4.12.0")

    // Coroutines
    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")

    // ViewModel
    implementation ("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.0")

    // Firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.storage.ktx)
    implementation("androidx.navigation:navigation-compose:2.7.7")

    // Material Icons
    implementation(libs.androidx.material.icons.extended)
}