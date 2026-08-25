plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.kotlin.serialization)
    id("kotlin-parcelize")
}

android {
    namespace = "com.example.foodhub_android"

    compileSdk {
        version = release(37) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "com.example.foodhub_android"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner =
            "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    buildFeatures {
        compose = true
        resValues = true
    }

    flavorDimensions += "environment"

    productFlavors {
        create("customer") {
            dimension = "environment"
        }

        create("restaurant") {
            dimension = "environment"
            applicationIdSuffix = ".restaurant"
            resValue(
                type = "string",
                name = "app_name",
                value = "FH Restaurant"
            )
        }

        create("rider") {
            dimension = "environment"
            applicationIdSuffix = ".rider"
            resValue(
                type = "string",
                name = "app_name",
                value = "FH Rider"
            )
        }
    }
}

dependencies {
    // Compose versions are managed by the BOM.
    implementation(platform(libs.androidx.compose.bom))

    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)

    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.foundation.layout)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material.icons.extended)

    implementation(libs.androidx.navigation.runtime.ktx)
    implementation(libs.androidx.navigation.compose)

    implementation(libs.core.splashscreen)

    implementation(libs.hilt.android)
    ksp(libs.hilt.android.compiler)
    implementation(
        libs.androidx.hilt.lifecycle.viewmodel.compose.v140
    )

    implementation(libs.retrofit)
    implementation(libs.converter.gson)
    implementation(libs.logging.interceptor)

    implementation(libs.kotlinx.serialization.json)

    implementation(libs.androidx.credentials)
    implementation(
        libs.androidx.credentials.play.services.auth
    )
    implementation(libs.googleid)

    implementation(libs.coil.compose)
    implementation(libs.coil.network.okhttp)

    implementation(
        libs.kotlinx.coroutines.play.services
    )
    implementation(
        libs.play.services.location
    )

    implementation(libs.maps.compose)
    implementation(libs.play.services.maps)
    implementation(libs.play.services.base)

    implementation(
        libs.stripe.android
    )

    testImplementation(libs.junit)

    androidTestImplementation(
        platform(libs.androidx.compose.bom)
    )
    androidTestImplementation(
        libs.androidx.compose.ui.test.junit4
    )
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)

    debugImplementation(
        libs.androidx.compose.ui.test.manifest
    )
    debugImplementation(
        libs.androidx.compose.ui.tooling
    )
}
