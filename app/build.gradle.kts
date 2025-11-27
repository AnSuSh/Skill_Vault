import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt.gradle)
    alias(libs.plugins.ksp)
}

val keystoreProperties = Properties()
val keystorePropertiesFile = rootProject.file("local.properties")
if (keystorePropertiesFile.exists()) {
    keystoreProperties.load(FileInputStream(keystorePropertiesFile))
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

    signingConfigs {
        create("release") {
            // Use the properties if they exist, otherwise use null
            storeFile = keystoreProperties["RELEASE_STORE_FILE"]?.let { file(it as String) }
            storePassword = keystoreProperties["RELEASE_STORE_PASSWORD"] as String?
            keyAlias = keystoreProperties["RELEASE_KEY_ALIAS"] as String?
            keyPassword = keystoreProperties["RELEASE_KEY_PASSWORD"] as String?
        }
    }

    buildTypes {
        debug{
            isMinifyEnabled = false
            isDebuggable = true
            isDefault = true
            versionNameSuffix = "-debug"
        }
        release {
            isMinifyEnabled = true
            isDebuggable = false
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
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
    implementation(libs.androidx.splashscreen)
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
    testImplementation(libs.test.mockk.default)
    testImplementation(libs.test.mockk.agent)
    testImplementation(libs.test.mockk.android)
    testImplementation(libs.test.cash.turbine)

    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.test.junit4)
}