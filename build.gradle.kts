buildscript {
    dependencies {
        // AGP 9.x uses built-in Kotlin. Pin a newer KGP because the Compose
        // compiler plugin version must match the Kotlin version we target.
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:2.4.10")
    }
}

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.compose.compiler) apply false
}
