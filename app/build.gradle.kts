plugins {
    alias(libs.plugins.android.application)
}

import java.text.SimpleDateFormat
import java.util.*
import java.util.Properties
import java.io.FileInputStream

// Load keystore properties from local.properties
val keystorePropertiesFile = rootProject.file("local.properties")
val keystoreProperties = Properties()
if (keystorePropertiesFile.exists()) {
    keystoreProperties.load(FileInputStream(keystorePropertiesFile))
}

android {
    namespace = "id.tugas.pos"
    compileSdk = 35

    defaultConfig {
        applicationId = "id.tugas.pos"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        create("release") {
//            storeFile = file(keystoreProperties["KEYSTORE_FILE"] ?: "")
//            storePassword = keystoreProperties["KEYSTORE_PASSWORD"] as String? ?: ""
//            keyAlias = keystoreProperties["KEY_ALIAS"] as String? ?: ""
//            keyPassword = keystoreProperties["KEY_PASSWORD"] as String? ?: ""
        }
        getByName("debug") {
//            storeFile = file(keystoreProperties["KEYSTORE_FILE"] ?: "")
//            storePassword = keystoreProperties["KEYSTORE_PASSWORD"] as String? ?: ""
//            keyAlias = keystoreProperties["KEY_ALIAS"] as String? ?: ""
//            keyPassword = keystoreProperties["KEY_PASSWORD"] as String? ?: ""
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            signingConfig = signingConfigs.getByName("release")
        }
        debug {
            isDebuggable = true
            signingConfig = signingConfigs.getByName("debug")
        }
    }
    
    // Custom APK naming
    // Format: MyPos-{BuildType}-v{VersionName}-{VersionCode}-{Timestamp}.apk
    // Example: MyPos-Debug-v1.0-1-20241219-143022.apk
    applicationVariants.all {
        val variant = this
        variant.outputs.forEach { output ->
            val timestamp = SimpleDateFormat("yyyyMMdd-HHmmss", Locale.getDefault()).format(Date())
            val buildType = variant.buildType.name
            val versionName = defaultConfig.versionName
            val versionCode = defaultConfig.versionCode
            
            // Custom naming pattern
            val outputFileName = when (buildType) {
                "debug" -> "MyPos-Debug-v${versionName}-${versionCode}-$timestamp.apk"
                "release" -> "MyPos-Release-v${versionName}-${versionCode}-$timestamp.apk"
                else -> "MyPos-${variant.name}-v${versionName}-$timestamp.apk"
            }
            
            (output as com.android.build.gradle.internal.api.BaseVariantOutputImpl).outputFileName = outputFileName
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
    
    // Excel Export
    implementation(libs.poi)
    implementation(libs.poi.ooxml)

    // Image Loading
    implementation(libs.glide)
    annotationProcessor(libs.compiler)
    
    // Date/Time handling
    implementation(libs.threetenabp)
    
    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    //gson
    implementation(libs.gson)
    
    // ArcMenu for FAB menu
    implementation(libs.androidveil)
}