// Top-level build file where you can add configuration options common to all sub-projects/modules.

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    id("com.google.gms.google-services") version "4.4.2" apply false
}

buildscript {
    val kotlin_version: String by extra("1.8.0") // Certifique-se de que a versão do Kotlin esteja atualizada

    repositories {
        google()
        mavenCentral()
    }

    dependencies {
        classpath("com.android.tools.build:gradle:8.11.1") // Certifique-se de que o plugin do Android esteja atualizado
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlin_version") // Plugin Kotlin
        classpath ("com.google.gms:google-services:4.3.10")

    }
}