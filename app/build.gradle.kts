plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
//    alias(libs.plugins.kotlin.kapt)
    alias(libs.plugins.hilt)
    alias(libs.plugins.kotlin.ksp)
    id("com.google.gms.google-services") version "4.4.2"
}

configurations.all {
    exclude(group = "xpp3", module = "xpp3")
}


android {
    namespace = "com.hv.bukutm"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.hv.bukutm"
        minSdk = 26
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
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.compose.compiler.get()
    }
    kotlin {
        sourceSets {
            debug {
                kotlin.srcDir("build/generated/ksp/debug/kotlin")
            }
            release {
                kotlin.srcDir("build/generated/ksp/release/kotlin")
            }
        }
    }
}

    dependencies {
        implementation("org.apache.poi:poi-ooxml:5.2.5") {
            exclude(group = "xpp3", module = "xpp3")
        }

        implementation ("androidx.compose.material:material-icons-extended")
        implementation ("androidx.compose.animation:animation:1.5.0")
        implementation ("androidx.compose.ui:ui:1.5.0")

        implementation("org.apache.poi:poi-ooxml:5.2.5")
        implementation("com.kizitonwose.calendar:view:2.4.0")

        // The compose calendar library for Android
        implementation("com.kizitonwose.calendar:compose:2.4.0")
        //zxing qrCode
        implementation("com.google.zxing:core:3.5.3")
        implementation("com.journeyapps:zxing-android-embedded:4.3.0")

        //push notification
        implementation("com.google.firebase:firebase-messaging-ktx:23.4.0") // FCM
        implementation("com.squareup.okhttp3:okhttp:4.12.0") // WebSocket (OkHttp)
        implementation("com.google.code.gson:gson:2.10.1")

        implementation(libs.androidx.core.ktx)
        implementation(libs.androidx.lifecycle.runtime.ktx)

        // Compose
        implementation(libs.androidx.activity.compose)
        implementation(platform(libs.androidx.compose.bom))
        implementation(libs.androidx.ui)
        implementation(libs.androidx.ui.graphics)
        implementation(libs.androidx.ui.tooling.preview)
        implementation(libs.androidx.material3)
        implementation(libs.androidx.media3.exoplayer)
        implementation(libs.androidx.navigation.safe.args.generator)
        debugImplementation(libs.androidx.ui.tooling)
        debugImplementation(libs.androidx.ui.test.manifest)

        // Hilt Dependency Injection
        implementation(libs.hilt.android)
        ksp(libs.hilt.compiler)
        implementation(libs.hilt.navigation.compose)

        // Navigation
        implementation(libs.navigation.compose)

        // Retrofit & OkHttp for networking
        implementation(libs.retrofit)
        implementation(libs.converter.gson)
        implementation(libs.okhttp)
        implementation(libs.okhttp.logging)
        implementation(libs.gson)

        // Coil for image loading
        implementation(libs.coil.compose)
        // DataStore for data persistence
        implementation(libs.datastore.preferences)
        implementation(libs.datastore.proto)

        // Testing
        testImplementation(libs.junit)
        androidTestImplementation(libs.androidx.junit)
        androidTestImplementation(libs.androidx.espresso.core)
        androidTestImplementation(platform(libs.androidx.compose.bom))
        androidTestImplementation(libs.androidx.ui.test.junit4)
    }