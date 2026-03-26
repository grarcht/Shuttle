plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.google.ksp)
    alias(libs.plugins.jetbrains.kotlin.serialization)
}

apply(from = "${project.rootDir}/detekt/detekt.gradle")

android {
    namespace = "com.grarcht.shuttle.demo.core"

    defaultConfig {
        compileSdk = libs.versions.compileSdk.get().toInt()
        minSdk = libs.versions.minSdk.get().toInt()

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.toVersion(libs.versions.sourceCompatibility.get())
        targetCompatibility = JavaVersion.toVersion(libs.versions.targetCompatibility.get())
    }

    kotlin.jvmToolchain(libs.versions.jvmTarget.get().toInt())
}

dependencies {
    implementation(libs.jetbrainsKotlinDeps.stdlib)
    implementation(libs.android.annotationJvm)
    implementation(libs.android.coreKtx)
    implementation(libs.android.lifecycle.extensions)
    implementation(libs.android.lifecycle.viewModel)

    implementation(project(":framework"))
    implementation(project(":framework-integrations-persistence"))
    implementation(project(":framework-integrations-extensions-room"))
}
