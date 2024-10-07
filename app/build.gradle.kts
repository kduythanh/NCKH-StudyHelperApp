plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "com.example.nlcs"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.nlcs"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    packagingOptions {
        exclude("META-INF/DEPENDENCIES")
        exclude("META-INF/INDEX.LIST")
        exclude("META-INF/LICENSE")
        exclude("META-INF/LICENSE.txt")
        exclude("META-INF/NOTICE")
        exclude("META-INF/NOTICE.txt")
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

    buildFeatures {
        viewBinding = true
    }

}

dependencies {
    implementation(libs.androidx.webkit)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.firebase.auth)
    implementation(libs.androidx.swiperefreshlayout)
    implementation(libs.firebase.database.ktx)
    implementation(libs.firebase.firestore.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    implementation(libs.google.api.client.android)
    implementation(libs.google.api.services.calendar)
    implementation(libs.play.services.auth)
    implementation(libs.google.http.client.jackson2)
    implementation(platform(libs.firebase.bom))
    implementation(libs.google.firebase.firestore.ktx)
    implementation(libs.firebase.auth.ktx)
    implementation("com.google.firebase:firebase-auth-ktx")
    implementation("com.google.firebase:firebase-firestore-ktx")
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.firestore)

    
    implementation(libs.firebase.firestore.ktx)
    implementation(libs.firebase.storage.ktx)
    
    implementation(libs.glide)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    
    androidTestImplementation(libs.androidx.espresso.core)
    // Import the BoM for the Firebase platform
    implementation(platform("com.google.firebase:firebase-bom:33.1.2"))
    // Add the dependency for the Realtime Database library
    // When using the BoM, you don't specify versions in Firebase library dependencies
    implementation ("com.google.firebase:firebase-database:20.0.3")
    
    implementation(libs.firebase.database.ktx)
    implementation(libs.firebase.firestore)
    
    implementation(libs.neo4j.java.driver)
}
