plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt.gradle)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.quickthought.skillvault"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.quickthought.skillvault"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        ksp {
            arg("room.schemaLocation", "$projectDir/schemas")
        }

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
}

dependencies {

    implementation(libs.hilt.android)
    implementation(libs.androidx.ui)
    ksp(libs.hilt.compiler)

    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    implementation(libs.sql.cipher)
    ksp(libs.room.compiler)

    implementation(libs.coroutines.android)
    implementation(libs.security.crypto)
    implementation(libs.biometric)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.lifecycle.viewmodel.compose)
    implementation(libs.navigation.compose)
    implementation(libs.hilt.navigation.compose)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons)
    implementation(libs.androidx.material3)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidTest.compose.junit4)
    testImplementation(libs.test.coroutines)
    testImplementation(libs.test.mockk.agent)
    testImplementation(libs.test.mockk.android)


    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.test.junit4)
}