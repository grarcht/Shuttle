plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.google.dagger.hilt)
    alias(libs.plugins.google.ksp)
    alias(libs.plugins.jetbrains.dokka)
}

apply(from = "${project.rootDir}/detekt/detekt.gradle")

android {
    namespace = "com.grarcht.shuttle.demo.core.di"

    defaultConfig {
        compileSdk = libs.versions.compileSdk.get().toInt()
        minSdk = libs.versions.minSdk.get().toInt()
        consumerProguardFiles("consumer-rules.pro")
    }

    compileOptions {
        sourceCompatibility = JavaVersion.toVersion(libs.versions.sourceCompatibility.get())
        targetCompatibility = JavaVersion.toVersion(libs.versions.targetCompatibility.get())
    }

    kotlin.jvmToolchain(libs.versions.jvmTarget.get().toInt())
}

dependencies {
    implementation(libs.dependencyInjectionDeps.hilt)
    ksp(libs.dependencyInjectionDeps.hiltCompiler)
    implementation(project(":framework-integrations-extensions-room"))
}
