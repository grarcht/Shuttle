plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.google.dagger.hilt)
    alias(libs.plugins.jetbrains.dokka)
    alias(libs.plugins.google.ksp)
    alias(libs.plugins.jetbrains.kotlin.android)
}

apply(from = "${project.rootDir}/detekt/detekt.gradle")

tasks.named("dokkaHtml") {
    (this as org.jetbrains.dokka.gradle.DokkaTask).outputDirectory.set(file("documentation/kotlin"))
}

android {
    namespace = "com.grarcht.shuttle.demo.mvvm"

    defaultConfig {
        applicationId = "com.grarcht.shuttle.demo.mvvm"
        compileSdk = libs.versions.compileSdk.get().toInt()
        minSdk = libs.versions.minSdk.get().toInt()
        targetSdk = libs.versions.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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

    buildFeatures {
        viewBinding = true
    }

    val dependenciesType = "dependenciesType"
    flavorDimensions.add(dependenciesType)

    productFlavors {
        create("artifacts") { dimension = dependenciesType }
        create("modules") { dimension = dependenciesType }
    }
}

dependencies {
    implementation(libs.jetbrainsKotlinDeps.stdlib)
    implementation(libs.jetbrainsKotlinDeps.coroutines)
    implementation(libs.android.appCompat)
    implementation(libs.android.constraintLayout)
    implementation(libs.android.material)
    implementation(libs.android.fragmentKtx)
    implementation(libs.android.lifecycle.extensions)
    implementation(libs.android.lifecycle.viewModel)
    implementation(libs.dependencyInjectionDeps.hilt)
    implementation(libs.android.coreKtx)
    ksp(libs.dependencyInjectionDeps.hiltCompiler)
    implementation(libs.android.annotationJvm)
    implementation(project(":demos-core-lib"))

    // Lighter weight, independent dependencies
    implementation(project(":framework"))
    implementation(project(":framework-integrations-persistence"))
    implementation(project(":framework-integrations-extensions-room"))

    // To use maven dependencies, use the following:
    // implementation(libs.shuttle.framework)
    // implementation(libs.shuttle.integrationsPersistence)
    // implementation(libs.shuttle.integrationsExtensionsRoom)
}
