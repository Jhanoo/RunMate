import java.util.Properties

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.jetbrains.kotlin.android)
    id("kotlin-kapt")
    id("com.google.dagger.hilt.android")
    id("kotlin-parcelize")
}
val properties = Properties().apply {
    load(rootProject.file("apikey.properties").inputStream())
}

val restApiKey: String = properties.getProperty("rest_api_key") ?: ""
val nativeApiKey: String = properties.getProperty("native_api_key") ?: ""
val serverUrl: String = properties.getProperty("base_url") ?: ""
val kakaoApiUrl: String = properties.getProperty("kakao_url") ?: ""


android {
    namespace = "com.D107.runmate.domain"
    compileSdk = 34

    defaultConfig {
        minSdk = 24

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        buildConfigField("String", "REST_API_KEY", restApiKey)
        buildConfigField("String", "NATIVE_API_KEY", nativeApiKey)
        buildConfigField("String", "BASE_URL", serverUrl)
        buildConfigField("String", "KAKAO_API_URL", kakaoApiUrl)
        consumerProguardFiles("consumer-rules.pro")
        externalNativeBuild {
            cmake {
                cppFlags("")
            }
        }
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
    externalNativeBuild {
        cmake {
            path("src/main/cpp/CMakeLists.txt")
            version = "3.22.1"
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

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    implementation(libs.androidx.activity)

    implementation("com.squareup.retrofit2:retrofit:2.9.0")

    // Data Store
    implementation ("androidx.datastore:datastore-preferences:1.1.4")
    implementation ("androidx.datastore:datastore-core:1.1.4")

    // Hilt
    implementation("com.google.dagger:hilt-android:2.51.1")
    kapt("com.google.dagger:hilt-android-compiler:2.51.1")

    // Logging-Interceptor
    implementation("com.squareup.okhttp3:okhttp-urlconnection:4.10.0")
    implementation ("com.squareup.okhttp3:logging-interceptor:4.10.0")

    // Moshi
    implementation(libs.moshi)
    implementation(libs.converter.moshi)

    //Timber
    implementation("com.jakewharton.timber:timber:5.0.1")
}