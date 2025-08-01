plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    // O plugin de serialização deve ter a MESMA versão do Kotlin usado no projeto:
    kotlin("plugin.serialization") version libs.versions.kotlin.get()
    id("kotlin-kapt")
    id("com.google.gms.google-services")

}

android {
    namespace = "com.apol.myapplication"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.apol.myapplication"
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
    }

    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.ui.test.android)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    implementation("androidx.cardview:cardview:1.0.0")
    implementation("com.google.firebase:firebase-auth-ktx")

    implementation("com.github.bumptech.glide:glide:4.16.0")
    annotationProcessor("com.github.bumptech.glide:compiler:4.16.0")

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")

    // Serialização JSON kotlinx - mantenha versão compatível com Kotlin
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:${libs.versions.kotlinSerialization.get()}")

    implementation("com.google.code.gson:gson:2.10.1")

    implementation("com.google.firebase:firebase-auth:23.2.0")

    // Room 2.6.1
    implementation("androidx.room:room-runtime:2.6.1")
    kapt("androidx.room:room-compiler:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")

    // Firebase BOM para gerenciar versões Firebase
    implementation(platform("com.google.firebase:firebase-bom:33.13.0"))
    implementation("com.google.firebase:firebase-analytics")
}
