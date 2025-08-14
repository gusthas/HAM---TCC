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
    // Dependências Base do Android
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.ui.test.android)

    // Testes
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // UI & Imagens
    implementation("androidx.cardview:cardview:1.0.0")
    implementation("com.github.bumptech.glide:glide:4.16.0")
    annotationProcessor("com.github.bumptech.glide:compiler:4.16.0")

    // Serialização (versão do catálogo de versões)
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:${libs.versions.kotlinSerialization.get()}")

    // Room (Banco de Dados)
    implementation("androidx.room:room-runtime:2.6.1")
    kapt("androidx.room:room-compiler:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")

    // --- Firebase (Configuração Corrigida e Limpa) ---
    // A plataforma BOM gere TODAS as versões das bibliotecas Firebase abaixo
    implementation(platform("com.google.firebase:firebase-bom:33.1.1"))
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")

    // Módulos Firebase que você precisa (sem especificar a versão)
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-auth-ktx")
    implementation("com.google.firebase:firebase-appcheck-playintegrity")
    implementation("com.google.firebase:firebase-appcheck-ktx")
    // A linha "firebase-ktx" é geralmente transitiva (incluída por outras),
    // mas para garantir, podemos adicioná-la explicitamente.
    implementation("com.google.firebase:firebase-common-ktx") // Usar firebase-common-ktx é a prática mais moderna
}
