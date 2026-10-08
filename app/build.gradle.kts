plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

// Keep the existing APK workflow unchanged: CI assembly also verifies the
// scorecard parser. Normal on-device/developer builds need no live API key.
tasks.matching { it.name == "assembleDebug" }.configureEach {
    if (System.getenv("CI") == "true") {
        dependsOn("testDebugUnitTest")
    }
}

android {
    namespace = "com.drc.golftourbillion"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.drc.golftourbillion.master.b33"
        minSdk = 28
        targetSdk = 33
        versionCode = 302
        versionName = "3.0-build34-free-course-api"
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

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.9.0")
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.json:json:20240303")
    implementation(platform("androidx.compose:compose-bom:2024.09.03"))
    implementation("androidx.activity:activity-compose:1.9.3")
    implementation("androidx.activity:activity-ktx:1.9.3")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    debugImplementation("androidx.compose.ui:ui-tooling")
}
