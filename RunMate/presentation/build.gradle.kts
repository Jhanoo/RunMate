plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.jetbrains.kotlin.android)
    id("kotlin-kapt")
    id ("kotlin-parcelize")
    id("com.google.dagger.hilt.android")
    id("androidx.navigation.safeargs.kotlin")
}

android {
    namespace = "com.D107.runmate.presentation"
    compileSdk = 34

    defaultConfig {
        minSdk = 24

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
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
    viewBinding {
        enable = true
    }
}

dependencies {
    implementation(project(":domain"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(project(":data"))
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // Navigation
    implementation(libs.androidx.navigation.fragment)
    implementation(libs.androidx.navigation.ui)

    // Glide
    implementation (libs.glide)

    // MPAndroidChart
    implementation(libs.mpandroidchart)

    // Hilt
    implementation(libs.hilt.android)
    kapt(libs.hilt.android.compiler)

    // DotLottie
    implementation(libs.dotlottie.android)

    // KakaoMap
    implementation(libs.android)

    // Google Location
    implementation(libs.play.services.location)

    // GPX parser
    implementation(libs.android.gpx.parser)

    implementation(libs.androidx.media)

    //Timber
    implementation("com.jakewharton.timber:timber:5.0.1")

    //GIF
    implementation(libs.glide.v4132)
    implementation(libs.androidx.emoji2)
    implementation(libs.androidx.emoji2.views)

    implementation("com.prolificinteractive:material-calendarview:1.4.3")

    implementation("com.squareup.okhttp3:logging-interceptor:4.10.0")
    implementation("com.squareup.okhttp3:okhttp-urlconnection:4.10.0")

    implementation("com.google.android.gms:play-services-wearable:18.1.0")


    implementation ("com.google.android.flexbox:flexbox:3.0.0")
}

kapt {
    correctErrorTypes = true
}