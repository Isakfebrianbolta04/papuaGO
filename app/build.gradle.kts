plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.gms.google.services)
    alias(libs.plugins.google.firebase.crashlytics)
}

android {
    namespace = "com.example.myapplication"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.myapplication"
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
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.firebase.crashlytics)
    implementation(libs.legacy.support.v4)

    // Firebase Authentication
    implementation("com.google.firebase:firebase-auth:23.1.0")

    // Firebase Realtime Database
    implementation("com.google.firebase:firebase-database:21.0.0")

    // Firebase Cloud Messaging (FCM) for Push Notifications
    implementation("com.google.firebase:firebase-messaging:24.0.0")

    // Retrofit for API Integration
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")

    // ZXing for QR Code Generation and Scanning
    implementation("com.google.zxing:core:3.5.1")
    implementation("com.journeyapps:zxing-android-embedded:4.3.0")

    // ViewPager2 for TabLayout
    implementation("androidx.viewpager2:viewpager2:1.0.0")

    // Glide for image loading
    implementation("com.github.bumptech.glide:glide:4.16.0")

    // CardView
    implementation("androidx.cardview:cardview:1.0.0")
    implementation(libs.firebase.database)
    implementation(libs.firebase.inappmessaging)

    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}
