plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "com.lensnap.app"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.lensnap.app"
        minSdk = 24
        targetSdk = 35
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
        freeCompilerArgs += listOf("-Xinline-classes", "-Xexperimental=kotlin.ExperimentalContracts")
    }
    buildFeatures {
        compose = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "META-INF/LICENSE"
            excludes += "META-INF/LICENSE.txt"
            excludes += "META-INF/NOTICE"
            excludes += "META-INF/NOTICE.txt"
            excludes += "license/LICENSE.dom-documentation.txt"
            excludes += "license/NOTICE"
            excludes += "license/README.dom.txt"
            excludes += "META-INF/AL2.0"
            excludes += "META-INF/LGPL2.1"
            excludes += "license/LICENSE.dom-software.txt"
            excludes += "license/LICENSE"
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.firebase.auth)
    implementation(libs.firebase.database)
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.storage)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    implementation(libs.coil.compose) // Coil for image loading
    implementation(libs.imagepicker) // Image Picker library
    implementation(libs.androidx.navigation.compose)
    implementation("com.google.zxing:core:3.4.1")
    implementation("com.github.kenglxn.QRGen:javase:3.0.1") // Updated dependency
    implementation("com.github.Drjacky:ImagePicker:2.1.13") // Add this line for ImagePicker
    implementation("com.journeyapps:zxing-android-embedded:4.3.0")

    // Defining CameraX version separately
    val camerax_version = "1.1.0-beta03"
    implementation("androidx.camera:camera-core:$camerax_version")
    implementation("androidx.camera:camera-camera2:$camerax_version")
    implementation("androidx.camera:camera-lifecycle:$camerax_version")
    implementation("androidx.camera:camera-view:$camerax_version")
    implementation("androidx.camera:camera-extensions:$camerax_version")

    // Add ML Kit Barcode Scanning dependency
    implementation("com.google.mlkit:barcode-scanning:17.3.0")

    //Guava dependency
    implementation("com.google.guava:guava:32.1.3-android")

    //firebase App check
    implementation ("com.google.firebase:firebase-appcheck-playintegrity:16.0.0")
    implementation ("com.google.firebase:firebase-storage:20.0.0")

    //Material icons dependencies
    implementation ("androidx.compose.material:material-icons-core:1.0.0")
    implementation ("androidx.compose.material:material-icons-extended:1.0.0")

    //GPUImage dependency(for filters)
    implementation ("jp.co.cyberagent.android:gpuimage:2.1.0") // Replace with the latest version

    //Live data import(for countinuous fetching of event data)
    implementation ("androidx.compose.runtime:runtime-livedata:1.0.0") // or the latest version
    implementation ("androidx.lifecycle:lifecycle-runtime-ktx:2.3.1") // or the latest version

    //gson import
    implementation ("com.google.code.gson:gson:2.8.8")

    //Accompanist(for full screen display of the photo capture and event room)
    implementation ("com.google.accompanist:accompanist-pager:0.23.1")
    implementation ("com.google.accompanist:accompanist-pager-indicators:0.23.1")

    //Refresh app(Scroll down gesture)
    implementation ("com.google.accompanist:accompanist-swiperefresh:0.32.0")

    //Coil remember image painter
    implementation ("com.google.accompanist:accompanist-coil:0.15.0")

    //WebRTC (calls)
    implementation("io.github.webrtc-sdk:android:125.6422.06.1")

    //permissions
    implementation ("androidx.lifecycle:lifecycle-runtime-ktx:2.3.1")
    implementation ("androidx.lifecycle:lifecycle-viewmodel-ktx:2.3.1")
    implementation ("androidx.lifecycle:lifecycle-livedata-ktx:2.3.1")

    //Palette for detection of dominant color on a post/Image
    implementation ("androidx.palette:palette-ktx:1.0.0")

    //Video player
    implementation ("com.google.android.exoplayer:exoplayer:2.14.2")
}
