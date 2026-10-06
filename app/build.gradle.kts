plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "com.drc.golftourbillion"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.drc.golftourbillion.master.b33"
        minSdk = 28
        targetSdk = 33
        versionCode = 302
        versionName = "3.1-rebuild-test"
    }

    buildTypes {
        getByName("debug") {
            applicationIdSuffix = ".rebuild"
            versionNameSuffix = "-side-by-side"
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
        compose = true
    }

    packaging {
        resources.excludes += "/META-INF/{AL2.0,LGPL2.1}"
    }
}
