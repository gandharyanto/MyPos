plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "id.tugas.pos"
    compileSdk = 35

    defaultConfig {
        applicationId = "id.tugas.pos"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    
    buildFeatures {
        viewBinding = true
        dataBinding = true
    }
}

dependencies {
    // Core Android dependencies
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    
    // Architecture Components
    implementation(libs.androidx.lifecycle.viewmodel)
    implementation(libs.androidx.lifecycle.livedata)
    implementation(libs.androidx.lifecycle.viewmodel.savedstate)
    
    // Room Database
    implementation(libs.androidx.room.runtime)
    annotationProcessor(libs.androidx.room.compiler)
    implementation(libs.androidx.room.ktx)
    
    // Navigation Component
    implementation(libs.navigation.fragment)
    implementation(libs.androidx.navigation.ui)
    
    // RecyclerView
    implementation(libs.androidx.recyclerview)
    
    // CardView
    implementation(libs.cardview)
    
    // Preference
    implementation(libs.preference)
    
    // WorkManager for background tasks
    implementation(libs.work.runtime)
    
    // Thermal Printer Support
    implementation(libs.escpos.thermalprinter.android)
    
    // PDF Generation
    implementation(libs.itext7.core)
    
    // Image Loading
    implementation(libs.glide)
    annotationProcessor(libs.compiler)
    
    // Date/Time handling
    implementation(libs.threetenabp)
    
    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}