import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    id("kotlin-kapt")
    id("com.google.dagger.hilt.android")
}
val properties = Properties().apply {
    load(rootProject.file("apikey.properties").inputStream())
}

val restApiKey: String = properties.getProperty("rest_api_key") ?: ""
val nativeApiKey: String = properties.getProperty("native_api_key") ?: ""
val serverUrl: String = properties.getProperty("base_url") ?: ""

android {
    namespace = "com.D107.runmate"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.D107.runmate"
        minSdk = 28
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        buildConfigField("String", "REST_API_KEY", restApiKey)
        buildConfigField("String", "NATIVE_API_KEY", nativeApiKey)
        buildConfigField("String", "BASE_URL", serverUrl)
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
        buildConfig = true
    }
}

dependencies {
    implementation(project(":domain"))
    implementation(project(":data"))
    implementation(project(":presentation"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // Hilt
    implementation(libs.hilt.android)
    kapt(libs.hilt.android.compiler)

    // KakaoMap
    implementation(libs.android)
}